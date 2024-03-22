package streamviewer;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;


import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * A Swing video panel, created by rapidly changing the underlying BufferedImage
 */

public class VideoPanel extends JPanel  {

	/**
     * The last image read from the stream.
     */
    private BufferedImage bufferedImage = null;

    /**
     * Thread-Safe. Displays a new image in the panel.
     *
     * @param image the image to display
     */
    public void displayNewImage(BufferedImage image) {
    	/* Image resized - Warning CPU consumption - By default size is 640 x 480 */
    	/* Image tmpImg = image.getScaledInstance(1440, 1080, Image.SCALE_DEFAULT);
    	BufferedImage dImg = new BufferedImage(1440, 1080, BufferedImage.TYPE_INT_ARGB);
    	Graphics2D g2d = dImg.createGraphics();
    	g2d.drawImage(tmpImg,  0,  0,  null);
    	g2d.dispose();
    	this.bufferedImage = dImg; */
        /* Image not resized : */
        this.bufferedImage = image;
        SwingUtilities.invokeLater(this::repaint);
     }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(640, 480);
    	/* return new Dimension(1440, 1080); */
    }

    @Override
    public void paint(Graphics graphics) {
        if (this.bufferedImage != null) {
            graphics.drawImage(this.bufferedImage, 0, 0, null);
        }
    }

}
