package greenloop.view;

import greenloop.controller.*;
import greenloop.model.*;
import greenloop.util.UITheme;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class OrderPanel extends JPanel {

    private OrderController   oc = new OrderController();
    private ClientController  cc = new ClientController();
    private ProductController pc = new ProductController();

    private JTextField txtOrderId, txtDate;
    private JComboBox<Client> cmbClient;
    private JComboBox<String> cmbPayment;
    private JComboBox<Product> cmbProduct;
    private JTextField txtUnitPrice;
    private JSpinner spnQty;
    private JTextArea txtNotes;
    private JLabel lblSubtotal, lblVat, lblTotal;

    private JTable itemsTable;
    private DefaultTableModel itemsModel;
    private List<OrderItem> currentItems = new ArrayList<>();

    private JTable ordersTable;
    private DefaultTableModel ordersModel;

    public OrderPanel() {
        setLayout(new BorderLayout(12, 12));
        setBackground(UITheme.BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        initUI();
        loadOrdersTable();
    }

    private void initUI() {
        JLabel title = new JLabel("Process Client Order");
        title.setFont(UITheme.FONT_TITLE);
        add(title, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            buildOrderForm(), buildOrdersListPanel());
        split.setDividerLocation(650);
        split.setOpaque(false);
        split.setBorder(null);
        add(split, BorderLayout.CENTER);
    }

    private JPanel buildOrderForm() {
        JPanel p = new JPanel(new BorderLayout(0, 15));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));

        
        JPanel infoRow = new JPanel(new GridLayout(1, 4, 10, 0));
        infoRow.setOpaque(false);
        txtOrderId = UITheme.makeField(); txtOrderId.setEditable(false);
        txtOrderId.setBackground(new Color(245, 245, 245));
        txtOrderId.setText("(Auto)");
        txtDate = UITheme.makeField();
        txtDate.setText(java.time.LocalDate.now().toString());
        txtDate.setEditable(false);

        List<Client> clients = cc.getAllClients();
        cmbClient = new JComboBox<>(clients.toArray(new Client[0]));
        cmbClient.setFont(UITheme.FONT_BODY);
        
        if (clients.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No clients found. Please add a client first.",
                "No Clients", JOptionPane.WARNING_MESSAGE);
        }

        cmbPayment = new JComboBox<>(new String[]{"Immediate", "15 Days", "30 Days", "60 Days"});
        cmbPayment.setFont(UITheme.FONT_BODY);

        infoRow.add(labelField("Order ID",      txtOrderId));
        infoRow.add(labelField("Order Date",    txtDate));
        infoRow.add(labelField("Client",        cmbClient));
        infoRow.add(labelField("Payment Terms", cmbPayment));
        p.add(infoRow, BorderLayout.NORTH);

        
        JPanel centerContainer = new JPanel(new BorderLayout(0, 10));
        centerContainer.setOpaque(false);

        JPanel addSection = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        addSection.setBorder(BorderFactory.createTitledBorder("Add Products"));
        addSection.setBackground(Color.WHITE);

        List<Product> products = pc.getAllProducts();
        cmbProduct = new JComboBox<>(products.toArray(new Product[0]));
        cmbProduct.setPreferredSize(new Dimension(180, 32));

        txtUnitPrice = UITheme.makeField(); txtUnitPrice.setPreferredSize(new Dimension(80, 32));
        spnQty = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));
        spnQty.setPreferredSize(new Dimension(60, 32));

        JButton btnAdd = UITheme.makeButton("Add to Order", UITheme.MID_GREEN);
        btnAdd.setPreferredSize(new Dimension(120, 32));

        cmbProduct.addActionListener(e -> {
            Product pr = (Product) cmbProduct.getSelectedItem();
            if (pr != null) txtUnitPrice.setText(String.format("%.2f", pr.getPrice()));
        });
        if (!products.isEmpty()) txtUnitPrice.setText(String.format("%.2f", products.get(0).getPrice()));

        addSection.add(new JLabel("Product:")); addSection.add(cmbProduct);
        addSection.add(new JLabel("Price:"));   addSection.add(txtUnitPrice);
        addSection.add(new JLabel("Qty:"));     addSection.add(spnQty);
        addSection.add(btnAdd);
        centerContainer.add(addSection, BorderLayout.NORTH);

        
        String[] cols = {"#", "Product", "Price", "Qty", "Total", "Remove"};
        itemsModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        itemsTable = new JTable(itemsModel);
        itemsTable.setRowHeight(25);
        
        itemsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int col = itemsTable.columnAtPoint(e.getPoint());
                int row = itemsTable.rowAtPoint(e.getPoint());
                if (col == 5 && row >= 0 && row < currentItems.size()) {
                    currentItems.remove(row);
                    itemsModel.removeRow(row);
                   
                    for (int i = 0; i < itemsModel.getRowCount(); i++) {
                        itemsModel.setValueAt(i + 1, i, 0);
                    }
                    recalcTotals();
                }
            }
        });
        JScrollPane tableScroll = new JScrollPane(itemsTable);
        tableScroll.setPreferredSize(new Dimension(400, 200));
        centerContainer.add(tableScroll, BorderLayout.CENTER);
        p.add(centerContainer, BorderLayout.CENTER);

        
        JPanel southContainer = new JPanel(new BorderLayout(0, 10));
        southContainer.setOpaque(false);

        JPanel totalsAndNotes = new JPanel(new GridLayout(1, 2, 15, 0));
        totalsAndNotes.setOpaque(false);

        txtNotes = new JTextArea(3, 1); txtNotes.setLineWrap(true);
        JPanel notesPanel = new JPanel(new BorderLayout());
        notesPanel.setBorder(BorderFactory.createTitledBorder("Order Notes"));
        notesPanel.add(new JScrollPane(txtNotes));

        JPanel totalsPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        totalsPanel.setBackground(Color.WHITE);
        totalsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        lblSubtotal = new JLabel("0.00", SwingConstants.RIGHT);
        lblVat      = new JLabel("0.00", SwingConstants.RIGHT);
        lblTotal    = new JLabel("0.00", SwingConstants.RIGHT);
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTotal.setForeground(UITheme.MID_GREEN);
        totalsPanel.add(new JLabel("Subtotal:"));    totalsPanel.add(lblSubtotal);
        totalsPanel.add(new JLabel("VAT (18%):")); totalsPanel.add(lblVat);
        totalsPanel.add(new JLabel("Total (Rs):")); totalsPanel.add(lblTotal);

        totalsAndNotes.add(notesPanel);
        totalsAndNotes.add(totalsPanel);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnBar.setOpaque(false);
        JButton btnSave   = UITheme.makeButton("Save Order", UITheme.MID_GREEN);
        JButton btnClear  = UITheme.makeButton("Clear",      UITheme.BLUE_BTN);
        JButton btnCancel = UITheme.makeButton("Cancel",     UITheme.GRAY_BTN);
        btnBar.add(btnCancel); btnBar.add(btnClear); btnBar.add(btnSave);

        southContainer.add(totalsAndNotes, BorderLayout.CENTER);
        southContainer.add(btnBar, BorderLayout.SOUTH);
        p.add(southContainer, BorderLayout.SOUTH);

        btnAdd.addActionListener(e -> addItemToOrder());
        btnSave.addActionListener(e -> saveOrder());
        btnClear.addActionListener(e -> clearForm());
        btnCancel.addActionListener(e -> clearForm());

        return p;
    }

    private JPanel buildOrdersListPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Recent Orders"),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));

        String[] cols = {"Order ID", "Client", "Status", "Total", "Date"};
        ordersModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        ordersTable = new JTable(ordersModel);
        ordersTable.setRowHeight(25);
        ordersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        p.add(new JScrollPane(ordersTable), BorderLayout.CENTER);

        JPanel ctrl = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        ctrl.setOpaque(false);
        JComboBox<String> cmbStatus = new JComboBox<>(
            new String[]{"Pending", "Processing", "Dispatched", "Delivered", "Cancelled"});
        JButton btnUpdate = UITheme.makeButton("Update Status", UITheme.BLUE_BTN);
        ctrl.add(new JLabel("Status:")); ctrl.add(cmbStatus); ctrl.add(btnUpdate);
        p.add(ctrl, BorderLayout.SOUTH);

        btnUpdate.addActionListener(e -> {
            int row = ordersTable.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select an order first."); return; }
            
            String rawId = ordersModel.getValueAt(row, 0).toString().replace("ORD-", "");
            try {
                int oid = Integer.parseInt(rawId);
                oc.updateOrderStatus(oid, (String) cmbStatus.getSelectedItem());
                loadOrdersTable();
                JOptionPane.showMessageDialog(this, "Order status updated.");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid order ID.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return p;
    }

    private void addItemToOrder() {
        Product pr = (Product) cmbProduct.getSelectedItem();
        if (pr == null) { JOptionPane.showMessageDialog(this, "No product selected."); return; }

        
        for (OrderItem existing : currentItems) {
            if (existing.getProductId() == pr.getProductId()) {
                JOptionPane.showMessageDialog(this,
                    "\"" + pr.getProductName() + "\" is already in the order.\n" +
                    "Remove it first and re-add with the correct quantity.",
                    "Duplicate Product", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        try {
            double price = Double.parseDouble(txtUnitPrice.getText().trim());
            if (price < 0) { JOptionPane.showMessageDialog(this, "Price cannot be negative."); return; }
            int qty = (Integer) spnQty.getValue();
            OrderItem item = new OrderItem(pr.getProductId(), pr.getProductName(), qty, price);
            currentItems.add(item);
            itemsModel.addRow(new Object[]{
                currentItems.size(), pr.getProductName(),
                String.format("%.2f", price), qty,
                String.format("%.2f", item.getTotal()), "🗑"
            });
            recalcTotals();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid price.");
        }
    }

    private void recalcTotals() {
        double sub = currentItems.stream().mapToDouble(OrderItem::getTotal).sum();
        double vat = sub * 0.18;
        lblSubtotal.setText(String.format("%.2f", sub));
        lblVat.setText(String.format("%.2f", vat));
        lblTotal.setText(String.format("%.2f", sub + vat));
    }

    private void saveOrder() {
        if (currentItems.isEmpty()) { JOptionPane.showMessageDialog(this, "Add at least one product."); return; }
        Client client = (Client) cmbClient.getSelectedItem();
        if (client == null) { JOptionPane.showMessageDialog(this, "Select a client."); return; }

        Order order = new Order();
        order.setClientId(client.getClientId());
        order.setOrderDate(txtDate.getText());
        double sub = currentItems.stream().mapToDouble(OrderItem::getTotal).sum();
        order.setTotalAmount(sub * 1.18);
        order.setNotes(txtNotes.getText().trim());

        int orderId = oc.saveOrder(order, currentItems);
        if (orderId == -2) {
            JOptionPane.showMessageDialog(this,
                "Order could not be saved: one or more products have insufficient stock.\n"
                + "Please check inventory and adjust quantities.",
                "Insufficient Stock", JOptionPane.WARNING_MESSAGE);
        } else if (orderId > 0) {
            
            JOptionPane.showMessageDialog(this, "Order ORD-" + orderId + " saved successfully!");
            clearForm();
            loadOrdersTable();
        } else {
            JOptionPane.showMessageDialog(this, "Error saving order.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        currentItems.clear();
        itemsModel.setRowCount(0);
        txtNotes.setText("");
        lblSubtotal.setText("0.00");
        lblVat.setText("0.00");
        lblTotal.setText("0.00");
       
        if (cmbProduct.getItemCount() > 0) {
            cmbProduct.setSelectedIndex(0);
            Product first = (Product) cmbProduct.getSelectedItem();
            if (first != null) txtUnitPrice.setText(String.format("%.2f", first.getPrice()));
        }
    }

    private void loadOrdersTable() {
        ordersModel.setRowCount(0);
        List<Order> orders = oc.getAllOrders();
        if (orders != null) {
            for (Order o : orders) {
                String date = o.getOrderDate() != null && o.getOrderDate().length() >= 10
                              ? o.getOrderDate().substring(0, 10) : "";
                ordersModel.addRow(new Object[]{
                    "ORD-" + o.getOrderId(),
                    o.getClientName(),
                    o.getStatus(),
                    String.format("Rs. %.2f", o.getTotalAmount()),
                    date
                });
            }
        }
    }

    private JPanel labelField(String label, Component field) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        JLabel lbl = new JLabel(label); lbl.setFont(UITheme.FONT_BODY);
        p.add(lbl, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }
}
