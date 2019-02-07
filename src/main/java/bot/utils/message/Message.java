package bot.utils.message;

import bot.Chatbot;
import bot.utils.Database;
import bot.utils.Human;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import javax.net.ssl.SSLHandshakeException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static bot.utils.CONSTANTS.*;
import static bot.utils.XPATHS.*;

public class Message {
	//region Constants
	private final Human sender;
	private final List<MessageComponent> messageComponents;
	private final LocalDate date;
	private final int id;
	//endregion

	//region Constructors
	public Message(Human sender, List<MessageComponent> messageComponents, LocalDate date, int id) {
		this.sender = sender;
		this.messageComponents = messageComponents;
		this.date = date;
		this.id = id;
	}

	public Message(Message message) {
		this.sender = message.getSender();
		this.messageComponents = message.getMessageComponents();
		this.date = message.getDate();
		this.id = message.getId();
	}

	public Message(Database db, ResultSet resultSet) throws SQLException {
		this.id = resultSet.getInt("M_ID");
		this.sender = new Human(resultSet);
		this.date = resultSet.getDate("M_date").toLocalDate();

		this.messageComponents = extractComponents(db, resultSet.getString("M_message"));
	}

	public static Message fromElement(Chatbot chatbot, WebElement webElement) {
		String senderName = webElement.findElement(By.xpath(MESSAGE_SENDER_REAL_NAME)).getAttribute("data-tooltip-content");

		List<WebElement> imageElements = webElement.findElements(By.xpath(MESSAGE_IMAGE));
		List<WebElement> messageBodies = webElement.findElements(By.xpath(MESSAGE_TEXT));
		List<MessageComponent> messageComponents = new ArrayList<>();

		for (WebElement imageElement : imageElements) {
			String imageUrl = imageElement.getAttribute("src");
			try {
				messageComponents.add(new Image(imageUrl));
			} catch (SSLHandshakeException e) {
				messageComponents.add(new Text(imageUrl));
			}
		}

		if (messageBodies.size() > 0) {
			WebElement messageBody = messageBodies.get(0);
			List<WebElement> components = messageBody.findElements(By.xpath(MESSAGE_TEXT_COMPONENTS));
			for (WebElement component : components) {

				if (component.getAttribute("class").equals(MESSAGE_TEXT_TAGS)) {
					//If element is a taqged element
					String url = component.getAttribute("href").replace("https://www.messenger.com/t/", "");
					messageComponents.add(chatbot.getHumanFromUrl(url));
				} else {
					//Element is a text element
					messageComponents.add(new Text(messageBody.getAttribute("aria-label")));
				}
			}
		}
		try {
			return Message.fromResultSet(chatbot.getDb(), chatbot.getDb().saveMessage(senderName, messageComponents));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Message fromResultSet(Database db, ResultSet resultSet) throws SQLException {
		if (resultSet != null && resultSet.next()) {
			return new Message(db, resultSet);
		} else {
			return null;
		}
	}
	//endregion

	//region Components
	public static List<MessageComponent> extractComponents(Database db, String combinedComponents) {
		//Quick check if message has tags
		List<MessageComponent> messageComponents = new ArrayList<>();
		for (String component : combinedComponents.split(COMPOSITE_MESSAGE_DIVIDER)) {
			Character firstChar = component.charAt(0);
			if (firstChar == TAG_SYMBOL) {
				//Component is a tag
				messageComponents.add(db.getHumanFromID(Integer.parseInt(component)));
			} else if (firstChar == IMAGE_SYMBOL) {
				//Component is an image
				try {
					//is it a number if so probably the database index
					messageComponents.add(
						db.getImageFromID(
							Integer.parseInt(component.substring(1))
						)
					);
				} catch (SSLHandshakeException e) {
					messageComponents.add(new Text(component));
				}
			} else {
				//Component is text
				messageComponents.add(new Text(component));
			}
		}
		return messageComponents;
	}

	public static String combineComponents(List<MessageComponent> messageComponents) {
		return messageComponents.stream()
			.map(MessageComponent::combine)
			.collect(Collectors.joining(COMPOSITE_MESSAGE_DIVIDER));
	}

	public String combineComponents() {
		return combineComponents(messageComponents);
	}

	//region Send message
	private static void sendMessage(WebElement inputBox, List<MessageComponent> messageComponents) {
		if (messageComponents != null) {
			for (MessageComponent messageComponent : messageComponents) {
				messageComponent.send(inputBox);
			}
		}
		inputBox.sendKeys(Keys.ENTER);
	}
	//endregion

	public void sendMessage(WebElement inputBox) {
		sendMessage(inputBox, messageComponents);
	}
	//endregion

	//region Getters

	public Human getSender() {
		return sender;
	}

	public List<MessageComponent> getMessageComponents() {
		return messageComponents;
	}

	public LocalDate getDate() {
		return date;
	}

	public int getId() {
		return id;
	}

	//endregion

	@Override
	public String toString() {
		return "Message{" +
			"sender=" + sender +
			", message='" + messageComponents + '\'' +
			", date=" + date +
			", id=" + id +
			'}';
	}
}
