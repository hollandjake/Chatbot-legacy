package bot.modules;

import bot.core.Chatbot;
import bot.core.utils.message.CommandMatch;
import bot.core.utils.message.Message;
import bot.core.utils.module.CommandModule;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static bot.core.utils.CONSTANTS.ACTIONIFY;
import static bot.core.utils.CONSTANTS.DATE_TIME_FORMATTER;

public class Stats implements CommandModule {
	//region Constants
	private final String STATS_REGEX = ACTIONIFY("stats");
	private final String UPTIME_REGEX = ACTIONIFY("uptime");
	private final String PUPTIME_REGEX = ACTIONIFY("puptime");

	//Put all regexes into a list
	private final List<String> regexes = Arrays.asList(STATS_REGEX, UPTIME_REGEX, PUPTIME_REGEX);
	private final Chatbot chatbot;
	//endregion

	public Stats(Chatbot chatbot) {
		this.chatbot = chatbot;
	}

	private String getUptime() {
		LocalDateTime startupTime = chatbot.getStartupTime();
		LocalDateTime now = LocalDateTime.now();
		long diff = now.toEpochSecond(ZoneOffset.UTC) - startupTime.toEpochSecond(ZoneOffset.UTC);
		long diffSeconds = TimeUnit.SECONDS.convert(diff, TimeUnit.SECONDS) % 60;
		long diffMinutes = TimeUnit.MINUTES.convert(diff, TimeUnit.SECONDS) % 60;
		long diffHours = TimeUnit.HOURS.convert(diff, TimeUnit.SECONDS) % 24;
		long diffDays = TimeUnit.DAYS.convert(diff, TimeUnit.SECONDS);
		return "I've been running since " + DATE_TIME_FORMATTER.format(startupTime) + "\n[" +
			(diffDays > 0 ? diffDays + " day" + (diffDays != 1 ? "s" : "") + " " : "") +
			(diffHours > 0 ? diffHours + " hour" + (diffHours != 1 ? "s" : "") + " " : "") +
			(diffMinutes > 0 ? diffMinutes + " minute" + (diffMinutes != 1 ? "s" : "") + " " : "") +
			diffSeconds + " second" + (diffSeconds != 1 ? "s" : "") + "]";
	}

	public String getMinifiedStats() {
		return "Version: " + chatbot.getVersion() + "\n" +
			"Java version: " + System.getProperty("java.version") + "\n" +
			"Operating System: " + System.getProperty("os.name");
	}

	private String getStats() {
		return getMinifiedStats() + "\n" +
			"\n" +
			getUptime() + "\n" +
			"Number of messages stored: " + chatbot.getMessageCount();
	}

	//region Overrides
	@Override
	public boolean process(Message message) {
		CommandMatch match = CommandMatch.findMatch(regexes, message);
		if (match != null) {
			if (match.regexMatch(STATS_REGEX)) {
				chatbot.sendMessage(getStats());
			} else if (match.regexMatch(UPTIME_REGEX, PUPTIME_REGEX)) {
				chatbot.sendMessage(getUptime());
			}
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