package bot.dogbot;

import bot.Chatbot;
import bot.modules.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Dogbot extends Chatbot {
    private final String VERSION = "V3.30.0";

    //region Overrides
    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public String appendRootPath(String path) {
        return "src/main/resources/versions/dogbot/" + path;
    }

    @Override
    protected void initMessage() {
        List<String> bootMessages = new ArrayList<>();
        List<File> bootImages = new ArrayList<>();
        try {
            bootMessages = Files.readAllLines(Paths.get(appendRootPath("modules/Boot/messages.txt")));
            bootImages = List.of(new File(appendRootPath("modules/Boot/images")).listFiles());
        } catch (IOException e) {
            System.out.println("Boot messages are unavailable");
        }

        Stats stats = (Stats) modules.get("Stats");
        webController.sendImageWithMessage(
                bootImages.get((int) (Math.random() * bootImages.size())).getPath(),
                "Dogbot is online" +
                        (bootMessages.size() > 0 ?
                                "\n" + bootMessages.get((int) (Math.random() * bootMessages.size())) :
                                ""
                        ) + "\n\n" + stats.getMinifiedStats());
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
        modules.put("Dab", new Dab(this));
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
    public Dogbot(String username, String password, String threadId, boolean debugMode, boolean silentMode, boolean debugMessages, boolean headless, boolean maximised) {
        super(username, password, threadId, debugMode, silentMode, debugMessages, headless, maximised);
    }

    public Dogbot(String configName, String threadId, boolean debugMode, boolean silentMode, boolean debugMessages, boolean headless, boolean maximised) {
        super(configName, threadId, debugMode, silentMode, debugMessages, headless, maximised);
    }

    public Dogbot(String configName, boolean debugMode, boolean silentMode, boolean debugMessages, boolean headless, boolean maximised) {
        super(configName, debugMode, silentMode, debugMessages, headless, maximised);
    }
    //endregion

    public static void main(String[] args) {
        Chatbot bot;

        String configName = "config";
        String threadId = null;

        String username = null;
        String password = null;

        boolean debugMode = false;
        boolean silentMode = false;
        boolean debugMessages = false;
        boolean headless = false;
        boolean maximised = false;

        for (String arg : args) {
            //Remove any extra quotes someone may have added
            String[] subArgs = Arrays
                    .stream(arg.split("="))
                    .map(subArg -> subArg.replace("\"", ""))
                    .toArray(String[]::new);

            //Check for condtions
            switch (subArgs[0]) {
                case "config":
                    configName = subArgs[1];
                    break;
                case "threadId":
                case "threadid":
                    threadId = subArgs[1];
                    break;
                case "pass":
                    password = subArgs[1];
                    break;
                case "user":
                    username = subArgs[1];
                    break;
                case "-debug":
                case "-d":
                    debugMode = true;
                    break;
                case "-silent":
                case "-s":
                    silentMode = true;
                    break;
                case "-debugMessages":
                case "-dms":
                    debugMessages = true;
                    break;
                case "-headless":
                    headless = true;
                    break;
                case "-maximised":
                    maximised = true;
                    break;
            }
        }

        //Create bot
        if (username != null && password != null && threadId != null) {
            bot = new Dogbot(username, password, threadId, debugMode, silentMode, debugMessages, headless, maximised);
        } else {
            if (threadId != null) {
                bot = new Dogbot(configName, threadId, debugMode, silentMode, debugMessages, headless, maximised);
            } else {
                bot = new Dogbot(configName, debugMode, silentMode, debugMessages, headless, maximised);
            }
        }
    }
}
