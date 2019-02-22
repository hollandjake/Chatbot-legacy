package bot.core.utils.message;

import bot.core.Chatbot;
import bot.core.utils.Database;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static bot.core.utils.CONSTANTS.*;

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

	public Message(Chatbot chatbot, ResultSet resultSet) throws SQLException {
		this.id = resultSet.getInt("M_ID");
		this.sender = new Human(resultSet);
		this.date = resultSet.getDate("M_date").toLocalDate();

		this.messageComponents = extractComponents(chatbot, resultSet.getString("M_message"));
	}

	public static Message fromElement(Chatbot chatbot, WebElement webElement) {
		Human sender = Human.getSender(chatbot, webElement);

		List<MessageComponent> messageComponents = new ArrayList<>();
		messageComponents.addAll(Image.fromElement(chatbot, webElement));
		messageComponents.addAll(Text.fromElement(chatbot, webElement));
		return chatbot.getDb().saveMessage(sender, messageComponents);
	}
	//endregion

	//region Components
	public static List<MessageComponent> extractComponents(Chatbot chatbot, String combinedComponents) {
		//Quick check if message has tags
		List<MessageComponent> messageComponents = new ArrayList<>();
		Database db = chatbot.getDb();
		for (String component : combinedComponents.split(COMPOSITE_MESSAGE_DIVIDER)) {
			if (component.length() == 0) {
				continue;
			}
			Character firstChar = component.charAt(0);
			if (firstChar == TAG_SYMBOL) {
				//Component is a tag
				Human human = db.getHumanFromID(Integer.parseInt(component.substring(1)));
				if (!human.matches(chatbot.getMe())) {
					messageComponents.add(human);
				} else {
					messageComponents.add(new Text("@" + human.getName()));
				}
			} else if (firstChar == IMAGE_SYMBOL) {
				//Component is an image
				//is it a number if so probably the database index
				Image image = db.getImageFromID(Integer.parseInt(component.substring(1)));
				if (image == null) {
					messageComponents.add(new Text(component));
				} else {
					messageComponents.add(image);
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
	private static void sendMessage(WebElement inputBox, WebDriverWait wait, List<MessageComponent> messageComponents) {
		if (messageComponents != null) {
			for (MessageComponent messageComponent : messageComponents) {
				messageComponent.send(inputBox, wait);
			}
		}
		inputBox.sendKeys(Keys.ENTER);
	}
	//endregion

	public void sendMessage(WebElement inputBox, WebDriverWait wait) {
		sendMessage(inputBox, wait, messageComponents);
	}

	public void sendDebugMessage(WebElement inputBox, WebDriverWait wait) {
		sendMessage(inputBox, wait); //TODO: Needs implementing
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
			", message='" + combineComponents() + '\'' +
			", date=" + date +
			", id=" + id +
			'}';
	}
}
