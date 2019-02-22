package bot.core.utils.message;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;

public interface MessageComponent {
	static List<MessageComponent> union(final List<MessageComponent> list1, final List<MessageComponent> list2) {
		final List<MessageComponent> result = new ArrayList<>(list1);
		result.addAll(list2);
		return result;
	}

	static List<MessageComponent> append(final List<MessageComponent> list1, final MessageComponent obj) {
		return union(list1, List.of(obj));
	}

	boolean matches(Object obj);

	void send(WebElement inputBox, WebDriverWait wait);

	String combine();
}
