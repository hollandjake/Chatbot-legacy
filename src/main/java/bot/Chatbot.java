package bot;

import bot.utils.Human;
import bot.utils.Message;
import bot.utils.Module;
import bot.utils.WebController;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.ResourceBundle;

public class Chatbot {
    //Constants
    private final ArrayList<Message> messageLog = new ArrayList<>();
    private final ArrayList<Human> people = new ArrayList<>();

    private final String shutdownCode = Integer.toString(new Random().nextInt(99999));
    private final Date startTime = new Date();

    private final Duration messageTimeout = Duration.ofMinutes(1);
    private final WebController webController = new WebController(this);

    //Variables
    private boolean running = true;
    private String threadId;
    private Human me;

    private String initMessage = "Chatbot is online";

    protected final ArrayList<Module> modules = new ArrayList<>();

    public Chatbot(String username, String password, String threadId, boolean debugMode) {
        run(username, password, threadId, debugMode);
    }

    public Chatbot(String configName, String threadId, boolean debugMode) {
        runFromConfigWithThreadId(configName, threadId, debugMode);
    }

    public Chatbot(String configName, boolean debugMode) {
        runFromConfig(configName, debugMode);
    }

    public Chatbot() {
        runFromConfig("config", false);
    }

    protected void loadModules() {
    }

    public boolean containsCommand(Message message) {
        for (Module module : modules) {
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

    private void runFromConfig(String configName, boolean debugMode) {
        ResourceBundle config = ResourceBundle.getBundle(configName);
        String threadId = config.getString("threadId");

        runFromConfigWithThreadId(configName, threadId, debugMode);
    }

    private void runFromConfigWithThreadId(String configName, String threadId, boolean debugMode) {
        ResourceBundle config = ResourceBundle.getBundle(configName);
        String username = config.getString("username");
        String password = config.getString("password");

        run(username, password, threadId, debugMode);
    }

    private void run(String username, String password, String threadId, boolean debugMode) {
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
        if (!debugMode) {
            webController.sendMessage(initMessage);
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
                for (Module module : modules) {
                    module.process(newMessage);
                }

            } catch (TimeoutException e) {
                if (debugMode) {
                    System.out.println("No messaged received in the last " + messageTimeout + "s");
                }
            } catch (WebDriverException e) {
                System.out.println("Browser was closed, program is ended");
                webController.quit();
                System.exit(1);
            }
        }
    }
}
