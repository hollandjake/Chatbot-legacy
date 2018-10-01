package bot.dogbot;

import bot.Chatbot;

public class Dogbot extends Chatbot {

    public Dogbot(String username, String password, String threadId) {
    }

    public Dogbot(String configName, String threadId) {
    }

    public Dogbot(String configName) {
    }

    public Dogbot() {
    }

    public static void main(String[] args) {
        Chatbot bot;

        String configName = null;
        String threadId = null;

        for (int i = 0; i < args.length - 1; i++) {
            //Skip last one since this is only checking for modifiers
            String arg = args[i];
            if (arg.equals("-c")) {
                configName = args[i + 1];
            } else if (arg.equals("-t")) {
                threadId = args[i + 1];
            }
        }
        if (configName != null) {
            if (threadId != null) {
                bot = new Dogbot(configName, threadId);
            } else {
                bot = new Dogbot(configName);
            }
        } else {
            bot = new Dogbot();
        }
    }
}
