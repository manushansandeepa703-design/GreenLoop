package greenloop.view;

import greenloop.controller.ClientController;
import greenloop.controller.DeliveryAgentController;
import greenloop.controller.EmailController;
import greenloop.model.Client;
import greenloop.model.DeliveryAgent;
import greenloop.model.EmailLog;
import greenloop.util.UITheme;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class EmailNotificationPanel extends JPanel {

    private EmailController        ec  = new EmailController();
    private ClientController       cc  = new ClientController();
    private DeliveryAgentController agc = new DeliveryAgentController();

    private DefaultTableModel tableModel;
    private JTable            table;
    private JTextArea         txtPreview;
    private JButton           btnAll, btnClients, btnAgents;

    public EmailNotificationPanel() {
        setLayout(new BorderLayout(12,12));
        setBackground(UITheme.BG);
        setBorder(BorderFactory.createEmptyBorder(24,28,16,28));
        initUI();
        loadAll();
    }

    private void initUI() {
        
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        JLabel title = new JLabel("Email Notifications"); title.setFont(UITheme.FONT_TITLE);
        JButton btnSend = UITheme.makeButton("Send New Email", UITheme.MID_GREEN);
        btnSend.setPreferredSize(new Dimension(160,38));
        topBar.add(title, BorderLayout.WEST);
        topBar.add(btnSend, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

       
        JPanel main = new JPanel(new BorderLayout(0,10));
        main.setBackground(Color.WHITE);
        main.setBorder(BorderFactory.createLineBorder(new Color(220,220,220)));

       
        JPanel tabBar = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
        tabBar.setBackground(Color.WHITE);
        tabBar.setBorder(BorderFactory.createMatteBorder(0,0,1,0,new Color(200,200,200)));
        btnAll     = makeTabButton("All",         true);
        btnClients = makeTabButton("To Clients",  false);
        btnAgents  = makeTabButton("To Delivery Agents", false);
        tabBar.add(btnAll); tabBar.add(btnClients); tabBar.add(btnAgents);
        main.add(tabBar, BorderLayout.NORTH);

      
        String[] cols = {"Date & Time","To","Subject","Type","Status"};
        tableModel = new DefaultTableModel(cols,0) { public boolean isCellEditable(int r,int c){return false;} };
        table = new JTable(tableModel) {
            public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r,row,col);
                String status = (String)getModel().getValueAt(row,4);
                if ("Sent".equals(status)) c.setForeground(UITheme.MID_GREEN);
                else c.setForeground(UITheme.RED_BTN);
                if (isRowSelected(row)) { c.setBackground(UITheme.TABLE_SEL); c.setForeground(Color.BLACK); }
                else c.setBackground(Color.WHITE);
                return c;
            }
        };
        UITheme.styleTable(table);
        table.getTableHeader().setFont(UITheme.FONT_SUBTITLE);
        table.getTableHeader().setBackground(UITheme.TABLE_HDR);
        table.setSelectionBackground(UITheme.TABLE_SEL);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        main.add(new JScrollPane(table), BorderLayout.CENTER);

        // Preview panel
        JPanel previewPanel = new JPanel(new BorderLayout(0,6));
        previewPanel.setBackground(Color.WHITE);
        previewPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1,0,0,0,new Color(200,200,200)),
            BorderFactory.createEmptyBorder(12,16,12,16)));
        JLabel previewTitle = new JLabel("Email Preview"); previewTitle.setFont(UITheme.FONT_SUBTITLE);
        txtPreview = new JTextArea(5,1);
        txtPreview.setFont(UITheme.FONT_BODY);
        txtPreview.setEditable(false);
        txtPreview.setLineWrap(true);
        txtPreview.setBackground(new Color(250,250,250));
        txtPreview.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        previewPanel.add(previewTitle, BorderLayout.NORTH);
        previewPanel.add(txtPreview, BorderLayout.CENTER);
        main.add(previewPanel, BorderLayout.SOUTH);

        add(main, BorderLayout.CENTER);

        // Events
        btnAll.addActionListener(e     -> { setActiveTab(btnAll);     loadAll(); });
        btnClients.addActionListener(e -> { setActiveTab(btnClients); loadByType("Client"); });
        btnAgents.addActionListener(e  -> { setActiveTab(btnAgents);  loadByType("Agent"); });
        btnSend.addActionListener(e    -> showSendDialog());

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow()>=0) {
                String subject = (String)tableModel.getValueAt(table.getSelectedRow(),2);
                String to      = (String)tableModel.getValueAt(table.getSelectedRow(),1);
                String date    = (String)tableModel.getValueAt(table.getSelectedRow(),0);
                String type    = (String)tableModel.getValueAt(table.getSelectedRow(),3);
                String body;
                if ("Agent".equals(type)) {
                    body = "Dear Agent,\n" + subject + "\nPlease ensure timely delivery.\n– GreenLoop Team";
                } else {
                    String orderId = subject.contains("ORD") ? subject.replaceAll(".*?(ORD-\\S+).*","$1") : "";
                    body = "Dear Valued Client,\nYour order " + orderId + " update: " + subject +
                           "\nThank you for choosing GreenLoop!\n– GreenLoop Team";
                }
                txtPreview.setText(body);
            }
        });
    }

    private void loadAll() {
        tableModel.setRowCount(0);
        for (EmailLog log : ec.getAllEmails()) addLogRow(log);
    }

    private void loadByType(String type) {
        tableModel.setRowCount(0);
        for (EmailLog log : ec.getEmailsByType(type)) addLogRow(log);
    }

    private void addLogRow(EmailLog log) {
        String sentAt = log.getSentAt() != null ? log.getSentAt() : "";
        String email  = log.getRecipientEmail();
        if (email != null && email.length()>22) email = email.substring(0,20)+"...";
        tableModel.addRow(new Object[]{sentAt, email, log.getSubject(), log.getType(), log.getStatus()});
    }

    private JButton makeTabButton(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setFont(UITheme.FONT_BODY);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(160,36));
        if (active) {
            btn.setBackground(Color.WHITE);
            btn.setForeground(UITheme.DARK_GREEN);
            btn.setBorder(BorderFactory.createMatteBorder(0,0,2,0,UITheme.DARK_GREEN));
        } else {
            btn.setBackground(new Color(245,245,245));
            btn.setForeground(Color.GRAY);
        }
        return btn;
    }

    private void setActiveTab(JButton active) {
        for (JButton b : new JButton[]{btnAll,btnClients,btnAgents}) {
            b.setBackground(new Color(245,245,245)); b.setForeground(Color.GRAY);
            b.setBorder(BorderFactory.createEmptyBorder());
        }
        active.setBackground(Color.WHITE);
        active.setForeground(UITheme.DARK_GREEN);
        active.setBorder(BorderFactory.createMatteBorder(0,0,2,0,UITheme.DARK_GREEN));
    }

    private void showSendDialog() {
        JDialog dlg = new JDialog((Frame)SwingUtilities.getWindowAncestor(this),"Send Email",true);
        dlg.setSize(480,380); dlg.setLocationRelativeTo(this);
        JPanel p = new JPanel(new BorderLayout(0,10));
        p.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));
        p.setBackground(Color.WHITE);

        JPanel fields = new JPanel(new GridLayout(5,2,8,8)); fields.setOpaque(false);

        
        JComboBox<String> cmbType = new JComboBox<>(new String[]{"Client","Delivery Agent"});
        cmbType.setFont(UITheme.FONT_BODY);

         
        List<Client> clients = cc.getAllClients();
        JComboBox<String> cmbRecipient = new JComboBox<>();
        for (Client c : clients) cmbRecipient.addItem(c.getBusinessName() + " <" + c.getEmail()+">");
        cmbRecipient.setFont(UITheme.FONT_BODY);
        cmbRecipient.setEditable(true);

        cmbType.addActionListener(e -> {
            cmbRecipient.removeAllItems();
            if ("Client".equals(cmbType.getSelectedItem())) {
                for (Client c : cc.getAllClients()) cmbRecipient.addItem(c.getBusinessName()+" <"+c.getEmail()+">");
            } else {
                for (DeliveryAgent a : agc.getAllAgents()) cmbRecipient.addItem(a.getFullName()+" <"+a.getEmail()+">");
            }
        });

        JTextField txtSubject = UITheme.makeField();
        JTextArea  txtBody    = new JTextArea(6,1);
        txtBody.setFont(UITheme.FONT_BODY); txtBody.setLineWrap(true);

        fields.add(new JLabel("Type:"));      fields.add(cmbType);
        fields.add(new JLabel("Recipient:")); fields.add(cmbRecipient);
        fields.add(new JLabel("Subject:"));   fields.add(txtSubject);
        p.add(fields, BorderLayout.NORTH);
        JPanel bodyPanel = new JPanel(new BorderLayout(0,4)); bodyPanel.setOpaque(false);
        bodyPanel.add(new JLabel("Message Body:"), BorderLayout.NORTH);
        bodyPanel.add(new JScrollPane(txtBody), BorderLayout.CENTER);
        p.add(bodyPanel, BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); btns.setOpaque(false);
        JButton btnSend = UITheme.makeButton("Send",   UITheme.MID_GREEN);
        JButton btnCancel = UITheme.makeButton("Cancel",UITheme.GRAY_BTN);
        btns.add(btnCancel); btns.add(btnSend);
        p.add(btns, BorderLayout.SOUTH);
        dlg.add(p);

        btnSend.addActionListener(e -> {
            String recipStr = (String)cmbRecipient.getSelectedItem();
            String subject  = txtSubject.getText().trim();
            String body     = txtBody.getText().trim();
            if (recipStr == null || recipStr.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Please select a recipient."); return;
            }
            if (subject.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Subject cannot be empty."); return;
            }
            if (body.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Message body cannot be empty."); return;
            }
            String email = recipStr.contains("<") ?
                recipStr.replaceAll(".*<(.+)>.*","$1") : recipStr;
            ec.sendEmail(email, subject, body);
            dlg.dispose();
            loadAll();
            JOptionPane.showMessageDialog(this,"Email sent and logged!");
        });
        btnCancel.addActionListener(e -> dlg.dispose());
        dlg.setVisible(true);
    }
}
