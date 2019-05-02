package bot.core.utils.message;

import bot.core.Chatbot;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static bot.core.utils.CONSTANTS.CLIPBOARD;
import static bot.core.utils.CONSTANTS.PASTE;
import static bot.core.utils.XPATHS.*;
import static org.apache.commons.lang.StringEscapeUtils.unescapeHtml;

public class Text implements MessageComponent {

	private final String text;

	public Text(String text) {
		this.text = text;
	}

	public Text(WebElement webElement) {
		if (webElement.getTagName().equals("span")) {
			this.text = webElement.getAttribute("innerHTML").replaceAll("<img alt=\"(.)\" .+?>", "$1");
		} else if (webElement.getTagName().equals("div")) {
			this.text = webElement.getAttribute("aria-label");
		} else {
			this.text = webElement.getText();
		}
	}

	public static List<MessageComponent> fromElement(Chatbot chatbot, WebElement webElement) {
		List<WebElement> text = webElement.findElements(By.xpath(MESSAGE_TEXT));
		List<MessageComponent> textComponents = new ArrayList<>();
		if (text.isEmpty()) {
			return textComponents;
		} else {
			WebElement textHolder = text.get(0);
			List<WebElement> textElements = textHolder.findElements(By.xpath(MESSAGE_TEXT_COMPONENTS));
			for (WebElement textElement : textElements) {
				if (textElement.getAttribute("class").equals(MESSAGE_TEXT_TAGS)) {
					//Element is a tagged element
					Human tag = Human.fromTag(chatbot, textElement);
					if (tag != null) {
						textComponents.add(tag);
					} else {
						textComponents.add(new Text(textElement.getText() + " "));
					}
				} else {
					//Element is a text element
					textComponents.add(new Text(textElement));
				}
			}
			if (textElements.isEmpty()) {
				textComponents.add(new Text(textHolder));
			}
			return textComponents;
		}
	}

	@Override
	public boolean matches(Object obj) {
		if (obj instanceof String) {
			boolean primitiveMatch = obj.equals(text);
			if (primitiveMatch) {
				return true;
			} else {
				Matcher matcher = Pattern.compile((String) obj).matcher(text);
				return matcher.find();
			}
		} else if (obj instanceof Text) {
			return ((Text) obj).text.equals(text);
		} else {
			return false;
		}

	}

	@Override
	public void send(WebElement inputBox, WebDriverWait wait) {
		CLIPBOARD.setContents(new StringSelection(unescapeHtml(text) + " "), null);
		inputBox.sendKeys(PASTE);
	}

	@Override
	public String combine() {
		return text;
	}

	public String getText() {
		return text;
	}
}
