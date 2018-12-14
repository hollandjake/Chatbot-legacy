package bot.modules;

import bot.Chatbot;
import bot.utils.CommandModule;
import bot.utils.Message;

import java.util.ArrayList;

import static bot.utils.CONSTANTS.*;

public class Inspire implements CommandModule {
    //region Constants
    private final String INSPIRE_REGEX = ACTIONIFY("inspire");
    private final Chatbot chatbot;
    //endregion

    public Inspire(Chatbot chatbot) {
        this.chatbot = chatbot;
    }

    //region Overrides
    @Override
    @SuppressWarnings("Duplicates")
    public boolean process(Message message) {
        String match = getMatch(message);
        if (match.equals(INSPIRE_REGEX)) {
            String imgURL = GET_PAGE_SOURCE("http://inspirobot.me/api?generate=true");
            chatbot.sendImageWithMessage(imgURL, "Inspiring.");
            return true;
        } else {
            return false;
        }
    }

    @Override
    @SuppressWarnings("Duplicates")
    public String getMatch(Message message) {
        String messageBody = message.getMessage();
        if (messageBody.matches(INSPIRE_REGEX)) {
            return INSPIRE_REGEX;
        } else {
            return "";
        }
    }

    @Override
    @SuppressWarnings("Duplicates")
    public ArrayList<String> getCommands() {
        ArrayList<String> commands = new ArrayList<>();
        commands.add(DEACTIONIFY(INSPIRE_REGEX));
        return commands;
    }
    //endregion
}