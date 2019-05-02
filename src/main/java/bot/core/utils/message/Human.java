package bot.core.utils.message;

import bot.core.Chatbot;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.awt.datatransfer.StringSelection;
import java.sql.ResultSet;
import java.sql.SQLException;

import static bot.core.utils.CONSTANTS.*;
import static bot.core.utils.XPATHS.MESSAGE_SENDER_NAME;
import static bot.core.utils.XPATHS.MESSAGE_SENDER_URL;

public class Human implements MessageComponent {
	//region Constants
	private final String name;
	private final String url;
	private final int ID;
	//endregion

	//region Constructors
	public Human(String name, String url, int id) {
		this.name = name;
		this.url = url.replace("https://www.messenger.com/t/", "");
		this.ID = id;
	}

	public Human(String name, String url) {
		this.name = name;
		this.url = url.replace("https://www.messenger.com/t/", "");
		this.ID = 0;
	}

	public Human(ResultSet resultSet) throws SQLException {
		this.ID = resultSet.getInt("H_ID");
		this.url = resultSet.getString("H_url");
		this.name = resultSet.getString("H_name");
	}

	public static Human getSender(Chatbot chatbot, WebElement webElement) {
		try {
			String senderURL = webElement.findElement(By.xpath(MESSAGE_SENDER_URL)).getAttribute("href").replace("https://www.messenger.com/t/", "");
			Human sender = chatbot.getHumanFromUrl(senderURL);
			if (sender == null) {
				String senderName = webElement.findElement(By.xpath(MESSAGE_SENDER_NAME)).getAttribute("data-tooltip-content");
				sender = chatbot.saveHuman(new Human(senderName, senderURL));
			}
			return sender;
		} catch (NoSuchElementException e) {
			return null;
		}
	}

	public static Human fromTag(Chatbot chatbot, WebElement webElement) {
		String url = webElement.getAttribute("href").replace("https://www.messenger.com/t/", "");
		return chatbot.getHumanFromUrl(url);
	}
	//endregion

	//region Getters
	public String getName() {
		return name;
	}

	public String getUrl() {
		return url;
	}

	public int getID() {
		return ID;
	}
	//endregion

	public String toString() {
		return name;
	}

	@Override
	public boolean matches(Object obj) {
		if (obj instanceof String) {
			return obj.equals(name) || obj.equals(ID);
		} else if (obj instanceof Human) {
			return ((Human) obj).name.equals(name);
		} else {
			return false;
		}
	}

	@Override
	public void send(WebElement inputBox, WebDriverWait wait) {
		inputBox.sendKeys(Keys.BACK_SPACE);
		CLIPBOARD.setContents(new StringSelection("@" + name), null);
		inputBox.sendKeys(PASTE);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[@data-testid='mentions-contextual-layer']")));
		inputBox.sendKeys(Keys.ENTER);
	}

	@Override
	public String combine() {
		return TAG_SYMBOL + String.valueOf(ID);
	}
}
