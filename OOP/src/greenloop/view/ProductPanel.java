package greenloop.view;

import greenloop.controller.ProductController;
import greenloop.model.Product;
import greenloop.util.UITheme;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class ProductPanel extends JPanel {

    private ProductController ctrl = new ProductController();
    private JTextField txtId, txtName, txtPrice;
    private JComboBox<String> cmbCategory, cmbStatus;
    private JSpinner spnRating;
    private JTextArea txtDesc;
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton btnSave, btnUpdate, btnDelete, btnClear;
    private int selectedProductId = -1;

    public ProductPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(UITheme.BG);
        initUI();
        loadTable(ctrl.getAllProducts());
    }

    private void initUI() {
        add(UITheme.pageHeader("Product Management", "Add, edit and manage eco-friendly products"), BorderLayout.NORTH);

        JPanel body = new JPanel(new GridLayout(1, 2, 12, 0));
        body.setBackground(UITheme.BG);
        body.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));
        body.add(buildForm());
        body.add(buildTablePanel());
        add(body, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        btnPanel.setBackground(UITheme.BG);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(0, 16, 8, 16));
        btnSave   = UITheme.makeButton("Save",   UITheme.MID_GREEN);
        btnUpdate = UITheme.makeButton("Update", UITheme.BLUE_BTN);
        btnDelete = UITheme.makeButton("Delete", UITheme.RED_BTN);
        btnClear  = UITheme.makeButton("Clear",  UITheme.GRAY_BTN);
        for (JButton b : new JButton[]{btnSave, btnUpdate, btnDelete, btnClear})
            b.setPreferredSize(new Dimension(120, 38));
        btnPanel.add(btnSave); btnPanel.add(btnUpdate); btnPanel.add(btnDelete); btnPanel.add(btnClear);
        add(btnPanel, BorderLayout.SOUTH);
        wireEvents();
    }

    private JPanel buildForm() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 230, 220), 1),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(6, 4, 6, 4);
        gc.weightx = 1;

        txtId = makeField(false);
        txtName = makeField(true);
        cmbCategory = new JComboBox<>(new String[]{"Bags","Boxes","Wraps","Tape","Paper","Specialty"});
        cmbCategory.setFont(UITheme.FONT_BODY);
        txtPrice = makeField(true);
        spnRating = new JSpinner(new SpinnerNumberModel(3, 1, 5, 1));
        spnRating.setFont(UITheme.FONT_BODY);
        txtDesc = new JTextArea(3, 1);
        txtDesc.setFont(UITheme.FONT_BODY);
        txtDesc.setLineWrap(true);
        cmbStatus = new JComboBox<>(new String[]{"Active", "Inactive"});
        cmbStatus.setFont(UITheme.FONT_BODY);

        int row = 0;
        addFormRow(p, gc, row++, "Product ID",      txtId);
        addFormRow(p, gc, row++, "Product Name",    txtName);
        addFormRow(p, gc, row++, "Category",        cmbCategory);
        addFormRow(p, gc, row++, "Price (Rs.)",     txtPrice);
        addFormRow(p, gc, row++, "Eco Rating (1-5)", spnRating);
        addFormRow(p, gc, row++, "Status",          cmbStatus);

        gc.gridx=0; gc.gridy=row; gc.gridwidth=1; gc.weightx=0;
        JLabel descLbl = new JLabel("Description"); descLbl.setFont(UITheme.FONT_BODY);
        p.add(descLbl, gc);
        gc.gridx=1; gc.weightx=1;
        p.add(new JScrollPane(txtDesc), gc);
        row++;

        
        JPanel imgPnl = new JPanel(new BorderLayout(4, 4));
        imgPnl.setBackground(new Color(248, 252, 248));
        imgPnl.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 230, 220)),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        JLabel imgLbl = new JLabel("No Image", SwingConstants.CENTER);
        imgLbl.setPreferredSize(new Dimension(100, 90));
        imgLbl.setOpaque(true);
        imgLbl.setBackground(new Color(240, 248, 240));
        imgLbl.setFont(UITheme.FONT_SMALL);
        imgLbl.setForeground(Color.GRAY);
        JButton btnImg = UITheme.makeButton("Choose Image", UITheme.GRAY_BTN);
        btnImg.setPreferredSize(new Dimension(140, 32));
        btnImg.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Image Files", "jpg","jpeg","png","gif","bmp"));
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                ImageIcon icon = new ImageIcon(fc.getSelectedFile().getAbsolutePath());
                imgLbl.setIcon(new ImageIcon(icon.getImage().getScaledInstance(100, 90, Image.SCALE_SMOOTH)));
                imgLbl.setText("");
            }
        });
        imgPnl.add(imgLbl,  BorderLayout.CENTER);
        imgPnl.add(btnImg,  BorderLayout.SOUTH);

        gc.gridx=0; gc.gridy=row; gc.gridwidth=2; gc.weightx=1;
        p.add(imgPnl, gc);
        return p;
    }

    private JPanel buildTablePanel() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(UITheme.BG);

        JPanel searchBar = new JPanel(new BorderLayout(6, 0));
        searchBar.setBackground(UITheme.BG);
        JTextField sf = makeField(true); sf.setToolTipText("Search products...");
        JButton sb = UITheme.makeButton("Search", UITheme.DARK_GREEN);
        sb.setPreferredSize(new Dimension(100, 34));
        searchBar.add(sf, BorderLayout.CENTER);
        searchBar.add(sb, BorderLayout.EAST);
        p.add(searchBar, BorderLayout.NORTH);

        String[] cols = {"ID","Product Name","Price (Rs.)","Eco Rating","Stock"};
        tableModel = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r,int c){return false;} };
        table = new JTable(tableModel);
        UITheme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(220, 230, 220)));
        sp.getViewport().setBackground(Color.WHITE);
        p.add(sp, BorderLayout.CENTER);

        JPanel foot = new JPanel(new BorderLayout());
        foot.setBackground(UITheme.BG);
        JLabel totalLbl = new JLabel("", SwingConstants.RIGHT);
        totalLbl.setFont(UITheme.FONT_SMALL);
        totalLbl.setForeground(UITheme.DARK_GREEN);
        foot.add(totalLbl, BorderLayout.EAST);
        p.add(foot, BorderLayout.SOUTH);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                String idStr = tableModel.getValueAt(table.getSelectedRow(), 0).toString();
                int pid = Integer.parseInt(idStr.replace("P-",""));
                for (Product pr : ctrl.getAllProducts()) if (pr.getProductId()==pid) { populateForm(pr); break; }
            }
        });
        sb.addActionListener(e -> { List<Product> r=ctrl.searchProducts(sf.getText().trim()); loadTable(r); totalLbl.setText("Results: "+r.size()+"  "); });
        sf.addActionListener(e -> sb.doClick());
        return p;
    }

    private void loadTable(List<Product> products) {
        tableModel.setRowCount(0);
        for (Product p : products)
            tableModel.addRow(new Object[]{"P-"+p.getProductId(), p.getProductName(),
                String.format("%.2f", p.getPrice()), p.getEcoRating()+"/5", p.getStock()});
    }

    private void populateForm(Product p) {
        selectedProductId = p.getProductId();
        txtId.setText("P-"+p.getProductId());
        txtName.setText(p.getProductName());
        cmbCategory.setSelectedItem(p.getCategory());
        txtPrice.setText(String.valueOf(p.getPrice()));
        spnRating.setValue((Integer) p.getEcoRating());
        txtDesc.setText(p.getDescription());
        cmbStatus.setSelectedItem(p.getStatus()!=null?p.getStatus():"Active");
    }

    private void clearForm() {
        selectedProductId=-1; txtId.setText("Auto"); txtName.setText("");
        txtPrice.setText(""); txtDesc.setText(""); cmbCategory.setSelectedIndex(0);
        spnRating.setValue(3); table.clearSelection();
    }

    private void wireEvents() {
        btnSave.addActionListener(e -> {
            if (!validateForm()) return;
            if (ctrl.addProduct(buildProduct())) { JOptionPane.showMessageDialog(this,"Product saved!"); loadTable(ctrl.getAllProducts()); clearForm(); }
            else JOptionPane.showMessageDialog(this,"Error saving.","Error",JOptionPane.ERROR_MESSAGE);
        });
        btnUpdate.addActionListener(e -> {
            if (selectedProductId<0){JOptionPane.showMessageDialog(this,"Select a product.");return;}
            if (!validateForm()) return;
            Product p=buildProduct(); p.setProductId(selectedProductId);
            if (ctrl.updateProduct(p)) { JOptionPane.showMessageDialog(this,"Updated!"); loadTable(ctrl.getAllProducts()); }
        });
        btnDelete.addActionListener(e -> {
            if (selectedProductId<0){JOptionPane.showMessageDialog(this,"Select a product.");return;}
            if (JOptionPane.showConfirmDialog(this,"Delete this product?","Confirm",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION)
            { ctrl.deleteProduct(selectedProductId); loadTable(ctrl.getAllProducts()); clearForm(); }
        });
        btnClear.addActionListener(e -> clearForm());
    }

    private boolean validateForm() {
        if (txtName.getText().trim().isEmpty()){JOptionPane.showMessageDialog(this,"Name required.");return false;}
        try{Double.parseDouble(txtPrice.getText().trim());}catch(NumberFormatException ex){JOptionPane.showMessageDialog(this,"Valid price required.");return false;}
        return true;
    }

    private Product buildProduct() {
        Product p=new Product();
        p.setProductName(txtName.getText().trim());
        p.setCategory((String)cmbCategory.getSelectedItem());
        p.setPrice(Double.parseDouble(txtPrice.getText().trim()));
        p.setEcoRating((Integer)spnRating.getValue());
        p.setDescription(txtDesc.getText().trim());
        p.setStatus((String)cmbStatus.getSelectedItem());
        return p;
    }

    private JTextField makeField(boolean editable) {
        JTextField tf = new JTextField();
        tf.setFont(UITheme.FONT_BODY);
        tf.setEditable(editable);
        if (!editable) tf.setBackground(new Color(245,248,245));
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200,220,200)),
            BorderFactory.createEmptyBorder(5,8,5,8)));
        tf.setPreferredSize(new Dimension(200,34));
        return tf;
    }

    private void addFormRow(JPanel p,GridBagConstraints gc,int row,String label,Component field) {
        gc.gridx=0;gc.gridy=row;gc.gridwidth=1;gc.weightx=0;
        JLabel lbl=new JLabel(label);lbl.setFont(UITheme.FONT_BODY);p.add(lbl,gc);
        gc.gridx=1;gc.weightx=1;p.add(field,gc);
    }
}