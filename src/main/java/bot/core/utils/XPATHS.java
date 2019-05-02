package bot.core.utils;

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
	String MESSAGES_ALL = "//div[contains(@class,'_o46')]";
	String MESSAGES_OTHERS = MESSAGES_ALL + "[contains(@class,'_29_7')][./../../div/a[@href]]";
	String MESSAGES_OTHERS_RECENT = "(" + MESSAGES_OTHERS + ")[last()]";
	String MESSAGES_MINE = MESSAGES_ALL + "[contains(@class,'_o46')]";

	/*
		(//div[contains(@class,'_o46')][contains(@class,'_29_7')])[last()]/div/div[@aria-label]/span
	 */

	//region Requires MESSAGE ELEMENT

	//region Sender
	/**
	 * REQUIRES <strong>MESSAGE ELEMENT</strong>
	 * <br><br>RETURNS -> @<strong>href</strong>
	 */
	String MESSAGE_SENDER_URL = "./../../div/a[@href]";
	/**
	 * REQUIRES <strong>MESSAGE ELEMENT</strong>
	 * <br><br>RETURNS -> @<strong>data-tooltip-content</strong>
	 */
	String MESSAGE_SENDER_NAME = MESSAGE_SENDER_URL + "/div[@data-tooltip-content]";
	//endregion

	/**
	 * REQUIRES <strong>MESSAGE ELEMENT</strong>
	 * <br><br>RETURNS -> @<strong>src</strong>
	 */
	String MESSAGE_IMAGE = "./div/div/div/div/img[@src]";
	/**
	 * REQUIRES <strong>MESSAGE ELEMENT</strong><br><br>
	 * This gives the entire text content as one string but can be subdivided by using {@link #MESSAGE_TEXT_COMPONENTS}
	 * or {@link #MESSAGE_TEXT_TAGS}
	 * <br><br>RETURNS -> @<strong>aria-label</strong>
	 */
	String MESSAGE_TEXT = "./div/div[@aria-label]";
	/**
	 * REQUIRES <strong>MESSAGE TEXT</strong>
	 */
	String MESSAGE_TEXT_COMPONENTS = "./span/*";
	/**
	 * REQUIRES <strong>MESSAGE TEXT</strong>
	 * <br><br>RETURNS -> @<strong>aria-label</strong>
	 */
	String MESSAGE_EMOJI = "./div/img[@aria-label]";
	/**
	 * REQUIRES <strong>MESSAGE TEXT COMPONENT</strong>
	 * <br><br>RETURNS -> @<strong>href</strong>
	 */
	String MESSAGE_TEXT_TAGS = "_ih-";

	//endregion

	//region Anti-facebook protection
	String CONTENT_NO_LONGER_AVAILABLE = "//a[contains(@class,'autofocus')]";
	//endregion
}
