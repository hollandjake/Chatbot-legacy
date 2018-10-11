package bot.dogbot;

import bot.Chatbot;
import bot.modules.Commands;
import bot.modules.Quotes;
import bot.modules.Stats;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Dogbot extends Chatbot {
    private final String VERSION = "V0.1.0";

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
        modules.put("Quotes", new Quotes(this));
        modules.put("Stats", new Stats(this));
        modules.put("Commands", new Commands(this, "https://github.com/hollandjake/Chatbot/blob/master/src/main/java/bot/dogbot"));
    }
    //endregion

    //region Constructors
    public Dogbot(String username, String password, String threadId, boolean debugMode, boolean silentMode, boolean debugMessages) {
        super(username, password, threadId, debugMode, silentMode, debugMessages);
    }

    public Dogbot(String configName, String threadId, boolean debugMode, boolean silentMode, boolean debugMessages) {
        super(configName, threadId, debugMode, silentMode, debugMessages);
    }

    public Dogbot(String configName, boolean debugMode, boolean silentMode, boolean debugMessages) {
        super(configName, debugMode, silentMode, debugMessages);
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
            }
        }

        //Create bot
        if (username != null && password != null && threadId != null) {
            bot = new Dogbot(username, password, threadId, debugMode, silentMode, debugMessages);
        } else {
            if (threadId != null) {
                bot = new Dogbot(configName, threadId, debugMode, silentMode, debugMessages);
            } else {
                bot = new Dogbot(configName, debugMode, silentMode, debugMessages);
            }
        }
    }
}
