package bot.utils;

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
import java.util.ArrayList;

import static bot.utils.XPATHS.*;

public class WebController {
    private Human me;
    private final ChromeDriverService service;
    private final WebDriver webDriver;
    private final Actions keyboard;
    private final WebDriverWait wait;
    private final WebDriverWait messageWait;

    public WebController(int messageTimeout) {
        ClassLoader classLoader = getClass().getClassLoader();
        File driver = System.getProperty("os.name").toLowerCase().contains("windows") ?
                new File(classLoader.getResource("drivers/windows/chromedriver.exe").getFile()) :
                new File(classLoader.getResource("drivers/linux/chromedriver").getFile());

        service = new ChromeDriverService.Builder()
                .usingDriverExecutable(driver)
                .usingAnyFreePort()
                .build();
        try {
            service.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        webDriver = new RemoteWebDriver(service.getUrl(), new ChromeOptions());
        keyboard = new Actions(webDriver);

        wait = new WebDriverWait(webDriver, 5);
        messageWait = new WebDriverWait(webDriver, messageTimeout);
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
        me = Human.createForBot(getMyUserId());
    }

    public WebElement getMyUserId() {
        return webDriver.findElement(By.xpath(MY_USER_ID));
    }

    public void sendMessage(String message) {
        new Message(me, message).sendMessage(selectInputBox());
    }

    public void sendMessage(Message message) {
        message.sendMessage(selectInputBox());
    }

    private WebElement selectInputBox() {
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath(INPUT_FIELD)));
        WebElement inputBoxElement = webDriver.findElement(By.xpath(INPUT_FIELD));
        inputBoxElement.click();

        return inputBoxElement;
    }

    public Message getLatestMessage(ArrayList<Human> people) {
        WebElement messageElement = webDriver.findElement(By.xpath(MESSAGES_OTHERS_RECENT));
        return new Message(messageElement, people);
    }

    public void waitForMessagesToLoad() {
        messageWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(MESSAGE_CONTAINER)));
    }

    public void waitForNewMessage() {
        messageWait.until(ExpectedConditions.numberOfElementsToBeMoreThan(
                By.xpath(MESSAGES_OTHERS),
                getNumberOfMessagesDisplayed()));
    }

    public int getNumberOfMessagesDisplayed() {
        return webDriver.findElements(By.xpath(MESSAGES_OTHERS)).size();
    }

}

