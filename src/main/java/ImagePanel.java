import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.image.BufferedImage;

// TODO: repaint? (Linux)
public class ImagePanel extends JPanel {
    private final BufferedImage image;
    private Image imageToShow;
    private JSlider slider;

    private final double zoomFactor = 0.2;
    private double factor;

    public ImagePanel() {
        image = null;
        imageToShow = null;
        factor = 1;
    }

    public ImagePanel(Image img, JSlider slider) {
        image = imageCopy(img);
        imageToShow = imageCopy(img);
        setSize(img);
        factor = 1;
        slider.setValue((slider.getMaximum() - slider.getMinimum()) / 2);
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

    public boolean zoomIn() {
        if (image == null)
            return false;

        double oldFactor = factor;
        factor += zoomFactor;

        int width = (int) (image.getWidth(null) * factor);
        int height = (int) (image.getHeight(null) * factor);
        imageToShow = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        setSize(imageToShow);

        zoomRecalc(oldFactor);

        return true;
    }

    public boolean zoomOut() {
        if (image == null || factor <= zoomFactor)
            return false;

        int width = (int) (image.getWidth(null) * (factor - zoomFactor));
        int height = (int) (image.getHeight(null) * (factor - zoomFactor));
        if (width != 0 && height != 0) {
            imageToShow = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            setSize(imageToShow);

            double oldFactor = factor;
            factor -= zoomFactor;
            zoomRecalc(oldFactor);

            return true;
        }
        return false;
    }

    // TODO: logarithmic slider
    public boolean zoom(ChangeEvent e) {
        slider = (JSlider) e.getSource();

        int value = slider.getValue();
        int max = slider.getMaximum();
        int min = slider.getMinimum();

        return zoom((double) value / (max - min) * 2.0);
    }

    public boolean zoom(double zoomFactor) {
        int width = (int) (image.getWidth(null) * zoomFactor);
        int height = (int) (image.getHeight(null) * zoomFactor);
        if (width != 0 && height != 0) {
            imageToShow = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            setSize(imageToShow);
            factor = zoomFactor;
            return true;
        }
        return false;
    }

    private void zoomRecalc(double oldFactor) {
        double diff = (factor - oldFactor) / 2.0;
        if (diff == 0.0) return;

        int value = slider.getValue();
        int max = slider.getMaximum();
        int min = slider.getMinimum();

        value = value + (int) (diff * (max - min));

        slider.setValue(value);
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
