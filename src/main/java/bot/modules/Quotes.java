package bot.modules;

import bot.Chatbot;
import bot.utils.Message;
import bot.utils.Module;
import bot.utils.exceptions.MalformedCommandException;
import com.google.common.collect.Lists;
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

import static bot.utils.CONSTANTS.*;

public class Quotes implements Module {
    //region Constants
    private final String FULL_CAPS_QUOTE_REGEX = ACTIONIFY_CASE("QUOTE");
    private final String SHAKEY_QUOTE_REGEX = ACTIONIFY_CASE("quote");
    private final String QUOTE_REGEX = ACTIONIFY("quote");
    private final String GRAB_REGEX = ACTIONIFY("grab");
    private final String GRAB_OFFSET_REGEX = ACTIONIFY("grab (\\d+)");
    private final String LOCATE_REGEX = ACTIONIFY("locate (.+)");
    private final String QUOTE_COUNT_REGEX = ACTIONIFY("quotecount (.+)");
    private final String RELOAD_QUOTE_REGEX = ACTIONIFY("quote reload");
    private final JSONParser jsonParser = new JSONParser();
    private final File quoteFile;
    private final Chatbot chatbot;
    //endregion

    //region Variables
    private JSONArray quotesList;
    //endregion

    public Quotes(Chatbot chatbot) {
        this.chatbot = chatbot;
        quoteFile = new File(appendModulePath(chatbot.getThreadId() + "-quotes.json"));
        reloadQuotes();
    }

    //region Overrides
    @Override
    public boolean process(Message message) throws MalformedCommandException {
        String match = getMatch(message);
        if (match.equals(QUOTE_REGEX)) {
            int numUpper = 0;
            int numLetters = 0;
            String text = message.getMessage();
            for (char c : text.toCharArray()) {
                if (Character.isUpperCase(c)) {
                    numUpper++;
                }
                if (Character.isAlphabetic(c)) {
                    numLetters++;
                }
            }
            if (numUpper == numLetters) {
                quote("caps");
            } else if (numUpper > 1) {
                quote("shaky");
            } else {
                quote("normal");
            }
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
        } else if (match.equals(LOCATE_REGEX)) {
            Matcher matcher = Pattern.compile(LOCATE_REGEX).matcher(message.getMessage());
            if (matcher.find()) {
                locate(message, matcher.group(1));

                return true;
            } else {
                throw new MalformedCommandException();
            }
        } else if (match.equals(RELOAD_QUOTE_REGEX)) {
            reloadQuotes();
            chatbot.sendMessage("Quotes updated");

            return true;
        } else if (match.equals(QUOTE_COUNT_REGEX)) {
            Matcher matcher = Pattern.compile(QUOTE_COUNT_REGEX).matcher(message.getMessage());
            if (matcher.find()) {
                quoteCount(matcher.group(1));

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
        if (messageBody.matches(QUOTE_REGEX)) {
            return QUOTE_REGEX;
        } else if (messageBody.matches(GRAB_REGEX)) {
            return GRAB_REGEX;
        } else if (messageBody.matches(GRAB_OFFSET_REGEX)) {
            return GRAB_OFFSET_REGEX;
        } else if (messageBody.matches(LOCATE_REGEX)) {
            return LOCATE_REGEX;
        } else if (messageBody.matches(QUOTE_COUNT_REGEX)) {
            return QUOTE_COUNT_REGEX;
        } else if (messageBody.matches(RELOAD_QUOTE_REGEX)) {
            return RELOAD_QUOTE_REGEX;
        } else {
            return "";
        }
    }

    @Override
    @SuppressWarnings("Duplicates")
    public ArrayList<String> getCommands() {
        ArrayList<String> commands = new ArrayList<>();
        commands.add(DEACTIONIFY_CASE(FULL_CAPS_QUOTE_REGEX));
        commands.add(DEACTIONIFY_CASE(SHAKEY_QUOTE_REGEX));
        commands.add(DEACTIONIFY(QUOTE_REGEX));
        commands.add(DEACTIONIFY(GRAB_REGEX));
        commands.add(DEACTIONIFY(GRAB_OFFSET_REGEX));
        commands.add(DEACTIONIFY(LOCATE_REGEX));
        commands.add(DEACTIONIFY(QUOTE_COUNT_REGEX));
        commands.add(DEACTIONIFY(RELOAD_QUOTE_REGEX));
        return commands;
    }

    @Override
    public String appendModulePath(String message) {
        return chatbot.appendRootPath("modules/" + getClass().getSimpleName() + "/" + message);
    }
    //endregion

    private void quote(String type) {
        if (quotesList.size() > 0) {
            JSONObject quote = (JSONObject) GET_RANDOM(quotesList);
            JSONObject sender = (JSONObject) quote.get("sender");
            String message = (String) quote.get("message");
            switch (type) {
                case "caps":
                    message = message.toUpperCase();
                    break;
                case "shaky":
                    Boolean isCaps = false;
                    String tempMessage = "";
                    for (char x : message.toCharArray()) {
                        if (Character.isAlphabetic(x)) {
                            String c = String.valueOf(x);
                            tempMessage += isCaps ? c.toLowerCase() : c.toUpperCase();
                            isCaps = !isCaps;
                        } else {
                            tempMessage += x;
                        }
                    }
                    message = tempMessage;
            }
            chatbot.sendMessage("\"" + message + "\" - " + sender.get("name") + " [" + quote.get("timestamp") + "]");
        } else {
            chatbot.sendMessage("There are no quotes available, why not try !grab or !grab [x] to make some");
        }
    }

    private void quoteCount(String query) {
        int count = 0;
        for (Object q : quotesList) {
            JSONObject quote = (JSONObject) q;
            JSONObject sender = (JSONObject) quote.get("sender");
            String fullname = sender.get("name").toString();
            String nickname = sender.get("nickname").toString();
            String qry = query.toLowerCase();
            if (fullname.toLowerCase().contains(qry) || nickname.toLowerCase().contains(qry)) {
                query = fullname;
                count++;
            }
        }

        chatbot.sendMessage("\"" + query + "\" has " + count + " quotes!" + (count > 0 ? " :O" : " :'("));
    }

    private void grab(Message commandMessage, int offset) {
        ArrayList<Message> messageLog = chatbot.getMessageLog();
        int commandIndex = messageLog.indexOf(commandMessage);
        int targetMessageIndex = commandIndex - offset;

        try {
            Message previousMessage = messageLog.get(targetMessageIndex);
            save(commandMessage, previousMessage);
        } catch (IndexOutOfBoundsException e) {
            chatbot.sendMessage("That grab is a little too far for me");
        }
    }

    private void locate(Message commandMessage, String query) {
        query = query.toLowerCase();
        ArrayList<Message> messages = chatbot.getMessageLog();
        for (Message message : Lists.reverse(messages)) {
            if (!message.equals(commandMessage) && !message.doesContainsCommand() && message.getMessage().toLowerCase().contains(query)) {
                save(commandMessage, message);
                return;
            }
        }
        chatbot.sendMessage("I can't seem to find a message with \"" + query + "\" in it :'(");
    }

    private boolean save(Message commandMessage, Message message) {
        //Check if message contains a command
        if (message.doesContainsCommand()) {
            chatbot.sendMessage("Don't do that >:(");
            return false;
        } else if (message.getSender().equals(commandMessage.getSender())) {
            chatbot.sendMessage("Did you just try and grab yourself? \uD83D\uDE20");
            return false;
        } else if (message.getMessage().length() == 0) {
            chatbot.sendMessage("That message is empty");
            return false;
        }

        quotesList.add(message.toJSON());
        try {
            FileWriter fileWriter = new FileWriter(quoteFile, false);
            quotesList.writeJSONString(fileWriter);
            fileWriter.close();
        } catch (IOException e) {
            System.out.println("Failed to save quote");
            e.printStackTrace();
        }

        chatbot.sendMessage("Grabbed \"" + message.getMessage() + "\"");
        return true;
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