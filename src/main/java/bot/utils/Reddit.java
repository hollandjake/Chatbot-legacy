package bot.utils;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.List;

import static bot.utils.CONSTANTS.GET_RANDOM;

public class Reddit {
    public static String getSubredditPicture(List<String> subreddits) {
        while (subreddits != null) {
            //Pick subreddit
            String subreddit = GET_RANDOM(subreddits);

            //Get reddit path
            String redditPath = "https://www.reddit.com/r/" + subreddit + "/random.json";

            try {
                JSONArray data = (JSONArray) new JSONParser().parse(
                        Unirest.get(redditPath)
                                .header("User-agent", "Dogbot Reborn")
                                .asString()
                                .getBody()
                );
                String imageID = ((String)
                        ((JSONObject)
                                ((JSONObject)
                                        ((JSONArray)
                                                ((JSONObject)
                                                        ((JSONObject) data.get(0))
                                                                .get("data"))
                                                        .get("children"))
                                                .get(0))
                                        .get("data"))
                                .get("url"));

                return imageID;
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (UnirestException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
