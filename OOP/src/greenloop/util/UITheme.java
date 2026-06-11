package greenloop.util;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;

public class UITheme {

    
    public static final Color DARK_GREEN   = new Color(27,  94,  32);
    public static final Color MID_GREEN    = new Color(56, 142,  60);
    public static final Color LIGHT_GREEN  = new Color(200, 230, 201);
    public static final Color ACCENT_GREEN = new Color(46, 125,  50);
    public static final Color RED_BTN      = new Color(198,  40,  40);
    public static final Color BLUE_BTN     = new Color(25, 118, 210);
    public static final Color GRAY_BTN     = new Color(117, 117, 117);
    public static final Color WHITE        = Color.WHITE;
    public static final Color BG           = new Color(245, 248, 245);
    public static final Color TABLE_HDR    = new Color(224, 242, 224);
    public static final Color TABLE_SEL    = new Color(165, 214, 167);
    public static final Color LOW_STOCK    = new Color(255, 152,   0);
    public static final Color OUT_STOCK    = new Color(211,  47,  47);
    public static final Color IN_STOCK     = new Color(46, 125,  50);
    public static final Color ORANGE_BTN   = new Color(230, 120,  20);
    public static final Color CARD_BG      = Color.WHITE;
    public static final Color BORDER_COLOR = new Color(220, 230, 220);

    
    public static final Font FONT_TITLE    = new Font("Segoe UI", Font.BOLD,  22);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD,  14);
    public static final Font FONT_BODY     = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL    = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_NAV      = new Font("Segoe UI", Font.PLAIN, 13);

    private UITheme() {}

   
    public static JButton makeButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(120, 36));
        btn.setOpaque(true);

        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            Color original = bg;
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(bg.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(original);
            }
        });
        return btn;
    }

    
    public static JTextField makeField() {
        JTextField tf = new JTextField();
        tf.setFont(FONT_BODY);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        tf.setPreferredSize(new Dimension(200, 34));
        return tf;
    }

    
    public static JLabel makeLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_BODY);
        return lbl;
    }

    
    public static JPanel card(String title, JPanel inner) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_COLOR), title,
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                FONT_SUBTITLE, DARK_GREEN),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        card.add(inner, BorderLayout.CENTER);
        return card;
    }

 
    public static JPanel shadowCard() {
        JPanel card = new JPanel();
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)));
        return card;
    }

    
    public static JPanel pageHeader(String title, String subtitle) {
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setBackground(DARK_GREEN);
        hdr.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        JLabel lbl = new JLabel(title);
        lbl.setFont(FONT_TITLE);
        lbl.setForeground(Color.WHITE);

        JLabel sub = new JLabel(subtitle);
        sub.setFont(FONT_SMALL);
        sub.setForeground(new Color(200, 230, 200));

        JPanel text = new JPanel(new GridLayout(2, 1, 0, 2));
        text.setOpaque(false);
        text.add(lbl);
        text.add(sub);
        hdr.add(text, BorderLayout.WEST);

       
        JLabel date = new JLabel(
            new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()) + "  ");
        date.setFont(FONT_SMALL);
        date.setForeground(new Color(200, 230, 200));
        hdr.add(date, BorderLayout.EAST);

        return hdr;
    }

  
    public static void styleTable(javax.swing.JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(32);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(TABLE_SEL);
        table.setSelectionForeground(DARK_GREEN);
        table.getTableHeader().setFont(FONT_SUBTITLE);
        table.getTableHeader().setBackground(TABLE_HDR);
        table.getTableHeader().setForeground(DARK_GREEN);
        table.getTableHeader().setReorderingAllowed(false);
       
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    javax.swing.JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                if (!sel) {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 250, 245));
                }
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return this;
            }
        });
    }

 
    public static JLabel statusBadge(String text) {
        JLabel lbl = new JLabel(" " + text + " ");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setOpaque(true);
        lbl.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        switch (text.toLowerCase()) {
            case "active":
            case "available":
            case "delivered":  lbl.setBackground(new Color(200,230,200)); lbl.setForeground(IN_STOCK);   break;
            case "inactive":
            case "cancelled":  lbl.setBackground(new Color(255,205,205)); lbl.setForeground(OUT_STOCK);  break;
            case "pending":
            case "assigned":   lbl.setBackground(new Color(255,236,179)); lbl.setForeground(new Color(180,100,0)); break;
            default:           lbl.setBackground(new Color(220,220,220)); lbl.setForeground(Color.DARK_GRAY);
        }
        return lbl;
    }
}
