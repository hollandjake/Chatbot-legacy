package bot.dogbot;

import bot.Chatbot;
import bot.modules.*;
import bot.utils.GithubController;
import bot.utils.exceptions.MissingConfigurationsException;

import java.util.HashMap;
import java.util.List;

import static bot.utils.CONSTANTS.REPOSITORY;

public class Dogbot extends Chatbot {
    private final String VERSION = "V4.1.0";

    //region Overrides
    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    protected void initMessage() {
        Stats stats = (Stats) modules.get("Stats");
        sendImageWithMessage(boot.getRandomBootImage(),
                "Dogbot is online\n" +
                        boot.getRandomBootMessage() + "\n\n" + stats.getMinifiedStats()
        );
    }

    @Override
    protected void loadModules() {
        super.loadModules();
        //Overrides
        modules.put("Commands", new OneLinkCommand(this,
                List.of("commands", "help"),
                "https://github.com/hollandjake/Chatbot/blob/master/src/main/java/bot/dogbot/README.md",
                "A list of commands can be found at"));

        //Image responses
        modules.put("Birds", new Birds(this));
        modules.put("Cats", new Cats(this));
        modules.put("Dogs", new Dogs(this));
        modules.put("Inspire", new Inspire(this));
        modules.put("Reacts", new Reacts(this));
        modules.put("Tab", new Tab(this));
        modules.put("XKCD", new XKCD(this));

        //Message responses
        modules.put("8Ball", new EightBall(this));
        modules.put("Feedback", new OneLinkCommand(this,
                List.of("feedback"),
                "https://docs.google.com/document/d/19Vquu0fh8LCqUXH0wwpm9H9MSq1LrEx1Z2Xg9NknKmg/edit?usp=sharing",
                "Feedback form"));
        modules.put("Quotes", new Quotes(this));
        modules.put("Reddit", new Reddit(this));
        modules.put("Roll", new Roll(this));
        modules.put("Think", new Think(this));
        modules.put("Trello", new OneLinkCommand(this,
                List.of("trello"),
                "https://trello.com/b/9f49WSW0/second-year-compsci",
                "Trello"));
    }
    //endregion

    //region Constructors
    public Dogbot(HashMap<String, String> config) throws MissingConfigurationsException {
        super(config);
    }
    //endregion

    public static void main(String[] args) {
        Chatbot bot;
        HashMap<String, String> config = Chatbot.createConfig(args);

        //Create bot
        try {
            throw new MissingConfigurationsException("Test");
//            bot = new Dogbot(config);
        } catch (MissingConfigurationsException e) {
            e.printStackTrace();
            GithubController.createIssue(REPOSITORY, config, e);
            System.exit(1);
        }
    }
}
