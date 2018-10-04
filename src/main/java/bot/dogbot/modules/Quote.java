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
import java.util.ArrayList;

import static bot.utils.CONSTANTS.ACTIONIFY;

public class Quote implements Module {
    private JSONParser jsonParser = new JSONParser();
    private JSONArray quotesList;
    private final File quoteFile;

    private final String QUOTE_REGEX = ACTIONIFY("quote");
    private final String GRAB_REGEX = ACTIONIFY("grab");
    private final String RELOAD_QUOTE_REGEX = ACTIONIFY("quote reload");

    private final ArrayList<Message> messageLog;

    public Quote(ArrayList<Message> messageLog) {
        this.messageLog = messageLog;
        quoteFile = new File("src/main/resources/modules/quotes.json");
        reloadQuotes();
    }

    @Override
    public boolean process(Chatbot chatbot, Message message) {
        String messageBody = message.getMessage();
        if (messageBody.matches(QUOTE_REGEX)) {
            quote(chatbot);
            return true;
        } else if (messageBody.matches(GRAB_REGEX)) {
            grab(chatbot, message);
            return true;
        } else if (messageBody.matches(RELOAD_QUOTE_REGEX)) {
            reloadQuotes();
            chatbot.sendMessage("Quotes Updated");
        }
        return false;
    }

    private void quote(Chatbot chatbot) {
        JSONObject quote = (JSONObject) quotesList.get((int) (Math.random() * quotesList.size()));
        JSONObject sender = (JSONObject) quote.get("sender");
        chatbot.sendMessage("\"" + quote.get("message") + "\" - " + sender.get("name") + " [" + quote.get("timestamp") + "]");
    }

    private void grab(Chatbot chatbot, Message message) {
        Message previousMessage = messageLog.get(messageLog.indexOf(message) - 1);
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