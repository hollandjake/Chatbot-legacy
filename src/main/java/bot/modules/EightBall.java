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

public class EightBall implements Module {
    //region Constants
    private final String NO_QUESTION_REGEX = ACTIONIFY("8ball|ask");
    private final String EIGHT_BALL_REGEX = ACTIONIFY("8ball (.*)");
    private final String ASK_REGEX = ACTIONIFY("ask (.*)");
    private final Chatbot chatbot;
    //endregion

    //region Variables
    private List<String> responses;
    //endregion

    public EightBall(Chatbot chatbot) {
        this.chatbot = chatbot;
        try {
            responses = Files.readAllLines(Paths.get(appendModulePath("responses.txt")));
        } catch (IOException e) {
            System.out.println("8Ball messages are not available this session");
        }
    }

    //region Overrides
    @Override
    public boolean process(Message message) throws MalformedCommandException {
        String match = getMatch(message);
        if (match.equals(NO_QUESTION_REGEX)) {
            chatbot.sendMessage("Please enter a question after the command");
            return true;
        }
        if (match.equals(EIGHT_BALL_REGEX) || match.equals(ASK_REGEX)) {
            Matcher matcher = Pattern.compile(match).matcher(message.getMessage());
            if (matcher.find() && !matcher.group(1).isEmpty()) {
                chatbot.sendMessage(GET_RANDOM(responses));
            } else {
                throw new MalformedCommandException();
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
        if (messageBody.matches(NO_QUESTION_REGEX)) {
            return NO_QUESTION_REGEX;
        } else if (messageBody.matches(EIGHT_BALL_REGEX)) {
            return EIGHT_BALL_REGEX;
        } else if (messageBody.matches(ASK_REGEX)) {
            return ASK_REGEX;
        } else {
            return "";
        }
    }

    @Override
    @SuppressWarnings("Duplicates")
    public ArrayList<String> getCommands() {
        ArrayList<String> commands = new ArrayList<>();
        commands.add(DEACTIONIFY(EIGHT_BALL_REGEX));
        commands.add(DEACTIONIFY(ASK_REGEX));
        return commands;
    }

    @Override
    public String appendModulePath(String message) {
        return chatbot.appendRootPath("modules/" + getClass().getSimpleName() + "/" + message);
    }

    //endregion
}