package bot.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;

public class WebController {
    private ChromeDriverService service;
    private WebDriver webDriver;
    private Actions keyboard;
    private WebDriverWait wait;
    private WebDriverWait messageWait;

    public WebController() {
        File driver;
        ClassLoader classLoader = getClass().getClassLoader();
        driver = System.getProperty("os.name").toLowerCase().contains("windows") ?
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
        messageWait = new WebDriverWait(webDriver, 1);
    }

    public void quit() {
        webDriver.quit();
    }

    public void login(String username, String password) {
        final String emailBox = "//input[@id='email']";
        final String passBox = "//input[@id='pass']";
        final String loginButton = "//button[@id='loginbutton']";

        //Goto page
        System.out.println("wow");
        webDriver.get("https://www.messenger.com");

        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(emailBox)));
        webDriver.findElement(By.xpath(emailBox)).sendKeys(username);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(passBox)));
        webDriver.findElement(By.xpath(passBox)).sendKeys(password);
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath(loginButton)));
        webDriver.findElement(By.xpath(loginButton)).click();
    }

    public void gotoFacebookThread(String threadId) {
        webDriver.get("https://www.messenger.com/t/" + threadId);
    }

    public void sendMessage(String message) {
        WebElement inputBox = selectInputBox();

        Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(new StringSelection(message), null);

        inputBox.sendKeys(Keys.chord(Keys.CONTROL,"v")+Keys.ENTER);
    }

    private WebElement selectInputBox() {
        final String inputBox = "//div[@class='notranslate _5rpu']";
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath(inputBox)));
        WebElement inputBoxElement = webDriver.findElement(By.xpath(inputBox));
        inputBoxElement.click();

        return inputBoxElement;
    }

    public String getLatestMessage(){
        WebElement messageElement = webDriver.findElement(By.xpath("(//div[@body and @data-tooltip-position='left'])[last()]"));
        return messageElement.getAttribute("body");
    }

    public void waitForNewMessage(){
        messageWait.until(ExpectedConditions.numberOfElementsToBeMoreThan(
                By.xpath("(//div[@body and @data-tooltip-position='left'])"),
                getNumberOfMessagesDisplayed()));
    }

    public int getNumberOfMessagesDisplayed(){
        return webDriver.findElements(By.xpath("(//div[@body and @data-tooltip-position='left'])")).size();
    }

}

