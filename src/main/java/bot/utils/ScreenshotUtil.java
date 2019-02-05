package bot.utils;

import org.codehaus.plexus.util.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

import static bot.utils.CONSTANTS.ERROR_DATE_FORMATTER;

public interface ScreenshotUtil {
	private static void screenshot(TakesScreenshot o) {
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
		} catch (IOException a) {
			a.printStackTrace();
		}
	}

	static void capture(WebDriver webDriver) {
		screenshot((TakesScreenshot) webDriver);
	}

	static void capture(WebElement webElement) {
		screenshot(webElement);
	}
}
