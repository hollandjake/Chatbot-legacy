package bot.utils;

public interface XPATHS {
    //LOGIN
    String LOGIN_EMAIL = "//input[@id='email']";
    String LOGIN_PASS = "//input[@id='pass']";
    String LOGIN_BUTTON = "//button[@id='loginbutton']";

    //Inputs
    String INPUT_FIELD = "//div[@class='notranslate _5rpu']";

    //Loading
    String MESSAGE_CONTAINER = "//div[@aria-label='Messages']";

    /**
     * REQUIRES <strong>post-processing to find "uid":"&lt;uid&gt;"</strong>
     */
    String MY_USER_ID = "//script[starts-with(text(),'requireLazy([\"ix\"],function(ix){ix.add({\"496757\"')]";

    //Messages
    String MESSAGES = "//div[div/div[contains(@class,'ui9')]]";
    String MESSAGES_ALL = MESSAGES + "/div[contains(@class,'_o46')]";
    String MESSAGES_OTHERS = MESSAGES + "/div[contains(@class,'_29_7')]";
    String MESSAGES_OTHERS_RECENT = "(" + MESSAGES_OTHERS + ")[last()]";
    String MESSAGES_MINE = MESSAGES + "/div[contains(@class,'_nd_')]";
    String MESSAGES_MINE_RECENT = "(" + MESSAGES_MINE + ")[last()]";


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

//    String messageXpath = "//div[@class='_41ud']";
//    String othersMessageXpath = "//div[@class='_41ud' and descendant::div[@data-tooltip-position='left']]";
//    String myMessageXpath = "//div[@class='_41ud' and descendant::div[@data-tooltip-position='right']]";
//
//    String messageSenderNickname = "//h5[@aria-label]";
//    String messageSenderRealName = "//parent::div/descendant::div[@class='_4ldz _1t_r' and @data-tooltip-content]";
//    String messageSenderId = "//descendant::div[@participants]";
//    String MESSAGE_BODY = "//descendant::div[@body]";
}
