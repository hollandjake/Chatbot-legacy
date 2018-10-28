package bot.utils;

import bot.Chatbot;
import org.json.simple.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static bot.utils.CONSTANTS.*;
import static bot.utils.XPATHS.MESSAGE_IMAGE;
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
        List<WebElement> imageElements = webElement.findElements(By.xpath(MESSAGE_IMAGE));
        if (imageElements.size() > 0) {
            String href = imageElements.get(0).getAttribute("src");
            this.image = imageFromUrl(href);
            this.sender = null;
            this.message = "";
        } else {
            this.image = null;
            this.sender = Human.getFromElement(webElement, chatbot.getPeople());

            List<WebElement> messageBodies = webElement.findElements(By.xpath(MESSAGE_TEXT));
            if (messageBodies.size() > 0) {
                this.message = messageBodies.get(0).getAttribute("aria-label");
            } else {
                this.message = "";
            }

        }
        this.containsCommand = chatbot.containsCommand(this);
    }

    public static Message withImageFromURL(Human me, String message, String url) {
        Image image = imageFromUrl(url);
        if (image != null) {
            return new Message(me, message, image);
        } else {
            return null;
        }
    }
    //endregion

    private static Image imageFromUrl(String url) {
        try {
            URL U = new URL(url);
//            BufferedImage image = ImageIO.read(U);

            URLConnection urlConnection = U.openConnection();
            urlConnection.connect();
            ImageInputStream imageInputStream = ImageIO.createImageInputStream(urlConnection.getInputStream());
            BufferedImage image = ImageIO.read(imageInputStream);
            return image;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

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

    public Image getImage() {
        return image;
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
        return (sender != null ? sender + " : " : "") +
                (message != null ? message + " : " : "") +
                (image != null ? image : "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message otherMessage = (Message) o;
        return Objects.equals(sender, otherMessage.sender) &&
                Objects.equals(message, otherMessage.message) &&
                Objects.equals(image, otherMessage.image) &&
                Objects.equals(date, otherMessage.date);
    }
}
