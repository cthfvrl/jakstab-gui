import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

// TODO: repaint? (Linux)
public class ImagePanel extends JPanel {
    private final BufferedImage image;
    private Image imageToShow;

    private final double zoomFactor = 0.2;
    private double widthFactor;
    private double heightFactor;

    public ImagePanel() {
        image = null;
        imageToShow = null;
        widthFactor = 1;
        heightFactor = 1;
    }

    public ImagePanel(Image img) {
        image = imageCopy(img);
        imageToShow = imageCopy(img);
        setSize(img);
        widthFactor = 1;
        heightFactor = 1;
    }

    private BufferedImage imageCopy(Image img) {
        BufferedImage newImage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics g = newImage.getGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        return newImage;
    }

    public void paintComponent(Graphics g) {
        g.drawImage(imageToShow, 0, 0, null);
    }

    public void zoomIn() {
        if (image == null) return;

        widthFactor += zoomFactor;
        heightFactor += zoomFactor;
        int width = (int) (image.getWidth(null) * widthFactor);
        int height = (int) (image.getHeight(null) * heightFactor);
        imageToShow = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        setSize(imageToShow);
    }

    public void zoomOut() {
        if (image == null)
            return;

        int width = (int) (image.getWidth(null) * (widthFactor - zoomFactor));
        int height = (int) (image.getHeight(null) * (heightFactor - zoomFactor));
        if (width != 0 && height != 0) {
            imageToShow = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            setSize(imageToShow);
            widthFactor -= zoomFactor;
            heightFactor -= zoomFactor;
        }
    }

    private void setSize(Image img) {
        Dimension size = new Dimension(img.getWidth(null), img.getHeight(null));
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);
        setSize(size);
        setLayout(null);
    }
}
