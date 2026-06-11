package greenloop.view;

import greenloop.controller.AuthController;
import greenloop.model.GlUser;
import greenloop.util.UITheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;

public class MainFrame extends JFrame {

    private JPanel  contentPanel;
    private GlUser  currentUser;
    private JButton activeBtn;

    private static final String[][] ADMIN_NAV = {
        {"Dashboard",           "🏠"},
        {"Products",            "📦"},
        {"Clients",             "👥"},
        {"Inventory",           "🗂️"},
        {"Orders",              "🛒"},
        {"Delivery Agents",     "🚚"},
        {"Delivery",            "📍"},
        {"Email Notifications", "✉️"},
        {"Reports",             "📊"},
        {"Settings",            "⚙️"},
        {"Logout",              "🚪"},
    };

    private static final String[][] EMPLOYEE_NAV = {
        {"Dashboard",           "🏠"},
        {"Orders",              "🛒"},
        {"Clients",             "👥"},
        {"Email Notifications", "✉️"},
        {"Settings",            "⚙️"},
        {"Logout",              "🚪"},
    };

    public MainFrame(GlUser user) {
        this.currentUser = user;
        setTitle("GreenLoop – Eco-Friendly Packaging Supply System");
        setSize(Toolkit.getDefaultToolkit().getScreenSize());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1024, 640));
        initUI();
        showPanel("Dashboard");
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.add(buildSidebar(), BorderLayout.WEST);
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(UITheme.BG);
        root.add(contentPanel, BorderLayout.CENTER);

        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(new Color(240, 245, 240));
        statusBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 220, 200)));
        statusBar.setPreferredSize(new Dimension(0, 24));
        JLabel statusLbl = new JLabel("  Status: Ready");
        statusLbl.setFont(UITheme.FONT_SMALL);
        JLabel userLbl = new JLabel("User: " +
            (currentUser != null ? currentUser.getFullName() : "Unknown") +
            (currentUser != null ? "  [" + currentUser.getRole() + "]" : "") + "  ");
        userLbl.setFont(UITheme.FONT_SMALL);
        statusBar.add(statusLbl, BorderLayout.WEST);
        statusBar.add(userLbl,   BorderLayout.EAST);
        root.add(statusBar, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private JPanel buildSidebar() {
        JPanel panel = new JPanel();
        panel.setBackground(UITheme.DARK_GREEN);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(230, 0));
        panel.add(buildLogoPanel());

        String[][] nav = isEmployee() ? EMPLOYEE_NAV : ADMIN_NAV;
        for (String[] item : nav) panel.add(createNavButton(item[0], item[1]));
        panel.add(Box.createVerticalGlue());

        JLabel ver = new JLabel("v1.0.0");
        ver.setForeground(new Color(150, 200, 150));
        ver.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        ver.setAlignmentX(Component.CENTER_ALIGNMENT);
        ver.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        panel.add(ver);
        return panel;
    }

    private JPanel buildLogoPanel() {
        JPanel logo = new JPanel();
        logo.setBackground(new Color(20, 70, 24));
        logo.setLayout(new BoxLayout(logo, BoxLayout.Y_AXIS));
        logo.setMaximumSize(new Dimension(230, 90));
        logo.setPreferredSize(new Dimension(230, 90));
        logo.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        URL imgURL = getClass().getResource("/greenloop/resources/logo.png");
        if (imgURL != null) {
            Image scaled = new ImageIcon(imgURL).getImage().getScaledInstance(42, 42, Image.SCALE_SMOOTH);
            JLabel img = new JLabel(new ImageIcon(scaled));
            img.setAlignmentX(Component.CENTER_ALIGNMENT);
            logo.add(img);
        } else {
            JLabel leaf = new JLabel("🌿");
            leaf.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
            leaf.setAlignmentX(Component.CENTER_ALIGNMENT);
            logo.add(leaf);
        }

        JLabel name = new JLabel("GreenLoop");
        name.setFont(new Font("Segoe UI", Font.BOLD, 16));
        name.setForeground(Color.WHITE);
        name.setAlignmentX(Component.CENTER_ALIGNMENT);
        logo.add(name);

        JLabel tag = new JLabel("Eco Packaging");
        tag.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        tag.setForeground(new Color(160, 210, 160));
        tag.setAlignmentX(Component.CENTER_ALIGNMENT);
        logo.add(tag);
        return logo;
    }

    private JButton createNavButton(String label, String icon) {
        JButton btn = new JButton(icon + "  " + label);
        btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        btn.setForeground(new Color(220, 255, 220));
        btn.setBackground(UITheme.DARK_GREEN);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(230, 44));
        btn.setPreferredSize(new Dimension(230, 44));
        btn.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 0));
        btn.setOpaque(true);

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { if (btn != activeBtn) btn.setBackground(UITheme.ACCENT_GREEN); }
            public void mouseExited(MouseEvent e)  { if (btn != activeBtn) btn.setBackground(UITheme.DARK_GREEN);   }
        });
        btn.addActionListener(e -> {
            if ("Logout".equals(label)) {
                int c = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?",
                    "Confirm Logout", JOptionPane.YES_NO_OPTION);
                if (c == JOptionPane.YES_OPTION) { AuthController.logout(); dispose(); new LoginFrame().setVisible(true); }
                return;
            }
            setActiveButton(btn);
            showPanel(label);
        });
        return btn;
    }

    private void setActiveButton(JButton btn) {
        if (activeBtn != null) { activeBtn.setBackground(UITheme.DARK_GREEN); activeBtn.setForeground(new Color(220,255,220)); }
        activeBtn = btn;
        btn.setBackground(UITheme.MID_GREEN);
        btn.setForeground(Color.WHITE);
    }

    private boolean isEmployee() {
        return currentUser != null && "employee".equalsIgnoreCase(currentUser.getRole());
    }

    public void showPanel(String name) {
        if (isEmployee()) {
            switch (name) {
                case "Dashboard": case "Orders": case "Clients":
                case "Email Notifications": case "Settings": break;
                default:
                    JOptionPane.showMessageDialog(this,
                        "Access denied.", "Access Restricted", JOptionPane.WARNING_MESSAGE);
                    return;
            }
        }
        contentPanel.removeAll();
        JPanel panel;
        switch (name) {
            case "Dashboard":           panel = new DashboardPanel();          break;
            case "Products":            panel = new ProductPanel();             break;
            case "Clients":             panel = new ClientPanel();              break;
            case "Inventory":           panel = new InventoryPanel();           break;
            case "Orders":              panel = new OrderPanel();               break;
            case "Delivery Agents":     panel = new DeliveryAgentPanel();       break;
            case "Delivery":            panel = new DeliveryPanel();            break;
            case "Email Notifications": panel = new EmailNotificationPanel();   break;
            case "Reports":             panel = new ReportsPanel();             break;
            case "Settings":            panel = new SettingsPanel(currentUser); break;
            default:                    panel = new DashboardPanel();
        }
        contentPanel.add(panel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }
}
