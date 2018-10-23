package bot.modules;

import bot.Chatbot;
import bot.utils.Message;
import bot.utils.Module;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static bot.utils.CONSTANTS.*;

public class Dab implements Module {
    //region Constants
    private final String DAB_REGEX = ACTIONIFY("dab");
    private final Chatbot chatbot;
    //endregion

    //region Variables
    private List<String> images;

    //endregion
    public Dab(Chatbot chatbot) {
        this.chatbot = chatbot;
        try {
            images = Files.readAllLines(Paths.get(appendModulePath("pics.txt")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //region Overrides
    @Override
    @SuppressWarnings("Duplicates")
    public boolean process(Message message) {
        String match = getMatch(message);
        if (match.equals(DAB_REGEX)) {
            chatbot.sendImageFromURLWithMessage(GET_RANDOM(images), "Dab on the haters! RAWR");

            return true;
        } else {
            return false;
        }
    }

    @Override
    @SuppressWarnings("Duplicates")
    public String getMatch(Message message) {
        String messageBody = message.getMessage();
        if (messageBody.matches(DAB_REGEX)) {
            return DAB_REGEX;
        } else {
            return "";
        }
    }

    @Override
    @SuppressWarnings("Duplicates")
    public ArrayList<String> getCommands() {
        ArrayList<String> commands = new ArrayList<>();
        commands.add(DEACTIONIFY(DAB_REGEX));
        return commands;
    }

    @Override
    public String appendModulePath(String message) {
        return chatbot.appendRootPath("modules/" + getClass().getSimpleName() + "/" + message);
    }
    //endregion
}