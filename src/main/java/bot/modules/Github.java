package bot.modules;

import bot.Chatbot;
import bot.utils.Message;
import bot.utils.Module;

import java.util.ArrayList;

import static bot.utils.CONSTANTS.ACTIONIFY;
import static bot.utils.CONSTANTS.DEACTIONIFY;

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
    @SuppressWarnings("Duplicates")
    public ArrayList<String> getCommands() {
        ArrayList<String> commands = new ArrayList<>();
        commands.add(DEACTIONIFY(GITHUB_REGEX));
        return commands;
    }

    @Override
    public String appendModulePath(String message) {
        return chatbot.appendRootPath("modules/" + getClass().getSimpleName() + "/" + message);
    }
    //endregion
}