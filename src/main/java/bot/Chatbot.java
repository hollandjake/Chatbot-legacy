package bot;

import bot.utils.Human;
import bot.utils.Message;
import bot.utils.Module;
import bot.utils.WebController;
import bot.utils.exceptions.MalformedCommandException;
import com.google.errorprone.annotations.ForOverride;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.ResourceBundle;

import static bot.utils.CONSTANTS.getVersion;

public class Chatbot {
    //Constants
    private final ArrayList<Message> messageLog = new ArrayList<>();
    private final ArrayList<Human> people = new ArrayList<>();

    private final String shutdownCode = Integer.toString(new Random().nextInt(99999));
    private final LocalDateTime startupTime = LocalDateTime.now();

    private final Duration messageTimeout = Duration.ofMinutes(1);
    protected final WebController webController;

    //Variables
    private boolean running = true;
    private String threadId;
    private Human me;

    protected final HashMap<String, Module> modules = new HashMap<>();

    public Chatbot(String username, String password, String threadId, boolean debugMode, boolean silentMode, boolean debugMessages) {
        webController = new WebController(this, debugMessages);
        run(username, password, threadId, debugMode, silentMode);
    }

    public Chatbot(String configName, String threadId, boolean debugMode, boolean silentMode, boolean debugMessages) {
        webController = new WebController(this, debugMessages);
        runFromConfigWithThreadId(configName, threadId, debugMode, silentMode);
    }

    public Chatbot(String configName, boolean debugMode, boolean silentMode, boolean debugMessages) {
        webController = new WebController(this, debugMessages);
        runFromConfig(configName, debugMode, silentMode);
    }

    public Chatbot() {
        webController = new WebController(this, false);
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
                System.out.println("Browser was closed, program is ended");
                webController.quit();
                System.exit(1);
            }
        }
    }


    //Methods ready to be overwritten
    @ForOverride
    protected void loadModules() {
    }

    @ForOverride
    protected void initMessage() {
        webController.sendMessage("Chatbot " + getVersion() + " is online!");
    }

    @ForOverride
    public String appendRootPath(String path) {
        return "/" + path;
    }

    //Methods
    public boolean containsCommand(Message message) {
        for (Module module : modules.values()) {
            if (!module.getMatch(message).equals("")) {
                return true;
            }
        }
        return false;
    }

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

    public void sendMessage(String message) {
        webController.sendMessage(message);
    }

    public void sendMessage(Message message) {
        webController.sendMessage(message);
    }

    public String getThreadId() {
        return threadId;
    }

    public LocalDateTime getStartupTime() {
        return startupTime;
    }
}
