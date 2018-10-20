package bot.utils;

import bot.utils.exceptions.MalformedCommandException;

import java.util.ArrayList;

public interface Module {
    boolean process(Message message) throws MalformedCommandException;

    String getMatch(Message message);

    String appendModulePath(String message);

    ArrayList<String> getCommands();
}