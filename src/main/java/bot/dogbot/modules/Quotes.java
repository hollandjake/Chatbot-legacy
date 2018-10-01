package bot.dogbot.modules;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Quotes {
    private JSONArray quotesList;
    private File file;

    public Quotes() {

        JSONParser jsonParser = new JSONParser();
        file = new File("resources/quotes.json");

        try {
            quotesList = (JSONArray) jsonParser.parse(new FileReader(file));
        } catch (IOException e) {
            System.out.println("Quotes are unavailable for this session due to an error reading the file");
            e.printStackTrace();
        } catch (ParseException e) {
            System.out.println("Quotes are unavailable for this session due to the error");
            e.printStackTrace();
        }
    }
}