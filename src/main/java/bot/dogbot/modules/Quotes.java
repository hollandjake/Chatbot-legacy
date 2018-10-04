package bot.dogbot.modules;

import bot.utils.Message;
import bot.utils.Module;
import bot.utils.WebController;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Quotes implements Module {
    private JSONArray quotesList;
    private final File file;
    private final String REGEX = "^!quote$";

    public Quotes() {
        JSONParser jsonParser = new JSONParser();
        file = new File("src/main/resources/modules/quotes.json");

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

    @Override
    public boolean process(WebController webController, Message message) {
        if (message.getMessage().matches(REGEX)) {
            JSONObject quote = (JSONObject) quotesList.get((int) (Math.random() * quotesList.size()));
            JSONObject sender = (JSONObject) quote.get("sender");
            webController.sendMessage("\"" + quote.get("message") + "\" (" + sender.get("name") + ", " + quote.get("timestamp") + ")");

            return true;
        }
        return false;
    }
}