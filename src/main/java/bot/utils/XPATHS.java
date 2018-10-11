package bot.utils;

public interface XPATHS {
    //region Login
    String LOGIN_EMAIL = "//input[@id='email']";
    String LOGIN_PASS = "//input[@id='pass']";
    String LOGIN_BUTTON = "//button[@id='loginbutton']";
    //endregion

    //region Inputs
    String INPUT_FIELD = "//div[@class='notranslate _5rpu']";
    //endregion

    //region Loading
    String MESSAGE_CONTAINER = "//div[@aria-label='Messages']";

    /**
     * REQUIRES <strong>post-processing to find "uid":"&lt;uid&gt;"</strong>
     */
    String MY_USER_ID = "//script[starts-with(text(),'requireLazy([\"ix\"],function(ix){ix.add({\"496757\"')]";
    //endregion

    //region Messages
    String MESSAGES = "//div[div/div[contains(@class,'ui9')]]";
    String MESSAGES_ALL = MESSAGES + "/div[contains(@class,'_o46')]";
    String MESSAGES_OTHERS = MESSAGES + "/div[contains(@class,'_29_7')]";
    String MESSAGES_OTHERS_RECENT = "(" + MESSAGES_OTHERS + ")[last()]";
    String MESSAGES_MINE = MESSAGES + "/div[contains(@class,'_nd_')]";
    String MESSAGES_MINE_RECENT = "(" + MESSAGES_MINE + ")[last()]";

    //region Requires MESSAGE ELEMENT
    /**
     * REQUIRES <strong>MESSAGE ELEMENT</strong>
     * <br><br>RETURNS -> @<strong>body</strong>
     */
    String MESSAGE_TEXT = "./div[@body]";
    /**
     * REQUIRES <strong>MESSAGE ELEMENT</strong>
     * <br><br>RETURNS -> @<strong>aria-label</strong>
     */
    String MESSAGE_SENDER_NICKNAME = "./../../div/h5[@aria-label]";
    /**
     * REQUIRES <strong>MESSAGE ELEMENT</strong>
     * <br><br>RETURNS -> @<strong>data-tooltip-content</strong>
     */
    String MESSAGE_SENDER_REAL_NAME = "./../../div/*/div[@data-tooltip-content]";
    /**
     * REQUIRES <strong>MESSAGE ELEMENT</strong>
     * <br><br>RETURNS -> @<strong>participants</strong>
     */
    String MESSAGE_SENDER_ID = "./div[@participants]";
    //endregion
    //endregion
}
