package bot.modules;

import bot.Chatbot;
import bot.utils.CommandModule;
import bot.utils.Message;
import bot.utils.RedditModule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static bot.utils.CONSTANTS.*;

public class Reddit implements CommandModule {
    //region Constants
    private final String REDDITS_REGEX = ACTIONIFY("reddits");
    private final Chatbot chatbot;
    //endregion

    public Reddit(Chatbot chatbot) {
        this.chatbot = chatbot;
    }

    public static String getSubredditPicture(String subreddit) {
        //Get reddit path
        String redditPath = "https://www.reddit.com/r/" + subreddit + "/random.json";

        String data = GET_PAGE_SOURCE(redditPath);
        Matcher matcher = Pattern.compile("https://i\\.redd\\.it/\\S+?\\.jpg").matcher(data);
        if (matcher.find()) {
            return matcher.group();
        } else {
            return null;
        }
    }

    //region Overrides
    @Override
    @SuppressWarnings("Duplicates")
    public boolean process(Message message) {
        String match = getMatch(message);
        if (match.equals(REDDITS_REGEX)) {
            StringBuilder text = new StringBuilder("Reddits currently in use\n");
            HashMap<String, CommandModule> modules = chatbot.getModules();
            for (CommandModule module : modules.values()) {
                if (module instanceof RedditModule) {
                    text.append("\n");
                    for (String subreddit : ((RedditModule) module).getSubreddits()) {
                        text.append("\thttps://www.reddit.com/r/" + subreddit + "\n");
                    }
                }
            }
            chatbot.sendMessage(text.toString());
            return true;
        } else {
            return false;
        }
    }

    @Override
    @SuppressWarnings("Duplicates")
    public String getMatch(Message message) {
        String messageBody = message.getMessage();
        if (messageBody.matches(REDDITS_REGEX)) {
            return REDDITS_REGEX;
        } else {
            return "";
        }
    }

    @Override
    @SuppressWarnings("Duplicates")
    public ArrayList<String> getCommands() {
        ArrayList<String> commands = new ArrayList<>();
        commands.add(DEACTIONIFY(REDDITS_REGEX));
        return commands;
    }
    //endregion
}