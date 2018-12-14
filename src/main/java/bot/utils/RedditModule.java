package bot.utils;

import bot.Chatbot;
import bot.modules.Reddit;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static bot.utils.CONSTANTS.GET_RANDOM;

public abstract class RedditModule implements CommandModule, DatabaseModule {

    //region Constants
    protected final Chatbot chatbot;
    protected final Database db;
    //endregion
    //region Database statements
    protected PreparedStatement GET_SUBREDDITS_STMT;
    protected PreparedStatement GET_RESPONSE_STMT;
    //endregion

    public RedditModule(Chatbot chatbot) {
        this.chatbot = chatbot;
        this.db = chatbot.getDb();
    }

    public String getResponse() {
        try {
            ResultSet resultSet = GET_RESPONSE_STMT.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("R_message");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getImage() {
        List<String> subreddits = getSubreddits();
        while (subreddits != null && subreddits.size() > 0) {
            String image = Reddit.getSubredditPicture(GET_RANDOM(getSubreddits()));
            if (image != null) {
                return image;
            }
        }
        return null;
    }

    public List<String> getSubreddits() {
        List<String> subreddits = new ArrayList<>();
        try {
            ResultSet resultSet = GET_SUBREDDITS_STMT.executeQuery();
            while (resultSet.next()) {
                subreddits.add(resultSet.getString("S_link"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return subreddits;
    }
}