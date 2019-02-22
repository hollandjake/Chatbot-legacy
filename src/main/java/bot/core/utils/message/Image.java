package bot.core.utils.message;

import bot.core.Chatbot;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.net.ssl.SSLHandshakeException;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static bot.core.utils.CONSTANTS.*;
import static bot.core.utils.XPATHS.MESSAGE_IMAGE;

public class Image implements MessageComponent, Transferable {
	private final int ID;
	private final String url;
	private final java.awt.Image image;

	public Image(String url) throws SSLHandshakeException {
		this.ID = 0;
		this.url = url;
		this.image = fromUrl(url);
	}

	public Image(ResultSet resultSet) {
		int ID = 0;
		String url = null;
		try {
			ID = resultSet.getInt("I_ID");
			url = resultSet.getString("I_url");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		this.ID = ID;
		this.url = url;
		BufferedImage image;
		try {
			image = fromUrl(this.url);
		} catch (SSLHandshakeException e) {
			image = null;
		}
		this.image = image;
	}

	private static BufferedImage fromUrl(String url) throws SSLHandshakeException {
		if (url == null || url.isEmpty()) {
			return null;
		}

		ImageInputStream imageInputStream = null;

		try {
			URL U = new URL(url);
			URLConnection urlConnection = U.openConnection();
			urlConnection.connect();

			imageInputStream = ImageIO.createImageInputStream(urlConnection.getInputStream());
			BufferedImage image = ImageIO.read(imageInputStream);

			if (image == null) {
				return null;
			}

			double size = urlConnection.getContentLength();

			//Scale image to fit in size
			double scaleFactor = Math.min(1, MAX_IMAGE_SIZE / size);
			int scaledWidth = (int) (image.getWidth() * scaleFactor);
			int scaledHeight = (int) (image.getHeight() * scaleFactor);

			if (scaledWidth <= 0 || scaledHeight <= 0) {
				return null;
			}
			java.awt.Image scaledImage = image.getScaledInstance(scaledWidth, scaledHeight, java.awt.Image.SCALE_SMOOTH);

			BufferedImage bufferedImage = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = bufferedImage.createGraphics();
			g.drawImage(scaledImage, 0, 0, null);
			g.dispose();
			return bufferedImage;
		} catch (IOException e) {
			e.printStackTrace();
			if (e instanceof SSLHandshakeException) {
				throw (SSLHandshakeException) e;
			}
			return null;
		} finally {
			try {
				if (imageInputStream != null) {
					imageInputStream.close();
				}
			} catch (IOException ignore) {
			}
		}
	}

	public static ArrayList<MessageComponent> fromElement(Chatbot chatbot, WebElement webElement) {
		List<WebElement> imageElements = webElement.findElements(By.xpath(MESSAGE_IMAGE));
		ArrayList<MessageComponent> imageComponents = new ArrayList<>();
		if (imageElements.isEmpty()) {
			return imageComponents;
		} else {
			for (WebElement imageElement : imageElements) {
				String imageUrl = imageElement.getAttribute("src");
				try {
					Image image = new Image(imageUrl);
					imageComponents.add(chatbot.saveImage(image));
				} catch (SSLHandshakeException e) {
					imageComponents.add(new Text(imageUrl));
				}
			}
			return imageComponents;
		}
	}

	//region Overrides
	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[]{DataFlavor.imageFlavor};
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return DataFlavor.imageFlavor.equals(flavor);
	}

	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
		if (!DataFlavor.imageFlavor.equals(flavor)) {
			throw new UnsupportedFlavorException(flavor);
		}
		return image;
	}

	@Override
	public boolean matches(Object obj) {
		if (obj instanceof String) {
			return ((String) obj).matches(url);
		} else if (obj instanceof Image) {
			return ((Image) obj).url.equals(url);
		} else {
			return false;
		}
	}

	@Override
	public void send(WebElement inputBox, WebDriverWait wait) {
		CLIPBOARD.setContents(this, null);
		inputBox.sendKeys(PASTE + Keys.ENTER);
	}

	@Override
	public String combine() {
		return IMAGE_SYMBOL + String.valueOf(ID);
	}

	public int getID() {
		return ID;
	}

	public String getUrl() {
		return url;
	}

	public java.awt.Image getImage() {
		return image;
	}
	//endregion


}