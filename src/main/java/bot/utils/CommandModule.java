package bot.utils;

import bot.utils.exceptions.MalformedCommandException;
import bot.utils.message.Message;

import java.util.ArrayList;

public interface CommandModule {
	boolean process(Message message) throws MalformedCommandException;

	String getMatch(Message message);

	ArrayList<String> getCommands();
}