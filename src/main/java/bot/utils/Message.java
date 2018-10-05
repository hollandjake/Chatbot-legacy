package bot.utils;

import bot.Chatbot;
import org.json.simple.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.Date;

import static bot.utils.CONSTANTS.DATE_FORMATTER;
import static bot.utils.CONSTANTS.PASTE;
import static bot.utils.XPATHS.MESSAGE_TEXT;

public class Message {
    private final Human sender;
    private final String message;
    private final Date date = new Date();

    private boolean containsCommand = false;

    public Message(Human me, String message) {
        this.sender = me; //Sender is the bot
        this.message = message;
    }

    public Message(WebElement webElement, Chatbot chatbot) {
        this.sender = Human.getFromElement(webElement, chatbot.getPeople());

        WebElement messageBody = webElement.findElement(By.xpath(MESSAGE_TEXT));
        if (messageBody != null) {
            this.message = messageBody.getAttribute("body");
        } else {
            this.message = "";
        }

        this.containsCommand = chatbot.containsCommand(this);
    }

    public Human getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public boolean doesContainsCommand() {
        return containsCommand;
    }

    private void sendMessage(WebElement inputBox, String message) {
        Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(new StringSelection(message), null);

        inputBox.sendKeys(PASTE + Keys.ENTER);
    }

    public void sendMessage(WebElement inputBox) {
        sendMessage(inputBox, message);
    }

    public void sendDebugMessage(WebElement inputBox) {
        sendMessage(inputBox, toString());
    }

    public String toString() {
        return sender != null ? (sender + " : " + message) : message;
    }

    public JSONObject toJSON() {
        JSONObject me = new JSONObject();
        me.put("sender", sender.toJSON());
        me.put("message", message);
        me.put("timestamp", DATE_FORMATTER.format(date));

        return me;
    }
}
