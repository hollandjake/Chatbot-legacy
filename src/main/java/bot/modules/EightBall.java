package bot.modules;

import bot.Chatbot;
import bot.utils.CommandModule;
import bot.utils.Database;
import bot.utils.DatabaseModule;
import bot.utils.Message;
import bot.utils.exceptions.MalformedCommandException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static bot.utils.CONSTANTS.ACTIONIFY;
import static bot.utils.CONSTANTS.DEACTIONIFY;

public class EightBall implements CommandModule, DatabaseModule {
    //region Constants
    private final String NO_QUESTION_REGEX = ACTIONIFY("(8ball|ask)");
    private final String EIGHT_BALL_REGEX = ACTIONIFY("8ball (.*)");
    private final String ASK_REGEX = ACTIONIFY("ask (.*)");
    private final Chatbot chatbot;
    private final Database db;
    //endregion

    //region Database statements
    private PreparedStatement GET_RANDOM_RESPONSE_STMT;
    //endregion

    public EightBall(Chatbot chatbot) {
        this.chatbot = chatbot;
        this.db = chatbot.getDb();
    }

    private String getResponse() {
        try {
            db.checkConnection();
            ResultSet resultSet = GET_RANDOM_RESPONSE_STMT.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("R_message");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
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
                chatbot.sendMessage(getResponse());
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
    public void prepareStatements(Connection connection) throws SQLException {
        GET_RANDOM_RESPONSE_STMT = connection.prepareStatement("" +
                "SELECT" +
                "   R.message as R_message " +
                "FROM EightBallResponses R " +
                "ORDER BY RAND() " +
                "LIMIT 1");
    }
    //endregion
}