package bot.core.utils.message;

import java.util.List;

public class CommandMatch {
	private final String regex;
	private final MessageComponent matchedComponent;

	public CommandMatch(String regex, MessageComponent matchedComponent) {
		this.regex = regex;
		this.matchedComponent = matchedComponent;
	}

	public static CommandMatch findMatch(List<String> regexes, MessageComponent component) {
		for (String regex : regexes) {
			if (component.matches(regex)) {
				return new CommandMatch(regex, component);
			}
		}
		return null;
	}

	public static CommandMatch findMatch(List<String> regexes, Message message) {
		for (MessageComponent component : message.getMessageComponents()) {
			CommandMatch match = findMatch(regexes, component);
			if (match != null) {
				return match;
			}
		}
		return null;
	}

	public boolean regexMatch(String... regexes) {
		for (String regex : regexes) {
			if (regex.equals(this.regex)) {
				return true;
			}
		}
		return false;
	}

	public boolean componentMatch(Object obj) {
		return matchedComponent.matches(obj);
	}

	//region Getters
	public String getRegex() {
		return regex;
	}

	public MessageComponent getMatchedComponent() {
		return matchedComponent;
	}
	//endregion
}
