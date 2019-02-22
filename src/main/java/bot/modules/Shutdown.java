package bot.modules;

import bot.core.Chatbot;
import bot.core.utils.exceptions.MalformedCommandException;
import bot.core.utils.message.*;
import bot.core.utils.module.CommandModule;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static bot.core.utils.CONSTANTS.ACTIONIFY;

public class Shutdown implements CommandModule {
	//region Constants
	private final String SHUTDOWN_REGEX = ACTIONIFY("shutdown (\\d*)");

	//Put all regexes into a list
	private final List<String> regexes = Collections.singletonList(SHUTDOWN_REGEX);

	private final Text shutdownCode;
	private final Chatbot chatbot;
	//endregion

	public Shutdown(Chatbot chatbot) {
		this.chatbot = chatbot;
		this.shutdownCode = new Text(Integer.toString(new Random().nextInt(99999)));
	}

	//region Overrides
	@Override
	public boolean process(Message message) throws MalformedCommandException {
		CommandMatch match = CommandMatch.findMatch(regexes, message);
		if (match != null) {
			if (match.regexMatch(SHUTDOWN_REGEX)) {
				Text component = (Text) match.getMatchedComponent();
				Matcher matcher = Pattern.compile(SHUTDOWN_REGEX).matcher(component.getText());
				if (matcher.find() && matcher.group(1).equals(shutdownCode.getText())) {
					chatbot.quit();
					return true;
				} else {
					throw new MalformedCommandException();
				}
			}
		}
		return false;
	}

	@Override
	public List<String> getRegexes() {
		return regexes;
	}
	//endregion

	public int getShutdownCode() {
		return Integer.parseInt(shutdownCode.getText());
	}
}