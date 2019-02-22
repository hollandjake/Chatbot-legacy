package bot.modules;

import bot.core.Chatbot;
import bot.core.utils.CONSTANTS;
import bot.core.utils.message.CommandMatch;
import bot.core.utils.message.Message;
import bot.core.utils.module.CommandModule;

import java.util.List;
import java.util.stream.Collectors;

public class OneLinkCommand implements CommandModule {
	//region Constants

	//Put all regexes into a list
	private final List<String> regexes;

	private final String url;
	private final String message;
	private final Chatbot chatbot;
	//endregion

	public OneLinkCommand(Chatbot chatbot, List<String> commands, String link, String message) {
		this.chatbot = chatbot;
		this.regexes = commands.stream().map(CONSTANTS::ACTIONIFY).collect(Collectors.toList());
		this.url = link;
		this.message = message;
	}

	//region Overrides
	@Override
	@SuppressWarnings("Duplicates")
	public boolean process(Message message) {
		CommandMatch match = CommandMatch.findMatch(regexes, message);
		if (match != null) {
			chatbot.sendMessage(this.message + ":\n" + url);
			return true;
		}
		return false;
	}

	@Override
	public List<String> getRegexes() {
		return regexes;
	}
	//endregion
}