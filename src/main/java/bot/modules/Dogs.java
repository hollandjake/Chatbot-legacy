package bot.modules;

import bot.Chatbot;
import bot.utils.Message;
import bot.utils.RedditModule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import static bot.utils.CONSTANTS.ACTIONIFY;
import static bot.utils.CONSTANTS.DEACTIONIFY;

public class Dogs extends RedditModule {
    //region Constants
    private final String DOG_REGEX = ACTIONIFY("dog");
    private final String DOGGO_REGEX = ACTIONIFY("doggo");
    private final String EXTRA_GOOD_DOG_REGEX = ACTIONIFY("extragooddog");
    //endregion

    //region Database statements
    private PreparedStatement GET_RANDOM_EXTRA_GOOD_IMAGE_STMT;
    //endregion

    public Dogs(Chatbot chatbot) {
        super(chatbot);
    }

    private String getExtraGoodImage() {
        try {
            db.checkConnection();
            ResultSet resultSet = GET_RANDOM_EXTRA_GOOD_IMAGE_STMT.executeQuery();
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
    @SuppressWarnings("Duplicates")
    public boolean process(Message message) {
        String match = getMatch(message);
        if (match.equals(DOG_REGEX) || match.equals(DOGGO_REGEX)) {
            String response = getResponse();
            String image = getImage();
            chatbot.sendImageWithMessage(image, response);
            return true;
        } else if (match.equals(EXTRA_GOOD_DOG_REGEX)) {
            String image = getExtraGoodImage();
            chatbot.sendImageWithMessage(image, "Extra good woof!");
            return true;
        } else {
            return false;
        }
    }

    @Override
    @SuppressWarnings("Duplicates")
    public String getMatch(Message message) {
        String messageBody = message.getMessage();
        if (messageBody.matches(DOG_REGEX)) {
            return DOG_REGEX;
        } else if (messageBody.matches(DOGGO_REGEX)) {
            return DOGGO_REGEX;
        } else if (messageBody.matches(EXTRA_GOOD_DOG_REGEX)) {
            return EXTRA_GOOD_DOG_REGEX;
        } else {
            return "";
        }
    }

    @Override
    @SuppressWarnings("Duplicates")
    public ArrayList<String> getCommands() {
        ArrayList<String> commands = new ArrayList<>();
        commands.add(DEACTIONIFY(DOG_REGEX));
        commands.add(DEACTIONIFY(DOGGO_REGEX));
        commands.add(DEACTIONIFY(EXTRA_GOOD_DOG_REGEX));
        return commands;
    }

    @Override
    public void prepareStatements(Connection connection) throws SQLException {
        GET_SUBREDDITS_STMT = connection.prepareStatement("" +
                "SELECT" +
                "   S.link as S_link " +
                "FROM Subreddits S " +
                "WHERE type = 'Dogs'");

        GET_RESPONSE_STMT = connection.prepareStatement("" +
                "SELECT" +
                "   R.message as R_message " +
                "FROM DogResponses R " +
                "ORDER BY RAND() " +
                "LIMIT 1");

        GET_RANDOM_EXTRA_GOOD_IMAGE_STMT = connection.prepareStatement("" +
                "SELECT" +
                "   I.url as I_url " +
                "FROM ExtraGoodDogs E " +
                "JOIN Images I on E.image_id = I.ID " +
                "ORDER BY RAND() " +
                "LIMIT 1");
    }
    //endregion
}