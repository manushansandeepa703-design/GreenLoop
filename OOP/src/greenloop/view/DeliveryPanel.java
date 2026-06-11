package greenloop.view;

import greenloop.controller.*;
import greenloop.model.*;
import greenloop.util.UITheme;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;


public class DeliveryPanel extends JPanel {

    private final DeliveryController      dc  = new DeliveryController();
    private final DeliveryAgentController agc = new DeliveryAgentController();
    private final OrderController         oc  = new OrderController();
    private final EmailController         ec  = new EmailController();

    private DefaultTableModel ordersModel;
    private JTable            ordersTable;
    private JTextField        txtOrderId, txtClient, txtDate, txtAmount, txtCurrentStatus;
    private JComboBox<DeliveryAgent> cmbAgent;
    private DefaultTableModel itemsModel;
    private JLabel            lblStatus1, lblStatus2, lblStatus3, lblStatus4;
    private JLabel            lblAssignedAgent;

    private int selectedOrderId = -1;

    public DeliveryPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UITheme.BG);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 16, 24));
        initUI();
        loadOrders();
    }

   

    private void initUI() {
        JLabel title = new JLabel("Delivery Management");
        title.setFont(UITheme.FONT_TITLE);
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.add(title, BorderLayout.WEST);
        JButton refreshBtn = UITheme.makeButton("Refresh", UITheme.GRAY_BTN);
        refreshBtn.addActionListener(e -> { loadOrders(); refreshAgentCombo(); });
        topBar.add(refreshBtn, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            buildDetailPanel(), buildOrdersPanel());
        split.setDividerLocation(500);
        split.setResizeWeight(0.45);
        split.setOpaque(false);
        add(split, BorderLayout.CENTER);
    }

    
    private JPanel buildDetailPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(14, 14, 14, 14)));

        
        JPanel orderPnl = new JPanel(new GridBagLayout());
        orderPnl.setBackground(Color.WHITE);
        orderPnl.setBorder(BorderFactory.createTitledBorder("Selected Order"));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;

        txtOrderId     = UITheme.makeField(); txtOrderId.setEditable(false);
        txtClient      = UITheme.makeField(); txtClient.setEditable(false);
        txtDate        = UITheme.makeField(); txtDate.setEditable(false);
        txtAmount      = UITheme.makeField(); txtAmount.setEditable(false);
        txtCurrentStatus = UITheme.makeField(); txtCurrentStatus.setEditable(false);
        lblAssignedAgent = new JLabel("–"); lblAssignedAgent.setFont(UITheme.FONT_BODY);

        addRow(orderPnl, gc, 0, "Order ID",        txtOrderId);
        addRow(orderPnl, gc, 1, "Client",          txtClient);
        addRow(orderPnl, gc, 2, "Order Date",      txtDate);
        addRow(orderPnl, gc, 3, "Amount",          txtAmount);
        addRow(orderPnl, gc, 4, "Current Status",  txtCurrentStatus);
        addRow(orderPnl, gc, 5, "Assigned Agent",  lblAssignedAgent);
        p.add(orderPnl, BorderLayout.NORTH);

        
        JPanel assignPnl = new JPanel(new GridBagLayout());
        assignPnl.setBackground(Color.WHITE);
        assignPnl.setBorder(BorderFactory.createTitledBorder("Assign / Update Delivery"));
        GridBagConstraints gc2 = new GridBagConstraints();
        gc2.insets = new Insets(5, 4, 5, 4);
        gc2.fill = GridBagConstraints.HORIZONTAL;

        cmbAgent = new JComboBox<>();
        cmbAgent.setFont(UITheme.FONT_BODY);
        refreshAgentCombo();

        addRow(assignPnl, gc2, 0, "Available Agent", cmbAgent);

        
        JButton btnAssign     = UITheme.makeButton("Assign Agent",    UITheme.MID_GREEN);
        JButton btnDispatched = UITheme.makeButton("Mark Dispatched", UITheme.BLUE_BTN);
        JButton btnDelivered  = UITheme.makeButton("Mark Delivered",  UITheme.ORANGE_BTN);

        gc2.gridy = 1; gc2.gridx = 0; gc2.gridwidth = 2;
        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        actionBtns.setOpaque(false);
        actionBtns.add(btnAssign);
        actionBtns.add(btnDispatched);
        actionBtns.add(btnDelivered);
        assignPnl.add(actionBtns, gc2);

        p.add(assignPnl, BorderLayout.CENTER);

        
        JPanel lower = new JPanel(new GridLayout(1, 2, 10, 0));
        lower.setOpaque(false);
        lower.add(buildItemsPanel());
        lower.add(buildStatusTimelinePanel());
        p.add(lower, BorderLayout.SOUTH);

        
        btnAssign.addActionListener(e -> assignDelivery());
        btnDispatched.addActionListener(e -> updateStatus("Dispatched"));
        btnDelivered.addActionListener(e -> updateStatus("Delivered"));

        return p;
    }

    private JPanel buildItemsPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createTitledBorder("Order Items"));
        String[] cols = {"Product", "Qty", "Unit Price (Rs.)", "Total (Rs.)"};
        itemsModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable itemsTable = new JTable(itemsModel);
        itemsTable.setFont(UITheme.FONT_BODY);
        itemsTable.setRowHeight(26);
        itemsTable.getTableHeader().setFont(UITheme.FONT_SUBTITLE);
        itemsTable.getTableHeader().setBackground(UITheme.TABLE_HDR);
        p.add(new JScrollPane(itemsTable), BorderLayout.CENTER);
        p.setPreferredSize(new Dimension(0, 160));
        return p;
    }

    private JPanel buildStatusTimelinePanel() {
        JPanel p = new JPanel(new GridLayout(4, 1, 0, 6));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createTitledBorder("Delivery Timeline"));
        lblStatus1 = makeStatusLbl("Order Placed",       false);
        lblStatus2 = makeStatusLbl("Assigned to Agent",  false);
        lblStatus3 = makeStatusLbl("Dispatched",         false);
        lblStatus4 = makeStatusLbl("Delivered",          false);
        p.add(lblStatus1); p.add(lblStatus2); p.add(lblStatus3); p.add(lblStatus4);
        return p;
    }

    
    private JPanel buildOrdersPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        JLabel hdr = new JLabel("Pending / Processing Orders");
        hdr.setFont(UITheme.FONT_SUBTITLE);
        p.add(hdr, BorderLayout.NORTH);

        String[] cols = {"Order ID", "Client", "Date", "Amount (Rs.)", "Status"};
        ordersModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        ordersTable = new JTable(ordersModel);
        UITheme.styleTable(ordersTable);
        ordersTable.setRowHeight(28);
        ordersTable.getTableHeader().setFont(UITheme.FONT_SUBTITLE);
        ordersTable.getTableHeader().setBackground(UITheme.TABLE_HDR);
        ordersTable.setSelectionBackground(UITheme.TABLE_SEL);
        ordersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        p.add(new JScrollPane(ordersTable), BorderLayout.CENTER);

        ordersTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && ordersTable.getSelectedRow() >= 0)
                selectOrder();
        });
        return p;
    }

  

    private void loadOrders() {
        ordersModel.setRowCount(0);
        List<Order> orders = dc.getPendingOrders();
        for (Order o : orders) {
            String date = o.getOrderDate() != null && o.getOrderDate().length() >= 10
                          ? o.getOrderDate().substring(0, 10) : "";
            ordersModel.addRow(new Object[]{
                "ORD-" + o.getOrderId(),
                o.getClientName(),
                date,
                String.format("%.2f", o.getTotalAmount()),
                o.getStatus()
            });
        }
        if (orders.isEmpty()) {
            
            ordersModel.addRow(new Object[]{"", "No pending/processing orders", "", "", ""});
        }
    }

    private void refreshAgentCombo() {
        cmbAgent.removeAllItems();
        List<DeliveryAgent> agents = agc.getAvailableAgents();
        for (DeliveryAgent a : agents) cmbAgent.addItem(a);
        if (agents.isEmpty()) {
            
            DeliveryAgent placeholder = new DeliveryAgent();
            placeholder.setFullName("No available agents");
            cmbAgent.addItem(placeholder);
        }
    }

    private void selectOrder() {
        int row = ordersTable.getSelectedRow();
        if (row < 0) return;
        String oidStr = ordersModel.getValueAt(row, 0).toString().replace("ORD-", "");
        if (oidStr.isEmpty()) return;
        try {
            selectedOrderId = Integer.parseInt(oidStr);
        } catch (NumberFormatException e) { return; }

        txtOrderId.setText("ORD-" + selectedOrderId);
        txtClient.setText(ordersModel.getValueAt(row, 1).toString());
        txtDate.setText(ordersModel.getValueAt(row, 2).toString());
        txtAmount.setText("Rs. " + ordersModel.getValueAt(row, 3).toString());
        txtCurrentStatus.setText(ordersModel.getValueAt(row, 4).toString());

        
        String agentName = dc.getAssignedAgentName(selectedOrderId);
        lblAssignedAgent.setText(agentName);

        
        itemsModel.setRowCount(0);
        for (OrderItem item : oc.getOrderItems(selectedOrderId)) {
            itemsModel.addRow(new Object[]{
                item.getProductName(),
                item.getQuantity(),
                String.format("%.2f", item.getUnitPrice()),
                String.format("%.2f", item.getTotal())
            });
        }

       
        String deliveryStatus = dc.getDeliveryStatus(selectedOrderId);
        String orderStatus    = ordersModel.getValueAt(row, 4).toString();
        updateTimeline(orderStatus, deliveryStatus);
    }

    private void assignDelivery() {
        if (selectedOrderId < 0) {
            JOptionPane.showMessageDialog(this, "Please select an order from the list first.");
            return;
        }
        DeliveryAgent agent = (DeliveryAgent) cmbAgent.getSelectedItem();
        if (agent == null || agent.getAgentId() == 0) {
            JOptionPane.showMessageDialog(this, "No available agents. Please add agents in the 'Delivery Agents' menu.",
                "No Agents", JOptionPane.WARNING_MESSAGE);
            return;
        }

        
        if (dc.isOrderAssigned(selectedOrderId)) {
            int c = JOptionPane.showConfirmDialog(this,
                "This order is already assigned. Re-assign to " + agent.getFullName() + "?",
                "Re-Assign", JOptionPane.YES_NO_OPTION);
            if (c != JOptionPane.YES_OPTION) return;
        }

        if (dc.assignDelivery(selectedOrderId, agent.getAgentId())) {
            
            if (agent.getEmail() != null && !agent.getEmail().isEmpty()) {
                ec.sendEmail(agent.getEmail(),
                    "New Delivery Assigned – ORD-" + selectedOrderId,
                    "Dear " + agent.getFullName() + ",\nA new delivery (ORD-" + selectedOrderId + ") has been assigned to you.\n– GreenLoop Team");
            }
            JOptionPane.showMessageDialog(this,
                "Agent " + agent.getFullName() + " assigned to ORD-" + selectedOrderId + " successfully!");
            loadOrders();
            refreshAgentCombo();
            selectOrderById(selectedOrderId);
        } else {
            JOptionPane.showMessageDialog(this, "Assignment failed. Check console for details.",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateStatus(String status) {
        if (selectedOrderId < 0) {
            JOptionPane.showMessageDialog(this, "Please select an order first.");
            return;
        }
        if (!dc.isOrderAssigned(selectedOrderId)) {
            JOptionPane.showMessageDialog(this, "Assign an agent first before updating status.",
                "Not Assigned", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (dc.updateDeliveryStatus(selectedOrderId, status)) {
            JOptionPane.showMessageDialog(this, "Status updated to: " + status);
            loadOrders();
            refreshAgentCombo();
            
            selectOrderById(selectedOrderId);
        } else {
            JOptionPane.showMessageDialog(this, "Update failed.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void selectOrderById(int orderId) {
        for (int i = 0; i < ordersModel.getRowCount(); i++) {
            String cell = ordersModel.getValueAt(i, 0).toString();
            if (("ORD-" + orderId).equals(cell)) {
                ordersTable.setRowSelectionInterval(i, i);
                selectOrder();
                return;
            }
        }
    }

    private void updateTimeline(String orderStatus, String deliveryStatus) {
        boolean placed     = true;
        boolean assigned   = !"Not Assigned".equals(deliveryStatus);
        boolean dispatched = "Dispatched".equals(deliveryStatus) || "Delivered".equals(deliveryStatus)
                          || "Dispatched".equals(orderStatus) || "Delivered".equals(orderStatus);
        boolean delivered  = "Delivered".equals(deliveryStatus) || "Delivered".equals(orderStatus);

        setStatusLbl(lblStatus1, "Order Placed",       placed);
        setStatusLbl(lblStatus2, "Assigned to Agent",  assigned);
        setStatusLbl(lblStatus3, "Dispatched",         dispatched);
        setStatusLbl(lblStatus4, "Delivered",          delivered);
    }

    

    private JLabel makeStatusLbl(String name, boolean done) {
        JLabel l = new JLabel((done ? "✔ " : "○ ") + name);
        l.setFont(UITheme.FONT_BODY);
        l.setForeground(done ? UITheme.MID_GREEN : Color.GRAY);
        return l;
    }

    private void setStatusLbl(JLabel lbl, String name, boolean done) {
        lbl.setText((done ? "✔ " : "○ ") + name);
        lbl.setForeground(done ? UITheme.MID_GREEN : Color.GRAY);
    }

    private void addRow(JPanel p, GridBagConstraints gc, int row, String label, Component comp) {
        gc.gridy = row; gc.gridx = 0; gc.weightx = 0; gc.gridwidth = 1;
        p.add(new JLabel(label), gc);
        gc.gridx = 1; gc.weightx = 1;
        p.add(comp, gc);
    }
}
