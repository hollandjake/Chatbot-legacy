package bot.utils;

import org.openqa.selenium.Keys;

import java.text.SimpleDateFormat;

public interface CONSTANTS {
    String COPY = Keys.chord(Keys.CONTROL, "c");
    String PASTE = Keys.chord(Keys.CONTROL, "v");

    SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy, dd MMM, HH:mm:ss");
}
