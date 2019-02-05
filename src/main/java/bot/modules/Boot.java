package bot.modules;

import bot.Chatbot;
import bot.utils.DatabaseModule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Boot implements DatabaseModule {
	//region Constants
	private final Chatbot chatbot;
	//endregion

	//region Database statements
	private PreparedStatement GET_RAND_BOOT_IMG_STMT;
	private PreparedStatement GET_RAND_BOOT_MSG_STMT;
	//endregion

	public Boot(Chatbot chatbot) {
		this.chatbot = chatbot;
	}

	public String getRandomBootImage() {
		try {
			chatbot.getDb().checkConnection();
			ResultSet resultSet = GET_RAND_BOOT_IMG_STMT.executeQuery();
			if (resultSet.next()) {
				return resultSet.getString("I_url");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getRandomBootMessage() {
		try {
			chatbot.getDb().checkConnection();
			ResultSet resultSet = GET_RAND_BOOT_MSG_STMT.executeQuery();
			if (resultSet.next()) {
				return resultSet.getString("B_message");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	//region Overrides
	@Override
	public void prepareStatements(Connection connection) throws SQLException {
		GET_RAND_BOOT_IMG_STMT = connection.prepareStatement("" +
			"SELECT " +
			"   I.url as I_URL " +
			"FROM BootImages B " +
			"JOIN Images I on B.image_id = I.ID " +
			"ORDER BY RAND() " +
			"LIMIT 1");
		GET_RAND_BOOT_MSG_STMT = connection.prepareStatement("" +
			"SELECT " +
			"   B.message as B_message " +
			"FROM BootResponses B " +
			"ORDER BY RAND() " +
			"LIMIT 1");
	}
	//endregion
}