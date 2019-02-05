package bot.modules;

import bot.Chatbot;
import bot.utils.CommandModule;
import bot.utils.message.Message;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static bot.utils.CONSTANTS.*;

public class Stats implements CommandModule {
	//region Constants
	private final String STATS_REGEX = ACTIONIFY("stats");
	private final String UPTIME_REGEX = ACTIONIFY("uptime");
	private final String PUPTIME_REGEX = ACTIONIFY("puptime");
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
			"Unique messages read this session: " + chatbot.getMessageCount();
	}

	//region Overrides
	@Override
	public boolean process(Message message) {
		String match = getMatch(message);
		if (match.equals(STATS_REGEX)) {
			chatbot.sendMessage(getStats());
			return true;
		} else if (match.equals(UPTIME_REGEX) || match.equals(PUPTIME_REGEX)) {
			chatbot.sendMessage(getUptime());
			return true;
		} else {
			return false;
		}
	}

	@Override
	@SuppressWarnings("Duplicates")
	public String getMatch(Message message) {
		String messageBody = message.getMessage();
		if (messageBody.matches(STATS_REGEX)) {
			return STATS_REGEX;
		} else if (messageBody.matches(UPTIME_REGEX)) {
			return UPTIME_REGEX;
		} else if (messageBody.matches(PUPTIME_REGEX)) {
			return PUPTIME_REGEX;
		} else {
			return "";
		}
	}

	@Override
	@SuppressWarnings("Duplicates")
	public ArrayList<String> getCommands() {
		ArrayList<String> commands = new ArrayList<>();
		commands.add(DEACTIONIFY(STATS_REGEX));
		commands.add(DEACTIONIFY(UPTIME_REGEX));
		commands.add(DEACTIONIFY(PUPTIME_REGEX));
		return commands;
	}

	//endregion
}