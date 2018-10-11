package bot.utils;

import org.json.simple.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static bot.utils.XPATHS.*;

public class Human {
    //region Constants
    protected final String userId;
    //endregion

    //region Variables
    protected String name;
    protected String nickname;
    //endregion

    //region Constructors
    private Human(WebElement webElement) {
        final Matcher matcher = Pattern.compile(".*?fbid:(\\d+).*")
                .matcher(webElement.findElement(By.xpath(MESSAGE_SENDER_ID)).getAttribute("participants"));

        userId = matcher.find() ? matcher.group(1) : null;
        nickname = webElement.findElement(By.xpath(MESSAGE_SENDER_NICKNAME)).getAttribute("aria-label");
        name = webElement.findElement(By.xpath(MESSAGE_SENDER_REAL_NAME)).getAttribute("data-tooltip-content");
    }

    private Human(String userId) {
        this.userId = userId;
    }

    public static Human getFromElement(WebElement webElement, ArrayList<Human> people) {
        final Matcher matcher = Pattern.compile(".*?fbid:(\\d+).*")
                .matcher(webElement.findElement(By.xpath(MESSAGE_SENDER_ID)).getAttribute("participants"));
        String userId = matcher.find() ? matcher.group(1) : null;

        for (Human p : people) {
            if (p.userId.equals(userId)) {
                return p;
            }
        }
        Human newHuman = new Human(webElement);
        people.add(newHuman);

        return newHuman;
    }

    public static Human createForBot(WebElement webElement) {
        final Matcher matcher = Pattern.compile("\"USER_ID\":\"(\\d*)\"")
                .matcher(webElement.getText());

        String userId = matcher.find() ? matcher.group(1) : null;

        return new Human(userId);
    }
    //endregion

    //region Getters and Setters
    public String getName() {
        return name;
    }

    public String getNickname() {
        return nickname;
    }

    public String getId() {
        return userId;
    }
    //endregion

    public JSONObject toJSON() {
        JSONObject me = new JSONObject();
        me.put("name", name);
        me.put("nickname", nickname);
        me.put("USER_ID", userId);

        return me;
    }

    public String toString() {
        return "[" + userId + "] " + name + (nickname != null ? "(" + nickname + ")" : "");
    }
}
