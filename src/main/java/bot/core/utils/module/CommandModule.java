package bot.core.utils.module;

import bot.core.utils.exceptions.MalformedCommandException;
import bot.core.utils.message.Message;

import java.util.List;

public interface CommandModule {
	boolean process(Message message) throws MalformedCommandException;

	List<String> getRegexes();
}