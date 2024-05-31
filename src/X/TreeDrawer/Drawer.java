package X.TreeDrawer;

import X.Nodes.AST;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Drawer {

    private DrawerFrame frame;
    private DrawerPanel panel;

    private AST ast;
    private DrawingTree theDrawing;

    public void draw(AST A) {
        ast = A;
        panel = new DrawerPanel(this);
        panel.setBackground(Color.white);

        frame = new DrawerFrame(panel);

        Font font = new Font("Lucida Bright", Font.BOLD, 20);
        frame.setFont(font);

        FontMetrics fontMetrics = frame.getFontMetrics(font);

        LayoutVisitor layout = new LayoutVisitor(fontMetrics);
        layout.enableDebugging();

        theDrawing = (DrawingTree) ast.visit(layout, null);
        theDrawing.position(new Point(500, 10));
        frame.setVisible(true);
    }

    public void paintAST(Graphics g) {
        g.setColor(Color.white);
        g.setColor(panel.getBackground());
        Dimension d = panel.getSize();
        g.fillRect(0, 0, d.width, d.height);

        if (theDrawing != null) {
            theDrawing.paint(g);
        }
    }


}