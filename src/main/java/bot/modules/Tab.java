package bot.modules;

import bot.Chatbot;
import bot.utils.CommandModule;
import bot.utils.Message;

import java.util.ArrayList;

import static bot.utils.CONSTANTS.ACTIONIFY;
import static bot.utils.CONSTANTS.DEACTIONIFY;

public class Tab implements CommandModule {
    //region Constants
    private final String TAB_REGEX = ACTIONIFY("tab");
    private final Chatbot chatbot;
    //endregion

    public Tab(Chatbot chatbot) {
        this.chatbot = chatbot;
    }

    //region Database commands

    //region Overrides
    @Override
    @SuppressWarnings("Duplicates")
    public boolean process(Message message) {
        String match = getMatch(message);
        if (match.equals(TAB_REGEX)) {
            chatbot.sendImageWithMessage("https://www.hollandjake.com/dogbot/tabulance.png", "\uD83D\uDEA8 WEE WOO WEE WOO \uD83D\uDEA8");
            return true;
        } else {
            return false;
        }
    }

    @Override
    @SuppressWarnings("Duplicates")
    public String getMatch(Message message) {
        String messageBody = message.getMessage();
        if (messageBody.matches(TAB_REGEX)) {
            return TAB_REGEX;
        } else {
            return "";
        }
    }

    @Override
    @SuppressWarnings("Duplicates")
    public ArrayList<String> getCommands() {
        ArrayList<String> commands = new ArrayList<>();
        commands.add(DEACTIONIFY(TAB_REGEX));
        return commands;
    }
    //endregion
}