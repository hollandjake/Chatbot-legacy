package bot.modules;

import bot.Chatbot;
import bot.utils.Message;
import bot.utils.Module;
import bot.utils.exceptions.MalformedCommandException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static bot.utils.CONSTANTS.ACTIONIFY;

public class Shutdown implements Module {
    //region Constants
    private final String SHUTDOWN_REGEX = ACTIONIFY("shutdown (\\d*)");
    private final Chatbot chatbot;
    //endregion

    public Shutdown(Chatbot chatbot) {
        this.chatbot = chatbot;
    }

    //region Overrides
    @Override
    public boolean process(Message message) throws MalformedCommandException {
        String match = getMatch(message);
        if (match.equals(SHUTDOWN_REGEX)) {
            Matcher matcher = Pattern.compile(SHUTDOWN_REGEX).matcher(message.getMessage());
            if (matcher.find() && matcher.group(1).equals(chatbot.getShutdownCode())) {
                chatbot.quit();
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
        if (messageBody.matches(SHUTDOWN_REGEX)) {
            return SHUTDOWN_REGEX;
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