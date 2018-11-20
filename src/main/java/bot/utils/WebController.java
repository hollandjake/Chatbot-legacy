package bot.utils;

import bot.Chatbot;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
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
    //region Variables
    private Chatbot chatbot;
    private final ChromeDriverService service;
    private final WebDriver webDriver;
    private final Actions keyboard;
    private final WebDriverWait wait;
    private final WebDriverWait messageWait;
    private final boolean debugMessages;
    //endregion

    public WebController(Chatbot chatbot, boolean debugMessages, boolean headless, boolean maximised) {
        this.chatbot = chatbot;
        this.debugMessages = debugMessages;

        ClassLoader classLoader = getClass().getClassLoader();
        File driver = System.getProperty("os.name").toLowerCase().contains("windows") ?
                new File(classLoader.getResource("drivers/windows/chromedriver.exe").getFile()) :
                new File(classLoader.getResource("drivers/linux/chromedriver").getFile());
        driver.setExecutable(true);

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
        ChromeOptions chromeOptions = new ChromeOptions();
        if (headless) {
            chromeOptions.addArguments("headless", "window-size=1920,1080");
        } else if (maximised) {
            chromeOptions.addArguments("start-maximized");
        }
        webDriver = new RemoteWebDriver(service.getUrl(), chromeOptions);
        keyboard = new Actions(webDriver);

        //Setup waits
        wait = new WebDriverWait(webDriver, 10);
        messageWait = new WebDriverWait(webDriver, chatbot.getMessageTimeout().getSeconds(), chatbot.getRefreshRate());

        Thread.setDefaultUncaughtExceptionHandler((thread, e) -> {
            e.printStackTrace();
            screenshot();
            quit(false);
        });
    }

    public void quit(boolean withMessage) {
        if (withMessage) {
            sendMessage("I'm off to sleep now, see you soon!");
        }
        webDriver.quit();
        System.exit(0);
    }

    //region Login
    public void login(String username, String password) {
        //Goto page
        webDriver.get("https://www.messenger.com");

        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(LOGIN_EMAIL)));
        webDriver.findElement(By.xpath(LOGIN_EMAIL)).sendKeys(username);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(LOGIN_PASS)));
        webDriver.findElement(By.xpath(LOGIN_PASS)).sendKeys(password);

        webDriver.findElement(By.xpath(COOKIES_CLOSE)).click();

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath(LOGIN_BUTTON)));
        webDriver.findElement(By.xpath(LOGIN_BUTTON)).click();
    }

    public void gotoFacebookThread(String threadId) {
        chatbot.setMe(Human.createForBot(getMyRealName()));
        webDriver.get("https://www.messenger.com/t/" + threadId);
    }
    //endregion

    //region Sending messages
    public void sendMessage(Message message) {
        int myMessageCount = getNumberOfMyMessagesDisplayed();
        WebElement inputBox = selectInputBox();
        if (debugMessages) {
            message.sendDebugMessage(inputBox);
        } else {
            message.sendMessage(inputBox);
        }
        //Wait for message to be sent
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(By.xpath(MESSAGES_MINE),
                myMessageCount));
    }

    public void screenshot() {
        ScreenshotUtil.capture(webDriver);
    }

    public void sendMessage(String message) {
        sendMessage(new Message(chatbot.getMe(), message));
    }

    public void sendImageWithMessage(String image, String message) {
        sendMessage(new Message(chatbot.getMe(), message, image));
    }

    public void sendImageFromURLWithMessage(String url, String message) {
        sendMessage(Message.withImageFromURL(chatbot.getMe(), message, url));
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
    //endregion

    //region Getters
    public Message getLatestMessage() {
        WebElement messageElement = webDriver.findElement(By.xpath(MESSAGES_OTHERS_RECENT));
        //Move mouse over message so messenger marks it as read
        keyboard.moveToElement(messageElement);
        return new Message(messageElement, chatbot);
    }

    public int getNumberOfMessagesDisplayed() {
        return webDriver.findElements(By.xpath(MESSAGES_OTHERS)).size();
    }

    public int getNumberOfMyMessagesDisplayed() {
        return webDriver.findElements(By.xpath(MESSAGES_MINE)).size();
    }

    public String getMyRealName() {
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath(SETTINGS_COG)));
        webDriver.findElement(By.xpath(SETTINGS_COG)).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath(SETTINGS_DROPDOWN)));
        webDriver.findElement(By.xpath(SETTINGS_DROPDOWN)).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(MY_REAL_NAME)));
        String name = webDriver.findElement(By.xpath(MY_REAL_NAME)).getText();
        webDriver.findElement(By.xpath(SETTINGS_DONE)).click();
        return name;

    }
    //endregion

    //region Waits
    public void waitForMessagesToLoad() {
        messageWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(MESSAGE_CONTAINER)));
    }

    public void waitForNewMessage() throws TimeoutException {
        messageWait.until(ExpectedConditions.numberOfElementsToBeMoreThan(
                By.xpath(MESSAGES_OTHERS),
                getNumberOfMessagesDisplayed()));
    }
    //endregion
}

