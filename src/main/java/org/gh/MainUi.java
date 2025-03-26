package org.gh;
import java.awt.event.*;
import com.formdev.flatlaf.extras.components.FlatTextField;
import java.awt.Desktop;
import java.net.URI;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.DefaultCaret;
import java.awt.*;

public class MainUi extends JFrame {
    private static final int TIMER_DELAY = 5;

    private static JCheckBox useEmailsTxtCheckBox;
    private static JTextField kopeechkaEmailPrefText;

    private static JCheckBox smsPvaCheckBox;
    private static JCheckBox fivesimCheckBox;
    private static JCheckBox smsHubCheckBox;
    private static JCheckBox customCountryCheckBox;
    private static JTextField customCountryText;

    int desiredLogoWidth = 30;
    int desiredLogoHeight = 30;
    private static JTextArea consoleTextArea;
    private static JPanel settingsPanel;
    private static JCheckBox proxyCheckBox;
    private static JTextField accountsText;
    private static JTextField usernameText;
    private static JTextField apiKeyText;
    private static JTextField tabsToRunText;
    private static JTextField operatorText;
    private static JCheckBox smsApiCheckBox;
    private static JTextField smsApiKeyText;
    private static JTextField countryText;

    ImageIcon logoIcon = new ImageIcon("logo.png");
    Image logo = logoIcon.getImage().getScaledInstance(desiredLogoWidth, desiredLogoHeight, Image.SCALE_SMOOTH);
    private Point logoPosition = new Point(100, 100);
    private Point logoDirection = new Point(2, 2);

    private static JLabel smsApiKeyLabel;
    private static JLabel countryLabel;
    private String titleText = "        Steam Generator          ";
    private Timer titleRollingTimer;

    private Point mouseClickPoint = null;

    public MainUi() {
        setUndecorated(true);


        UIManager.put("Button.background", new Color(200, 50, 50));
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Panel.background", new Color(30, 30, 30));
        UIManager.put("TextField.background", new Color(30, 30, 30));
        UIManager.put("TextArea.background", new Color(30, 30, 30));

        JPanel titlePanel = new JPanel(new BorderLayout());

        JPanel innerTitlePanel = new JPanel(new BorderLayout());

        JLabel title = new JLabel(titleText, SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        innerTitlePanel.add(title, BorderLayout.CENTER);



        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        Font buttonFont = new Font("Arial", Font.BOLD, 14);

        JButton minimizeButton = new JButton("-");
        minimizeButton.setFont(buttonFont);
        minimizeButton.setPreferredSize(new Dimension(25, 23));
        minimizeButton.addActionListener(event -> setExtendedState(JFrame.ICONIFIED));
        buttonPanel.add(minimizeButton);

        JButton closeButton = new JButton("X");
        closeButton.addActionListener(event -> {
            titleRollingTimer.stop();
            System.exit(0);
        });
        buttonPanel.add(closeButton);

        titlePanel.add(buttonPanel, BorderLayout.EAST);


        titlePanel.add(innerTitlePanel, BorderLayout.CENTER);

        titleRollingTimer = new Timer(300, e -> rollTitle(title));
        titleRollingTimer.start();

        int desiredWidth = 20;
        int desiredHeight = 20;

        class RoundIconButton extends JButton {
            public RoundIconButton(Icon icon) {
                super(icon);
                setOpaque(false);
                setFocusPainted(false);
                setBorderPainted(false);
                setContentAreaFilled(false);
                setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                super.paintComponent(g2);
                g2.dispose();
            }
        }

        JPanel buttonPanelLeft = new JPanel(new FlowLayout(FlowLayout.LEFT));

        ImageIcon discordIcon = resizeIcon(new ImageIcon("discord.png"), desiredWidth, desiredHeight);
        JButton discordButton = new RoundIconButton(discordIcon);
        discordButton.addActionListener(e -> {
            try {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI("https://discord.gg/2Cc4mpxQjW"));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        buttonPanelLeft.add(discordButton);

        ImageIcon sellixIcon = resizeIcon(new ImageIcon("sellix.png"), desiredWidth, desiredHeight);
        JButton sellixButton = new RoundIconButton(sellixIcon);
        sellixButton.addActionListener(e -> {
            try {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI("https://ghaccs.sellix.io/"));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        buttonPanelLeft.add(sellixButton);

        titlePanel.add(buttonPanelLeft, BorderLayout.WEST);


        JPanel mainPanel = new JPanel() {
            public void paint(Graphics g) {
                super.paint(g);

                g.drawImage(logo, logoPosition.x, logoPosition.y, null);

                logoPosition.x += logoDirection.x;
                logoPosition.y += logoDirection.y;

                if (logoPosition.x <= 0 || logoPosition.x + logo.getWidth(null) >= getWidth()) {
                    logoDirection.x = -logoDirection.x;
                }

                if (logoPosition.y <= 0 || logoPosition.y + logo.getHeight(null) >= getHeight()) {
                    logoDirection.y = -logoDirection.y;
                }
            }

        };


        Timer logoTimer = new Timer(TIMER_DELAY, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                repaint();
            }
        });
        logoTimer.start();

        setContentPane(mainPanel);
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(titlePanel, BorderLayout.NORTH);

        titlePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mouseClickPoint = e.getPoint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                mouseClickPoint = null;
            }
        });

        titlePanel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                Point newPoint = e.getLocationOnScreen();
                setLocation(newPoint.x - mouseClickPoint.x, newPoint.y - mouseClickPoint.y);
            }
        });

        setSize(525, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        add(panel, BorderLayout.CENTER);

        placeComponents(panel);

        Settings settings = Main.loadSettings();
        accountsText.setText(settings.getNumberOfAccounts());
        usernameText.setText(settings.getCustomUsername());
        apiKeyText.setText(settings.getApiKey());
        tabsToRunText.setText(settings.getTabsToRun());
        smsApiKeyText.setText(settings.getSmsApiKey());
        countryText.setText(settings.getCountry());

        proxyCheckBox.setSelected(settings.isUseProxies());
        useEmailsTxtCheckBox.setSelected(settings.isUseEmailsTxt());
        smsApiCheckBox.setSelected(settings.isUseSmsApi());
        smsPvaCheckBox.setSelected(settings.isUseSmspva());
        fivesimCheckBox.setSelected(settings.isUse5sim());
        smsHubCheckBox.setSelected(settings.isUseSmshub());
        operatorText.setText(settings.getCountryOperator());


    }

    private static void placeComponents(JPanel panel) {
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JPanel accountsPanel = createSection("Accounts");
        accountsPanel.setMaximumSize(new Dimension(100, 125));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.5;
        gbc.weighty = 0.5;
        gbc.insets = new Insets(2, 2, 2, 2);
        panel.add(accountsPanel, gbc);

        JPanel consolePanel = createSection("Console");
        consolePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        accountsPanel.setPreferredSize(new Dimension(100, 90));

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weighty = 0.5;
        panel.add(consolePanel, gbc);



    }
    private static void addComponentToPanel(JPanel panel, JLabel label, JComponent component, int x, int y, GridBagConstraints gbc) {
        label.setForeground(Color.WHITE);
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(label, gbc);

        component.setPreferredSize(new Dimension(100, 20));
        gbc.gridx = x+1;
        panel.add(component, gbc);
    }

    private static JPanel createSection(String title) {
        JPanel sectionPanel = new JPanel(new BorderLayout());
        sectionPanel.setBorder(new TitledBorder(new LineBorder(Color.WHITE) {
            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setStroke(new BasicStroke(1));
                g2d.drawRoundRect(x, y, width - 1, height - 1, 20, 20);
            }
        }, title, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Arial", Font.BOLD, 14), Color.WHITE));

        sectionPanel.setOpaque(false);

        switch (title) {
            case "Accounts" -> {
                accountsText = new FlatTextField();
                usernameText = new FlatTextField();
                apiKeyText = new FlatTextField();
                proxyCheckBox = new JCheckBox();
                tabsToRunText = new JTextField(5);
                smsApiCheckBox = new JCheckBox();
                smsPvaCheckBox = new JCheckBox();
                fivesimCheckBox = new JCheckBox();
                smsApiKeyText = new JTextField();
                countryText = new JTextField();
                smsApiKeyLabel = new JLabel("SMS API Key:");
                countryLabel = new JLabel("Country Code:");
                smsHubCheckBox = new JCheckBox();
                customCountryCheckBox = new JCheckBox();
                customCountryText = new JTextField();
                JLabel customCountryTextLabel = new JLabel("Custom Country Code:");
                useEmailsTxtCheckBox = new JCheckBox();


                customCountryTextLabel.setVisible(false);
                customCountryText.setVisible(false);


                JLabel operatorLabel = new JLabel("Operator Code:");
                operatorText = new JTextField();
                operatorLabel.setVisible(false);
                operatorText.setVisible(false);

                JPanel accountsPanel = new JPanel(new GridBagLayout());
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.insets = new Insets(2, 2, 2, 2);
                accountsPanel.setOpaque(false);
                addComponentToPanel(accountsPanel, new JLabel("Use Proxies?"), proxyCheckBox, 0, 0, gbc);
                addComponentToPanel(accountsPanel, new JLabel("Number of Accounts:"), accountsText, 0, 3, gbc);
                addComponentToPanel(accountsPanel, new JLabel("Threads to run at once:"), tabsToRunText, 0, 4, gbc);
                addComponentToPanel(accountsPanel, new JLabel("Custom Username:"), usernameText, 0, 5, gbc);
                addComponentToPanel(accountsPanel, new JLabel("Kopeechka API Key:"), apiKeyText, 0, 6, gbc);

                JLabel emailPrefLabel = new JLabel("Kopeechka Email Preference:");
                kopeechkaEmailPrefText = new JTextField(20);


                gbc.gridx = 0;
                gbc.gridy++;
                accountsPanel.add(emailPrefLabel, gbc);

                gbc.gridx = 1;
                accountsPanel.add(kopeechkaEmailPrefText, gbc);
                
                addComponentToPanel(accountsPanel, new JLabel("Use emails.txt?"), useEmailsTxtCheckBox, 0, 1, gbc);

                addComponentToPanel(accountsPanel, new JLabel("Use SMS API?"), smsApiCheckBox, 2, 0, gbc);
                addComponentToPanel(accountsPanel, new JLabel("SMS PVA:"), smsPvaCheckBox, 2, 1, gbc);
                addComponentToPanel(accountsPanel, new JLabel("5Sim:"), fivesimCheckBox, 2, 2, gbc);
                addComponentToPanel(accountsPanel, smsApiKeyLabel, smsApiKeyText, 2, 4, gbc);
                addComponentToPanel(accountsPanel, countryLabel, countryText, 2, 5, gbc);
                addComponentToPanel(accountsPanel, operatorLabel, operatorText, 2, 6, gbc);
                addComponentToPanel(accountsPanel, new JLabel("SMS HUB:"), smsHubCheckBox, 2, 3, gbc);
                addComponentToPanel(accountsPanel, new JLabel("Custom Country?"), customCountryCheckBox, 0, 2, gbc);
                addComponentToPanel(accountsPanel, customCountryTextLabel, customCountryText, 0, 8, gbc);



                customCountryCheckBox.addItemListener(new ItemListener() {
                    public void itemStateChanged(ItemEvent e) {
                        if (e.getStateChange() == ItemEvent.SELECTED) {
                            customCountryTextLabel.setVisible(true);
                            customCountryText.setVisible(true);
                        } else {
                            customCountryTextLabel.setVisible(false);
                            customCountryText.setVisible(false);
                        }
                    }
                });


                fivesimCheckBox.addItemListener(new ItemListener() {
                    public void itemStateChanged(ItemEvent e) {
                        if (e.getStateChange() == ItemEvent.SELECTED) {
                            smsApiKeyLabel.setVisible(true);
                            smsApiKeyText.setVisible(true);
                            countryLabel.setVisible(true);
                            countryText.setVisible(true);
                            operatorLabel.setVisible(true);
                            operatorText.setVisible(true);
                        } else {
                            operatorLabel.setVisible(false);
                            operatorText.setVisible(false);
                        }
                    }
                });


                JButton startButton = new JButton("Start");
                startButton.setBackground(new Color(200, 50, 50));
                startButton.setForeground(Color.WHITE);
                startButton.setPreferredSize(new Dimension(40, 20));
                gbc.gridx = 1;
                gbc.gridy = 11;
                gbc.gridwidth = 1;
                gbc.insets = new Insets(10, 0, 0, 5);
                gbc.anchor = GridBagConstraints.CENTER;
                accountsPanel.add(startButton, gbc);

                startButton.addActionListener(e -> {
                    Settings settings = new Settings();
                    settings.setNumberOfAccounts(accountsText.getText());
                    settings.setCustomUsername(usernameText.getText());
                    settings.setApiKey(apiKeyText.getText());
                    settings.setTabsToRun(tabsToRunText.getText());
                    settings.setSmsApiKey(smsApiKeyText.getText());
                    settings.setCountry(countryText.getText());

                    settings.setUseProxies(proxyCheckBox.isSelected());
                    settings.setUseEmailsTxt(useEmailsTxtCheckBox.isSelected());
                    settings.setCustomCountry(customCountryText.getText());
                    settings.setUseSmsApi(smsApiCheckBox.isSelected());
                    settings.setUseSmspva(smsPvaCheckBox.isSelected());
                    settings.setUse5sim(fivesimCheckBox.isSelected());
                    settings.setUseSmshub(smsHubCheckBox.isSelected());
                    settings.setCountryOperator(operatorText.getText());


                    Main.saveSettings(settings);
                    if (accountsText.getText().trim().isEmpty() || usernameText.getText().trim().isEmpty() || apiKeyText.getText().trim().isEmpty()) {
                        showErrorPrompt("Please fill in all required fields.");
                        return;
                    }

                    Main.onButtonClick(proxyCheckBox.isSelected(), accountsText.getText(), usernameText.getText(), tabsToRunText.getText());

                });


                JButton stopButton = new JButton("Stop");
                stopButton.setBackground(new Color(200, 50, 50));
                stopButton.setForeground(Color.WHITE);
                stopButton.setPreferredSize(new Dimension(40, 20));
                gbc.gridx = 2;
                accountsPanel.add(stopButton, gbc);
                stopButton.addActionListener(e -> {
                    Main.stopAllThreadsAndCloseTabs();
                });

                sectionPanel.add(accountsPanel, BorderLayout.NORTH);
            }


            case "Console" -> {
                consoleTextArea = new JTextArea();
                consoleTextArea.setEditable(false);
                DefaultCaret caret = (DefaultCaret)consoleTextArea.getCaret();


                caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
                JScrollPane scrollPane = new JScrollPane(consoleTextArea);
                scrollPane.setPreferredSize(new Dimension(300, 30));
                sectionPanel.add(scrollPane, BorderLayout.CENTER);
            }

        }

        return sectionPanel;
    }

    public static boolean isUseProxiesSelected() {
        return proxyCheckBox.isSelected();
    }

    public static String getNumberOfAccounts() {
        return accountsText.getText();
    }

    public static String getCustomUsername() {
        return usernameText.getText();
    }

    public static JTextArea getConsoleTextArea() {
        return consoleTextArea;
    }


    public static String getApiKey() {
        return apiKeyText.getText();
    }

    public static String getTabsToRun() {
        return tabsToRunText.getText();
    }

    public static boolean isSmsApiChecked() {
        return smsApiCheckBox.isSelected();
    }

    public static String getSmsApiKey() {
        return smsApiKeyText.getText();
    }

    public static String getCountry() {
        return countryText.getText();
    }
    private static void showErrorPrompt(String message) {
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    private void rollTitle(JLabel titleLabel) {
        titleText = titleText.charAt(titleText.length() - 1) + titleText.substring(0, titleText.length() - 1);
        titleLabel.setText(titleText);
    }

    public static boolean isSmsPvaChecked() {return smsPvaCheckBox.isSelected();}

    public static boolean is5simChecked() {return fivesimCheckBox.isSelected();}
    public static String getOperatorText() {
        return operatorText.getText();
    }
    public static boolean isSmsHubSelected() {
        return smsHubCheckBox.isSelected();
    }

    public static boolean isEmailsDotTxtSelected() {
        return useEmailsTxtCheckBox.isSelected();
    }

    public static String getCustomCountryCode() {
        return customCountryText.getText();
    }

    public static boolean isCustomCountrySelected() {
        return customCountryCheckBox.isSelected();
    }
    public static String getKopeechkaEmailPrefText() {
        return kopeechkaEmailPrefText.getText();
    }
    public ImageIcon resizeIcon(ImageIcon icon, int width, int height) {
        Image img = icon.getImage();
        Image resizedImage = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImage);
    }

}
