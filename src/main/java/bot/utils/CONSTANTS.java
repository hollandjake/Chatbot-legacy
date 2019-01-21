package bot.utils;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.openqa.selenium.Keys;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

public interface CONSTANTS {
    //region Strings
    String REPOSITORY = "hollandjake/Chatbot";
    //endregion

    //region Keyboard operations
    Clipboard CLIPBOARD = Toolkit.getDefaultToolkit().getSystemClipboard();
    String COPY = Keys.chord(Keys.CONTROL, "c");
    String PASTE = Keys.chord(Keys.CONTROL, "v");
    //endregion

    //region Date formats
    DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yy");
    DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yy/MM/dd HH:mm:ss");
    DateTimeFormatter ERROR_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");
    //endregion

    //region Image
    int MAX_IMAGE_SIZE = 200000; //In Bytes
    //endregion

    Random RANDOM = new Random();

    static String ACTIONIFY(String arg) {
        return "(?i)^!\\s*" + arg + "$";
    }

    static String ACTIONIFY_CASE(String arg) {
        return "^!\\s*" + arg + "$";
    }

    static String DEACTIONIFY(String regex) {
        return regex.replaceAll("\\(\\?i\\)\\^!\\\\\\\\s\\*(\\S+?)\\$", "$1");
    }

    static String DEACTIONIFY_CASE(String regex) {
        return regex.replaceAll("\\^!\\\\\\\\s\\*(\\S+?)\\$", "$1");
    }

    static <T> T GET_RANDOM(List<T> list) {
        return list.get(RANDOM.nextInt(list.size()));
    }

    static String GET_PAGE_SOURCE(String url) {
        try {
            return Unirest.get(url).header("User-agent", "Dogbot Reborn").asString().getBody();
        } catch (UnirestException e) {
            System.out.println("Page doesn't exist");
            e.printStackTrace();
            return "";
        }
    }
}
