package bot.modules;

import bot.Chatbot;
import bot.utils.CommandModule;
import bot.utils.Message;
import bot.utils.exceptions.MalformedCommandException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static bot.utils.CONSTANTS.*;

public class XKCD implements CommandModule {
    //region Constants
    private final String XKCD_REGEX = ACTIONIFY("xkcd");
    private final String LATEST_SHORT_XKCD_REGEX = ACTIONIFY("xkcd l");
    private final String LATEST_XKCD_REGEX = ACTIONIFY("xkcd latest");
    private final String SPECIFIC_XKCD_REGEX = ACTIONIFY("xkcd ([1-9][0-9]*)");
    private final Chatbot chatbot;
    //endregion

    //region Variables
    int highestNumber;
    //endregion

    public XKCD(Chatbot chatbot) {
        this.chatbot = chatbot;
        this.highestNumber = (int) new JSONObject(GET_PAGE_SOURCE("https://xkcd.com/info.0.json")).get("num");
    }

    private void sendXKCD(int number) {
        this.highestNumber = (int) new JSONObject(GET_PAGE_SOURCE("https://xkcd.com/info.0.json")).get("num");
        if (number < 1 || highestNumber < number) {
            chatbot.sendMessage("XKCD number out of range. Please try XKCD's in range 1-" + highestNumber);
        } else {
            JSONObject xkcd = new JSONObject(GET_PAGE_SOURCE("https://xkcd.com/" + number + "/info.0.json"));

            String title = xkcd.get("safe_title").toString();
            String alt = xkcd.get("alt").toString();
            String imgURL = xkcd.get("img").toString();

            String response =
                    "Title: " + title +
                            "\nNumber: " + number +
                            "\nAlt text: " + alt;

            chatbot.sendImageWithMessage(imgURL, response);
        }
    }

    private void sendRandomXKCD() {
        sendXKCD((int) (Math.random() * highestNumber) + 1);
    }

    //region Overrides
    @Override
    @SuppressWarnings("Duplicates")
    public boolean process(Message message) throws MalformedCommandException {
        String match = getMatch(message);
        if (match.equals(XKCD_REGEX)) {
            sendRandomXKCD();
            return true;
        } else if (match.equals(LATEST_SHORT_XKCD_REGEX) || match.equals(LATEST_XKCD_REGEX)) {
            sendXKCD(highestNumber);
            return true;
        } else if (match.equals(SPECIFIC_XKCD_REGEX)) {
            Matcher matcher = Pattern.compile(SPECIFIC_XKCD_REGEX).matcher(message.getMessage());
            if (matcher.find()) {
                int number = Integer.parseInt(matcher.group(1));
                sendXKCD(number);
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
        if (messageBody.matches(XKCD_REGEX)) {
            return XKCD_REGEX;
        } else if (messageBody.matches(LATEST_XKCD_REGEX)) {
            return LATEST_XKCD_REGEX;
        } else if (messageBody.matches(LATEST_SHORT_XKCD_REGEX)) {
            return LATEST_SHORT_XKCD_REGEX;
        } else if (messageBody.matches(SPECIFIC_XKCD_REGEX)) {
            return SPECIFIC_XKCD_REGEX;
        } else {
            return "";
        }
    }

    @Override
    @SuppressWarnings("Duplicates")
    public ArrayList<String> getCommands() {
        ArrayList<String> commands = new ArrayList<>();
        commands.add(DEACTIONIFY(XKCD_REGEX));
        commands.add(DEACTIONIFY(LATEST_XKCD_REGEX));
        commands.add(DEACTIONIFY(LATEST_SHORT_XKCD_REGEX));
        commands.add(DEACTIONIFY(SPECIFIC_XKCD_REGEX));
        return commands;
    }

    //endregion
}