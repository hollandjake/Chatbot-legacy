package bot.modules;

import bot.Chatbot;
import bot.utils.CONSTANTS;
import bot.utils.Message;
import bot.utils.Module;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OneLinkCommand implements Module {
    //region Constants
    private final List<String> COMMAND_REGEXES;
    private final String url;
    private final String message;
    private final Chatbot chatbot;
    //endregion

    public OneLinkCommand(Chatbot chatbot, List<String> commands, String link, String message) {
        this.chatbot = chatbot;
        this.COMMAND_REGEXES = commands.stream().map(CONSTANTS::ACTIONIFY).collect(Collectors.toList());
        this.url = link;
        this.message = message;
    }

    //region Overrides
    @Override
    @SuppressWarnings("Duplicates")
    public boolean process(Message message) {
        String match = getMatch(message);
        for (String command : COMMAND_REGEXES) {
            if (match.equals(command)) {
                chatbot.sendMessage(this.message + ":\n" + url);
                return true;
            }
        }
        return false;
    }

    @Override
    @SuppressWarnings("Duplicates")
    public String getMatch(Message message) {
        String messageBody = message.getMessage();
        for (String command : COMMAND_REGEXES) {
            if (messageBody.matches(command)) {
                return command;
            }
        }
        return "";
    }

    @Override
    @SuppressWarnings("Duplicates")
    public ArrayList<String> getCommands() {
        return (ArrayList<String>) COMMAND_REGEXES.stream().map(CONSTANTS::DEACTIONIFY).collect(Collectors.toList());
    }

    @Override
    public String appendModulePath(String message) {
        return chatbot.appendRootPath("modules/" + getClass().getSimpleName() + "/" + message);
    }
    //endregion
}