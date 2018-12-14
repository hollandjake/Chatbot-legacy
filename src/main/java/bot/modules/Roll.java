package bot.modules;

import bot.Chatbot;
import bot.utils.CommandModule;
import bot.utils.Message;
import bot.utils.exceptions.MalformedCommandException;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static bot.utils.CONSTANTS.ACTIONIFY;
import static bot.utils.CONSTANTS.DEACTIONIFY;

public class Roll implements CommandModule {
    //region Constants
    private final String ROLL_DICE_REGEX = ACTIONIFY("roll");
    private final String ROLL_REGEX = ACTIONIFY("roll (\\d+)");
    private final Chatbot chatbot;
    //endregion

    public Roll(Chatbot chatbot) {
        this.chatbot = chatbot;
    }

    private void roll(int lower, int upper) {
        int number = (int) (Math.random() * (upper - lower) + lower);
        chatbot.sendMessage("You rolled " + number);
    }

    //region Overrides
    @Override
    public boolean process(Message message) throws MalformedCommandException {
        String match = getMatch(message);
        if (match.equals(ROLL_DICE_REGEX)) {
            roll(1, 6);
            return true;
        } else if (match.equals(ROLL_REGEX)) {
            Matcher matcher = Pattern.compile(ROLL_REGEX).matcher(message.getMessage());
            if (matcher.find()) {
                try {
                    roll(1, Integer.parseInt(matcher.group(1)));
                } catch (NumberFormatException e) {
                    throw new MalformedCommandException();
                }
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
        if (messageBody.matches(ROLL_REGEX)) {
            return ROLL_REGEX;
        } else if (messageBody.matches(ROLL_DICE_REGEX)) {
            return ROLL_DICE_REGEX;
        } else {
            return "";
        }
    }

    @Override
    @SuppressWarnings("Duplicates")
    public ArrayList<String> getCommands() {
        ArrayList<String> commands = new ArrayList<>();
        commands.add(DEACTIONIFY(ROLL_REGEX));
        commands.add(DEACTIONIFY(ROLL_DICE_REGEX));
        return commands;
    }

    //endregion

}