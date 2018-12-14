package bot.modules;

import bot.Chatbot;
import bot.utils.Message;
import bot.utils.RedditModule;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import static bot.utils.CONSTANTS.ACTIONIFY;
import static bot.utils.CONSTANTS.DEACTIONIFY;

public class Birds extends RedditModule {
    //region Constants
    private final String BIRD_REGEX = ACTIONIFY("bird");
    private final String BIRB_REGEX = ACTIONIFY("birb");
    //endregion

    public Birds(Chatbot chatbot) {
        super(chatbot);
    }

    //region Overrides
    @Override
    @SuppressWarnings("Duplicates")
    public boolean process(Message message) {
        String match = getMatch(message);
        if (match.equals(BIRD_REGEX) || match.equals(BIRB_REGEX)) {
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
        if (messageBody.matches(BIRD_REGEX)) {
            return BIRD_REGEX;
        } else if (messageBody.matches(BIRB_REGEX)) {
            return BIRB_REGEX;
        } else {
            return "";
        }
    }

    @Override
    @SuppressWarnings("Duplicates")
    public ArrayList<String> getCommands() {
        ArrayList<String> commands = new ArrayList<>();
        commands.add(DEACTIONIFY(BIRD_REGEX));
        commands.add(DEACTIONIFY(BIRB_REGEX));
        return commands;
    }

    @Override
    public void prepareStatements(Connection connection) throws SQLException {
        GET_SUBREDDITS_STMT = connection.prepareStatement("" +
                "SELECT" +
                "   S.link as S_link " +
                "FROM Subreddits S " +
                "WHERE type = 'Birds'");

        GET_RESPONSE_STMT = connection.prepareStatement("" +
                "SELECT" +
                "   R.message as R_message " +
                "FROM BirdResponses R " +
                "ORDER BY RAND() " +
                "LIMIT 1");
    }
    //endregion
}