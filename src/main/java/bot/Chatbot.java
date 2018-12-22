package bot;

import bot.modules.*;
import bot.modules.Shutdown;
import bot.utils.*;
import bot.utils.exceptions.MalformedCommandException;
import bot.utils.exceptions.MissingConfigurationsException;
import com.google.errorprone.annotations.ForOverride;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;

import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Chatbot {
    //region Constants
    private final String VERSION = "V2.0.0";
    protected final HashMap<String, CommandModule> modules = new HashMap<>();
    protected final WebController webController;

    protected final Database db;
    protected final Boot boot;
    protected final String threadName;
    protected final int threadId;
    private final String shutdownCode = Integer.toString(new Random().nextInt(99999));
    private final Duration messageTimeout;
    private final LocalDateTime startupTime = LocalDateTime.now();
    private final long refreshRate = 100;
    //endregion

    //region Variables
    private boolean running = true;
    protected Human me;
    //endregion

    public Chatbot(HashMap<String, String> config) throws MissingConfigurationsException {
        //Check for core configutations
        if (!config.containsKey("threadName") || !config.containsKey("username") || !config.containsKey("password")) {
            throw new MissingConfigurationsException("threadName", "username", "password");
        }

        this.messageTimeout = config.containsKey("messageTimeout") ? Duration.ofMillis(Long.valueOf(config.get("messageTimeout"))) : Duration.ofMinutes(1);
        this.boot = new Boot(this);
        this.db = new Database(config, this);
        this.webController = new WebController(this, config);

        //Output Shutdown code
        System.out.println("Shutdown code: " + shutdownCode);

        this.threadName = config.containsKey("debug-threadName") ? config.get("debug-threadName") : config.get("threadName");
        this.threadId = db.getThreadIdFromName(threadName);
        loadModules();
        try {
            db.createQueries(this);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Couldn't create initial queries");
            System.exit(1);
        }

        //Run setup
        webController.login(config.get("username"), config.get("password"));
        webController.gotoFacebookThread(config.get("threadName"));

        //Wait until messages have loaded
        webController.waitForMessagesToLoad();

        //Init message
        if (!config.containsKey("silent")) {
            initMessage();
        }
        System.out.println("System is running");

        while (running) {
            try {
                webController.waitForNewMessage();
                Message newMessage = webController.getLatestMessage();
                if (newMessage == null) {
                    continue;
                }

                if (config.containsKey("debug")) {
                    System.out.println(newMessage);
                }
                //Handle options
                try {
                    for (CommandModule commandModule : modules.values()) {
                        commandModule.process(newMessage);
                    }
                } catch (MalformedCommandException e) {
                    sendMessage("There seems to be an issue with your command");
                }
            } catch (TimeoutException e) {
                if (config.containsKey("debug")) {
                    System.out.println("No messaged received in the last " + messageTimeout);
                }
            } catch (WebDriverException e) {
                e.printStackTrace();
                System.out.println("Browser was closed, program is ended");
                webController.quit(true);
                System.exit(1);
            }
        }
    }

    public static HashMap<String, String> createConfig(String... args) {
        HashMap<String, String> config = new HashMap<>();

        for (String arg : args) {
            //Remove any extra quotes someone may have added
            String[] subArgs = Arrays
                    .stream(arg.split("="))
                    .map(subArg -> subArg.replace("\"", ""))
                    .toArray(String[]::new);

            //Check for condtions
            switch (subArgs[0]) {
                case "config":
                    config.put("config", subArgs[1]);
                    break;
                case "threadName":
                case "threadname":
                    config.put("threadName", subArgs[1]);
                    break;
                case "pass":
                    config.put("password", subArgs[1]);
                    break;
                case "user":
                    config.put("username", subArgs[1]);
                    break;
                case "dbUrl":
                    config.put("dbUrl", subArgs[1]);
                    break;
                case "dbUser":
                    config.put("dbUsername", subArgs[1]);
                    break;
                case "dbPass":
                    config.put("dbPassword", subArgs[1]);
                case "-debug":
                case "-d":
                    config.put("debug", "true");
                    break;
                case "-debug-threadName":
                case "-debug-threadname":
                    config.put("debug-threadName", subArgs[1]);
                    break;
                case "-silent":
                case "-s":
                    config.put("silent", "true");
                    break;
                case "-debugMessages":
                case "-dms":
                    config.put("debug-messages", "true");
                    break;
                case "-headless":
                    config.put("headless", "true");
                    break;
                case "-maximised":
                    config.put("maximised", "true");
                    break;
            }
        }
        return config;
    }

    //region ForOverrides
    @ForOverride
    public String getVersion() {
        return VERSION;
    }

    @ForOverride
    protected void loadModules() {
        modules.put("Shutdown", new Shutdown(this));
        modules.put("Stats", new Stats(this));
        modules.put("Ping", new Ping(this));
        modules.put("Github", new OneLinkCommand(this,
                List.of("github", "repo"),
                "https://github.com/hollandjake/Chatbot",
                "Github repository"));
        modules.put("Commands", new OneLinkCommand(this,
                List.of("commands", "help"),
                "https://github.com/hollandjake/Chatbot/blob/master/README.md",
                "A list of commands can be found at"));
    }

    @ForOverride
    protected void initMessage() {
        sendMessage("Chatbot " + getVersion() + " is online!");
    }

    @ForOverride
    public String appendRootPath(String path) {
        return "/" + path;
    }
    //endregion

    //region Send Message
    public void sendMessage(String message) {
        webController.sendMessage(new Message(me, message, null, null, 0));
    }

    public void sendImageWithMessage(String image, String message) {
        webController.sendMessage(new Message(me, message, image, null, 0));
    }

    public void sendMessage(Message message) {
        webController.sendMessage(message);
    }
    //endregion

    public boolean containsCommand(Message message) {
        for (CommandModule commandModule : modules.values()) {
            if (!commandModule.getMatch(message).equals("")) {
                return true;
            }
        }
        return false;
    }

    public void screenshot() {
        webController.screenshot();
    }

    public void quit() {
        webController.quit(true);
    }

    //region Database commands
    public int getMessageCount() {
        return db.getMessageCount(threadId);
    }
    //endregion

    //region Getters & Setters

    public String getVERSION() {
        return VERSION;
    }

    public HashMap<String, CommandModule> getModules() {
        return modules;
    }

    public WebController getWebController() {
        return webController;
    }

    public Database getDb() {
        return db;
    }

    public String getShutdownCode() {
        return shutdownCode;
    }

    public Duration getMessageTimeout() {
        return messageTimeout;
    }

    public LocalDateTime getStartupTime() {
        return startupTime;
    }

    public long getRefreshRate() {
        return refreshRate;
    }

    public boolean isRunning() {
        return running;
    }

    public int getThreadId() {
        return threadId;
    }

    public String getThreadName() {
        return threadName;
    }

    public Human getMe() {
        return me;
    }

    public void setMe(Human me) {
        this.me = me;
    }

    public Boot getBootModule() {
        return boot;
    }
    //endregion
}
