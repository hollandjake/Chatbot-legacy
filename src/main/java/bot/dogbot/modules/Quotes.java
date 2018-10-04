package bot.dogbot.modules;

import bot.utils.Message;
import bot.utils.Module;
import bot.utils.WebController;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Quotes implements Module {
    private JSONArray quotesList;
    private final File file;

    public Quotes() {

        JSONParser jsonParser = new JSONParser();
        file = new File("resources/modules/quotes.json");

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
    public void process(WebController webController, Message message) {
        webController.sendMessage(new Message(message.getSender(), "Quote Module: " + message.getMessage()));
    }
}