package X.TreeDrawer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class DrawerFrame extends JFrame {

    public DrawerFrame(JPanel panel) {
        setSize(1000, 800);
        Toolkit kt = Toolkit.getDefaultToolkit();
        Dimension d = kt.getScreenSize();
        int screenHeight = d.height;
        int screenWidth = d.width;
        setTitle("The X Compiler AST");
        setSize((screenWidth * 9) / 10, (screenHeight * 9) / 10);
        setLocation(screenWidth / 20, screenHeight / 20);

        addWindowListener(
                new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        System.exit(0);
                    }
                }
        );
        Container contentPane = getContentPane();
        contentPane.add(new JScrollPane(panel));

    }

}
