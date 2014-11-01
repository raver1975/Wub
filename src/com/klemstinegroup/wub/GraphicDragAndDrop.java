package com.klemstinegroup.wub;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
 
public class GraphicDragAndDrop extends JPanel {
    Rectangle rect = new Rectangle(100,100,150,75);
 
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(Color.blue);
        g2.draw(rect);
    }
 
    public void setRect(int x, int y) {
        rect.setLocation(x, y);
        repaint();
    }
 
    public static void main(String[] args) {
        GraphicDragAndDrop test = new GraphicDragAndDrop();
        new GraphicDragController(test);
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add(test);
        f.setSize(400,400);
        f.setLocation(100,100);
        f.setVisible(true);
    }
}
 
class GraphicDragController extends MouseInputAdapter {
    GraphicDragAndDrop component;
    Point offset = new Point();
    boolean dragging = false;
 
    public GraphicDragController(GraphicDragAndDrop gdad) {
        component = gdad;
        component.addMouseListener(this);
        component.addMouseMotionListener(this);
    }
 
    public void mousePressed(MouseEvent e) {
        Point p = e.getPoint();
        Rectangle r = component.rect;
        if(r.contains(p)) {
            offset.x = p.x - r.x;
            offset.y = p.y - r.y;
            dragging = true;
        }
    }
 
    public void mouseReleased(MouseEvent e) {
        dragging = false;
    }
 
    public void mouseDragged(MouseEvent e) {
        if(dragging) {
            int x = e.getX() - offset.x;
            int y = e.getY() - offset.y;
            component.setRect(x, y);
        }
    }
}