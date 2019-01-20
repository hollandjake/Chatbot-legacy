package bot.utils;

import bot.Chatbot;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static bot.utils.XPATHS.*;

public class WebController {
    //region Variables
    private final Chatbot chatbot;
    private final Database db;
    private final ChromeDriverService service;
    private final WebDriver webDriver;
    private final Actions keyboard;
    private final WebDriverWait wait;
    private final WebDriverWait messageWait;

    private final HashMap<String, String> config;
    //endregion

    public WebController(Chatbot chatbot, HashMap<String, String> config) {
        this.chatbot = chatbot;
        this.config = config;
        this.db = chatbot.getDb();

        ClassLoader classLoader = getClass().getClassLoader();
        File driver = System.getProperty("os.name").toLowerCase().contains("windows") ?
                new File(classLoader.getResource("drivers/windows/chromedriver.exe").getFile()) :
                new File(classLoader.getResource("drivers/linux/chromedriver").getFile());
        driver.setExecutable(true);

        //region Create service
        service = new ChromeDriverService.Builder()
                .usingDriverExecutable(driver)
                .usingAnyFreePort()
                .build();
        try {
            service.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //endregion

        //region Setup drivers
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("mute-audio", "console");
        if (config.containsKey("headless")) {
            chromeOptions.addArguments("headless", "window-size=1920,1080");
        } else if (config.containsKey("maximised")) {
            chromeOptions.addArguments("start-maximized");
        }
        webDriver = new RemoteWebDriver(service.getUrl(), chromeOptions);
        keyboard = new Actions(webDriver);
        //endregion

        //region Setup waits
        wait = new WebDriverWait(webDriver, 30);
        messageWait = new WebDriverWait(webDriver, chatbot.getMessageTimeout().getSeconds(), chatbot.getRefreshRate());
        //endregion

        Thread.setDefaultUncaughtExceptionHandler((thread, e) -> {
            e.printStackTrace();
            screenshot();
            quit(false);
        });
    }

    public void quit(boolean withMessage) {
        if (withMessage) {
            chatbot.sendMessage("I'm off to sleep now, see you soon!");
        }
        webDriver.quit();
        System.exit(0);
    }

    //region Login
    public void login(String username, String password) {
        //Goto page
        webDriver.get("https://www.messenger.com");

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath(LOGIN_EMAIL)));
        webDriver.findElement(By.xpath(LOGIN_EMAIL)).sendKeys(username);
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath(LOGIN_PASS)));
        webDriver.findElement(By.xpath(LOGIN_PASS)).sendKeys(password);

        webDriver.findElement(By.xpath(COOKIES_CLOSE)).click();

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath(LOGIN_BUTTON)));
        webDriver.findElement(By.xpath(LOGIN_BUTTON)).click();
    }

    public void gotoFacebookThread(String threadId) {
        Human me = db.getHumanFromName(getMyRealName());
        chatbot.setMe(me);
        webDriver.get("https://www.messenger.com/t/" + threadId);
    }
    //endregion

    //region Sending messages
    public void sendMessage(Message message) {
        int myMessageCount = getNumberOfMyMessagesDisplayed();
        WebElement inputBox = selectInputBox();
        if (config.containsKey("debug-messages")) {
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

    private WebElement selectInputBox() {
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath(INPUT_FIELD)));
        WebElement inputBoxElement = webDriver.findElement(By.xpath(INPUT_FIELD));

        try {
            inputBoxElement.click();
            return inputBoxElement;
        } catch (WebDriverException e) {
            webDriver.navigate().refresh();
            return selectInputBox();
        }
    }
    //endregion

    //region Getters
    public Message getLatestMessage() {
        List<WebElement> contentNoLongerAvailable = webDriver.findElements(By.xpath(CONTENT_NO_LONGER_AVAILABLE));

        if (contentNoLongerAvailable.isEmpty()) {
            WebElement messageElement = webDriver.findElement(By.xpath(MESSAGES_OTHERS_RECENT));
            //Move mouse over message so messenger marks it as read
            selectInputBox();
            return Message.fromElement(chatbot, messageElement);
        } else {
            System.out.println("Swatted a popup");
            contentNoLongerAvailable.get(0).click();
            return null;
        }
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
        messageWait.until(ExpectedConditions.or(
                ExpectedConditions.numberOfElementsToBeMoreThan(
                        By.xpath(MESSAGES_OTHERS),
                        getNumberOfMessagesDisplayed()),
                ExpectedConditions.elementToBeClickable(By.xpath(CONTENT_NO_LONGER_AVAILABLE))
                )
        );
    }
    //endregion
}

