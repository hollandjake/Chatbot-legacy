package bot.utils;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

class ImageTransferable implements Transferable {
    private Image image;

    ImageTransferable(Image image) {
        this.image = image;
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
    //endregion
}