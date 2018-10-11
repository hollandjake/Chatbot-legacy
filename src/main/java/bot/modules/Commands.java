package bot.modules;

import bot.Chatbot;
import bot.utils.Message;
import bot.utils.Module;

import static bot.utils.CONSTANTS.ACTIONIFY;

public class Commands implements Module {
    //region Constants
    private final String HELP_REGEX = ACTIONIFY("help");
    private final String COMMANDS_REGEX = ACTIONIFY("commands");
    private final String url;
    private final Chatbot chatbot;
    //endregion

    public Commands(Chatbot chatbot, String url) {
        this.chatbot = chatbot;
        this.url = url;
    }

    //region Overrides
    @Override
    @SuppressWarnings("Duplicates")
    public boolean process(Message message) {
        String match = getMatch(message);
        if (match.equals(COMMANDS_REGEX) || match.equals(HELP_REGEX)) {
            chatbot.sendMessage("A list of commands can be found at " + url);
            return true;
        } else {
            return false;
        }
    }

    @Override
    @SuppressWarnings("Duplicates")
    public String getMatch(Message message) {
        String messageBody = message.getMessage();
        if (messageBody.matches(COMMANDS_REGEX)) {
            return COMMANDS_REGEX;
        } else if (messageBody.matches(HELP_REGEX)) {
            return HELP_REGEX;
        } else {
            return "";
        }
    }

    @Override
    public String appendModulePath(String message) {
        return chatbot.appendRootPath("modules/" + getClass().getSimpleName() + "/" + message);
    }
    //endregion
}