package bot.utils;

import bot.Chatbot;
import bot.utils.exceptions.MissingConfigurationsException;
import bot.utils.message.Image;
import bot.utils.message.Message;
import bot.utils.message.MessageComponent;

import javax.net.ssl.SSLHandshakeException;
import java.sql.*;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Database {
	private final String url;
	private final String username;
	private final String password;
	private final Chatbot chatbot;
	private final long connectionTimeout;
	private Connection connection;
	private final Thread shutdownThread = new Thread(this::closeConnection);
	private java.util.Date lastConnectionRequest;
	//region Queries
	private PreparedStatement GET_ALL_HUMANS_STMT;
	private PreparedStatement GET_HUMAN_FROM_ID_STMT;
	private PreparedStatement GET_HUMAN_FROM_NAME_STMT;
	private PreparedStatement GET_HUMAN_FROM_URL_STMT;
	private PreparedStatement SAVE_HUMAN_STMT;

	private PreparedStatement GET_MESSAGE_FROM_ID_STMT;
	private PreparedStatement GET_MESSAGE_WITH_MESSAGE_STMT;
	private PreparedStatement GET_NUM_MESSAGES_STMT;
	private PreparedStatement SAVE_MESSAGE_STMT;
	private PreparedStatement SAVE_MESSAGE_WITH_DATE_STMT;
	private PreparedStatement GET_IMAGE_FROM_ID_STMT;

	//endregion

	public Database(HashMap<String, String> config, Chatbot chatbot) throws MissingConfigurationsException {
		if (!config.containsKey("db_url") || !config.containsKey("db_username") || !config.containsKey("db_password")) {
			throw new MissingConfigurationsException("dbUrl", "dbUsername", "dbPassword");
		}
		this.url = config.get("db_url");
		this.username = config.get("db_username");
		this.password = config.get("db_password");
		this.chatbot = chatbot;
		this.connectionTimeout = chatbot.getMessageTimeout().toMillis();

		openConnection(url, username, password, chatbot);
	}

	public Database(HashMap<String, String> config) throws MissingConfigurationsException {
		if (!config.containsKey("db_url") || !config.containsKey("db_username") || !config.containsKey("db_password")) {
			throw new MissingConfigurationsException("db_url", "db_username", "db_password");
		}
		this.url = config.get("db_url");
		this.username = config.get("db_username");
		this.password = config.get("db_password");
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
			try {
				Runtime.getRuntime().addShutdownHook(shutdownThread);
			} catch (IllegalArgumentException ignore) {
			}
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
	public void checkConnection() {
		java.util.Date now = new java.util.Date();
		if (now.getTime() - lastConnectionRequest.getTime() >= connectionTimeout) {
			try {
				PreparedStatement stmt = connection.prepareStatement("SELECT NOW()");
				stmt.execute();
			} catch (SQLException e) {
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

		GET_ALL_HUMANS_STMT = connection.prepareStatement("" +
			"SELECT" +
			"   H.ID AS H_ID," +
			"   H.name AS H_name " +
			"FROM Humans H");

		GET_HUMAN_FROM_ID_STMT = connection.prepareStatement("" +
			"SELECT " +
			"   H.ID as H_ID," +
			"   H.name as H_name," +
			"   H.url as H_url " +
			"FROM Humans H " +
			"WHERE H.ID = ?");
		GET_HUMAN_FROM_NAME_STMT = connection.prepareStatement("" +
			"SELECT " +
			"   H.ID as H_ID," +
			"   H.name as H_name," +
			"   H.url as H_url " +
			"FROM Humans H " +
			"WHERE H.name = ?");
		GET_HUMAN_FROM_URL_STMT = connection.prepareStatement("" +
			"SELECT " +
			"   H.ID as H_ID," +
			"   H.name as H_name," +
			"   H.url as H_url " +
			"FROM Humans H " +
			"WHERE H.url = ?");
		SAVE_HUMAN_STMT = connection.prepareStatement("INSERT INTO Humans (name) VALUES (?)");

		GET_MESSAGE_FROM_ID_STMT = connection.prepareStatement("" +
			"SELECT" +
			"   M.ID as M_ID," +
			"   H.ID as H_ID," +
			"   H.name as H_name," +
			"   M.message as M_message," +
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
			"   M.date as M_date " +
			"FROM Messages M " +
			"JOIN Humans H on M.sender_id = H.ID " +
			"AND M.message COLLATE UTF8MB4_GENERAL_CI LIKE CONCAT('%', ? ,'%')" +
			"ORDER BY M.ID DESC");
		GET_NUM_MESSAGES_STMT = connection.prepareStatement("" +
			"SELECT COUNT(M.ID) " +
			"FROM Messages M");
		SAVE_MESSAGE_STMT = connection.prepareStatement("" +
			"INSERT INTO Messages (sender_id, date, message) " +
			"VALUES (?, NOW(), ?)");
		SAVE_MESSAGE_WITH_DATE_STMT = connection.prepareStatement("" +
			"INSERT INTO Messages (sender_id, date, message) " +
			"VALUES (?, ?, ?)");

		GET_IMAGE_FROM_ID_STMT = connection.prepareStatement("" +
			"SELECT " +
			"   I.url as I_url " +
			"FROM Images I " +
			"WHERE I.ID = ?");
	}

	//region System

	//endregion

	//region Humans
	public List<Human> getAllHumans() {
		List<Human> humans = new ArrayList<>();
		try {
			checkConnection();
			ResultSet resultSet = GET_ALL_HUMANS_STMT.executeQuery();
			while (resultSet.next()) {
				humans.add(new Human(resultSet));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return humans;
	}

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

	public Human getHumanFromUrl(String url) {
		try {
			checkConnection();
			GET_HUMAN_FROM_URL_STMT.setString(1, url);
			ResultSet resultSet = GET_HUMAN_FROM_URL_STMT.executeQuery();
			if (resultSet.next()) {
				return new Human(resultSet);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void saveHuman(String name, String url) {
		try {
			checkConnection();
			SAVE_HUMAN_STMT.setString(1, name);
			SAVE_HUMAN_STMT.setString(2, url);
			SAVE_HUMAN_STMT.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	//endregion

	//region Messages
	public Message getMessage(int messageId) {
		try {
			checkConnection();
			GET_MESSAGE_FROM_ID_STMT.setInt(1, messageId);
			ResultSet resultSet = GET_MESSAGE_FROM_ID_STMT.executeQuery();
			if (resultSet.next()) {
				return new Message(this, resultSet);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public ArrayList<Message> getMessagesWithMessageLike(String query) {
		ArrayList<Message> messages = new ArrayList<>();
		try {
			checkConnection();
			GET_MESSAGE_WITH_MESSAGE_STMT.setString(1, query);
			ResultSet resultSet = GET_MESSAGE_WITH_MESSAGE_STMT.executeQuery();
			while (resultSet.next()) {
				messages.add(new Message(this, resultSet));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return messages;
	}

	public Integer getMessageCount() {
		try {
			checkConnection();
			ResultSet resultSet = GET_NUM_MESSAGES_STMT.executeQuery();
			if (resultSet.next()) {
				return resultSet.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public ResultSet saveMessage(String senderName, List<MessageComponent> messageComponents) {
		try {
			checkConnection();
			SAVE_MESSAGE_STMT.setString(1, senderName);
			SAVE_MESSAGE_STMT.setString(2, Message.combineComponents(messageComponents));
			return SAVE_MESSAGE_STMT.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public ResultSet saveMessage(String senderName, String message, String imageUrl, LocalDate date) {
		try {
			checkConnection();
			SAVE_MESSAGE_WITH_DATE_STMT.setString(1, senderName);
			SAVE_MESSAGE_WITH_DATE_STMT.setString(2, message);
			SAVE_MESSAGE_WITH_DATE_STMT.setString(3, imageUrl);
			SAVE_MESSAGE_WITH_DATE_STMT.setDate(4, java.sql.Date.valueOf(date));
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

	//region Images
	public Image getImageFromID(int id) throws SSLHandshakeException {
		try {
			checkConnection();
			GET_IMAGE_FROM_ID_STMT.setInt(1, id);
			ResultSet resultSet = GET_IMAGE_FROM_ID_STMT.executeQuery();
			if (resultSet.next()) {
				return new Image(resultSet);
			}
		} catch (SQLException e) {
			e.printStackTrace();
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
