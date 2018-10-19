package bot.utils;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static bot.utils.CONSTANTS.GET_RANDOM;

public class Reddit {
    public static Image getSubredditPicture(List<String> subreddits) {
        while (subreddits != null) {
            //Pick subreddit
            String subreddit = GET_RANDOM(subreddits);

            //Get reddit path
            String redditPath = "https://www.reddit.com/r/" + subreddit + "/random.json";

            try {
                String data = Unirest.get(redditPath)
                                .header("User-agent", "Dogbot Reborn")
                                .asString()
                        .getBody();
                Matcher matcher = Pattern.compile("https://i\\.redd\\.it/\\S+?\\.jpg").matcher(data);
                if (matcher.find()) {
                    BufferedImage image = ImageIO.read(new URL(matcher.group()));
                    int size = image.getData().getDataBuffer().getSize();
                    if (size < 25000000) {
                        return image;
                    }
                }
            } catch (UnirestException e) {
                e.printStackTrace();
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
