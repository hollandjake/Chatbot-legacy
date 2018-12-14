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

public class Reacts implements CommandModule, DatabaseModule {
    //region Constants
    private final String REACT_REGEX = ACTIONIFY("react (.*)");
    private final String REAC_REGEX = ACTIONIFY("reac+ (.*)");
    private final Chatbot chatbot;
    private final Database db;
    //endregion

    //region Database statements
    private PreparedStatement GET_RANDOM_REACT_STMT;
    //endregion

    public Reacts(Chatbot chatbot) {
        this.chatbot = chatbot;
        this.db = chatbot.getDb();
    }

    private String getReact() {
        try {
            db.checkConnection();
            ResultSet resultSet = GET_RANDOM_REACT_STMT.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("I_url");
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
        if (match.equals(REACT_REGEX) || match.equals(REAC_REGEX)) {
            Matcher matcher = Pattern.compile(match).matcher(message.getMessage());
            if (matcher.find() && matcher.group(1) != null) {
                chatbot.sendImageWithMessage(getReact(), "Judging. \uD83E\uDD14");
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
        if (messageBody.matches(REACT_REGEX)) {
            return REACT_REGEX;
        } else if (messageBody.matches(REAC_REGEX)) {
            return REAC_REGEX;
        } else {
            return "";
        }
    }

    @Override
    @SuppressWarnings("Duplicates")
    public ArrayList<String> getCommands() {
        ArrayList<String> commands = new ArrayList<>();
        commands.add(DEACTIONIFY(REACT_REGEX));
        commands.add(DEACTIONIFY(REAC_REGEX));
        return commands;
    }

    @Override
    public void prepareStatements(Connection connection) throws SQLException {
        GET_RANDOM_REACT_STMT = connection.prepareStatement("" +
                "SELECT" +
                "   I.url as I_url " +
                "FROM Reacts R " +
                "JOIN Images I on R.image_id = I.ID " +
                "ORDER BY RAND() " +
                "LIMIT 1");
    }
    //endregion
}