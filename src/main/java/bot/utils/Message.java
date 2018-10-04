package bot.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;

import static bot.utils.CONSTANTS.PASTE;
import static bot.utils.XPATHS.MESSAGE_TEXT;

public class Message {
    private final Human sender;
    private final String message;

    public Message(Human me, String message) {
        this.sender = me; //Sender is the bot
        this.message = message;
    }

    public Message(WebElement webElement, ArrayList<Human> people) {
        this.sender = Human.getFromElement(webElement, people);

        WebElement messageBody = webElement.findElement(By.xpath(MESSAGE_TEXT));
        if (messageBody != null) {
            this.message = messageBody.getAttribute("body");
        } else {
            this.message = "";
        }
    }

    public Human getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
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
}
