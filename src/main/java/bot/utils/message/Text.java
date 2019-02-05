package bot.utils.message;

import org.openqa.selenium.WebElement;

import java.awt.datatransfer.StringSelection;

import static bot.utils.CONSTANTS.CLIPBOARD;
import static bot.utils.CONSTANTS.PASTE;
import static org.apache.commons.lang.StringEscapeUtils.unescapeHtml;

public class Text implements MessageComponent {

	private final String text;

	public Text(String text) {
		this.text = text;
	}

	@Override
	public boolean matches(Object obj) {
		if (obj instanceof String) {
			return ((String) obj).matches(text);
		} else if (obj instanceof Text) {
			return ((Text) obj).text.equals(text);
		} else {
			return false;
		}
	}

	@Override
	public void send(WebElement inputBox) {
		CLIPBOARD.setContents(new StringSelection(unescapeHtml(text) + " "), null);
		inputBox.sendKeys(PASTE);
	}

	@Override
	public String combine() {
		return text;
	}


}
