package bot.modules;

import bot.Chatbot;
import bot.utils.Message;
import bot.utils.Module;
import bot.utils.exceptions.MalformedCommandException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static bot.utils.CONSTANTS.*;

public class Reacts implements Module {
    //region Constants
    private final String REACT_REGEX = ACTIONIFY("react (.*)");
    private final String REAC_REGEX = ACTIONIFY("reac (.*)");
    private final String REACC_REGEX = ACTIONIFY("reacc (.*)");
    private final Chatbot chatbot;
    //endregion

    //region Variables
    private List<String> reacts;
    //endregion

    public Reacts(Chatbot chatbot) {
        this.chatbot = chatbot;
        try {
            reacts = Files.readAllLines(Paths.get(appendModulePath("catReacts.txt")));
        } catch (IOException e) {
            System.out.println("Reacts are not available this session");
        }
    }

    //region Overrides
    @Override
    public boolean process(Message message) throws MalformedCommandException {
        String match = getMatch(message);
        if (match.equals(REACT_REGEX) || match.equals(REAC_REGEX) || match.equals(REACC_REGEX)) {
            Matcher matcher = Pattern.compile(match).matcher(message.getMessage());
            if (matcher.find() && matcher.group(1) != null) {
                chatbot.sendImageFromURLWithMessage(GET_RANDOM(reacts), "Judging. \uD83E\uDD14");
                return true;
            } else {
                throw new MalformedCommandException();
            }
        } else {
            return false;
        }
    }

    @Override
    @SuppressWarnings("Duplicates")
    public String getMatch(Message message) {
        String messageBody = message.getMessage();
        if (messageBody.matches(REACT_REGEX)) {
            return REACT_REGEX;
        }
        if (messageBody.matches(REAC_REGEX)) {
            return REAC_REGEX;
        }
        if (messageBody.matches(REACC_REGEX)) {
            return REACC_REGEX;
        } else {
            return "";
        }
    }

    @Override
    @SuppressWarnings("Duplicates")
    public ArrayList<String> getCommands() {
        ArrayList<String> commands = new ArrayList<>();
        commands.add(DEACTIONIFY(REACT_REGEX));
        commands.add(DEACTIONIFY(REAC_REGEX));
        commands.add(DEACTIONIFY(REACC_REGEX));
        return commands;
    }

    @Override
    public String appendModulePath(String message) {
        return chatbot.appendRootPath("modules/" + getClass().getSimpleName() + "/" + message);
    }
    //endregion
}