package bot.core.utils;

import bot.core.Chatbot;
import bot.core.utils.exceptions.MissingConfigurationsException;
import bot.core.utils.message.*;
import bot.core.utils.module.CommandModule;
import bot.core.utils.module.DatabaseModule;

import java.sql.Date;
import java.sql.*;
import java.time.Duration;
import java.util.*;

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
	private PreparedStatement GET_HUMAN_FROM_ID_STMT;
	private PreparedStatement GET_HUMAN_FROM_NAME_STMT;
	private PreparedStatement GET_HUMAN_FROM_URL_STMT;
	private CallableStatement SAVE_HUMAN_STMT;

	private PreparedStatement GET_MESSAGE_WITH_MESSAGE_STMT;
	private PreparedStatement GET_NUM_MESSAGES_STMT;
	private CallableStatement SAVE_MESSAGE_STMT;
	private CallableStatement SAVE_MESSAGE_WITH_DATE_STMT;

	private PreparedStatement GET_IMAGE_FROM_ID_STMT;
	private CallableStatement SAVE_IMAGE_STMT;

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
		//region Human
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
		SAVE_HUMAN_STMT = connection.prepareCall("{CALL SaveHuman(?,?)}");
		//endregion

		//<editor-fold desc="Description">
		GET_MESSAGE_WITH_MESSAGE_STMT = connection.prepareStatement("" +
			"SELECT" +
			"   M.ID as M_ID," +
			"   H.ID as H_ID," +
			"   H.name as H_name," +
			"	H.url as H_url," +
			"   M.message as M_message," +
			"   M.date as M_date " +
			"FROM Messages M " +
			"JOIN Humans H on M.sender_id = H.ID " +
			"AND M.message COLLATE UTF8MB4_GENERAL_CI LIKE CONCAT('%', ? ,'%')" +
			"ORDER BY M.ID DESC");
		GET_NUM_MESSAGES_STMT = connection.prepareStatement("" +
			"SELECT COUNT(M.ID) " +
			"FROM Messages M");

		SAVE_MESSAGE_STMT = connection.prepareCall("{CALL SaveMessage(?, NOW(), ?)}");
		SAVE_MESSAGE_WITH_DATE_STMT = connection.prepareCall("{CALL SaveMessage(?, ?, ?)}");
		//</editor-fold>

		GET_IMAGE_FROM_ID_STMT = connection.prepareStatement("" +
			"SELECT " +
			"   I.ID as I_ID," +
			"	I.url as I_url " +
			"FROM Images I " +
			"WHERE I.ID = ?");
		SAVE_IMAGE_STMT = connection.prepareCall("{CALL SaveImage(?)}");

		if (chatbot != null) {
			for (CommandModule module : chatbot.getModules().values()) {
				if (module instanceof DatabaseModule) {
					((DatabaseModule) module).prepareStatements(connection);
				}
			}
			chatbot.getBootModule().prepareStatements(connection);
		}
	}

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

	public Human saveHuman(String name, String url) {
		try {
			checkConnection();
			SAVE_HUMAN_STMT.setString(1, url);
			SAVE_HUMAN_STMT.setString(2, name);
			ResultSet resultSet = SAVE_HUMAN_STMT.executeQuery();
			if (resultSet.next()) {
				return new Human(resultSet);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	//endregion

	//region Message
	public ArrayList<Message> getMessagesWithMessageLike(String query) {
		ArrayList<Message> messages = new ArrayList<>();
		try {
			checkConnection();
			GET_MESSAGE_WITH_MESSAGE_STMT.setString(1, query);
			ResultSet resultSet = GET_MESSAGE_WITH_MESSAGE_STMT.executeQuery();
			while (resultSet.next()) {
				messages.add(new Message(chatbot, resultSet));
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

	public Message saveMessage(Human sender, List<MessageComponent> messageComponents) {
		try {
			checkConnection();
			String comb = Message.combineComponents(messageComponents);
			SAVE_MESSAGE_STMT.setInt(1, sender.getID());
			SAVE_MESSAGE_STMT.setString(2, comb);
			ResultSet resultSet = SAVE_MESSAGE_STMT.executeQuery();
			if (resultSet.next()) {
				return new Message(chatbot, resultSet);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Message saveMessage(Message message) {
		try {
			checkConnection();
			SAVE_MESSAGE_WITH_DATE_STMT.setInt(1, message.getSender().getID());
			SAVE_MESSAGE_WITH_DATE_STMT.setDate(2, Date.valueOf(message.getDate()));
			SAVE_MESSAGE_WITH_DATE_STMT.setString(3, message.combineComponents());
			ResultSet resultSet = SAVE_MESSAGE_WITH_DATE_STMT.executeQuery();
			if (resultSet.next()) {
				return new Message(chatbot, resultSet);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	//endregion

	//region Images
	public Image getImageFromID(int id) {
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

	public Image saveImage(String url) {
		try {
			checkConnection();
			SAVE_IMAGE_STMT.setString(1, url);
			ResultSet resultSet = SAVE_IMAGE_STMT.executeQuery();
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
}
