package greenloop;

import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.FlatLaf;
import greenloop.util.UITheme;
import greenloop.view.LoginFrame;

import javax.swing.*;
import java.net.URL;

public class Main {
    public static void main(String[] args) {

        
        try {
            FlatLightLaf.setup();

            
            UIManager.put("Button.arc",                    999);   
            UIManager.put("Component.arc",                 8);     
            UIManager.put("TextComponent.arc",             8);
            UIManager.put("Component.focusWidth",          1);
            UIManager.put("Button.defaultButtonFollowsFocus", false);

            
            UIManager.put("Component.accentColor",         UITheme.MID_GREEN);
            UIManager.put("CheckBox.icon.selectedColor",   UITheme.MID_GREEN);
            UIManager.put("RadioButton.icon.selectedColor",UITheme.MID_GREEN);
            UIManager.put("ProgressBar.foreground",        UITheme.MID_GREEN);
            UIManager.put("Slider.thumbColor",             UITheme.MID_GREEN);

            
            UIManager.put("Table.selectionBackground",     UITheme.TABLE_SEL);
            UIManager.put("Table.selectionForeground",     UITheme.DARK_GREEN);
            UIManager.put("TableHeader.background",        UITheme.TABLE_HDR);
            UIManager.put("TableHeader.foreground",        UITheme.DARK_GREEN);

            
            UIManager.put("ScrollBar.width",               8);
            UIManager.put("ScrollBar.thumbArc",            999);
            UIManager.put("ScrollBar.thumbInsets",         new java.awt.Insets(2, 2, 2, 2));
            UIManager.put("ScrollBar.thumb",               UITheme.MID_GREEN);

            
            UIManager.put("ToolTip.background",            UITheme.DARK_GREEN);
            UIManager.put("ToolTip.foreground",            java.awt.Color.WHITE);

        } catch (Exception e) {
            
            System.err.println("[Main] FlatLaf not found, using system L&F. Add flatlaf JAR to libraries.");
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}
        }

        SwingUtilities.invokeLater(() -> {
            LoginFrame login = new LoginFrame();

            
            try {
                URL iconURL = Main.class.getResource("/greenloop/resources/icon.png");
                if (iconURL != null) {
                    login.setIconImage(new ImageIcon(iconURL).getImage());
                }
            } catch (Exception e) {
                System.err.println("Could not load icon: " + e.getMessage());
            }

            login.setVisible(true);
        });
    }
}
