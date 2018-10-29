package bot.modules;

import bot.Chatbot;
import bot.utils.Message;
import bot.utils.RedditModule;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static bot.modules.Reddit.loadSubreddits;
import static bot.utils.CONSTANTS.*;

public class Cats implements RedditModule {
    //region Constants
    private final String CAT_REGEX = ACTIONIFY("cat");
    private final Chatbot chatbot;
    //endregion

    //region Variables
    private List<String> subreddits;
    private List<String> responses;
    private Image preloadedImage;
    //endregion

    public Cats(Chatbot chatbot) {
        this.chatbot = chatbot;
        subreddits = loadSubreddits(new File(appendModulePath("subreddits.txt")));
        try {
            responses = Files.readAllLines(Paths.get(appendModulePath("responses.txt")));
        } catch (IOException e) {
            System.out.println("Cat quotes are not available this session");
        }
        preloadedImage = Reddit.getSubredditPicture(subreddits);
    }

    //region Overrides
    @Override
    @SuppressWarnings("Duplicates")
    public boolean process(Message message) {
        String match = getMatch(message);
        if (match.equals(CAT_REGEX)) {
            String quote = GET_RANDOM(responses);
            chatbot.sendImageWithMessage(preloadedImage, quote);
            preloadedImage = Reddit.getSubredditPicture(subreddits);
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
    public String appendModulePath(String message) {
        return chatbot.appendRootPath("modules/" + getClass().getSimpleName() + "/" + message);
    }

    @Override
    public String toString() {
        String message = getClass().getSimpleName() + ": \n";
        for (String subreddit : subreddits) {
            message += "\t" + subreddit + "\n";
        }
        return message;
    }
    //endregion
}