package bot.modules;

import bot.Chatbot;
import bot.utils.*;
import bot.utils.exceptions.MalformedCommandException;

import java.sql.*;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static bot.utils.CONSTANTS.*;

public class Quotes implements CommandModule, DatabaseModule {
    //region Constants
    private final String FULL_CAPS_QUOTE_REGEX = ACTIONIFY_CASE("QUOTE (.+)");
    private final String QUOTE_REGEX = ACTIONIFY("quote (.+)");
    private final String GRAB_REGEX = ACTIONIFY("grab");
    private final String GRAB_OFFSET_REGEX = ACTIONIFY("grab (\\d+)");
    private final String LOCATE_REGEX = ACTIONIFY("(locate|grab) (.+)");
    private final String QUOTE_COUNT_REGEX = ACTIONIFY("quotecount (.+)");
    private final String QUOTE_TOTAL_COUNT_REGEX = ACTIONIFY("quotecount");
    private final Chatbot chatbot;
    private final Database db;
    //endregion

    //region Database Statements
    private PreparedStatement GET_RAND_QUOTE_FROM_THREAD_STMT;
    private PreparedStatement GET_RAND_QUOTE_FROM_THREAD_AND_NAME_STMT;
    private PreparedStatement GET_NUM_QUOTES_FROM_THREAD_STMT;
    private CallableStatement GET_NUM_QUOTES_FROM_THREAD_AND_NAME_STMT;
    private PreparedStatement SAVE_QUOTE_STMT;
    private PreparedStatement CONTAINS_QUOTE_STMT;
    //endregion

    public Quotes(Chatbot chatbot) {
        this.chatbot = chatbot;
        this.db = chatbot.getDb();
    }

    public Quotes(Database db) {
        //used just for initialising the database
        this.chatbot = null;
        this.db = db;
    }

    private void quote(Message quote, String type) {
        if (quote != null) {
            String message = quote.getMessage();
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
            chatbot.sendImageWithMessage(quote.getImageUrl(),
                    (message.length() > 0 ? "\"" + message + "\" - " : "") + quote.getSender() + " [" + quote.getDate().format(CONSTANTS.DATE_FORMATTER) + "]");
        } else {
            chatbot.sendMessage("There are no quotes available, why not try !grab or !grab [x] to make some");
        }
    }

    private void quoteTotal() {
        Integer count = getNumberOfQuotes();

        if (count != null) {
            chatbot.sendMessage("This chat has " + count + " quote" + (count == 1 ? "" : "s"));
        } else {
            chatbot.sendMessage("This chat has 0 quotes. why not try !grab or !grab [x] to make some");
        }
    }

    private void quoteCount(String query) {
        AbstractMap.SimpleEntry<String, Integer> countData = getNumberOfQuotesFromName(query);
        Integer count = countData.getValue();
        String quoteName = countData.getKey();
        if (count != null) {
            chatbot.sendMessage("\"" + quoteName + "\" has " + count + " quotes! :O");
        } else {
            chatbot.sendMessage("\"" + quoteName + "\" has 0 quotes! :'(");
        }
    }

    private void grab(Message commandMessage, int offset) {
        int targetIndex = commandMessage.getId() - offset;
        Message targetMessage = db.getMessage(chatbot.getThreadId(), targetIndex);
        if (targetMessage == null) {
            chatbot.sendMessage("That grab is a little too far for me");
        } else {
            save(commandMessage, targetMessage);
        }
    }

    private void locate(Message commandMessage, String query) {
        Optional<Message> targetMessage = db.getMessagesWithMessageLike(chatbot.getThreadId(), query).stream().filter(message -> !message.equals(commandMessage) && !chatbot.containsCommand(message)).findFirst();
        if (targetMessage.isPresent()) {
            save(commandMessage, targetMessage.get());
        } else {
            chatbot.sendMessage("I can't seem to find a message with \"" + query + "\" in it :'(");
        }
    }

    private boolean save(Message commandMessage, Message message) {
        //Check if message contains a command
        if (message.getMessage().length() == 0 && message.getImageUrl() == null) {
            chatbot.sendMessage("That message is empty");
            return false;
        } else if (message.getSender().equals(commandMessage.getSender())) {
            chatbot.sendMessage("Did you just try and grab yourself? \uD83D\uDE20");
            return false;
        } else if (chatbot.containsCommand(message)) {
            chatbot.sendMessage("Don't do that >:(");
            return false;
        } else if (containsQuote(chatbot.getThreadId(), message)) {
            chatbot.sendMessage("That quote has already been grabbed");
            return false;
        } else {
            saveQuote(chatbot.getThreadId(), message);
            chatbot.sendImageWithMessage(message.getImageUrl(), "Grabbed" + (message.getMessage().length() > 0 ? " \"" + message.getMessage() + "\"" : ""));
            return true;
        }
    }

    //region Database
    public boolean containsQuote(int threadId, Message message) {
        try {
            db.checkConnection();
            CONTAINS_QUOTE_STMT.setInt(1, threadId);
            CONTAINS_QUOTE_STMT.setInt(2, message.getId());
            ResultSet resultSet = CONTAINS_QUOTE_STMT.executeQuery();
            return resultSet.absolute(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean saveQuote(int threadId, Message message) {
        try {
            db.checkConnection();
            SAVE_QUOTE_STMT.setInt(1, threadId);
            SAVE_QUOTE_STMT.setInt(2, message.getId());
            return SAVE_QUOTE_STMT.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Message getRandomQuote() {
        try {
            db.checkConnection();
            GET_RAND_QUOTE_FROM_THREAD_STMT.setInt(1, chatbot.getThreadId());
            return Message.fromResultSet(GET_RAND_QUOTE_FROM_THREAD_STMT.executeQuery());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Message getRandomQuoteFromName(String name) {
        try {
            db.checkConnection();
            GET_RAND_QUOTE_FROM_THREAD_AND_NAME_STMT.setInt(1, chatbot.getThreadId());
            GET_RAND_QUOTE_FROM_THREAD_AND_NAME_STMT.setString(2, name);
            return Message.fromResultSet(GET_RAND_QUOTE_FROM_THREAD_AND_NAME_STMT.executeQuery());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Integer getNumberOfQuotes() {
        try {
            db.checkConnection();
            GET_NUM_QUOTES_FROM_THREAD_STMT.setInt(1, chatbot.getThreadId());
            ResultSet resultSet = GET_NUM_QUOTES_FROM_THREAD_STMT.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public AbstractMap.SimpleEntry<String, Integer> getNumberOfQuotesFromName(String name) {
        try {
            db.checkConnection();
            GET_NUM_QUOTES_FROM_THREAD_AND_NAME_STMT.setInt(1, chatbot.getThreadId());
            GET_NUM_QUOTES_FROM_THREAD_AND_NAME_STMT.setString(2, name);
            ResultSet resultSet = GET_NUM_QUOTES_FROM_THREAD_AND_NAME_STMT.executeQuery();
            if (resultSet.next()) {
                String quoteName = resultSet.getString("H_name");
                Integer count = resultSet.getInt("Q_count");
                return new AbstractMap.SimpleEntry<>(quoteName, count);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    //endregion

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
            Matcher matcher = Pattern.compile(QUOTE_REGEX).matcher(message.getMessage());
            Message quote = null;
            if (matcher.find()) {
                String quoteName = matcher.group(1);
                quote = getRandomQuoteFromName(quoteName);
                if (quote == null) {
                    chatbot.sendMessage("\"" + quoteName + "\" has 0 quotes! :'(");
                    return true;
                }
            }
            if (quote == null) {
                quote = getRandomQuote();
            }
            if (numUpper == numLetters) {
                quote(quote, "caps");
            } else if (numUpper > 1) {
                quote(quote, "shaky");
            } else {
                quote(quote, "normal");
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
                locate(message, matcher.group(2));
                return true;
            } else {
                throw new MalformedCommandException();
            }
        } else if (match.equals(QUOTE_COUNT_REGEX)) {
            Matcher matcher = Pattern.compile(QUOTE_COUNT_REGEX).matcher(message.getMessage());
            if (matcher.find()) {
                quoteCount(matcher.group(1));

                return true;
            } else {
                throw new MalformedCommandException();
            }
        } else if (match.equals(QUOTE_TOTAL_COUNT_REGEX)) {
            quoteTotal();
            return true;
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
        } else if (messageBody.matches(QUOTE_TOTAL_COUNT_REGEX)) {
            return QUOTE_TOTAL_COUNT_REGEX;
        } else {
            return "";
        }
    }

    @Override
    @SuppressWarnings("Duplicates")
    public ArrayList<String> getCommands() {
        ArrayList<String> commands = new ArrayList<>();
        commands.add(DEACTIONIFY_CASE(FULL_CAPS_QUOTE_REGEX));
        commands.add(DEACTIONIFY(QUOTE_REGEX));
        commands.add(DEACTIONIFY(GRAB_REGEX));
        commands.add(DEACTIONIFY(GRAB_OFFSET_REGEX));
        commands.add(DEACTIONIFY(LOCATE_REGEX));
        commands.add(DEACTIONIFY(QUOTE_COUNT_REGEX));
        commands.add(DEACTIONIFY(QUOTE_TOTAL_COUNT_REGEX));
        return commands;
    }

    @Override
    public void prepareStatements(Connection connection) throws SQLException {
        GET_RAND_QUOTE_FROM_THREAD_STMT = connection.prepareStatement("" +
                "SELECT" +
                "   Q.ID as Q_ID," +
                "   Q.thread_id as Q_T," +
                "   M.ID as M_ID," +
                "   H.ID as H_ID," +
                "   H.name as H_name," +
                "   M.message as M_message," +
                "   I.url as I_url," +
                "   M.date as M_date " +
                "FROM Quotes Q " +
                "JOIN Messages M on Q.ID = M.ID and Q.thread_id = M.thread_id " +
                "JOIN Humans H on M.sender_id = H.ID " +
                "LEFT JOIN Images I on M.image_id = I.ID " +
                "WHERE Q.thread_id = ? " +
                "ORDER BY RAND() " +
                "LIMIT 1");
        GET_RAND_QUOTE_FROM_THREAD_AND_NAME_STMT = connection.prepareStatement("" +
                "SELECT" +
                "   Q.ID as Q_ID," +
                "   Q.thread_id as Q_T," +
                "   M.ID as M_ID," +
                "   H.ID as H_ID," +
                "   H.name as H_name," +
                "   M.message as M_message," +
                "   I.url as I_url," +
                "   M.date as M_date " +
                "FROM Quotes Q " +
                "JOIN Messages M on Q.ID = M.ID and Q.thread_id = M.thread_id " +
                "JOIN Humans H on M.sender_id = H.ID " +
                "LEFT JOIN Images I on M.image_id = I.ID " +
                "WHERE Q.thread_id = ? " +
                "AND H.name LIKE CONCAT('%',?,'%')" +
                "ORDER BY RAND() " +
                "LIMIT 1");
        GET_NUM_QUOTES_FROM_THREAD_STMT = connection.prepareStatement("" +
                "SELECT" +
                "   COUNT(Q.ID) " +
                "FROM Quotes Q " +
                "WHERE Q.thread_id = ?");
        GET_NUM_QUOTES_FROM_THREAD_AND_NAME_STMT = connection.prepareCall("{CALL GetNumQuotesFromThreadAndName(?,?)}");
        SAVE_QUOTE_STMT = connection.prepareStatement("INSERT INTO Quotes (thread_id, ID) VALUES (?, ?)");
        CONTAINS_QUOTE_STMT = connection.prepareStatement("" +
                "SELECT Q.ID " +
                "FROM Quotes Q " +
                "WHERE Q.thread_id = ? AND Q.ID = ?");
    }

    //endregion
}