import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

// TODO: repaint? (Linux)
public class ImagePanel extends JPanel {
    private final BufferedImage image;
    private Image imageToShow;

    private final double zoomFactor = 0.2;
    private double factor;

    public ImagePanel() {
        image = null;
        imageToShow = null;
        factor = 1;
    }

    public ImagePanel(Image img) {
        image = imageCopy(img);
        imageToShow = imageCopy(img);
        setSize(img);
        factor = 1;
    }

    public double getFactor() {
        if (image == null)
            return -1.0;
        return factor;
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

    public double zoomIn() {
        if (image == null)
            return -1.0;

        factor += zoomFactor;
        int width = (int) (image.getWidth(null) * factor);
        int height = (int) (image.getHeight(null) * factor);
        imageToShow = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        setSize(imageToShow);
        return factor;
    }

    public double zoomOut() {
        if (image == null)
            return -1.0;

        if(factor <= zoomFactor)
            return factor;
        
        int width = (int) (image.getWidth(null) * (factor - zoomFactor));
        int height = (int) (image.getHeight(null) * (factor - zoomFactor));
        if (width != 0 && height != 0) {
            imageToShow = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            setSize(imageToShow);
            factor -= zoomFactor;
        }
        return factor;
    }

    public void zoom(double zoomFactor) {
        int width = (int) (image.getWidth(null) * zoomFactor);
        int height = (int) (image.getHeight(null) * zoomFactor);
        if (width != 0 && height != 0) {
            imageToShow = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            setSize(imageToShow);
            factor = zoomFactor;
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
