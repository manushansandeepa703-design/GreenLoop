package greenloop.view;

import greenloop.controller.DeliveryAgentController;
import greenloop.model.DeliveryAgent;
import greenloop.util.UITheme;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;


public class DeliveryAgentPanel extends JPanel {

    private final DeliveryAgentController ctrl = new DeliveryAgentController();

    
    private JTextField  txtId, txtName, txtNic, txtDob, txtPhone, txtEmail;
    private JTextField  txtAddress, txtLicense, txtJoining;
    private JTextField  txtVehicleNo, txtMake, txtYear, txtVehicleModel, txtVehicleColor;
    private JComboBox<String> cmbVehicleType, cmbStatus, cmbSalutation;
    private JTextArea   txtRemarks;
    private JLabel      lblPhoto;
    private String      currentPhotoPath = "";

   
    private JTable            table;
    private DefaultTableModel tableModel;

    private int selectedAgentId = -1; 

    public DeliveryAgentPanel() {
        setLayout(new BorderLayout(8, 8));
        setBackground(UITheme.BG);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 16, 24));
        initUI();
        loadTable();
    }

   
    private void initUI() {
     
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);
        JLabel title = new JLabel("Delivery Agent Management");
        title.setFont(UITheme.FONT_TITLE);
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        JButton btnNew    = UITheme.makeButton("+ New Agent",  UITheme.MID_GREEN);
        JButton btnEdit   = UITheme.makeButton("Edit",         UITheme.BLUE_BTN);
        JButton btnDelete = UITheme.makeButton("Delete",       UITheme.RED_BTN);
        JButton btnRefresh= UITheme.makeButton("Refresh",      UITheme.GRAY_BTN);
        btnRow.add(btnNew); btnRow.add(btnEdit); btnRow.add(btnDelete); btnRow.add(btnRefresh);
        toolbar.add(title,  BorderLayout.WEST);
        toolbar.add(btnRow, BorderLayout.EAST);
        add(toolbar, BorderLayout.NORTH);

        
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            buildTablePanel(), buildFormPanel());
        split.setDividerLocation(260);
        split.setResizeWeight(0.45);
        split.setBorder(null);
        add(split, BorderLayout.CENTER);

       
        btnNew.addActionListener(e -> {
            clearForm();
            selectedAgentId = -1;
            txtId.setText("(Auto)");
        });

        btnEdit.addActionListener(e -> {
            if (selectedAgentId < 0) {
                JOptionPane.showMessageDialog(this, "Please select an agent from the table first.");
                return;
            }
            
            JOptionPane.showMessageDialog(this,
                "Form loaded. Make changes below and click Save.", "Edit Mode",
                JOptionPane.INFORMATION_MESSAGE);
        });

        btnDelete.addActionListener(e -> {
            if (selectedAgentId < 0) {
                JOptionPane.showMessageDialog(this, "Please select an agent first.");
                return;
            }
            int c = JOptionPane.showConfirmDialog(this,
                "Delete this agent? This cannot be undone.", "Confirm Delete",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (c == JOptionPane.YES_OPTION) {
                if (ctrl.deleteAgent(selectedAgentId)) {
                    JOptionPane.showMessageDialog(this, "Agent deleted.");
                    clearForm();
                    loadTable();
                } else {
                    JOptionPane.showMessageDialog(this, "Delete failed. Agent may be linked to deliveries.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnRefresh.addActionListener(e -> loadTable());
    }

   
    private JPanel buildTablePanel() {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Agents List"),
            BorderFactory.createEmptyBorder(4, 6, 6, 6)));

        
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        searchBar.setOpaque(false);
        JTextField searchFld = UITheme.makeField();
        searchFld.setPreferredSize(new Dimension(240, 30));
        searchFld.setToolTipText("Search by name, NIC, phone or email");
        JButton searchBtn = UITheme.makeButton("Search", UITheme.DARK_GREEN);
        JButton clearSearch = UITheme.makeButton("All", UITheme.GRAY_BTN);
        searchBar.add(searchFld); searchBar.add(searchBtn); searchBar.add(clearSearch);
        p.add(searchBar, BorderLayout.NORTH);

        String[] cols = {"ID", "Full Name", "NIC", "Phone", "Email", "Vehicle No.", "Vehicle Type", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);
        table.setRowHeight(28);
        table.getTableHeader().setFont(UITheme.FONT_SUBTITLE);
        table.getTableHeader().setBackground(UITheme.TABLE_HDR);
        table.setSelectionBackground(UITheme.TABLE_SEL);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    
        table.getColumnModel().getColumn(0).setPreferredWidth(60);
        table.getColumnModel().getColumn(7).setPreferredWidth(100);

        p.add(new JScrollPane(table), BorderLayout.CENTER);

        
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                String idStr = tableModel.getValueAt(table.getSelectedRow(), 0).toString().replace("AG-", "");
                try {
                    int id = Integer.parseInt(idStr);
                    for (DeliveryAgent a : ctrl.getAllAgents()) {
                        if (a.getAgentId() == id) { populateForm(a); break; }
                    }
                } catch (NumberFormatException ignored) {}
            }
        });

        searchBtn.addActionListener(e -> {
            String kw = searchFld.getText().trim();
            if (!kw.isEmpty()) loadTableWith(ctrl.searchAgents(kw));
            else loadTable();
        });
        searchFld.addActionListener(e -> searchBtn.doClick());
        clearSearch.addActionListener(e -> { searchFld.setText(""); loadTable(); });

        return p;
    }

    
    private JPanel buildFormPanel() {
        JPanel outer = new JPanel(new BorderLayout(0, 8));
        outer.setBackground(Color.WHITE);
        outer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Agent Details – Fill in and click Save"),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)));

      
        JPanel cols = new JPanel(new GridLayout(1, 3, 10, 0));
        cols.setBackground(Color.WHITE);
        cols.add(buildAgentInfoPanel());
        cols.add(buildVehicleInfoPanel());
        cols.add(buildPhotoStatusPanel());
        outer.add(cols, BorderLayout.CENTER);

        
        JButton btnSave   = UITheme.makeButton("Save",  UITheme.MID_GREEN);
        JButton btnClear  = UITheme.makeButton("Clear",    UITheme.GRAY_BTN);
        JPanel savePnl = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
        savePnl.setBackground(Color.WHITE);
        savePnl.add(btnSave); savePnl.add(btnClear);
        outer.add(savePnl, BorderLayout.SOUTH);

        btnSave.addActionListener(e -> saveAgent());
        btnClear.addActionListener(e -> clearForm());

        return outer;
    }

    private JPanel buildAgentInfoPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createTitledBorder("Personal Info"));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(3, 4, 3, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;

        txtId      = UITheme.makeField(); txtId.setEditable(false); txtId.setBackground(new Color(245,245,245));
        txtName    = UITheme.makeField();
        txtNic     = UITheme.makeField();
        txtDob     = UITheme.makeField(); txtDob.setToolTipText("YYYY-MM-DD");
        txtPhone   = UITheme.makeField();
        txtEmail   = UITheme.makeField();
        txtAddress = UITheme.makeField();
        txtLicense = UITheme.makeField();
        txtJoining = UITheme.makeField(); txtJoining.setToolTipText("YYYY-MM-DD");

        cmbSalutation = new JComboBox<>(new String[]{"Mr.", "Mrs.", "Ms.", "Dr.", "Prof."});
        cmbSalutation.setFont(UITheme.FONT_BODY);

        
        JPanel nameRow = new JPanel(new BorderLayout(4, 0));
        nameRow.setOpaque(false);
        cmbSalutation.setPreferredSize(new Dimension(70, 30));
        nameRow.add(cmbSalutation, BorderLayout.WEST);
        nameRow.add(txtName,       BorderLayout.CENTER);

        addRow(p, gc, 0, "Agent ID",       txtId);
        addRow(p, gc, 1, "Full Name *",    nameRow);
        addRow(p, gc, 2, "NIC Number *",   txtNic);
        addRow(p, gc, 3, "Date of Birth",  txtDob);
        addRow(p, gc, 4, "Phone *",        txtPhone);
        addRow(p, gc, 5, "Email",          txtEmail);
        addRow(p, gc, 6, "Address",        txtAddress);
        addRow(p, gc, 7, "License No.",    txtLicense);
        addRow(p, gc, 8, "Date Joined",    txtJoining);
        return p;
    }

    private JPanel buildVehicleInfoPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createTitledBorder("Vehicle Info"));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(3, 4, 3, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;

        txtVehicleNo    = UITheme.makeField();
        cmbVehicleType  = new JComboBox<>(new String[]{"Motorcycle", "Van", "Lorry", "Three-Wheeler", "Car"});
        cmbVehicleType.setFont(UITheme.FONT_BODY);
        txtMake         = UITheme.makeField();
        txtVehicleModel = UITheme.makeField();
        txtYear         = UITheme.makeField();
        txtVehicleColor = UITheme.makeField();

        addRow(p, gc, 0, "Vehicle No. *",  txtVehicleNo);
        addRow(p, gc, 1, "Vehicle Type",   cmbVehicleType);
        addRow(p, gc, 2, "Make (Brand)",   txtMake);
        addRow(p, gc, 3, "Model",          txtVehicleModel);
        addRow(p, gc, 4, "Year",           txtYear);
        addRow(p, gc, 5, "Color",          txtVehicleColor);
        return p;
    }

    private JPanel buildPhotoStatusPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createTitledBorder("Photo & Status"));

        
        lblPhoto = new JLabel("No Photo", SwingConstants.CENTER);
        lblPhoto.setPreferredSize(new Dimension(120, 130));
        lblPhoto.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        lblPhoto.setFont(UITheme.FONT_SMALL);
        lblPhoto.setOpaque(true);
        lblPhoto.setBackground(new Color(245, 245, 245));

        JButton btnPhoto = UITheme.makeButton("Choose Photo", UITheme.GRAY_BTN);
        btnPhoto.setPreferredSize(new Dimension(120, 30));
        btnPhoto.addActionListener(e -> choosePhoto());

        JPanel photoPnl = new JPanel(new BorderLayout(0, 4));
        photoPnl.setOpaque(false);
        photoPnl.add(lblPhoto,  BorderLayout.CENTER);
        photoPnl.add(btnPhoto,  BorderLayout.SOUTH);
        p.add(photoPnl, BorderLayout.NORTH);

        
        JPanel statusPnl = new JPanel(new GridBagLayout());
        statusPnl.setBackground(Color.WHITE);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;

        cmbStatus = new JComboBox<>(new String[]{"Available", "On Delivery", "On Leave", "Inactive"});
        cmbStatus.setFont(UITheme.FONT_BODY);
        txtRemarks = new JTextArea(3, 1);
        txtRemarks.setFont(UITheme.FONT_BODY);
        txtRemarks.setLineWrap(true);
        txtRemarks.setWrapStyleWord(true);

        addRow(statusPnl, gc, 0, "Status *", cmbStatus);
        gc.gridy = 1; gc.gridx = 0; gc.weightx = 0;
        statusPnl.add(new JLabel("Remarks"), gc);
        gc.gridx = 1; gc.weightx = 1;
        statusPnl.add(new JScrollPane(txtRemarks), gc);

        p.add(statusPnl, BorderLayout.CENTER);
        return p;
    }

    
    private void addRow(JPanel p, GridBagConstraints gc, int row, String label, Component comp) {
        gc.gridy = row; gc.gridx = 0; gc.weightx = 0;
        p.add(new JLabel(label), gc);
        gc.gridx = 1; gc.weightx = 1;
        p.add(comp, gc);
    }

    

    private void loadTable() {
        loadTableWith(ctrl.getAllAgents());
    }

    private void loadTableWith(List<DeliveryAgent> agents) {
        tableModel.setRowCount(0);
        for (DeliveryAgent a : agents) {
            tableModel.addRow(new Object[]{
                "AG-" + a.getAgentId(),
                a.getSalutation() + " " + (a.getFullName() != null ? a.getFullName() : ""),
                a.getNicNumber() != null ? a.getNicNumber() : "",
                a.getPhone() != null ? a.getPhone() : "",
                a.getEmail() != null ? a.getEmail() : "",
                a.getVehicleNumber() != null ? a.getVehicleNumber() : "",
                a.getVehicleType() != null ? a.getVehicleType() : "",
                a.getStatus() != null ? a.getStatus() : ""
            });
        }
    }

    private void populateForm(DeliveryAgent a) {
        selectedAgentId = a.getAgentId();
        txtId.setText("AG-" + a.getAgentId());
        cmbSalutation.setSelectedItem(a.getSalutation());
        txtName.setText(orEmpty(a.getFullName()));
        txtNic.setText(orEmpty(a.getNicNumber()));
        txtDob.setText(orEmpty(a.getDateOfBirth()));
        txtPhone.setText(orEmpty(a.getPhone()));
        txtEmail.setText(orEmpty(a.getEmail()));
        txtAddress.setText(orEmpty(a.getAddress()));
        txtLicense.setText(orEmpty(a.getLicenseNumber()));
        txtJoining.setText(orEmpty(a.getDateOfJoining()));
        txtVehicleNo.setText(orEmpty(a.getVehicleNumber()));
        cmbVehicleType.setSelectedItem(a.getVehicleType() != null ? a.getVehicleType() : "Motorcycle");
        txtMake.setText(orEmpty(a.getVehicleMake()));
        txtVehicleModel.setText(orEmpty(a.getVehicleModel()));
        txtYear.setText(orEmpty(a.getVehicleYear()));
        txtVehicleColor.setText(orEmpty(a.getVehicleColor()));
        cmbStatus.setSelectedItem(a.getStatus() != null ? a.getStatus() : "Available");
        txtRemarks.setText(orEmpty(a.getRemarks()));
        currentPhotoPath = orEmpty(a.getPhotoPath());
        updatePhotoLabel(currentPhotoPath);
    }

    
    private void saveAgent() {
        
        if (txtName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Full Name is required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            txtName.requestFocus(); return;
        }
        if (txtNic.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "NIC Number is required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            txtNic.requestFocus(); return;
        }
        if (txtPhone.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Phone is required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            txtPhone.requestFocus(); return;
        }
        if (txtVehicleNo.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vehicle Number is required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            txtVehicleNo.requestFocus(); return;
        }

        DeliveryAgent a = buildAgentFromForm();
        boolean success;

        if (selectedAgentId < 0) {
            
            success = ctrl.addAgent(a);
            if (success) JOptionPane.showMessageDialog(this, "Agent added successfully!");
            else         JOptionPane.showMessageDialog(this, "Failed to add agent. Check console for errors.", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            
            a.setAgentId(selectedAgentId);
            success = ctrl.updateAgent(a);
            if (success) JOptionPane.showMessageDialog(this, "Agent updated successfully!");
            else         JOptionPane.showMessageDialog(this, "Failed to update agent.", "Error", JOptionPane.ERROR_MESSAGE);
        }

        if (success) {
            clearForm();
            loadTable();
        }
    }

    private DeliveryAgent buildAgentFromForm() {
        DeliveryAgent a = new DeliveryAgent();
        a.setSalutation((String) cmbSalutation.getSelectedItem());
        a.setFullName(txtName.getText().trim());
        a.setNicNumber(txtNic.getText().trim());
        a.setDateOfBirth(txtDob.getText().trim());
        a.setEmail(txtEmail.getText().trim());
        a.setPhone(txtPhone.getText().trim());
        a.setAddress(txtAddress.getText().trim());
        a.setLicenseNumber(txtLicense.getText().trim());
        a.setDateOfJoining(txtJoining.getText().trim());
        a.setVehicleNumber(txtVehicleNo.getText().trim());
        a.setVehicleType((String) cmbVehicleType.getSelectedItem());
        a.setVehicleMake(txtMake.getText().trim());
        a.setVehicleModel(txtVehicleModel.getText().trim());
        a.setVehicleYear(txtYear.getText().trim());
        a.setVehicleColor(txtVehicleColor.getText().trim());
        a.setStatus((String) cmbStatus.getSelectedItem());
        a.setRemarks(txtRemarks.getText().trim());
        a.setPhotoPath(currentPhotoPath);
        return a;
    }

    
    private void clearForm() {
        selectedAgentId = -1;
        txtId.setText("(Auto)");
        cmbSalutation.setSelectedIndex(0);
        txtName.setText(""); txtNic.setText(""); txtDob.setText("");
        txtPhone.setText(""); txtEmail.setText(""); txtAddress.setText("");
        txtLicense.setText(""); txtJoining.setText("");
        txtVehicleNo.setText(""); txtMake.setText("");
        txtVehicleModel.setText(""); txtYear.setText(""); txtVehicleColor.setText("");
        cmbVehicleType.setSelectedIndex(0);
        cmbStatus.setSelectedIndex(0);
        txtRemarks.setText("");
        currentPhotoPath = "";
        lblPhoto.setIcon(null);
        lblPhoto.setText("No Photo");
        table.clearSelection();
    }

    private void choosePhoto() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Image Files", "jpg", "jpeg", "png", "gif"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            currentPhotoPath = fc.getSelectedFile().getAbsolutePath();
            updatePhotoLabel(currentPhotoPath);
        }
    }

    private void updatePhotoLabel(String path) {
        if (path != null && !path.isEmpty()) {
            try {
                ImageIcon icon = new ImageIcon(path);
                Image scaled = icon.getImage().getScaledInstance(120, 130, Image.SCALE_SMOOTH);
                lblPhoto.setIcon(new ImageIcon(scaled));
                lblPhoto.setText("");
            } catch (Exception ex) {
                lblPhoto.setIcon(null);
                lblPhoto.setText("Photo set");
            }
        } else {
            lblPhoto.setIcon(null);
            lblPhoto.setText("No Photo");
        }
    }

    private String orEmpty(String s) { return s != null ? s : ""; }
}
