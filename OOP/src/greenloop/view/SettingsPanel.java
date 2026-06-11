package greenloop.view;

import greenloop.controller.AuthController;
import greenloop.database.DBConnection;
import greenloop.model.GlUser;
import greenloop.util.UITheme;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SettingsPanel extends JPanel {

    private GlUser currentUser;
    private JTextField  txtFullName, txtEmail, txtDbUrl, txtDbUser;
    private JPasswordField txtDbPass;
    private JPasswordField txtOldPass, txtNewPass, txtConfirmPass;

    public SettingsPanel(GlUser user) {
        this.currentUser = user;
        setLayout(new BorderLayout(12,12));
        setBackground(UITheme.BG);
        setBorder(BorderFactory.createEmptyBorder(24,28,16,28));
        initUI();
    }

    private void initUI() {
        JLabel title = new JLabel("Settings");
        title.setFont(UITheme.FONT_TITLE);
        add(title, BorderLayout.NORTH);

        boolean isEmployee = currentUser != null &&
            "employee".equalsIgnoreCase(currentUser.getRole());

        if (isEmployee) {
            JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT));
            wrapper.setOpaque(false);
            wrapper.add(buildReadOnlyProfilePanel());
            add(wrapper, BorderLayout.CENTER);
        } else {
            
            JPanel grid = new JPanel(new GridLayout(1, 3, 16, 0));
            grid.setOpaque(false);
            grid.add(buildProfilePanel());
            grid.add(buildManageUserPasswordsPanel());
            grid.add(buildDbPanel());

            JPanel center = new JPanel(new BorderLayout(0, 16));
            center.setOpaque(false);
            center.add(grid, BorderLayout.CENTER);
            center.add(buildAddEmployeePanel(), BorderLayout.SOUTH);
            add(center, BorderLayout.CENTER);
        }
    }

    private JPanel buildReadOnlyProfilePanel() {
        JPanel profile = new JPanel(new GridBagLayout());
        profile.setBackground(Color.WHITE);
        profile.setPreferredSize(new Dimension(380, 200));
        profile.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("My Profile"),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(6, 4, 6, 4);
        gc.weightx = 1;

        JTextField roName  = UITheme.makeField();
        JTextField roEmail = UITheme.makeField();
        JTextField roRole  = UITheme.makeField();

        roName.setText(currentUser != null ? currentUser.getFullName() : "");
        roEmail.setText(currentUser != null ? currentUser.getEmail() : "");
        roRole.setText(currentUser != null ? currentUser.getRole() : "");

        for (JTextField f : new JTextField[]{roName, roEmail, roRole}) {
            f.setEditable(false);
            f.setBackground(new Color(245, 245, 245));
        }

        int row = 0;
        addRow(profile, gc, row++, "Full Name", roName);
        addRow(profile, gc, row++, "Email",     roEmail);
        addRow(profile, gc, row++, "Role",      roRole);

        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 2;
        JLabel note = new JLabel("Contact your administrator to update your profile or password.");
        note.setFont(UITheme.FONT_SMALL);
        note.setForeground(Color.GRAY);
        profile.add(note, gc);

        return profile;
    }

    private JPanel buildProfilePanel() {
        JPanel outer = new JPanel(new BorderLayout(0,12));
        outer.setOpaque(false);

        JPanel profile = new JPanel(new GridBagLayout());
        profile.setBackground(Color.WHITE);
        profile.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("My Profile"),
            BorderFactory.createEmptyBorder(12,12,12,12)));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill=GridBagConstraints.HORIZONTAL; gc.insets=new Insets(5,4,5,4); gc.weightx=1;

        txtFullName = UITheme.makeField();
        txtFullName.setText(currentUser != null ? currentUser.getFullName() : "");
        txtEmail    = UITheme.makeField();
        txtEmail.setText(currentUser != null ? currentUser.getEmail() : "");
        JTextField txtRole = UITheme.makeField();
        txtRole.setText(currentUser != null ? currentUser.getRole() : "");
        txtRole.setEditable(false);
        txtRole.setBackground(new Color(245,245,245));

        int row=0;
        addRow(profile,gc,row++,"Full Name",txtFullName);
        addRow(profile,gc,row++,"Email",    txtEmail);
        addRow(profile,gc,row++,"Role",     txtRole);

        JButton btnSaveProfile = UITheme.makeButton("Save Profile",UITheme.MID_GREEN);
        gc.gridx=0; gc.gridy=row; gc.gridwidth=2;
        profile.add(btnSaveProfile, gc);
        
        btnSaveProfile.addActionListener(e -> saveProfile());

        JPanel passPanel = new JPanel(new GridBagLayout());
        passPanel.setBackground(Color.WHITE);
        passPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Change My Password"),
            BorderFactory.createEmptyBorder(12,12,12,12)));
        GridBagConstraints gc2 = new GridBagConstraints();
        gc2.fill=GridBagConstraints.HORIZONTAL; gc2.insets=new Insets(5,4,5,4); gc2.weightx=1;

        txtOldPass     = new JPasswordField(); txtOldPass.setFont(UITheme.FONT_BODY);
        txtNewPass     = new JPasswordField(); txtNewPass.setFont(UITheme.FONT_BODY);
        txtConfirmPass = new JPasswordField(); txtConfirmPass.setFont(UITheme.FONT_BODY);

        int pr=0;
        addRowPass(passPanel,gc2,pr++,"Current Password",txtOldPass);
        addRowPass(passPanel,gc2,pr++,"New Password",    txtNewPass);
        addRowPass(passPanel,gc2,pr++,"Confirm Password",txtConfirmPass);
        JButton btnChangePwd = UITheme.makeButton("Change Password",UITheme.BLUE_BTN);
        btnChangePwd.setPreferredSize(new Dimension(160,36));
        gc2.gridx=0; gc2.gridy=pr; gc2.gridwidth=2;
        passPanel.add(btnChangePwd, gc2);
        btnChangePwd.addActionListener(e -> changeOwnPassword());

        outer.add(profile,  BorderLayout.NORTH);
        outer.add(passPanel,BorderLayout.CENTER);
        return outer;
    }

   
    private void saveProfile() {
        String newName  = txtFullName.getText().trim();
        String newEmail = txtEmail.getText().trim();
        if (newName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Full name cannot be empty.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String sql = "UPDATE gl_users SET full_name=?, email=? WHERE user_id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, newName);
            ps.setString(2, newEmail);
            ps.setInt(3, currentUser.getUserId());
            if (ps.executeUpdate() > 0) {
                currentUser.setFullName(newName);
                currentUser.setEmail(newEmail);
                JOptionPane.showMessageDialog(this, "Profile updated successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "No changes were saved.", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel buildManageUserPasswordsPanel() {
        JPanel outer = new JPanel(new BorderLayout(0, 12));
        outer.setOpaque(false);

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Manage User Passwords"),
            BorderFactory.createEmptyBorder(12,12,12,12)));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(6, 4, 6, 4);
        gc.weightx = 1;

        JComboBox<String> cmbUsers = new JComboBox<>();
        cmbUsers.setFont(UITheme.FONT_BODY);
        List<int[]> userIds = new ArrayList<>();

        
        Connection conn = DBConnection.getConnection();
        if (conn != null) {
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(
                     "SELECT user_id, username, full_name, role FROM gl_users WHERE active=1 ORDER BY role, username")) {
                while (rs.next()) {
                    int uid   = rs.getInt("user_id");
                    String display = rs.getString("username") + " – " + rs.getString("full_name")
                                   + "  [" + rs.getString("role") + "]";
                    cmbUsers.addItem(display);
                    userIds.add(new int[]{uid});
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                cmbUsers.addItem("(Cannot load users – check DB connection)");
            }
        } else {
            cmbUsers.addItem("(No database connection)");
        }

        JPasswordField txtAdminNewPass     = new JPasswordField(); txtAdminNewPass.setFont(UITheme.FONT_BODY);
        JPasswordField txtAdminConfirmPass = new JPasswordField(); txtAdminConfirmPass.setFont(UITheme.FONT_BODY);

        int row = 0;
        gc.gridx=0; gc.gridy=row; gc.gridwidth=1; gc.weightx=0;
        p.add(new JLabel("Select User"), gc);
        gc.gridx=1; gc.weightx=1;
        p.add(cmbUsers, gc);
        row++;

        addRowPass(p, gc, row++, "New Password",     txtAdminNewPass);
        addRowPass(p, gc, row++, "Confirm Password", txtAdminConfirmPass);

        JButton btnReset = UITheme.makeButton("Reset Password", UITheme.RED_BTN);
        gc.gridx=0; gc.gridy=row; gc.gridwidth=2;
        p.add(btnReset, gc);

        btnReset.addActionListener(e -> {
            int selectedIdx = cmbUsers.getSelectedIndex();
            if (selectedIdx < 0 || selectedIdx >= userIds.size()) {
                JOptionPane.showMessageDialog(this, "Please select a user.");
                return;
            }
            String newPass  = new String(txtAdminNewPass.getPassword());
            String confirm  = new String(txtAdminConfirmPass.getPassword());
            if (newPass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "New password cannot be empty.");
                return;
            }
            if (!newPass.equals(confirm)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match.");
                return;
            }
            if (newPass.length() < 6) {
                JOptionPane.showMessageDialog(this, "Password must be at least 6 characters.");
                return;
            }
            int targetUserId = userIds.get(selectedIdx)[0];
            String selectedLabel = (String) cmbUsers.getSelectedItem();
            int confirm2 = JOptionPane.showConfirmDialog(this,
                "Reset password for:\n" + selectedLabel + "\n\nAre you sure?",
                "Confirm Reset", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm2 != JOptionPane.YES_OPTION) return;

            String sql = "UPDATE gl_users SET password_hash=? WHERE user_id=?";
            try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
                ps.setString(1, AuthController.sha256(newPass));
                ps.setInt(2, targetUserId);
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    JOptionPane.showMessageDialog(this,
                        "✅ Password reset successfully for:\n" + selectedLabel,
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    txtAdminNewPass.setText("");
                    txtAdminConfirmPass.setText("");
                } else {
                    JOptionPane.showMessageDialog(this, "❌ Failed to reset password.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "❌ Database error:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        outer.add(p, BorderLayout.NORTH);
        return outer;
    }

    private JPanel buildDbPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Database Connection"),
            BorderFactory.createEmptyBorder(12,12,12,12)));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill=GridBagConstraints.HORIZONTAL; gc.insets=new Insets(5,4,5,4); gc.weightx=1;

        txtDbUrl  = UITheme.makeField(); txtDbUrl.setText("jdbc:mysql://localhost:3306/master");
        txtDbUser = UITheme.makeField(); txtDbUser.setText("root");
        txtDbPass = new JPasswordField(); txtDbPass.setFont(UITheme.FONT_BODY);

        int row=0;
        addRow(p,gc,row++,"DB URL",      txtDbUrl);
        addRow(p,gc,row++,"DB Username", txtDbUser);
        addRowPass(p,gc,row++,"DB Password",txtDbPass);

        JButton btnTest = UITheme.makeButton("Test Connection", UITheme.MID_GREEN);
        btnTest.setPreferredSize(new Dimension(160,36));
        gc.gridx=0; gc.gridy=row; gc.gridwidth=2;
        p.add(btnTest, gc);

        btnTest.addActionListener(e -> {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                Connection conn = DriverManager.getConnection(
                    txtDbUrl.getText().trim(),
                    txtDbUser.getText().trim(),
                    new String(txtDbPass.getPassword()));
                conn.close();
                JOptionPane.showMessageDialog(this,"✅ Connection successful!","DB Test",JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,"❌ Connection failed:\n"+ex.getMessage(),"DB Test",JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel wrap = new JPanel(new BorderLayout(0,12));
        wrap.setOpaque(false);
        wrap.add(p, BorderLayout.NORTH);
        return wrap;
    }

    private void changeOwnPassword() {
        String oldPass = new String(txtOldPass.getPassword());
        String newPass = new String(txtNewPass.getPassword());
        String confirm = new String(txtConfirmPass.getPassword());
        if (oldPass.isEmpty()) { JOptionPane.showMessageDialog(this,"Enter your current password."); return; }
        if (!newPass.equals(confirm)) { JOptionPane.showMessageDialog(this,"Passwords do not match."); return; }
        if (newPass.length()<6) { JOptionPane.showMessageDialog(this,"Password must be at least 6 characters."); return; }
        String oldHash = AuthController.sha256(oldPass);
        String sql = "UPDATE gl_users SET password_hash=? WHERE user_id=? AND password_hash=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, AuthController.sha256(newPass));
            ps.setInt(2, currentUser.getUserId());
            ps.setString(3, oldHash);
            int rows = ps.executeUpdate();
            if (rows>0) JOptionPane.showMessageDialog(this,"Password changed successfully!");
            else JOptionPane.showMessageDialog(this,"Current password is incorrect.","Error",JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) { ex.printStackTrace(); }
        txtOldPass.setText(""); txtNewPass.setText(""); txtConfirmPass.setText("");
    }

    private JPanel buildAddEmployeePanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setOpaque(false);

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Add Employee Account"),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(6, 4, 6, 4);

        JTextField txtNewUsername  = UITheme.makeField();
        JTextField txtNewFullName  = UITheme.makeField();
        JTextField txtNewEmail     = UITheme.makeField();
        JPasswordField txtNewPwd   = new JPasswordField(); txtNewPwd.setFont(UITheme.FONT_BODY);
        JPasswordField txtConfPwd  = new JPasswordField(); txtConfPwd.setFont(UITheme.FONT_BODY);
        JComboBox<String> cmbRole  = new JComboBox<>(new String[]{"employee", "admin"});
        cmbRole.setFont(UITheme.FONT_BODY);

        
        int col = 0, row = 0;
       
        gc.gridx=0; gc.gridy=row; gc.weightx=0; gc.gridwidth=1; p.add(new JLabel("Username *"), gc);
        gc.gridx=1; gc.weightx=1; p.add(txtNewUsername, gc);
        gc.gridx=2; gc.weightx=0; p.add(new JLabel("Full Name *"), gc);
        gc.gridx=3; gc.weightx=1; p.add(txtNewFullName, gc);
        row++;
        
        gc.gridx=0; gc.gridy=row; gc.weightx=0; gc.gridwidth=1; p.add(new JLabel("Email"), gc);
        gc.gridx=1; gc.weightx=1; p.add(txtNewEmail, gc);
        gc.gridx=2; gc.weightx=0; p.add(new JLabel("Role *"), gc);
        gc.gridx=3; gc.weightx=1; p.add(cmbRole, gc);
        row++;
        
        gc.gridx=0; gc.gridy=row; gc.weightx=0; gc.gridwidth=1; p.add(new JLabel("Password *"), gc);
        gc.gridx=1; gc.weightx=1; p.add(txtNewPwd, gc);
        gc.gridx=2; gc.weightx=0; p.add(new JLabel("Confirm Password *"), gc);
        gc.gridx=3; gc.weightx=1; p.add(txtConfPwd, gc);
        row++;
       
        JButton btnCreate = UITheme.makeButton("Create Account", UITheme.MID_GREEN);
        btnCreate.setPreferredSize(new Dimension(160, 36));
        gc.gridx=3; gc.gridy=row; gc.gridwidth=1; gc.weightx=0;
        gc.anchor = GridBagConstraints.EAST;
        p.add(btnCreate, gc);

        btnCreate.addActionListener(e -> {
            String uname   = txtNewUsername.getText().trim();
            String fname   = txtNewFullName.getText().trim();
            String email   = txtNewEmail.getText().trim();
            String pwd     = new String(txtNewPwd.getPassword());
            String conf    = new String(txtConfPwd.getPassword());
            String role    = (String) cmbRole.getSelectedItem();

            
            if (uname.isEmpty() || fname.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username and Full Name are required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (uname.contains(" ")) {
                JOptionPane.showMessageDialog(this, "Username must not contain spaces.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (pwd.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Password is required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (pwd.length() < 6) {
                JOptionPane.showMessageDialog(this, "Password must be at least 6 characters.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!pwd.equals(conf)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

           
            Connection conn = DBConnection.getConnection();
            if (conn == null) {
                JOptionPane.showMessageDialog(this, "No database connection.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                PreparedStatement chk = conn.prepareStatement("SELECT user_id FROM gl_users WHERE username=?");
                chk.setString(1, uname);
                if (chk.executeQuery().next()) {
                    JOptionPane.showMessageDialog(this, "Username \"" + uname + "\" already exists. Choose a different username.", "Duplicate", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                String sql = "INSERT INTO gl_users (username, password_hash, full_name, email, role, active) VALUES (?,?,?,?,?,1)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, uname);
                ps.setString(2, AuthController.sha256(pwd));
                ps.setString(3, fname);
                ps.setString(4, email);
                ps.setString(5, role);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this,
                    "✅ Account created successfully!\n\nUsername: " + uname + "\nRole: " + role,
                    "Success", JOptionPane.INFORMATION_MESSAGE);

               
                txtNewUsername.setText("");
                txtNewFullName.setText("");
                txtNewEmail.setText("");
                txtNewPwd.setText("");
                txtConfPwd.setText("");
                cmbRole.setSelectedIndex(0);

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "❌ Database error:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        outer.add(p, BorderLayout.WEST);
        return outer;
    }

    private void addRow(JPanel p,GridBagConstraints gc,int row,String label,JTextField field) {
        gc.gridx=0;gc.gridy=row;gc.weightx=0;gc.gridwidth=1; p.add(new JLabel(label),gc);
        gc.gridx=1;gc.weightx=1; p.add(field,gc);
    }
    private void addRowPass(JPanel p,GridBagConstraints gc,int row,String label,JPasswordField field) {
        gc.gridx=0;gc.gridy=row;gc.weightx=0;gc.gridwidth=1; p.add(new JLabel(label),gc);
        gc.gridx=1;gc.weightx=1; p.add(field,gc);
    }
}
