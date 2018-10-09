package bot.modules;

import bot.Chatbot;
import bot.utils.Message;
import bot.utils.Module;
import bot.utils.exceptions.MalformedCommandException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static bot.utils.CONSTANTS.ACTIONIFY;

public class Quotes implements Module {
    //Constants
    private final String QUOTE_REGEX = ACTIONIFY("quote");
    private final String GRAB_REGEX = ACTIONIFY("grab");
    private final String GRAB_OFFSET_REGEX = ACTIONIFY("grab (\\d+)");
    private final String RELOAD_QUOTE_REGEX = ACTIONIFY("quote reload");
    private final File quoteFile;

    private final Chatbot chatbot;

    //Variables
    private JSONParser jsonParser = new JSONParser();
    private JSONArray quotesList;


    public Quotes(Chatbot chatbot) {
        this.chatbot = chatbot;
        quoteFile = new File(appendModulePath(chatbot.getThreadId() + "-quotes.json"));
        reloadQuotes();
    }

    @Override
    public boolean process(Message message) throws MalformedCommandException {
        String match = getMatch(message);
        if (match.equals(QUOTE_REGEX)) {
            quote();

            return true;
        } else if (match.equals(GRAB_REGEX)) {
            grab(message, 1);

            return true;
        } else if (match.equals(GRAB_OFFSET_REGEX)) {
            Matcher matcher = Pattern.compile(GRAB_OFFSET_REGEX).matcher(message.getMessage());
            if (matcher.find()) {
                grab(message, Integer.parseInt(matcher.group(1)));

                return true;
            } else {
                throw new MalformedCommandException();
            }
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
        } else if (messageBody.matches(GRAB_OFFSET_REGEX)) {
            return GRAB_OFFSET_REGEX;
        } else if (messageBody.matches(RELOAD_QUOTE_REGEX)) {
            return RELOAD_QUOTE_REGEX;
        } else {
            return "";
        }
    }

    @Override
    public String appendModulePath(String message) {
        return chatbot.appendRootPath("modules/" + getClass().getSimpleName() + "/" + message);
    }

    private void quote() {
        if (quotesList.size() > 0) {
            JSONObject quote = (JSONObject) quotesList.get((int) (Math.random() * quotesList.size()));
            JSONObject sender = (JSONObject) quote.get("sender");
            chatbot.sendMessage("\"" + quote.get("message") + "\" - " + sender.get("name") + " [" + quote.get("timestamp") + "]");
        } else {
            chatbot.sendMessage("There are no quotes available, why not try !grab or !grab [x] to make some");
        }
    }

    private void grab(Message commandMessage, int offset) {
        ArrayList<Message> messageLog = chatbot.getMessageLog();
        int commandIndex = messageLog.indexOf(commandMessage);
        int targetMessageIndex = commandIndex - offset;

        try {
            Message previousMessage = messageLog.get(targetMessageIndex);

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
        } catch (IndexOutOfBoundsException e) {
            chatbot.sendMessage("That grab is a little too far for me");
        }
    }

    private void reloadQuotes() {
        try {
            if (quoteFile.exists()) {
                FileReader fileReader = new FileReader(quoteFile);
                quotesList = (JSONArray) jsonParser.parse(fileReader);
                fileReader.close();
            } else {
                File directory = quoteFile.getParentFile();
                if (!directory.exists()) {
                    directory.mkdirs();
                }
                quoteFile.createNewFile();
                quotesList = new JSONArray();

                FileWriter fileWriter = new FileWriter(quoteFile, false);
                quotesList.writeJSONString(fileWriter);
                fileWriter.close();
            }
        } catch (IOException e) {
            System.out.println("Quotes are unavailable for this session due to an error reading the file");
            e.printStackTrace();
        } catch (ParseException e) {
            System.out.println("Quotes are unavailable for this session due to the error");
            e.printStackTrace();
        }
    }


}