package bot.modules;

import bot.Chatbot;
import bot.utils.Message;
import bot.utils.Module;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

import static bot.utils.CONSTANTS.ACTIONIFY;
import static bot.utils.CONSTANTS.DEACTIONIFY;

public class Tab implements Module {
    //region Constants
    private final String TAB_REGEX = ACTIONIFY("tab");
    private final Image tab;
    private final Chatbot chatbot;
    //endregion

    public Tab(Chatbot chatbot) {
        this.chatbot = chatbot;
        tab = new ImageIcon(appendModulePath("tabulance.png")).getImage();
    }

    //region Overrides
    @Override
    @SuppressWarnings("Duplicates")
    public boolean process(Message message) {
        String match = getMatch(message);
        if (match.equals(TAB_REGEX)) {
            chatbot.sendImageWithMessage(tab, "\uD83D\uDEA8 WEE WOO WEE WOO \uD83D\uDEA8");
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

    @Override
    public String appendModulePath(String message) {
        return chatbot.appendRootPath("modules/" + getClass().getSimpleName() + "/" + message);
    }
    //endregion
}