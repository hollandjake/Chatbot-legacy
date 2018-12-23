package bot.utils;

import bot.Chatbot;
import bot.utils.exceptions.MissingConfigurationsException;

import java.sql.*;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

public class Database {
    private final String url;
    private final String username;
    private final String password;
    private final Chatbot chatbot;

    private Connection connection;
    private java.util.Date lastConnectionRequest;
    private final long connectionTimeout;
    private final Thread shutdownThread = new Thread(this::closeConnection);

    //region Queries
    private PreparedStatement GET_THREAD_ID_FROM_NAME_STMT;

    private PreparedStatement GET_HUMAN_FROM_ID_STMT;
    private PreparedStatement GET_HUMAN_FROM_NAME_STMT;
    private PreparedStatement SAVE_HUMAN_STMT;

    private PreparedStatement GET_MESSAGE_FROM_ID_STMT;
    private PreparedStatement GET_MESSAGE_WITH_MESSAGE_STMT;
    private PreparedStatement GET_NUM_MESSAGES_FROM_THREAD_STMT;
    private CallableStatement SAVE_MESSAGE_STMT;
    private CallableStatement SAVE_MESSAGE_WITH_DATE_STMT;


    //endregion

    public Database(HashMap<String, String> config, Chatbot chatbot) throws MissingConfigurationsException {
        if (!config.containsKey("dbUrl") || !config.containsKey("dbUsername") || !config.containsKey("dbPassword")) {
            throw new MissingConfigurationsException("dbUrl", "dbUsername", "dbPassword");
        }
        this.url = config.get("dbUrl");
        this.username = config.get("dbUsername");
        this.password = config.get("dbPassword");
        this.chatbot = chatbot;
        this.connectionTimeout = chatbot.getMessageTimeout().toMillis();

        openConnection(url, username, password, chatbot);
    }

    public Database(HashMap<String, String> config) throws MissingConfigurationsException {
        if (!config.containsKey("dbUrl") || !config.containsKey("dbUsername") || !config.containsKey("dbPassword")) {
            throw new MissingConfigurationsException("dbUrl", "dbUsername", "dbPassword");
        }
        this.url = config.get("dbUrl");
        this.username = config.get("dbUsername");
        this.password = config.get("dbPassword");
        this.chatbot = null;
        this.connectionTimeout = Duration.ofMinutes(1).toMillis();

        openConnection(url, username, password, null);
    }

    //region Connection

    /**
     * Creates a connection and handles any errors
     *
     * @param url      {@link String}
     * @param username {@link String}
     * @param password {@link String}
     */
    private void openConnection(String url, String username, String password, Chatbot chatbot) {
        closeConnection();

        try {
            shutdownThread.run();
            System.out.println("Connecting to Database");
            connection = DriverManager.getConnection(
                    "jdbc:mariadb://" + url + "?" +
                            "&sessionVariables=wait_timeout=" + (connectionTimeout) + "," +
                            "character_set_client=utf8mb4,character_set_results=utf8mb4,character_set_connection=utf8mb4" +
                            "&autoReconnect=true" +
                            "&useCompression=true" +
                            "&allowMultiQueries=true" +
                            "&rewriteBatchedStatements=true",
                    username,
                    password);
            createQueries(chatbot);
            lastConnectionRequest = new java.util.Date();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            Runtime.getRuntime().addShutdownHook(shutdownThread);
        }
    }

    /**
     * Handles closing the connection when its finished with or failed
     */
    private void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    Runtime.getRuntime().removeShutdownHook(shutdownThread);
                    System.out.println("Connection closed");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (IllegalStateException ignore) {
            }
        }
    }

    /**
     * checks the connections status to make sure a connection is always active
     */
    public void checkConnection() throws SQLException {
        java.util.Date now = new java.util.Date();
        if (connection.isClosed() || now.getTime() - lastConnectionRequest.getTime() >= connectionTimeout) {
            try {
                connection.createStatement().getWarnings();
            } catch (SQLException e) {
                e.printStackTrace();
                openConnection(url, username, password, chatbot);
            }
        }
        lastConnectionRequest = now;
    }

    //endregion

    public void createQueries(Chatbot chatbot) throws SQLException {
        if (chatbot != null) {
            for (CommandModule module : chatbot.getModules().values()) {
                if (module instanceof DatabaseModule) {
                    ((DatabaseModule) module).prepareStatements(connection);
                }
            }
            chatbot.getBootModule().prepareStatements(connection);
        }

        GET_THREAD_ID_FROM_NAME_STMT = connection.prepareStatement("SELECT getThreadIdFromName(?) AS T_ID");

        GET_HUMAN_FROM_ID_STMT = connection.prepareStatement("" +
                "SELECT " +
                "   H.ID as H_ID," +
                "   H.name as H_name " +
                "FROM Humans H " +
                "WHERE H.ID = ?");
        GET_HUMAN_FROM_NAME_STMT = connection.prepareStatement("" +
                "SELECT " +
                "   H.ID as H_ID," +
                "   H.name as H_name " +
                "FROM Humans H " +
                "WHERE H.name = ?");
        SAVE_HUMAN_STMT = connection.prepareStatement("INSERT INTO Humans (name) VALUES (?)");

        GET_MESSAGE_FROM_ID_STMT = connection.prepareStatement("" +
                "SELECT" +
                "   M.ID as M_ID," +
                "   H.ID as H_ID," +
                "   H.name as H_name," +
                "   M.message as M_message," +
                "   I.url as I_url," +
                "   M.date as M_date " +
                "FROM Messages M " +
                "JOIN Humans H on M.sender_id = H.ID " +
                "LEFT JOIN Images I on M.image_id = I.ID " +
                "WHERE M.thread_id = ? AND M.ID = ? " +
                "LIMIT 1");
        GET_MESSAGE_WITH_MESSAGE_STMT = connection.prepareStatement("" +
                "SELECT" +
                "   M.ID as M_ID," +
                "   H.ID as H_ID," +
                "   H.name as H_name," +
                "   M.message as M_message," +
                "   I.url as I_url," +
                "   M.date as M_date " +
                "FROM Messages M " +
                "JOIN Humans H on M.sender_id = H.ID " +
                "LEFT JOIN Images I on M.image_id = I.ID " +
                "WHERE M.thread_id = ? " +
                "AND M.message COLLATE UTF8MB4_GENERAL_CI LIKE CONCAT('%', ? ,'%')" +
                "ORDER BY M.ID DESC");
        GET_NUM_MESSAGES_FROM_THREAD_STMT = connection.prepareStatement("" +
                "SELECT COUNT(M.ID) " +
                "FROM Messages M " +
                "WHERE M.thread_id = ?");
        SAVE_MESSAGE_STMT = connection.prepareCall("{CALL CreateMessage(?,?,?,?)}");
        SAVE_MESSAGE_WITH_DATE_STMT = connection.prepareCall("{CALL CreateMessageWithDate(?,?,?,?,?)}");
    }

    //region System
    public Integer getThreadIdFromName(String threadName) {
        try {
            GET_THREAD_ID_FROM_NAME_STMT.setString(1, threadName);
            ResultSet resultSet = GET_THREAD_ID_FROM_NAME_STMT.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("T_ID");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    //endregion

    //region Humans
    public Human getHumanFromID(int id) {
        try {
            checkConnection();
            GET_HUMAN_FROM_ID_STMT.setInt(1, id);
            ResultSet resultSet = GET_HUMAN_FROM_ID_STMT.executeQuery();
            if (resultSet.next()) {
                return new Human(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Human getHumanFromName(String name) {
        try {
            checkConnection();
            GET_HUMAN_FROM_NAME_STMT.setString(1, name);
            ResultSet resultSet = GET_HUMAN_FROM_NAME_STMT.executeQuery();
            if (resultSet.next()) {
                return new Human(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void saveHuman(String name) {
        try {
            checkConnection();
            SAVE_HUMAN_STMT.setString(1, name);
            SAVE_HUMAN_STMT.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    //endregion

    //region Messages
    public Message getMessage(int threadId, int messageId) {
        try {
            checkConnection();
            GET_MESSAGE_FROM_ID_STMT.setInt(1, threadId);
            GET_MESSAGE_FROM_ID_STMT.setInt(2, messageId);
            ResultSet resultSet = GET_MESSAGE_FROM_ID_STMT.executeQuery();
            if (resultSet.next()) {
                return new Message(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<Message> getMessagesWithMessageLike(int threadId, String query) {
        ArrayList<Message> messages = new ArrayList<>();
        try {
            checkConnection();
            GET_MESSAGE_WITH_MESSAGE_STMT.setInt(1, threadId);
            GET_MESSAGE_WITH_MESSAGE_STMT.setString(2, query);
            ResultSet resultSet = GET_MESSAGE_WITH_MESSAGE_STMT.executeQuery();
            while (resultSet.next()) {
                messages.add(new Message(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    public Integer getMessageCount(int threadId) {
        try {
            checkConnection();
            GET_NUM_MESSAGES_FROM_THREAD_STMT.setInt(1, threadId);
            ResultSet resultSet = GET_NUM_MESSAGES_FROM_THREAD_STMT.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ResultSet saveMessage(String senderName, int threadId, String message, String imageUrl) {
        try {
            checkConnection();
            SAVE_MESSAGE_STMT.setString(1, senderName);
            SAVE_MESSAGE_STMT.setInt(2, threadId);
            SAVE_MESSAGE_STMT.setString(3, message);
            SAVE_MESSAGE_STMT.setString(4, imageUrl);
            return SAVE_MESSAGE_STMT.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ResultSet saveMessage(String senderName, int threadId, String message, String imageUrl, LocalDate date) {
        try {
            checkConnection();
            SAVE_MESSAGE_WITH_DATE_STMT.setString(1, senderName);
            SAVE_MESSAGE_WITH_DATE_STMT.setInt(2, threadId);
            SAVE_MESSAGE_WITH_DATE_STMT.setString(3, message);
            SAVE_MESSAGE_WITH_DATE_STMT.setString(4, imageUrl);
            SAVE_MESSAGE_WITH_DATE_STMT.setDate(5, java.sql.Date.valueOf(date));
            return SAVE_MESSAGE_WITH_DATE_STMT.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                System.out.println(connection.getMetaData());
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }
        return null;
    }
    //endregion

    /**
     * Runs the SQL supplied to it
     *
     * @param SQL {@link String}
     */
    public void runSQL(String SQL) {
        try {
            checkConnection();
            PreparedStatement stmt = connection.prepareStatement(SQL);
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //region Getters & Setters

    public Connection getConnection() {
        return connection;
    }

    //endregion
}
