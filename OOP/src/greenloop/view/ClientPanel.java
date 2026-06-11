package greenloop.view;

import greenloop.controller.ClientController;
import greenloop.model.Client;
import greenloop.util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ClientPanel extends JPanel {

    private ClientController ctrl = new ClientController();

    private JTextField   txtId, txtName, txtContact, txtPhone, txtEmail;
    private JTextArea    txtAddress;
    private JComboBox<String> cmbStatus, cmbSalutation;
    private JTable       table;
    private DefaultTableModel tableModel;
    private JButton      btnSave, btnUpdate, btnDelete, btnClear;
    private JLabel       lblTotal;
    private int selectedClientId = -1;

    public ClientPanel() {
        setLayout(new BorderLayout(12,12));
        setBackground(UITheme.BG);
        setBorder(BorderFactory.createEmptyBorder(24,28,16,28));
        initUI();
        loadTable(ctrl.getAllClients());
    }

    private void initUI() {
        JLabel title = new JLabel("Client Management");
        title.setFont(UITheme.FONT_TITLE);
        add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(1,2,16,0));
        center.setOpaque(false);
        center.add(buildForm());
        center.add(buildTablePanel());
        add(center, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,10,0));
        btnPanel.setOpaque(false);
        btnSave   = UITheme.makeButton("Save",   UITheme.MID_GREEN);
        btnUpdate = UITheme.makeButton("Update", UITheme.BLUE_BTN);
        btnDelete = UITheme.makeButton("Delete", UITheme.RED_BTN);
        btnClear  = UITheme.makeButton("Clear",  UITheme.GRAY_BTN);
        btnPanel.add(btnSave); btnPanel.add(btnUpdate); btnPanel.add(btnDelete); btnPanel.add(btnClear);
        add(btnPanel, BorderLayout.SOUTH);

        wireEvents();
    }

    private JPanel buildForm() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Client Information"),
            BorderFactory.createEmptyBorder(8,12,8,12)));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(5,4,5,4);
        gc.weightx = 1;

        txtId       = UITheme.makeField(); txtId.setEditable(false); txtId.setBackground(new Color(245,245,245));
        txtName     = UITheme.makeField();
        txtContact  = UITheme.makeField();
        txtPhone    = UITheme.makeField();
        txtEmail    = UITheme.makeField();
        txtAddress  = new JTextArea(3,1); txtAddress.setFont(UITheme.FONT_BODY); txtAddress.setLineWrap(true);
        cmbStatus   = new JComboBox<>(new String[]{"Active","Inactive"});
        cmbSalutation = new JComboBox<>(new String[]{"Mr.", "Mrs.", "Ms.", "Dr.", "Prof."});
        cmbSalutation.setFont(UITheme.FONT_BODY);

        // Contact row: salutation combo + name field side by side
        JPanel contactRow = new JPanel(new BorderLayout(4, 0));
        contactRow.setOpaque(false);
        cmbSalutation.setPreferredSize(new Dimension(70, 34));
        contactRow.add(cmbSalutation, BorderLayout.WEST);
        contactRow.add(txtContact,    BorderLayout.CENTER);

        int row = 0;
        addRow(p, gc, row++, "Client ID",     txtId);
        addRow(p, gc, row++, "Client Name",   txtName);
        addRow(p, gc, row++, "Contact Person",contactRow);
        addRow(p, gc, row++, "Phone",         txtPhone);
        addRow(p, gc, row++, "Email",         txtEmail);
        addRow(p, gc, row++, "Status",        cmbStatus);
        gc.gridx=0; gc.gridy=row; p.add(new JLabel("Address"), gc);
        gc.gridx=1; p.add(new JScrollPane(txtAddress), gc);
        return p;
    }

    private JPanel buildTablePanel() {
        JPanel p = new JPanel(new BorderLayout(0,8));
        p.setOpaque(false);

        JPanel searchBar = new JPanel(new BorderLayout(6,0));
        searchBar.setOpaque(false);
        JTextField sf = UITheme.makeField(); sf.setToolTipText("Search client...");
        JButton sb = UITheme.makeButton("Search", UITheme.DARK_GREEN);
        sb.setPreferredSize(new Dimension(90,32));
        searchBar.add(sf, BorderLayout.CENTER); searchBar.add(sb, BorderLayout.EAST);
        p.add(searchBar, BorderLayout.NORTH);

        String[] cols = {"Client ID","Client Name","Phone","Email"};
        tableModel = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r,int c){return false;} };
        table = new JTable(tableModel);
        UITheme.styleTable(table);
        table.getTableHeader().setFont(UITheme.FONT_SUBTITLE);
        table.getTableHeader().setBackground(UITheme.TABLE_HDR);
        table.setSelectionBackground(UITheme.TABLE_SEL);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        p.add(new JScrollPane(table), BorderLayout.CENTER);

        lblTotal = new JLabel("Total Clients: 0", SwingConstants.RIGHT);
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 12));
        p.add(lblTotal, BorderLayout.SOUTH);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                String idStr = tableModel.getValueAt(table.getSelectedRow(), 0).toString();
                int cid = Integer.parseInt(idStr.replace("C-",""));
                for (Client c : ctrl.getAllClients()) if (c.getClientId() == cid) { populateForm(c); break; }
            }
        });
        sb.addActionListener(e -> loadTable(ctrl.searchClients(sf.getText().trim())));
        sf.addActionListener(e -> sb.doClick());
        return p;
    }

    private void loadTable(List<Client> clients) {
        tableModel.setRowCount(0);
        for (Client c : clients) tableModel.addRow(new Object[]{"C-"+c.getClientId(), c.getBusinessName(), c.getPhone(), c.getEmail()});
        lblTotal.setText("Total Clients: " + clients.size());
    }

    private void populateForm(Client c) {
        selectedClientId = c.getClientId();
        txtId.setText("C-" + c.getClientId());
        txtName.setText(c.getBusinessName());
        cmbSalutation.setSelectedItem(c.getSalutation());
        txtContact.setText(c.getContactPerson());
        txtPhone.setText(c.getPhone());
        txtEmail.setText(c.getEmail());
        txtAddress.setText(c.getAddress());
        cmbStatus.setSelectedItem(c.getStatus());
    }

    private void clearForm() {
        selectedClientId = -1;
        txtId.setText("Auto"); txtName.setText(""); txtContact.setText("");
        txtPhone.setText(""); txtEmail.setText(""); txtAddress.setText("");
        cmbSalutation.setSelectedIndex(0);
        cmbStatus.setSelectedIndex(0); table.clearSelection();
    }

    private void wireEvents() {
        btnSave.addActionListener(e -> {
            if (!validateInputs()) return; 
            if (ctrl.addClient(buildClient())) { 
                JOptionPane.showMessageDialog(this,"Client saved!"); 
                loadTable(ctrl.getAllClients()); 
                clearForm(); 
            }
            else JOptionPane.showMessageDialog(this,"Error saving client.","Error",JOptionPane.ERROR_MESSAGE);
        });
        btnUpdate.addActionListener(e -> {
            if (selectedClientId<0){JOptionPane.showMessageDialog(this,"Select a client.");return;}
            Client c = buildClient(); c.setClientId(selectedClientId);
            if (ctrl.updateClient(c)) { JOptionPane.showMessageDialog(this,"Client updated!"); loadTable(ctrl.getAllClients()); }
        });
        btnDelete.addActionListener(e -> {
            if (selectedClientId<0){JOptionPane.showMessageDialog(this,"Select a client.");return;}
            if (JOptionPane.showConfirmDialog(this,"Delete client?","Confirm",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION) {
                boolean deleted = ctrl.deleteClient(selectedClientId);
                if (deleted) {
                    loadTable(ctrl.getAllClients()); clearForm();
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Cannot delete this client.\nThey may have existing orders linked to their account.",
                        "Delete Failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        btnClear.addActionListener(e -> clearForm());
    }

    
    private boolean validateInputs() {
        if (txtName.getText().trim().isEmpty()) { 
            JOptionPane.showMessageDialog(this,"Client name required."); 
            return false; 
        }
        if (txtEmail.getText().trim().isEmpty()) { 
            JOptionPane.showMessageDialog(this,"Email required."); 
            return false; 
        }
        return true;
    }

    private Client buildClient() {
        Client c = new Client();
        c.setBusinessName(txtName.getText().trim());
        c.setSalutation((String) cmbSalutation.getSelectedItem());
        c.setContactPerson(txtContact.getText().trim());
        c.setEmail(txtEmail.getText().trim());
        c.setPhone(txtPhone.getText().trim());
        c.setAddress(txtAddress.getText().trim());
        c.setStatus((String)cmbStatus.getSelectedItem());
        return c;
    }

    private void addRow(JPanel p, GridBagConstraints gc, int row, String label, Component field) {
        gc.gridx=0; gc.gridy=row; gc.weightx=0; p.add(new JLabel(label),gc);
        gc.gridx=1; gc.weightx=1; p.add(field,gc);
    }
}