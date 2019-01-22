package bot.utils;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;

public class GithubController {
    public static void createIssue(String repoString, HashMap config, Throwable ex) {
        if (config.containsKey("access_token")) {
            String accessToken = (String) config.get("access_token");
            String issuePath = "https://api.github.com/repos/" + repoString + "/issues";

            try (CloseableHttpClient client = HttpClients.custom()
                    .setDefaultRequestConfig(RequestConfig.custom()
                            .setCookieSpec(CookieSpecs.IGNORE_COOKIES).build())
                    .build()) {
                //Make object
                JSONObject data = new JSONObject();
                StackTraceElement stackLine = ex.getStackTrace()[0];
                String newTitle = stackLine.getFileName() + " [" + stackLine.getLineNumber() + "] " + ex.getClass().getSimpleName();
                data.put("title", newTitle);
                data.put("labels", new JSONArray(Arrays.asList("bug", "auto-generated", System.getProperty("os.name"))));

                StringWriter errors = new StringWriter();
                ex.printStackTrace(new PrintWriter(errors));
                data.put("body", "```\n" + errors.toString() + "\n```\n" + CONSTANTS.DATE_TIME_FORMATTER.format(LocalDateTime.now()));
                String serialisedData = data.toString();

                //check if error has already been logged
                HttpResponse getResponse = client.execute(new HttpGet(issuePath + "?state=all"));
                JSONArray getData = new JSONArray(EntityUtils.toString(getResponse.getEntity()));
                for (Object row : getData) {
                    JSONObject issue = (JSONObject) row;
                    String title = (String) issue.get("title");
                    if (title.equals(newTitle)) {
                        System.out.println("Issue has already been made");
                        return;
                    }
                }

                HttpPost request = new HttpPost(issuePath + "?access_token=" + accessToken);
                request.setEntity(new StringEntity(serialisedData));
                HttpResponse postResponse = client.execute(request);
                JSONObject postData = new JSONObject(EntityUtils.toString(postResponse.getEntity()));

                System.out.println("Issue #" + postData.getInt("number") + " made at " + postData.getString("html_url"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No access token, issue not submitted");
        }
    }
}
