package org.example.ui;

import javax.swing.*;
import java.awt.*;

public class Node extends JPanel {
    private static final Color NODE_COLOR = new Color(97,137,255);
    private  int x=0, y=0, num=0;
    private boolean selected = false;
    public Node(Interface ui, int num) {
        this.num = num;
        x = ui.getWidth()/2/2;
        y = ui.getHeight()/2/2;
        super.paintComponent(ui.getGraphics());

        Graphics2D g2d = (Graphics2D) ui.getGraphics();

        g2d.setColor(NODE_COLOR);

        g2d.fillOval(x, y, 50, 50);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 36));
        if(num < 10) {
            g2d.drawString(num + "", x + 15, y + 37);
        }else{
            g2d.drawString("" + num, x + 4, y + 37);
        }

    }

    public void hideNode(){
        setVisible(false);
    }

    public int getNum() {
        return num;
    }

    public  Node getNodeByIndex(int index){
        if(index == num){
            return this;
        }
        return null;
    }

}
