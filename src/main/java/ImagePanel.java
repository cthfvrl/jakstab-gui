import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.image.BufferedImage;

// TODO: repaint? (Linux)
public class ImagePanel extends JPanel {
    private final BufferedImage image;
    private Image imageToShow;

    private JSlider slider;
    private JLabel label;

    private final double zoomDiffFactor = 0.2;
    private double factor;

    ImagePanel() {
        image = null;
        imageToShow = null;
        factor = 1;
    }

    ImagePanel(Image img, JSlider slider, JLabel label) {
        image = imageCopy(img);
        imageToShow = imageCopy(img);
        setSize(img);
        factor = 1;
        this.slider = slider;
        this.label = label;
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

    boolean zoomIn() {
        if (image == null)
            return false;

        zoomRecalc(factor + zoomDiffFactor);
        return true;
    }

    boolean zoomOut() {
        if (image == null || factor <= zoomDiffFactor)
            return false;

        double zoomFactor = factor - zoomDiffFactor;
        int width = (int) (image.getWidth(null) * (zoomFactor));
        int height = (int) (image.getHeight(null) * (zoomFactor));
        if (width != 0 && height != 0) {
            zoomRecalc(zoomFactor);
            return true;
        }
        return false;
    }

    boolean zoom(ChangeEvent e) {
        slider = (JSlider) e.getSource();
        int value = slider.getValue();

        if (value <= 50)
            return zoom((double) value / 50.0);
        else
            return zoom(((value - 50) / 10.0) + 1.0);
    }

    private boolean zoom(double zoomFactor) {
        int width = (int) (image.getWidth(null) * zoomFactor);
        int height = (int) (image.getHeight(null) * zoomFactor);
        if (width != 0 && height != 0) {
            imageToShow = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            setSize(imageToShow);
            factor = zoomFactor;
            labelPercentRecalc();

            return true;
        }
        return false;
    }

    private void zoomRecalc(double zoomFactor) {
        if (zoomFactor <= 1.0)
            slider.setValue((int) (zoomFactor * 50.0));
        else
            slider.setValue((int) ((zoomFactor - 1.0) * 10.0 + 50.0));
    }

    // TODO: prevent changing label size
    private void labelPercentRecalc() {
        int value = (int) (factor * 100.0);
        if (value < 100)
            label.setText(" " + value + "%");
        else
            label.setText(value + "%");
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
