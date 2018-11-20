package bot.utils;

public interface XPATHS {
    //region Login
    String COOKIES_CLOSE = "//*[@id='u_0_i']";
    String LOGIN_EMAIL = "//input[@id='email']";
    String LOGIN_PASS = "//input[@id='pass']";
    String LOGIN_BUTTON = "//button[@id='loginbutton']";
    //endregion

    //region Inputs
    String INPUT_FIELD = "//div[@class='notranslate _5rpu']";
    //endregion

    //region Loading
    String MESSAGE_CONTAINER = "//div[@aria-label='Messages']";

    String SETTINGS_COG = "//a[contains(@class,'_2fug')]";
    String SETTINGS_DROPDOWN = "//span[text()='Settings']";
    String SETTINGS_DONE = "//button[@class='_3quh _30yy _2t_ _5ixy']";
    /**
     * <br><br>RETURNS -> .<strong>text</strong>
     */
    String MY_REAL_NAME = "//div[@class='_6ct9']";
    //endregion

    //region Messages
    String MESSAGE_GROUPS = "//div[@id='js_1']/div";
    String MESSAGES_ALL = MESSAGE_GROUPS + "/div/div/div[contains(@class,'_o46')]";
    String MESSAGES_OTHERS = MESSAGE_GROUPS + "/div/div/div[contains(@class,'_29_7')]";
    String MESSAGES_OTHERS_RECENT = "(" + MESSAGES_OTHERS + ")[last()]";
    String MESSAGES_MINE = MESSAGE_GROUPS + "/div/div/div[contains(@class,'_nd_')]";
    String MESSAGES_MINE_RECENT = "(" + MESSAGES_MINE + ")[last()]";

    //region Requires MESSAGE ELEMENT
    /**
     * REQUIRES <strong>MESSAGE ELEMENT</strong>
     * <br><br>RETURNS -> @<strong>src</strong>
     */
    String MESSAGE_IMAGE = "./div/div/div/div/img[@src]";
    /**
     * REQUIRES <strong>MESSAGE ELEMENT</strong>
     * <br><br>RETURNS -> @<strong>aria-label</strong>
     */
    String MESSAGE_TEXT = "./div/div[@aria-label]";
    /**
     * REQUIRES <strong>MESSAGE ELEMENT</strong>
     * <br><br>RETURNS -> @<strong>aria-label</strong>
     */
    String MESSAGE_SENDER_NICKNAME = "./../h5[@aria-label]";
    /**
     * REQUIRES <strong>MESSAGE ELEMENT</strong>
     * <br><br>RETURNS -> @<strong>data-tooltip-content</strong>
     */
    String MESSAGE_SENDER_REAL_NAME = "./../../div/*/div[@data-tooltip-content]";
    //endregion
}
