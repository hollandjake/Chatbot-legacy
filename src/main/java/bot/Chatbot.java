package bot;

import bot.utils.Message;
import bot.utils.Module;
import bot.utils.WebController;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.concurrent.TimeoutException;

public class Chatbot {
    //Constants
    private final String initMessage = "Chatbot is online";


    public final ArrayList<Module> modules = new ArrayList<Module>();
    public final ArrayList<Message> messageLog = new ArrayList<Message>();

    private final String shutdownCode = Integer.toString(new Random().nextInt(99999));
    private final Date startTime = new Date();
    private String threadId;

    private WebController webController;

    private boolean running = true;


    public Chatbot(String username, String password, String threadId) {
        //Output Shutdown code
        System.out.println("Shutdown code: " + shutdownCode);

        this.threadId = threadId;

        webController = new WebController();

        //Run setup
        webController.login(username, password);
        webController.gotoFacebookThread(threadId);

        //Init message
        webController.sendMessage(initMessage);

        while (running) {
            try {
                webController.waitForNewMessage();
                webController.sendMessage(webController.getLatestMessage());
            } catch (org.openqa.selenium.TimeoutException e) {

            }
        }
    }

    public Chatbot(String configName, String threadId) {
        this.threadId = threadId;

        ResourceBundle config = ResourceBundle.getBundle(configName);

        String username = config.getString("username");
        String password = config.getString("password");
        new Chatbot(username, password, threadId);
    }

    public Chatbot(String configName) {
        ResourceBundle config = ResourceBundle.getBundle(configName);
        String threadId = config.getString("threadId");

        new Chatbot(configName, threadId);
    }

    public Chatbot() {
        new Chatbot("config");
    }
}
