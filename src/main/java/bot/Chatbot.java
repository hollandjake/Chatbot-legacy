package bot;

import bot.modules.*;
import bot.modules.Shutdown;
import bot.utils.Human;
import bot.utils.Message;
import bot.utils.Module;
import bot.utils.WebController;
import bot.utils.exceptions.MalformedCommandException;
import com.google.errorprone.annotations.ForOverride;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;

import java.awt.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.ResourceBundle;

public class Chatbot {
    //region Constants
    private final String VERSION = "V1.18.0";
    protected final HashMap<String, Module> modules = new HashMap<>();
    protected final WebController webController;
    private final ArrayList<Message> messageLog = new ArrayList<>();
    private final ArrayList<Human> people = new ArrayList<>();
    private final String shutdownCode = Integer.toString(new Random().nextInt(99999));
    private final LocalDateTime startupTime = LocalDateTime.now();
    private final Duration messageTimeout = Duration.ofMinutes(1);
    private final long refreshRate = 100;
    //endregion

    //region Variables
    private boolean running = true;
    private String threadId;
    private Human me;
    //endregion

    //region Constructors
    public Chatbot(String username, String password, String threadId, boolean debugMode, boolean silentMode, boolean debugMessages, boolean headless, boolean maximised) {
        webController = new WebController(this, debugMessages, headless, maximised);
        run(username, password, threadId, debugMode, silentMode);
    }

    public Chatbot(String configName, String threadId, boolean debugMode, boolean silentMode, boolean debugMessages, boolean headless, boolean maximised) {
        webController = new WebController(this, debugMessages, headless, maximised);
        runFromConfigWithThreadId(configName, threadId, debugMode, silentMode);
    }

    public Chatbot(String configName, boolean debugMode, boolean silentMode, boolean debugMessages, boolean headless, boolean maximised) {
        webController = new WebController(this, debugMessages, headless, maximised);
        runFromConfig(configName, debugMode, silentMode);
    }

    public Chatbot() {
        webController = new WebController(this, false, false, false);
        runFromConfig("config", false, false);
    }

    private void runFromConfig(String configName, boolean debugMode, boolean silentMode) {
        ResourceBundle config = ResourceBundle.getBundle(configName);
        String threadId = config.getString("threadId");

        runFromConfigWithThreadId(configName, threadId, debugMode, silentMode);
    }

    private void runFromConfigWithThreadId(String configName, String threadId, boolean debugMode, boolean silentMode) {
        ResourceBundle config = ResourceBundle.getBundle(configName);
        String username = config.getString("username");
        String password = config.getString("password");

        run(username, password, threadId, debugMode, silentMode);
    }

    private void run(String username, String password, String threadId, boolean debugMode, boolean silentMode) {
        //Output Shutdown code
        System.out.println("Shutdown code: " + shutdownCode);

        this.threadId = threadId;
        loadModules();

        //Run setup
        webController.login(username, password);
        webController.gotoFacebookThread(threadId);

        //Wait until messages have loaded
        webController.waitForMessagesToLoad();

        //Init message
        if (!silentMode) {
            initMessage();
        }
        System.out.println("System is running");

        while (running) {
            try {
                webController.waitForNewMessage();
                Message newMessage = webController.getLatestMessage();
                messageLog.add(newMessage);

                if (debugMode) {
                    System.out.println(newMessage);
                }

                //Handle options
                try {
                    for (Module module : modules.values()) {
                        module.process(newMessage);
                    }
                } catch (MalformedCommandException e) {
                    sendMessage("There seems to be an issue with your command");
                }

            } catch (TimeoutException e) {
                if (debugMode) {
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
    //endregion

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
        modules.put("Github", new Github(this));
        modules.put("Commands", new Commands(this, "https://github.com/hollandjake/Chatbot/blob/master/README.md"));
    }

    @ForOverride
    protected void initMessage() {
        webController.sendMessage("Chatbot " + getVersion() + " is online!");
    }

    @ForOverride
    public String appendRootPath(String path) {
        return "/" + path;
    }
    //endregion

    //region Getters & Setters
    public ArrayList<Message> getMessageLog() {
        return messageLog;
    }

    public Duration getMessageTimeout() {
        return messageTimeout;
    }

    public Human getMe() {
        return me;
    }

    public void setMe(Human me) {
        this.me = me;
    }

    public ArrayList<Human> getPeople() {
        return people;
    }

    public String getThreadId() {
        return threadId;
    }

    public HashMap<String, Module> getModules() {
        return modules;
    }

    public LocalDateTime getStartupTime() {
        return startupTime;
    }

    public String getShutdownCode() {
        return shutdownCode;
    }

    public long getRefreshRate() {
        return refreshRate;
    }

    //endregion

    //region Send Message
    public void sendMessage(String message) {
        webController.sendMessage(message);
    }

    public void sendImageWithMessage(String image, String message) {
        webController.sendImageWithMessage(image, message);
    }

    public void sendImageWithMessage(Image image, String message) {
        webController.sendMessage(new Message(me, message, image));
    }

    public void sendImageFromURLWithMessage(String url, String message) {
        webController.sendImageFromURLWithMessage(url, message);
    }

    public void sendMessage(Message message) {
        webController.sendMessage(message);
    }
    //endregion

    public boolean containsCommand(Message message) {
        for (Module module : modules.values()) {
            if (!module.getMatch(message).equals("")) {
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
}
