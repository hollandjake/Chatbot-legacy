package bot.modules;

import bot.Chatbot;
import bot.utils.Message;
import bot.utils.Module;

import static bot.utils.CONSTANTS.ACTIONIFY;

public class Github implements Module {
    //region Constants
    private final String GITHUB_REGEX = ACTIONIFY("github");
    private final String url = "https://github.com/hollandjake/Chatbot";
    private final Chatbot chatbot;
    //endregion

    public Github(Chatbot chatbot) {
        this.chatbot = chatbot;
    }

    //region Overrides
    @Override
    @SuppressWarnings("Duplicates")
    public boolean process(Message message) {
        String match = getMatch(message);
        if (match.equals(GITHUB_REGEX)) {
            chatbot.sendMessage("Github repository: " + url);
            return true;
        } else {
            return false;
        }
    }

    @Override
    @SuppressWarnings("Duplicates")
    public String getMatch(Message message) {
        String messageBody = message.getMessage();
        if (messageBody.matches(GITHUB_REGEX)) {
            return GITHUB_REGEX;
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