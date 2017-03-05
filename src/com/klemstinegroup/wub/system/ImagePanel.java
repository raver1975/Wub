package com.klemstinegroup.wub.system;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

public class ImagePanel extends JPanel{

    private BufferedImage image;

    public ImagePanel() {
//        super();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(image!=null)g.drawImage(getImage(), 0, 0, null); // see javadoc for more info on the parameters
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
        invalidate();
    }
}