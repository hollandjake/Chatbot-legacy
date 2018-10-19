package bot.modules;

import bot.Chatbot;
import bot.utils.Message;
import bot.utils.Module;
import bot.utils.Reddit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static bot.utils.CONSTANTS.ACTIONIFY;
import static bot.utils.CONSTANTS.GET_RANDOM;

public class Dogs implements Module {
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
        loadSubreddits(new File(appendModulePath("subreddits.txt")));
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
            String imageURL = Reddit.getSubredditPicture(subreddits);
            String quote = GET_RANDOM(responses);
            chatbot.sendImageFromURLWithMessage(imageURL, quote);
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
    public String appendModulePath(String message) {
        return chatbot.appendRootPath("modules/" + getClass().getSimpleName() + "/" + message);
    }
    //endregion

    private void loadSubreddits(File subredditFile) {
        try {
            if (subredditFile.exists()) {
                subreddits = new BufferedReader(new FileReader(subredditFile)).lines().collect(Collectors.toList());
            } else {
                File directory = subredditFile.getParentFile();
                if (!directory.exists()) {
                    directory.mkdirs();
                }
                subredditFile.createNewFile();
                throw new IOException();
            }
        } catch (IOException e) {
            System.out.println("No dog subreddits available for this session, maybe the file didn't exist");
            e.printStackTrace();
        }
    }
}