package bot.dogbot.modules;

import bot.Chatbot;
import bot.utils.Message;
import bot.utils.Module;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import static bot.utils.CONSTANTS.ACTIONIFY;

public class Quote implements Module {
    //Constants
    private final String QUOTE_REGEX = ACTIONIFY("quote");
    private final String GRAB_REGEX = ACTIONIFY("grab");
    private final String RELOAD_QUOTE_REGEX = ACTIONIFY("quote reload");
    private final File quoteFile;

    private final Chatbot chatbot;

    //Variables
    private JSONParser jsonParser = new JSONParser();
    private JSONArray quotesList;


    public Quote(Chatbot chatbot) {
        this.chatbot = chatbot;
        quoteFile = new File("src/main/resources/modules/quotes.json");
        reloadQuotes();
    }

    @Override
    public boolean process(Message message) {
        String match = getMatch(message);
        if (match.equals(QUOTE_REGEX)) {
            quote();

            return true;
        } else if (match.equals(GRAB_REGEX)) {

            try {
                grab(message, chatbot.getMessageLog().get(chatbot.getMessageLog().indexOf(message) - 1));
            } catch (IndexOutOfBoundsException e) {
                chatbot.sendMessage("That grab is a little too far for me");
            }

            return true;
        } else if (match.equals(RELOAD_QUOTE_REGEX)) {
            reloadQuotes();
            chatbot.sendMessage("Quotes updated");

            return true;
        } else {
            return false;
        }
    }

    @Override
    public String getMatch(Message message) {
        String messageBody = message.getMessage();
        if (messageBody.matches(QUOTE_REGEX)) {
            return QUOTE_REGEX;
        } else if (messageBody.matches(GRAB_REGEX)) {
            return GRAB_REGEX;
        } else if (messageBody.matches(RELOAD_QUOTE_REGEX)) {
            return RELOAD_QUOTE_REGEX;
        } else {
            return "";
        }
    }

    private void quote() {
        JSONObject quote = (JSONObject) quotesList.get((int) (Math.random() * quotesList.size()));
        JSONObject sender = (JSONObject) quote.get("sender");
        chatbot.sendMessage("\"" + quote.get("message") + "\" - " + sender.get("name") + " [" + quote.get("timestamp") + "]");
    }

    private void grab(Message commandMessage, Message previousMessage) {

        //Check if message contains a command
        if (previousMessage.doesContainsCommand()) {
            chatbot.sendMessage("Don't do that >:(");
            return;
        } else if (previousMessage.getSender().equals(commandMessage.getSender())) {
            chatbot.sendMessage("Did you just try and grab yourself? \uD83D\uDE20");
            return;
        }

        quotesList.add(previousMessage.toJSON());

        try {
            FileWriter fileWriter = new FileWriter(quoteFile, false);
            quotesList.writeJSONString(fileWriter);
            fileWriter.close();
        } catch (IOException e) {
            System.out.println("Failed to save quote");
            e.printStackTrace();
        }

        chatbot.sendMessage("Grabbed \"" + previousMessage.getMessage() + "\"");
    }

    private void reloadQuotes() {
        try {
            FileReader fileReader = new FileReader(quoteFile);
            quotesList = (JSONArray) jsonParser.parse(fileReader);
            fileReader.close();
        } catch (IOException e) {
            System.out.println("Quote are unavailable for this session due to an error reading the file");
            e.printStackTrace();
        } catch (ParseException e) {
            System.out.println("Quote are unavailable for this session due to the error");
            e.printStackTrace();
        }
    }
}