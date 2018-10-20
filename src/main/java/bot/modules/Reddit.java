package bot.modules;

import bot.Chatbot;
import bot.utils.Message;
import bot.utils.Module;
import bot.utils.RedditModule;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static bot.utils.CONSTANTS.*;

public class Reddit implements Module {
    //region Constants
    private final String REDDITS_REGEX = ACTIONIFY("reddits");
    private final Chatbot chatbot;
    //endregion

    public Reddit(Chatbot chatbot) {
        this.chatbot = chatbot;
    }

    //region Overrides
    @Override
    @SuppressWarnings("Duplicates")
    public boolean process(Message message) {
        String match = getMatch(message);
        if (match.equals(REDDITS_REGEX)) {
            String text = "Reddits currently in use\n";
            HashMap<String, Module> modules = chatbot.getModules();
            for (Module module : modules.values()) {
                if (module.getClass().isInstance(RedditModule.class)) {
                    text += "\n" + module.toString();
                }
            }
            chatbot.sendMessage(text);
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

    @Override
    public String appendModulePath(String message) {
        return chatbot.appendRootPath("modules/" + getClass().getSimpleName() + "/" + message);
    }
    //endregion

    public static Image getSubredditPicture(List<String> subreddits) {
        while (subreddits != null) {
            //Pick subreddit
            String subreddit = GET_RANDOM(subreddits);

            //Get reddit path
            String redditPath = "https://www.reddit.com/r/" + subreddit + "/random.json";

            try {
                String data = GET_PAGE_SOURCE(redditPath);
                Matcher matcher = Pattern.compile("https://i\\.redd\\.it/\\S+?\\.jpg").matcher(data);
                if (matcher.find()) {
                    BufferedImage image = ImageIO.read(new URL(matcher.group()));
                    int size = image.getData().getDataBuffer().getSize();
                    if (size < 25000000) {
                        return image;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static List<String> loadSubreddits(File subredditFile) {
        try {
            if (subredditFile.exists()) {
                return new BufferedReader(new FileReader(subredditFile)).lines().collect(Collectors.toList());
            } else {
                File directory = subredditFile.getParentFile();
                if (!directory.exists()) {
                    directory.mkdirs();
                }
                subredditFile.createNewFile();
                throw new IOException();
            }
        } catch (IOException e) {
            System.out.println("No subreddits available for this session, maybe the file didn't exist");
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}