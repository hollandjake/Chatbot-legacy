package bot.modules;

import bot.Chatbot;
import bot.utils.Message;
import bot.utils.RedditModule;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import static bot.utils.CONSTANTS.ACTIONIFY;
import static bot.utils.CONSTANTS.DEACTIONIFY;

public class Cats extends RedditModule {
    //region Constants
    private final String CAT_REGEX = ACTIONIFY("cat");
    //endregion

    public Cats(Chatbot chatbot) {
        super(chatbot);
    }

    //region Overrides
    @Override
    @SuppressWarnings("Duplicates")
    public boolean process(Message message) {
        String match = getMatch(message);
        if (match.equals(CAT_REGEX)) {
            String response = getResponse();
            String image = getImage();
            chatbot.sendImageWithMessage(image, response);
            return true;
        } else {
            return false;
        }
    }

    @Override
    @SuppressWarnings("Duplicates")
    public String getMatch(Message message) {
        String messageBody = message.getMessage();
        if (messageBody.matches(CAT_REGEX)) {
            return CAT_REGEX;
        } else {
            return "";
        }
    }

    @Override
    @SuppressWarnings("Duplicates")
    public ArrayList<String> getCommands() {
        ArrayList<String> commands = new ArrayList<>();
        commands.add(DEACTIONIFY(CAT_REGEX));
        return commands;
    }

    @Override
    public void prepareStatements(Connection connection) throws SQLException {
        GET_SUBREDDITS_STMT = connection.prepareStatement("" +
                "SELECT" +
                "   S.link as S_link " +
                "FROM Subreddits S " +
                "WHERE type = 'Cats'");

        GET_RESPONSE_STMT = connection.prepareStatement("" +
                "SELECT" +
                "   R.message as R_message " +
                "FROM CatResponses R " +
                "ORDER BY RAND() " +
                "LIMIT 1");
    }
    //endregion
}