package bot.utils;

import bot.utils.message.MessageComponent;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import java.awt.datatransfer.StringSelection;
import java.sql.ResultSet;
import java.sql.SQLException;

import static bot.utils.CONSTANTS.*;

public class Human implements MessageComponent {
	//region Constants
	private final String name;
	private final String url;
	private final int ID;
	//endregion

	//region Constructors
	public Human(String name, String url, int id) {
		this.name = name;
		this.url = url;
		this.ID = id;
	}

	public Human(ResultSet resultSet) throws SQLException {
		this.ID = resultSet.getInt("H_ID");
		this.url = resultSet.getString("H_url");
		this.name = resultSet.getString("H_name");
	}
	//endregion

	//region Getters and Setters
	public String getName() {
		return name;
	}

	public String getUrl() {
		return url;
	}

	public int getID() {
		return ID;
	}
	//endregion

	public String toString() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Human human = (Human) o;

		return ID == human.ID;
	}

	@Override
	public void send(WebElement inputBox) {
		CLIPBOARD.setContents(new StringSelection(name), null);
		inputBox.sendKeys(PASTE + Keys.ENTER);
	}

	@Override
	public String combine() {
		return TAG_SYMBOL + name;
	}
}
