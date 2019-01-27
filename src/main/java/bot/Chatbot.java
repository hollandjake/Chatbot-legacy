package bot;

import bot.modules.Shutdown;
import bot.modules.*;
import bot.utils.*;
import bot.utils.exceptions.MalformedCommandException;
import bot.utils.exceptions.MissingConfigurationsException;
import com.google.errorprone.annotations.ForOverride;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Chatbot {
    //region Constants
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
        if (!config.containsKey("thread_name") || !config.containsKey("username") || !config.containsKey("password")) {
            throw new MissingConfigurationsException("thread_name", "username", "password");
        }

        if (config.containsKey("full_debug")) {
            System.setProperty("javax.net.debug", "all");
        }

        this.messageTimeout = config.containsKey("message_timeout") ? Duration.ofMillis(Long.valueOf(config.get("message_timeout"))) : Duration.ofMinutes(1);
        this.boot = new Boot(this);
        this.db = new Database(config, this);
        this.webController = new WebController(this, config);

        //Output Shutdown code
        System.out.println("Shutdown code: " + shutdownCode);

        this.threadName = config.containsKey("debug_thread_name") ? config.get("debug_thread_name") : config.get("thread_name");
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
        webController.gotoFacebookThread(config.get("thread_name"));

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
            String[] subArgs = arg.split("=");
            config.put(subArgs[0].replaceFirst("-", ""), subArgs.length == 2 ? subArgs[1] : "true");
        }
        return config;
    }

    //region ForOverrides
    @ForOverride
    public String getVersion() {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = null;
        try {
            try {
                model = reader.read(new FileReader(new File("pom.xml")));
            } catch (IOException e) {
                model = reader.read(
                        new InputStreamReader(
                                getClass().getResourceAsStream(
                                        "/META-INF/maven/de.scrum-master.stackoverflow/aspectj-introduce-method/pom.xml"
                                )
                        )
                );
            }
        } catch (XmlPullParserException | IOException ignore) {
        }
        if (model != null) {
            return model.getVersion();
        }
        return "";
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

    public static void main(String[] args) {
        Chatbot bot;
        HashMap<String, String> config = createConfig(args);

        //Create bot
        try {
            bot = new Chatbot(config);
        } catch (MissingConfigurationsException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
