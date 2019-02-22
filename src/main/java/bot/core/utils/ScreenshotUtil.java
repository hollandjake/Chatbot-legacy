package bot.core.utils;

import org.codehaus.plexus.util.FileUtils;
import org.openqa.selenium.*;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

import static bot.core.utils.CONSTANTS.ERROR_DATE_FORMATTER;

public interface ScreenshotUtil {
	private static File screenshot(TakesScreenshot o) {
		try {
			int i = 0;
			File file = new File("src/errors/" + ERROR_DATE_FORMATTER.format(LocalDateTime.now()) + ".jpg");
			while (!file.createNewFile()) {
				i++;
				file = new File("src/errors/" + ERROR_DATE_FORMATTER.format(LocalDateTime.now()) + "-" + i + ".jpg");
			}
			FileUtils.copyFile(
				o.getScreenshotAs(OutputType.FILE),
				file);
			System.out.println("Screenshot taken!");
			return file;
		} catch (IOException a) {
			a.printStackTrace();
		}
		return null;
	}

	static File capture(WebDriver webDriver) {
		return screenshot((TakesScreenshot) webDriver);
	}

	static void capture(WebElement webElement) {
		screenshot(webElement);
	}
}
