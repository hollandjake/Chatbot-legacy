package bot.utils;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.openqa.selenium.Keys;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;

public interface CONSTANTS {
    Clipboard CLIPBOARD = Toolkit.getDefaultToolkit().getSystemClipboard();
    String COPY = Keys.chord(Keys.CONTROL, "c");
    String PASTE = Keys.chord(Keys.CONTROL, "v");

    SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd/MM/yy");

    static String ACTIONIFY(String arg) {
        return "(?i)^!\\s*" + arg + "$";
    }

    static String getVersion() {
        try {
            MavenXpp3Reader reader = new MavenXpp3Reader();
            Model model = reader.read(new FileReader("pom.xml"));
            return "V" + model.getVersion();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        return "VERSION-NOT-FOUND";
    }
}
