package bot.utils;

import bot.utils.exceptions.MalformedCommandException;

public interface Module {
    boolean process(Message message) throws MalformedCommandException;

    String getMatch(Message message);

    String appendModulePath(String message);
}