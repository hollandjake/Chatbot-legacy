package bot.utils;

import org.openqa.selenium.Keys;

import java.text.SimpleDateFormat;

public interface CONSTANTS {
    String COPY = Keys.chord(Keys.CONTROL, "c");
    String PASTE = Keys.chord(Keys.CONTROL, "v");

    SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd/MM/yy");

    static String ACTIONIFY(String arg) {
        return "(?i)^!\\s*" + arg + "$";
    }
}
