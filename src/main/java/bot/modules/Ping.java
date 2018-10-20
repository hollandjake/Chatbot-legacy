package bot.modules;

import bot.Chatbot;
import bot.utils.Message;
import bot.utils.Module;

import java.util.ArrayList;

import static bot.utils.CONSTANTS.ACTIONIFY;
import static bot.utils.CONSTANTS.DEACTIONIFY;

public class Ping implements Module {
    //region Constants
    private final String PING_REGEX = ACTIONIFY("ping");
    private final Chatbot chatbot;
    //endregion

    public Ping(Chatbot chatbot) {
        this.chatbot = chatbot;
    }

    //region Overrides
    @Override
    @SuppressWarnings("Duplicates")
    public boolean process(Message message) {
        String match = getMatch(message);
        if (match.equals(PING_REGEX)) {
            if (Math.random() < 0.1) {
                chatbot.sendImageFromURLWithMessage("https://www.rightthisminute.com/sites/default/files/styles/twitter_card/public/videos/images/munchkin-teddy-bear-dog-ping-pong-video.jpg?itok=ajJWbxY6", "Pong! \uD83C\uDFD3");
            } else {
                chatbot.sendMessage("Pong! \uD83C\uDFD3");
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    @SuppressWarnings("Duplicates")
    public String getMatch(Message message) {
        String messageBody = message.getMessage();
        if (messageBody.matches(PING_REGEX)) {
            return PING_REGEX;
        } else {
            return "";
        }
    }

    @Override
    @SuppressWarnings("Duplicates")
    public ArrayList<String> getCommands() {
        ArrayList<String> commands = new ArrayList<>();
        commands.add(DEACTIONIFY(PING_REGEX));
        return commands;
    }

    @Override
    public String appendModulePath(String message) {
        return chatbot.appendRootPath("modules/" + getClass().getSimpleName() + "/" + message);
    }
    //endregion
}