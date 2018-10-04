package bot.dogbot;

import bot.Chatbot;
import bot.dogbot.modules.Quotes;

import java.util.Arrays;

public class Dogbot extends Chatbot {

    @Override
    protected void loadModules() {
        modules.add(new Quotes());
    }


    public Dogbot(String username, String password, String threadId, boolean debugMode) {
        super(username, password, threadId, debugMode);
    }

    public Dogbot(String configName, String threadId, boolean debugMode) {
        super(configName, threadId, debugMode);
    }

    public Dogbot(String configName, boolean debugMode) {
        super(configName, debugMode);
    }

    public static void main(String[] args) {
        Chatbot bot;

        String configName = "config";
        String threadId = null;

        String username = null;
        String password = null;

        boolean debugMode = false;

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
            }
        }

        //Create bot
        if (username != null && password != null && threadId != null) {
            bot = new Dogbot(username, password, threadId, debugMode);
        } else {
            if (threadId != null) {
                bot = new Dogbot(configName, threadId, debugMode);
            } else {
                bot = new Dogbot(configName, debugMode);
            }
        }
    }
}
