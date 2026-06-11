package greenloop.view;

import greenloop.controller.StockController;
import greenloop.controller.ProductController;
import greenloop.model.Stock;
import greenloop.model.Product;
import greenloop.util.UITheme;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class InventoryPanel extends JPanel {

    private StockController   sc = new StockController();
    private ProductController pc = new ProductController();

    private JTable            table;
    private DefaultTableModel tableModel;
    private JLabel            lblTotal, lblLow, lblOut, lblValue;

    public InventoryPanel() {
        setLayout(new BorderLayout(12,12));
        setBackground(UITheme.BG);
        setBorder(BorderFactory.createEmptyBorder(24,28,16,28));
        initUI();
        loadTable(sc.getAllStock());
        updateSummary();
    }

    private void initUI() {
        JLabel title = new JLabel("Stock / Inventory Management");
        title.setFont(UITheme.FONT_TITLE);
        add(title, BorderLayout.NORTH);

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        toolbar.setOpaque(false);
        JButton btnStockIn = UITheme.makeButton("Stock In",    UITheme.MID_GREEN);
        JButton btnAdjust  = UITheme.makeButton("Adjust Stock",UITheme.BLUE_BTN);
        // BUG FIX: "All Categories" now correctly reloads all stock instead of
        // passing "All Categories" as a search keyword to searchStock()
        JComboBox<String> cmbCat = new JComboBox<>(
            new String[]{"All Categories","Bags","Boxes","Wraps","Tape","Paper","Specialty"});
        cmbCat.setFont(UITheme.FONT_BODY);
        JTextField searchFld = UITheme.makeField(); searchFld.setToolTipText("Search product...");
        JButton searchBtn = UITheme.makeButton("Search", UITheme.DARK_GREEN);
        searchBtn.setPreferredSize(new Dimension(90,32));
        toolbar.add(btnStockIn); toolbar.add(btnAdjust); toolbar.add(cmbCat);
        toolbar.add(Box.createHorizontalStrut(20)); toolbar.add(searchFld); toolbar.add(searchBtn);

        // Table
        String[] cols = {"Product ID","Product Name","Category","Unit","Current Stock","Reorder Level","Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r,int c) { return false; }
        };
        table = new JTable(tableModel) {
            public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r,row,col);
                String status = (String)getModel().getValueAt(row,6);
                if ("Low Stock".equals(status))    c.setForeground(UITheme.LOW_STOCK);
                else if ("Out of Stock".equals(status)) c.setForeground(UITheme.OUT_STOCK);
                else c.setForeground(UITheme.IN_STOCK);
                if (isRowSelected(row)) c.setBackground(UITheme.TABLE_SEL);
                else c.setBackground(Color.WHITE);
                return c;
            }
        };
        UITheme.styleTable(table);
        table.getTableHeader().setFont(UITheme.FONT_SUBTITLE);
        table.getTableHeader().setBackground(UITheme.TABLE_HDR);

        JPanel tablePanel = new JPanel(new BorderLayout(0,6));
        tablePanel.setOpaque(false);
        tablePanel.add(toolbar, BorderLayout.NORTH);
        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);
        add(tablePanel, BorderLayout.CENTER);

        // Summary cards
        JPanel summary = new JPanel(new GridLayout(1,4,12,0));
        summary.setOpaque(false);
        summary.setBorder(BorderFactory.createEmptyBorder(12,0,0,0));
        lblTotal = new JLabel("0"); lblLow  = new JLabel("0");
        lblOut   = new JLabel("0"); lblValue = new JLabel("0");
        summary.add(makeSummaryCard("Total Products",  lblTotal, UITheme.MID_GREEN));
        summary.add(makeSummaryCard("Low Stock Items", lblLow,   UITheme.LOW_STOCK));
        summary.add(makeSummaryCard("Out of Stock",    lblOut,   UITheme.OUT_STOCK));
        summary.add(makeSummaryCard("Total Stock Value",lblValue,new Color(106,27,154)));
        add(summary, BorderLayout.SOUTH);

        // Events
        btnStockIn.addActionListener(e -> showStockInDialog());
        btnAdjust.addActionListener(e -> showAdjustDialog());
        searchBtn.addActionListener(e -> {
            String kw = searchFld.getText().trim();
            // BUG FIX: empty search loads all; non-empty searches
            if (kw.isEmpty()) loadTable(sc.getAllStock());
            else loadTable(sc.searchStock(kw));
        });
        searchFld.addActionListener(e -> searchBtn.doClick());

        // BUG FIX: category filter properly calls getAllStock() for "All Categories"
        cmbCat.addActionListener(e -> {
            String cat = (String)cmbCat.getSelectedItem();
            if ("All Categories".equals(cat)) loadTable(sc.getAllStock());
            else loadTable(sc.searchStock(cat));
        });
    }

    private JPanel makeSummaryCard(String label, JLabel valLbl, Color color) {
        JPanel card = new JPanel(new BorderLayout(0,4));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220,220,220)),
            BorderFactory.createEmptyBorder(12,16,12,16)));
        JLabel lbl = new JLabel(label); lbl.setFont(UITheme.FONT_SMALL); lbl.setForeground(color);
        valLbl.setFont(new Font("Segoe UI",Font.BOLD,26)); valLbl.setForeground(color);
        card.add(lbl, BorderLayout.NORTH); card.add(valLbl, BorderLayout.CENTER);
        return card;
    }

    private void loadTable(List<Stock> stocks) {
        tableModel.setRowCount(0);
        for (Stock s : stocks) {
            tableModel.addRow(new Object[]{
                "P-"+s.getProductId(), s.getProductName(), s.getCategory(),
                "Unit",
                s.getQuantityOnHand(), s.getReorderLevel(), s.getStatus()
            });
        }
    }

    private void updateSummary() {
        lblTotal.setText(String.valueOf(sc.getTotalProducts()));
        lblLow.setText(String.valueOf(sc.getLowStockCount()));
        lblOut.setText(String.valueOf(sc.getOutOfStockCount()));
        lblValue.setText(String.format("Rs. %,.0f", sc.getTotalStockValue()));
    }

    private void showStockInDialog() {
        JDialog dlg = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Stock In", true);
        dlg.setSize(360, 260); dlg.setLocationRelativeTo(this);
        JPanel p = new JPanel(new GridLayout(4,2,8,8));
        p.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));

        List<Product> products = pc.getAllProducts();
        JComboBox<Product> cmbProd = new JComboBox<>(products.toArray(new Product[0]));
        JTextField txtQty      = UITheme.makeField();
        JTextField txtSupplier = UITheme.makeField();
        JButton btnOk     = UITheme.makeButton("Stock In", UITheme.MID_GREEN);
        JButton btnCancel = UITheme.makeButton("Cancel",   UITheme.GRAY_BTN);

        p.add(new JLabel("Product:"));  p.add(cmbProd);
        p.add(new JLabel("Quantity:")); p.add(txtQty);
        p.add(new JLabel("Supplier:")); p.add(txtSupplier);
        p.add(btnCancel); p.add(btnOk);
        dlg.add(p);

        btnOk.addActionListener(e -> {
            try {
                Product pr = (Product)cmbProd.getSelectedItem();
                if (pr == null) { JOptionPane.showMessageDialog(dlg,"Select a product."); return; }
                int qty = Integer.parseInt(txtQty.getText().trim());
                // BUG FIX: validate quantity > 0
                if (qty <= 0) { JOptionPane.showMessageDialog(dlg,"Quantity must be greater than zero."); return; }
                sc.stockIn(pr.getProductId(), qty, txtSupplier.getText().trim());
                loadTable(sc.getAllStock()); updateSummary();
                dlg.dispose();
                JOptionPane.showMessageDialog(this, "Stock added successfully!");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg,"Enter a valid number for quantity.");
            }
        });
        btnCancel.addActionListener(e -> dlg.dispose());
        dlg.setVisible(true);
    }

    private void showAdjustDialog() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this,"Select a product first."); return; }

        int productId = Integer.parseInt(tableModel.getValueAt(row,0).toString().replace("P-",""));
        List<Stock> allStock = sc.getAllStock();
        Stock selectedStock = null;
        for (Stock s : allStock) {
            if (s.getProductId() == productId) { selectedStock = s; break; }
        }
        if (selectedStock == null) { JOptionPane.showMessageDialog(this,"Stock record not found."); return; }
        final int stockId = selectedStock.getStockId();

        JDialog dlg = new JDialog((Frame)SwingUtilities.getWindowAncestor(this),"Adjust Stock",true);
        dlg.setSize(320,220); dlg.setLocationRelativeTo(this);
        JPanel p = new JPanel(new GridLayout(4,2,8,8));
        p.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));
        JTextField txtQty     = UITheme.makeField(); txtQty.setText(tableModel.getValueAt(row,4).toString());
        JTextField txtReorder = UITheme.makeField(); txtReorder.setText(tableModel.getValueAt(row,5).toString());
        JButton btnOk     = UITheme.makeButton("Save",   UITheme.MID_GREEN);
        JButton btnCancel = UITheme.makeButton("Cancel", UITheme.GRAY_BTN);
        p.add(new JLabel("New Quantity:"));  p.add(txtQty);
        p.add(new JLabel("Reorder Level:")); p.add(txtReorder);
        p.add(btnCancel); p.add(btnOk);
        dlg.add(p);

        btnOk.addActionListener(e -> {
            try {
                int newQty   = Integer.parseInt(txtQty.getText().trim());
                int reorder  = Integer.parseInt(txtReorder.getText().trim());
                // BUG FIX: validate non-negative values
                if (newQty < 0 || reorder < 0) {
                    JOptionPane.showMessageDialog(dlg, "Values cannot be negative."); return;
                }
                sc.adjustStock(stockId, newQty, reorder);
                loadTable(sc.getAllStock()); updateSummary(); dlg.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "Enter valid numbers.");
            }
        });
        btnCancel.addActionListener(e -> dlg.dispose());
        dlg.setVisible(true);
    }
}
