package org.gh;

import com.formdev.flatlaf.FlatDarkLaf;
import com.google.gson.Gson;
import javax.swing.*;
import java.awt.*;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LoginUi extends JFrame {
    private JPasswordField keyText;
    private JButton enterButton;
    private static final String KEY_FILE = "key.json";

    public LoginUi() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(true);
        setSize(315, 150);
        setLayout(new BorderLayout());
        setLookAndFeel();


        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(30, 30, 30));
        titlePanel.setLayout(new BorderLayout());

        JLabel title = new JLabel("Steam Gen", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        titlePanel.add(title, BorderLayout.CENTER);

        JButton closeButton = new JButton("X");
        closeButton.setBackground(new Color(200, 50, 50));
        closeButton.setForeground(Color.WHITE);
        closeButton.setBorderPainted(false);
        closeButton.addActionListener(event -> System.exit(0));

        closeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                closeButton.setBackground(new Color(180, 30, 30));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                closeButton.setBackground(new Color(200, 50, 50));
            }
        });

        titlePanel.add(closeButton, BorderLayout.EAST);


        add(titlePanel, BorderLayout.NORTH);

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setPaint(new Color(30, 30, 30));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };


        add(panel);
        placeComponents(panel);
        setLocationRelativeTo(null);
    }



    private void setLookAndFeel() {
        try {

            UIManager.setLookAndFeel(new FlatDarkLaf());
            FlatDarkLaf.install();
            JFrame.setDefaultLookAndFeelDecorated(true);
            UIManager.put( "TitlePane.unifiedBackground", true );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void placeComponents(JPanel panel) {
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel keyLabel = new JLabel("Key:");
        keyLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel.add(keyLabel, gbc);

        keyText = new JPasswordField(20);
        keyText.setBackground(new Color(50, 50, 50));
        keyText.setForeground(Color.WHITE);
        keyText.setCaretColor(Color.WHITE);
        gbc.gridx = 1;
        panel.add(keyText, gbc);


        JPanel buttonsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints buttonsGbc = new GridBagConstraints();


        enterButton = new JButton("Enter");
        enterButton.setBackground(new Color(200, 50, 50));
        enterButton.setForeground(Color.WHITE);
        buttonsPanel.add(enterButton, buttonsGbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(buttonsPanel, gbc);
    }

    public void setupUi(Application app) {
        enterButton.addActionListener(e -> {
            char[] keyChars = keyText.getPassword();
            String key = new String(keyChars);

            saveKeyToJson(key);

            app.validateKey(key, () -> {
                MainUi mainUi = new MainUi();
                mainUi.setVisible(true);

                this.dispose();
            });

        });

        String savedKey = readKeyFromJson();
        if (savedKey != null) {
            keyText.setText(savedKey);
        }
    }

    private void saveKeyToJson(String key) {
        Map<String, String> keyMap = new HashMap<>();
        keyMap.put("key", key);

        try (FileWriter writer = new FileWriter(KEY_FILE)) {
            new Gson().toJson(keyMap, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String readKeyFromJson() {
        try (FileReader reader = new FileReader(KEY_FILE)) {
            Map<String, String> keyMap = new Gson().fromJson(reader, Map.class);
            return keyMap.get("key");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

}