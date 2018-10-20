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

public class Dogs implements RedditModule {
    //region Constants
    private final String DOG_REGEX = ACTIONIFY("dog");
    private final String DOGGO_REGEX = ACTIONIFY("doggo");
    private final String EXTRA_GOOD_DOG_REGEX = ACTIONIFY("extragooddog");
    private final Chatbot chatbot;
    //endregion

    //region Variables
    private List<String> subreddits;
    private List<String> responses;

    private List<String> extraGoodDogImages;
    //endregion

    public Dogs(Chatbot chatbot) {
        this.chatbot = chatbot;
        subreddits = loadSubreddits(new File(appendModulePath("subreddits.txt")));
        try {
            responses = Files.readAllLines(Paths.get(appendModulePath("responses.txt")));
            extraGoodDogImages = Files.readAllLines(Paths.get(appendModulePath("extraGoodDogs.txt")));
        } catch (IOException e) {
            System.out.println("Dog quotes/images are not available this session");
        }
    }

    //region Overrides
    @Override
    @SuppressWarnings("Duplicates")
    public boolean process(Message message) {
        String match = getMatch(message);
        if (match.equals(DOG_REGEX) || match.equals(DOGGO_REGEX)) {
            Image image = Reddit.getSubredditPicture(subreddits);
            String quote = GET_RANDOM(responses);
            chatbot.sendImageWithMessage(image, quote);
            return true;
        } else if (match.equals(EXTRA_GOOD_DOG_REGEX)) {
            String imageURL = GET_RANDOM(extraGoodDogImages);
            chatbot.sendImageFromURLWithMessage(imageURL, "Woof!");
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