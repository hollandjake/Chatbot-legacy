package bot.modules;

import bot.core.Chatbot;
import bot.core.utils.message.CommandMatch;
import bot.core.utils.message.Message;
import bot.core.utils.module.CommandModule;

import java.util.Collections;
import java.util.List;

import static bot.core.utils.CONSTANTS.ACTIONIFY;

public class Ping implements CommandModule {
	//region Constants
	private final String PING_REGEX = ACTIONIFY("ping");

	//Put all regexes into a list
	private final List<String> regexes = Collections.singletonList(PING_REGEX);
	private final Chatbot chatbot;
	//endregion

	public Ping(Chatbot chatbot) {
		this.chatbot = chatbot;
	}

	//region Overrides
	@Override
	@SuppressWarnings("Duplicates")
	public boolean process(Message message) {
		CommandMatch match = CommandMatch.findMatch(regexes, message);
		if (match != null) {
			if (match.regexMatch(PING_REGEX)) {
				chatbot.sendMessage("Pong! \uD83C\uDFD3");
			}
			return true;
		}
		return false;
		//endregion
	}

	public List<String> getRegexes() {
		return regexes;
	}
}