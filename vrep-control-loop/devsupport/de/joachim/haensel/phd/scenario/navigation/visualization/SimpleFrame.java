package de.joachim.haensel.phd.scenario.navigation.visualization;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 *
 * @author Thanasis1101
 * @version 1.0
 */
public class SimpleFrame extends JFrame
{
    private ScrollableMovablePanel sMPanel;
    private JLabel infoLabel;

    public SimpleFrame()
    {
        initComponents();
    }

    private void initComponents()
    {
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(null);
        setTitle("Zoomable Panel");

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) screenSize.getWidth();
        int height = (int) screenSize.getHeight();

        sMPanel = new ScrollableMovablePanel();

        sMPanel.setBounds(50, 50, width - 100, height - 240);
        sMPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        this.add(sMPanel);
        sMPanel.setVisible(true);


        infoLabel = new JLabel("Roll to zoom. Click and drag to move.", JLabel.CENTER);
        infoLabel.setFont(new Font(infoLabel.getFont().getFontName(), Font.PLAIN, 26));
        infoLabel.setBounds(50, height - 180, width - 100, 80);
        this.add(infoLabel);
        infoLabel.setVisible(true);

    }

    public static void main(String args[])
    {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run()
            {
                SimpleFrame myFrame = new SimpleFrame();
                myFrame.setVisible(true);
            }
        });
    }
}