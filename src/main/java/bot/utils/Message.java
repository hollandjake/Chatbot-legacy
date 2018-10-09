package bot.utils;

import bot.Chatbot;
import org.json.simple.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.Date;

import static bot.utils.CONSTANTS.*;
import static bot.utils.XPATHS.MESSAGE_TEXT;

public class Message {
    private final Human sender;
    private final String message;
    private final Image image;
    private final Date date = new Date();

    private boolean containsCommand = false;

    public Message(Human me, String message) {
        this.sender = me; //Sender is the bot
        this.message = message;
        this.image = null;
    }

    public Message(WebElement webElement, Chatbot chatbot) {
        this.sender = Human.getFromElement(webElement, chatbot.getPeople());
        this.image = null;

        WebElement messageBody = webElement.findElement(By.xpath(MESSAGE_TEXT));
        if (messageBody != null) {
            this.message = messageBody.getAttribute("body");
        } else {
            this.message = "";
        }

        this.containsCommand = chatbot.containsCommand(this);
    }

    public Message(Human me, String message, String image) {
        this.sender = me; //Sender is the bot
        this.message = message;
        this.image = new ImageIcon(image).getImage();
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

    private static void sendMessage(WebElement inputBox, String message) {
        CLIPBOARD.setContents(new StringSelection(message), null);
        inputBox.sendKeys(PASTE + Keys.ENTER);
    }

    private static void sendMessageWithImage(WebElement inputBox, String message, Image image) {
        CLIPBOARD.setContents(new ImageTransferable(image), null);
        inputBox.sendKeys(PASTE);

        if (!message.isEmpty()) {
            CLIPBOARD.setContents(new StringSelection(message), null);
            inputBox.sendKeys(PASTE);
        }

        inputBox.sendKeys(Keys.ENTER);
    }

    public void sendMessage(WebElement inputBox) {
        if (image != null) {
            sendMessageWithImage(inputBox, message, image);
        } else {
            sendMessage(inputBox, message);
        }
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
