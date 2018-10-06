package bot.dogbot;

import bot.Chatbot;
import bot.dogbot.modules.Quote;

import java.util.Arrays;

public class Dogbot extends Chatbot {

    @Override
    protected void loadModules() {
        modules.add(new Quote(this));
    }

    public Dogbot(String username, String password, String threadId, boolean debugMode, boolean silentMode) {
        super(username, password, threadId, debugMode, silentMode);
    }

    public Dogbot(String configName, String threadId, boolean debugMode, boolean silentMode) {
        super(configName, threadId, debugMode, silentMode);
    }

    public Dogbot(String configName, boolean debugMode, boolean silentMode) {
        super(configName, debugMode, silentMode);
    }

    public static void main(String[] args) {
        Chatbot bot;

        String configName = "config";
        String threadId = null;

        String username = null;
        String password = null;

        boolean debugMode = false;
        boolean silentMode = false;

        for (int i = 0; i < args.length; i++) {
            //Remove any extra quotes someone may have added
            String[] subArgs = Arrays
                    .stream(args[i].split("="))
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
            }
        }

        //Create bot
        if (username != null && password != null && threadId != null) {
            bot = new Dogbot(username, password, threadId, debugMode, silentMode);
        } else {
            if (threadId != null) {
                bot = new Dogbot(configName, threadId, debugMode, silentMode);
            } else {
                bot = new Dogbot(configName, debugMode, silentMode);
            }
        }
    }
}
