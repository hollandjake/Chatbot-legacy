package bot.utils;

import org.openqa.selenium.Keys;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;

public interface CONSTANTS {
    //region Keyboard operations
    Clipboard CLIPBOARD = Toolkit.getDefaultToolkit().getSystemClipboard();
    String COPY = Keys.chord(Keys.CONTROL, "c");
    String PASTE = Keys.chord(Keys.CONTROL, "v");
    //endregion

    //region Date formats
    SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd/MM/yy");
    DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yy/MM/dd HH:mm:ss");
    //endregion

    static String ACTIONIFY(String arg) {
        return "(?i)^!\\s*" + arg + "$";
    }
}
