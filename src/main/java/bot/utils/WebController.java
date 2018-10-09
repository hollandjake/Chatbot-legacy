package bot.utils;

import bot.Chatbot;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;

import static bot.utils.XPATHS.*;

public class WebController {
    private Chatbot chatbot;
    private final ChromeDriverService service;
    private final WebDriver webDriver;
    private final Actions keyboard;
    private final WebDriverWait wait;
    private final WebDriverWait messageWait;
    private final boolean debugMessages;

    public WebController(Chatbot chatbot, boolean debugMessages) {
        this.chatbot = chatbot;
        this.debugMessages = debugMessages;

        ClassLoader classLoader = getClass().getClassLoader();
        File driver = System.getProperty("os.name").toLowerCase().contains("windows") ?
                new File(classLoader.getResource("drivers/windows/chromedriver.exe").getFile()) :
                new File(classLoader.getResource("drivers/linux/chromedriver").getFile());

        //Create service
        service = new ChromeDriverService.Builder()
                .usingDriverExecutable(driver)
                .usingAnyFreePort()
                .build();
        try {
            service.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Setup drivers
        webDriver = new RemoteWebDriver(service.getUrl(), new ChromeOptions());
        keyboard = new Actions(webDriver);

        //Setup waits
        wait = new WebDriverWait(webDriver, 5);
        messageWait = new WebDriverWait(webDriver, chatbot.getMessageTimeout().getSeconds());
    }

    public void quit() {
        webDriver.quit();
    }

    public void login(String username, String password) {

        //Goto page
        webDriver.get("https://www.messenger.com");

        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(LOGIN_EMAIL)));
        webDriver.findElement(By.xpath(LOGIN_EMAIL)).sendKeys(username);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(LOGIN_PASS)));
        webDriver.findElement(By.xpath(LOGIN_PASS)).sendKeys(password);
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath(LOGIN_BUTTON)));
        webDriver.findElement(By.xpath(LOGIN_BUTTON)).click();
    }

    public void gotoFacebookThread(String threadId) {
        webDriver.get("https://www.messenger.com/t/" + threadId);
        chatbot.setMe(Human.createForBot(getMyUserId()));
    }

    public WebElement getMyUserId() {
        return webDriver.findElement(By.xpath(MY_USER_ID));
    }

    //Sending Messages
    public void sendMessage(Message message) {
        WebElement inputBox = selectInputBox();
        if (debugMessages) {
            message.sendDebugMessage(inputBox);
        } else {
            message.sendMessage(inputBox);
        }
    }

    public void sendMessage(String message) {
        sendMessage(new Message(chatbot.getMe(), message));
    }

    public void sendImageWithMessage(String image, String message) {
        sendMessage(new Message(chatbot.getMe(), message, image));
    }

    public void sendImage(String image) {
        sendImageWithMessage(image, "");
    }

    private WebElement selectInputBox() {
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath(INPUT_FIELD)));
        WebElement inputBoxElement = webDriver.findElement(By.xpath(INPUT_FIELD));
        inputBoxElement.click();

        return inputBoxElement;
    }

    //Getters
    public Message getLatestMessage() {
        WebElement messageElement = webDriver.findElement(By.xpath(MESSAGES_OTHERS_RECENT));
        //Move mouse over message so messenger marks it as read
        new Actions(webDriver).moveToElement(messageElement);
        return new Message(messageElement, chatbot);
    }

    public int getNumberOfMessagesDisplayed() {
        return webDriver.findElements(By.xpath(MESSAGES_OTHERS)).size();
    }

    //Waits
    public void waitForMessagesToLoad() {
        messageWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(MESSAGE_CONTAINER)));
    }

    public void waitForNewMessage() {
        messageWait.until(ExpectedConditions.numberOfElementsToBeMoreThan(
                By.xpath(MESSAGES_OTHERS),
                getNumberOfMessagesDisplayed()));
    }
}

