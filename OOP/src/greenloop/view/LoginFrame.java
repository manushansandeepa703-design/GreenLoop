package greenloop.view;

import greenloop.controller.AuthController;
import greenloop.database.DBConnection;
import greenloop.model.GlUser;
import greenloop.util.UITheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;

public class LoginFrame extends JFrame {

    private JTextField     txtUsername;
    private JPasswordField txtPassword;
    private JLabel         lblError;

    public LoginFrame() {
        setTitle("GreenLoop – Login");
        setSize(440, 520);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG);

        
        JPanel header = new JPanel(new GridBagLayout());
        header.setBackground(UITheme.DARK_GREEN);
        header.setPreferredSize(new Dimension(440, 140));

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);

        
        URL logoURL = getClass().getResource("/greenloop/resources/logo.png");
        if (logoURL != null) {
            ImageIcon raw = new ImageIcon(logoURL);
            Image scaled  = raw.getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH);
            JLabel logoLbl = new JLabel(new ImageIcon(scaled));
            logoLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
            inner.add(logoLbl);
            inner.add(Box.createVerticalStrut(6));
        } else {
            JLabel leaf = new JLabel("🌿");
            leaf.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
            leaf.setAlignmentX(Component.CENTER_ALIGNMENT);
            inner.add(leaf);
        }

        JLabel title = new JLabel("GreenLoop");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Eco-Friendly Packaging Supply System");
        sub.setFont(UITheme.FONT_SMALL);
        sub.setForeground(new Color(200, 230, 200));
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        inner.add(title);
        inner.add(Box.createVerticalStrut(4));
        inner.add(sub);
        header.add(inner);
        root.add(header, BorderLayout.NORTH);

        
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 230, 220), 1),
            BorderFactory.createEmptyBorder(30, 40, 30, 40)));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill    = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        gc.gridx   = 0;
        gc.insets  = new Insets(6, 0, 6, 0);

        
        JLabel lblUser = new JLabel("Username");
        lblUser.setFont(UITheme.FONT_SUBTITLE);
        lblUser.setForeground(UITheme.DARK_GREEN);
        gc.gridy = 0; card.add(lblUser, gc);

        txtUsername = new JTextField();
        txtUsername.setFont(UITheme.FONT_BODY);
        txtUsername.setPreferredSize(new Dimension(320, 40));
        txtUsername.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 220, 200), 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        gc.gridy = 1; card.add(txtUsername, gc);

        
        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(UITheme.FONT_SUBTITLE);
        lblPass.setForeground(UITheme.DARK_GREEN);
        gc.gridy = 2; card.add(lblPass, gc);

        txtPassword = new JPasswordField();
        txtPassword.setFont(UITheme.FONT_BODY);
        txtPassword.setPreferredSize(new Dimension(320, 40));
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 220, 200), 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        gc.gridy = 3; card.add(txtPassword, gc);

       
        lblError = new JLabel(" ");
        lblError.setForeground(UITheme.RED_BTN);
        lblError.setFont(UITheme.FONT_SMALL);
        gc.gridy = 4; card.add(lblError, gc);

        
        JButton btnLogin = new JButton("Login");
        btnLogin.setBackground(UITheme.MID_GREEN);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnLogin.setFocusPainted(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setOpaque(true);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.setPreferredSize(new Dimension(320, 44));
        btnLogin.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btnLogin.setBackground(UITheme.DARK_GREEN); }
            public void mouseExited(MouseEvent e)  { btnLogin.setBackground(UITheme.MID_GREEN);  }
        });
        gc.gridy = 5; card.add(btnLogin, gc);

        
        JPanel cardWrap = new JPanel(new GridBagLayout());
        cardWrap.setBackground(UITheme.BG);
        cardWrap.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        cardWrap.add(card);
        root.add(cardWrap, BorderLayout.CENTER);

        
        JLabel footer = new JLabel("© 2026 GreenLoop System", SwingConstants.CENTER);
        footer.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        footer.setForeground(new Color(160, 160, 160));
        footer.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);

        ActionListener loginAction = e -> doLogin();
        btnLogin.addActionListener(loginAction);
        txtPassword.addActionListener(loginAction);
        txtUsername.addActionListener(loginAction);
    }

    private void doLogin() {
        String user = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword());
        if (user.isEmpty() || pass.isEmpty()) {
            lblError.setText("Please enter username and password.");
            return;
        }
        if (DBConnection.getConnection() == null) {
            lblError.setText("Cannot connect to database.");
            return;
        }
        GlUser u = AuthController.login(user, pass);
        if (u != null) {
            dispose();
            new MainFrame(u).setVisible(true);
        } else {
            lblError.setText("Invalid username or password.");
            txtPassword.setText("");
        }
    }
}
