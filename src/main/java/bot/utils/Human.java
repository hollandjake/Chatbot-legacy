package bot.utils;

import org.json.simple.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Objects;

import static bot.utils.XPATHS.MESSAGE_SENDER_NICKNAME;
import static bot.utils.XPATHS.MESSAGE_SENDER_REAL_NAME;

public class Human {
    //region Constants
    protected final String name;
    //endregion

    //region Variables
    protected String nickname;
    //endregion

    //region Constructors
    private Human(WebElement webElement) {
        nickname = webElement.findElement(By.xpath(MESSAGE_SENDER_NICKNAME)).getAttribute("aria-label");
        name = webElement.findElement(By.xpath(MESSAGE_SENDER_REAL_NAME)).getAttribute("data-tooltip-content");
    }

    private Human(String name) {
        this.name = name;
    }

    public static Human getFromElement(WebElement webElement, ArrayList<Human> people) {
        String name = webElement.findElement(By.xpath(MESSAGE_SENDER_REAL_NAME)).getAttribute("data-tooltip-content");
        for (Human p : people) {
            if (p.name.equals(name)) {
                return p;
            }
        }
        Human newHuman = new Human(webElement);
        people.add(newHuman);

        return newHuman;
    }

    public static Human getFromImageElement(WebElement webElement, ArrayList<Human> people) {
        return null;
    }

    public static Human createForBot(String name) {
        return new Human(name);
    }
    //endregion

    //region Getters and Setters
    public String getName() {
        return name;
    }

    public String getNickname() {
        return nickname;
    }

    //endregion

    public JSONObject toJSON() {
        JSONObject me = new JSONObject();
        me.put("name", name);
        me.put("nickname", nickname);

        return me;
    }

    public String toString() {
        return name + (nickname != null ? "(" + nickname + ")" : "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Human human = (Human) o;
        return Objects.equals(name, human.name);
    }
}
