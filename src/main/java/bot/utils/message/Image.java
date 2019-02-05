package bot.utils.message;

import org.openqa.selenium.WebElement;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.net.ssl.SSLHandshakeException;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.ResultSet;
import java.sql.SQLException;

import static bot.utils.CONSTANTS.*;

public class Image implements MessageComponent, Transferable {
	private final java.awt.Image image;
	private final String url;

	public Image(String url) throws SSLHandshakeException {
		this.url = url;
		this.image = fromUrl(url);
	}

	public Image(ResultSet resultSet) throws SSLHandshakeException {
		String url = null;
		try {
			url = resultSet.getString("I_url");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		this.url = url;
		this.image = fromUrl(this.url);
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
	public void send(WebElement inputBox) {
		CLIPBOARD.setContents(this, null);
		inputBox.sendKeys(PASTE);
	}

	@Override
	public String combine() {
		return "\uE000" + url;
	}
	//endregion


}