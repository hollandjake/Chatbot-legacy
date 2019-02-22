package bot.modules;

import bot.core.Chatbot;
import bot.core.utils.module.DatabaseModule;

import java.sql.Connection;
import java.sql.SQLException;

public class Boot implements DatabaseModule {
	private final Chatbot chatbot;

	public Boot(Chatbot chatbot) {
		this.chatbot = chatbot;
	}

	public void sendBootMessage() {
		chatbot.sendMessage("Chatbot " + chatbot.getVersion() + " is online!");
	}

	@Override
	public void prepareStatements(Connection connection) throws SQLException {
	}
}