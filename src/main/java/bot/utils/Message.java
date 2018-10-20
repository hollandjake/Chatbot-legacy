package bot.utils;

import bot.Chatbot;
import org.json.simple.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.net.URL;
import java.util.Date;

import static bot.utils.CONSTANTS.*;
import static bot.utils.XPATHS.MESSAGE_TEXT;
import static org.apache.commons.lang.StringEscapeUtils.unescapeHtml;

public class Message {
    //region Constants
    private final Human sender;
    private final String message;
    private final Image image;
    private final Date date = new Date();
    //endregion

    //region Variables
    private boolean containsCommand = false;
    //endregion

    //region Constructors
    public Message(Human me, String message) {
        this.sender = me; //Sender is the bot
        this.message = unescapeHtml(message);
        this.image = null;
    }

    public Message(Human me, String message, String image) {
        this.sender = me; //Sender is the bot
        this.message = unescapeHtml(message);
        this.image = new ImageIcon(image).getImage();
    }

    public Message(Human me, String message, Image image) {
        this.sender = me; //Sender is the bot
        this.message = unescapeHtml(message);
        this.image = image;
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

    public static Message withImageFromURL(Human me, String message, String url) {
        try {
            Image image = ImageIO.read(new URL(url));
            return new Message(me, message, image);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    //endregion

    //region Send message
    private void sendMessage(WebElement inputBox, String message) {
        CLIPBOARD.setContents(new StringSelection(unescapeHtml(message)), null);
        inputBox.sendKeys(PASTE + Keys.ENTER);
    }

    private void sendMessageWithImage(WebElement inputBox, String message, Image image) {
        CLIPBOARD.setContents(new ImageTransferable(image), null);
        inputBox.sendKeys(PASTE);

        if (!message.isEmpty()) {
            CLIPBOARD.setContents(new StringSelection(unescapeHtml(message)), null);
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
    //endregion

    public boolean doesContainsCommand() {
        return containsCommand;
    }

    //region Getters and Setters
    public Human getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }
    //endregion

    public JSONObject toJSON() {
        JSONObject me = new JSONObject();
        me.put("sender", sender.toJSON());
        me.put("message", message);
        me.put("timestamp", DATE_FORMATTER.format(date));
        if (image != null) {
            me.put("image", image);
        }

        return me;
    }

    public String toString() {
        return (sender != null ? sender + " : " : "") + message;
    }
}
