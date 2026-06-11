package greenloop.view;

import greenloop.controller.*;
import greenloop.model.Order;
import greenloop.model.Stock;
import greenloop.util.UITheme;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class DashboardPanel extends JPanel {

    private final ProductController pc = new ProductController();
    private final ClientController  cc = new ClientController();
    private final OrderController   oc = new OrderController();
    private final StockController   sc = new StockController();

    public DashboardPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(UITheme.BG);
        initUI();
    }

    private void initUI() {
        add(buildTopBar(),  BorderLayout.NORTH);
        add(buildBody(),    BorderLayout.CENTER);
    }

    
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(UITheme.DARK_GREEN);
        bar.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

        
        JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
        left.setOpaque(false);
        JLabel title = new JLabel("Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        JLabel welcome = new JLabel("Welcome, " +
            (AuthController.getLoggedInUser() != null
                ? AuthController.getLoggedInUser().getFullName() : "Admin") + "!");
        welcome.setFont(UITheme.FONT_SMALL);
        welcome.setForeground(new Color(200, 230, 200));
        left.add(title); left.add(welcome);
        bar.add(left, BorderLayout.WEST);

        
        JLabel date = new JLabel(LocalDate.now().toString() + "  ");
        date.setFont(UITheme.FONT_SMALL);
        date.setForeground(new Color(200, 230, 200));
        bar.add(date, BorderLayout.EAST);

        return bar;
    }

    
    private JPanel buildBody() {
        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(UITheme.BG);
        body.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill   = GridBagConstraints.BOTH;
        gc.insets = new Insets(8, 8, 8, 8);

        
        LocalDate now = LocalDate.now();
        int    totalProducts = pc.getAllProducts().size();
        int    totalClients  = cc.getTotalClientCount();
        int    pendingOrders = oc.getPendingOrderCount();
        double revenue       = oc.getMonthlyRevenue(now.getMonthValue(), now.getYear());

        Object[][] stats = {
            {"📦", "Products",  String.valueOf(totalProducts),          "Total Products",  new Color(46, 125, 50)},
            {"👥", "Clients",   String.valueOf(totalClients),           "Total Clients",   new Color(106, 27, 154)},
            {"🛒", "Orders",    String.valueOf(pendingOrders),          "Pending Orders",  new Color(2, 136, 209)},
            {"💰", "Revenue",   String.format("Rs. %,.0f", revenue),   "This Month",      new Color(230, 120, 20)},
        };

        gc.gridy = 0; gc.weighty = 0.0;
        for (int i = 0; i < stats.length; i++) {
            gc.gridx = i; gc.weightx = 0.25; gc.gridwidth = 1;
            body.add(makeStatCard(
                (String) stats[i][0],
                (String) stats[i][1],
                (String) stats[i][2],
                (String) stats[i][3],
                (Color)  stats[i][4]
            ), gc);
        }

        
        gc.gridy = 1; gc.weighty = 1.0;

        gc.gridx = 0; gc.gridwidth = 2; gc.weightx = 0.55;
        body.add(buildLowStockPanel(), gc);

        gc.gridx = 2; gc.gridwidth = 1; gc.weightx = 0.30;
        body.add(buildRecentOrdersPanel(), gc);

        gc.gridx = 3; gc.weightx = 0.15;
        body.add(buildQuickActions(), gc);

        return body;
    }

    
    private JPanel makeStatCard(String icon, String heading,
                                String value, String label, Color color) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 230, 220), 1),
            BorderFactory.createEmptyBorder(18, 20, 18, 20)));

        
        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topRow.setOpaque(false);
        JLabel iconChip = new JLabel(" " + icon + " ");
        iconChip.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        iconChip.setOpaque(true);
        iconChip.setBackground(lighter(color, 0.85f));
        iconChip.setForeground(color);
        iconChip.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        topRow.add(iconChip);

        
        JLabel valLbl = new JLabel(value);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 30));
        valLbl.setForeground(color);

       
        JLabel labLbl = new JLabel(label);
        labLbl.setFont(UITheme.FONT_SMALL);
        labLbl.setForeground(new Color(130, 130, 130));

       
        JPanel accent = new JPanel();
        accent.setBackground(color);
        accent.setPreferredSize(new Dimension(0, 3));

        JPanel center = new JPanel(new GridLayout(2, 1, 0, 2));
        center.setOpaque(false);
        center.add(valLbl);
        center.add(labLbl);

        card.add(topRow,  BorderLayout.NORTH);
        card.add(center,  BorderLayout.CENTER);
        card.add(accent,  BorderLayout.SOUTH);

        return card;
    }

    
    private JPanel buildLowStockPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 230, 220), 1),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)));

        
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false);
        JLabel title = new JLabel("⚠  Low Stock Alerts");
        title.setFont(UITheme.FONT_SUBTITLE);
        title.setForeground(UITheme.RED_BTN);
        JLabel viewAll = new JLabel("<html><u style='color:#1b5e20'>View All</u></html>");
        viewAll.setFont(UITheme.FONT_SMALL);
        viewAll.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewAll.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) { navigateTo("Inventory"); }
        });
        hdr.add(title,   BorderLayout.WEST);
        hdr.add(viewAll, BorderLayout.EAST);
        p.add(hdr, BorderLayout.NORTH);

        
        String[] cols = {"Product", "Current Stock", "Reorder Level"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Stock s : sc.getLowStockItems())
            model.addRow(new Object[]{s.getProductName(), s.getQuantityOnHand(), s.getReorderLevel()});

        JTable tbl = new JTable(model);
        UITheme.styleTable(tbl);

        
        tbl.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (!sel) {
                    int qty = v instanceof Integer ? (Integer) v : 0;
                    setBackground(qty == 0 ? new Color(255, 205, 210)
                                           : new Color(255, 236, 179));
                    setForeground(qty == 0 ? UITheme.OUT_STOCK : UITheme.LOW_STOCK);
                }
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return this;
            }
        });

        JScrollPane sp = new JScrollPane(tbl);
        sp.setBorder(BorderFactory.createLineBorder(new Color(220, 230, 220)));
        sp.getViewport().setBackground(Color.WHITE);
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    
    private JPanel buildRecentOrdersPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 230, 220), 1),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)));

        JLabel title = new JLabel("🕐  Recent Orders");
        title.setFont(UITheme.FONT_SUBTITLE);
        p.add(title, BorderLayout.NORTH);

        String[] cols = {"Order ID", "Client", "Status", "Date"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Order o : oc.getRecentOrders(5)) {
            String date = o.getOrderDate() != null ? o.getOrderDate().substring(0, 10) : "";
            model.addRow(new Object[]{"ORD-" + o.getOrderId(), o.getClientName(), o.getStatus(), date});
        }

        JTable tbl = new JTable(model);
        UITheme.styleTable(tbl);

        
        tbl.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                String s = v != null ? v.toString() : "";
                if (!sel) {
                    switch (s.toLowerCase()) {
                        case "pending":   setBackground(new Color(255,236,179)); setForeground(new Color(180,100,0)); break;
                        case "delivered": setBackground(new Color(200,230,200)); setForeground(UITheme.IN_STOCK);    break;
                        case "cancelled": setBackground(new Color(255,205,210)); setForeground(UITheme.OUT_STOCK);   break;
                        default:          setBackground(row%2==0?Color.WHITE:new Color(245,250,245));
                                          setForeground(Color.DARK_GRAY);
                    }
                }
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return this;
            }
        });

        JScrollPane sp = new JScrollPane(tbl);
        sp.setBorder(BorderFactory.createLineBorder(new Color(220, 230, 220)));
        sp.getViewport().setBackground(Color.WHITE);
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    
    private JPanel buildQuickActions() {
        boolean isEmployee = AuthController.getLoggedInUser() != null &&
            "employee".equalsIgnoreCase(AuthController.getLoggedInUser().getRole());

        String[][] actions = isEmployee
            ? new String[][]{{"🛒  New Order", "Orders"}, {"👥  Add Client", "Clients"}, {"✉  Send Email", "Email Notifications"}}
            : new String[][]{{"🛒  New Order", "Orders"}, {"📦  Add Product", "Products"}, {"👥  Add Client", "Clients"}, {"🗂  Stock In", "Inventory"}};

        JPanel wrap = new JPanel(new BorderLayout(0, 10));
        wrap.setOpaque(false);

        JLabel title = new JLabel("Quick Actions");
        title.setFont(UITheme.FONT_SUBTITLE);
        wrap.add(title, BorderLayout.NORTH);

        JPanel btns = new JPanel(new GridLayout(actions.length, 1, 0, 10));
        btns.setOpaque(false);
        for (String[] entry : actions) {
            JButton btn = new JButton(entry[0]);
            btn.setBackground(UITheme.MID_GREEN);
            btn.setForeground(Color.WHITE);
            btn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 13));
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setOpaque(true);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            btn.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
            final String target = entry[1];
            btn.addActionListener(e -> navigateTo(target));
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(UITheme.DARK_GREEN); }
                public void mouseExited(java.awt.event.MouseEvent e)  { btn.setBackground(UITheme.MID_GREEN);  }
            });
            btns.add(btn);
        }
        wrap.add(btns, BorderLayout.CENTER);
        return wrap;
    }

    
    private void navigateTo(String panel) {
        Container p = getParent();
        while (p != null && !(p instanceof MainFrame)) p = p.getParent();
        if (p instanceof MainFrame) ((MainFrame) p).showPanel(panel);
    }

    
    private Color lighter(Color c, float factor) {
        return new Color(
            Math.min(255, (int)(c.getRed()   + (255 - c.getRed())   * factor)),
            Math.min(255, (int)(c.getGreen() + (255 - c.getGreen()) * factor)),
            Math.min(255, (int)(c.getBlue()  + (255 - c.getBlue())  * factor))
        );
    }
}
