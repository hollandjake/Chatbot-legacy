package bot.utils;

import bot.Chatbot;
import org.openqa.selenium.*;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.net.ssl.SSLHandshakeException;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static bot.utils.CONSTANTS.*;
import static bot.utils.XPATHS.*;
import static org.apache.commons.lang.StringEscapeUtils.unescapeHtml;

public class Message {
	//region Constants
	private final Human sender;
	private final String message;
	private final String imageUrl;
	private final LocalDate date;
	private final int id;
	//endregion

	public Message(Human sender, String message, String imageUrl, LocalDate date, int id) {
		this.sender = sender;
		this.message = message;
		this.imageUrl = imageUrl;
		this.date = date;
		this.id = id;
	}

	public Message(Message message) {
		this.sender = message.getSender();
		this.message = message.getMessage();
		this.imageUrl = message.getImageUrl();
		this.date = message.getDate();
		this.id = message.getId();
	}

	public Message(ResultSet resultSet) throws SQLException {
		this.id = resultSet.getInt("M_ID");
		this.sender = new Human(resultSet);
		this.message = resultSet.getString("M_message");
		this.imageUrl = resultSet.getString("I_url");
		this.date = resultSet.getDate("M_date").toLocalDate();
	}

	public static BufferedImage imageFromUrl(String url) throws SSLHandshakeException {
		ImageInputStream imageInputStream = null;

		try {
			URL U = new URL(url);
			URLConnection urlConnection = U.openConnection();
			urlConnection.connect();

			imageInputStream = ImageIO.createImageInputStream(urlConnection.getInputStream());
			BufferedImage image = ImageIO.read(imageInputStream);

			if (image == null) {
				return null;
			}

			double size = urlConnection.getContentLength();

			//Scale image to fit in size
			double scaleFactor = Math.min(1, MAX_IMAGE_SIZE / size);
			int scaledWidth = (int) (image.getWidth() * scaleFactor);
			int scaledHeight = (int) (image.getHeight() * scaleFactor);

			if (scaledWidth <= 0 || scaledHeight <= 0) {
				return null;
			}
			Image scaledImage = image.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);

			BufferedImage bufferedImage = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = bufferedImage.createGraphics();
			g.drawImage(scaledImage, 0, 0, null);
			g.dispose();
			return bufferedImage;
		} catch (IOException e) {
			e.printStackTrace();
			if (e instanceof SSLHandshakeException) {
				throw (SSLHandshakeException) e;
			}
			return null;
		} finally {
			try {
				if (imageInputStream != null) {
					imageInputStream.close();
				}
			} catch (IOException ignore) {
			}
		}
	}

	//region Send message
	private void sendMessage(WebElement inputBox, String message) {
		CLIPBOARD.setContents(new StringSelection(unescapeHtml(message)), null);
		inputBox.sendKeys(PASTE);
		inputBox.sendKeys(Keys.ENTER);
	}

	private void sendMessageWithImage(WebElement inputBox, String message, Image image) {
		if (image != null) {
			CLIPBOARD.setContents(new ImageTransferable(image), null);
			inputBox.sendKeys(PASTE);
		}
		if (!message.isEmpty()) {
			CLIPBOARD.setContents(new StringSelection(unescapeHtml(message)), null);
			inputBox.sendKeys(PASTE);
		}
		inputBox.sendKeys(Keys.ENTER);
	}

	public void sendMessage(WebElement inputBox) {
		if (imageUrl != null) {
			try {
				sendMessageWithImage(inputBox, message, imageFromUrl(imageUrl));
			} catch (SSLHandshakeException e) {
				sendMessage(inputBox, message + "\n\n" + e.getMessage() + "\uD83D\uDE2D");
			}
		} else {
			sendMessage(inputBox, message);
		}
	}

	public void sendDebugMessage(WebElement inputBox) {
		sendMessage(inputBox, toString());
	}
	//endregion

	public static Message fromElement(Chatbot chatbot, WebElement webElement) {
		String senderName = webElement.findElement(By.xpath(MESSAGE_SENDER_REAL_NAME)).getAttribute("data-tooltip-content");

		String imageUrl = "";
		String message = "";

		List<WebElement> imageElements = webElement.findElements(By.xpath(MESSAGE_IMAGE));
		List<WebElement> messageBodies = webElement.findElements(By.xpath(MESSAGE_TEXT));

		if (imageElements.size() > 0) {
			imageUrl = imageElements.get(0).getAttribute("src");
		} else if (messageBodies.size() > 0) {
			message = messageBodies.get(0).getAttribute("aria-label");
		}
		try {
			return Message.fromResultSet(chatbot.getDb().saveMessage(senderName, chatbot.getThreadId(), message, imageUrl));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Message fromResultSet(ResultSet resultSet) throws SQLException {
		if (resultSet != null && resultSet.next()) {
			return new Message(resultSet);
		} else {
			return null;
		}
	}

	//region getters

	public Human getSender() {
		return sender;
	}

	public String getMessage() {
		return message;
	}

	public String getImageUrl() {
		return imageUrl;
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
			", message='" + message + '\'' +
			", imageUrl='" + imageUrl + '\'' +
			", date=" + date +
			", id=" + id +
			'}';
	}
}
