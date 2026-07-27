package com.pelugestion;

import com.formdev.flatlaf.FlatIntelliJLaf;
import com.pelugestion.dao.DatabaseManager;
import com.pelugestion.ui.LoginDialog;
import com.pelugestion.ui.MainFrame;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Punto de entrada de la aplicacion PeluGestion.
 *
 * Flujo de inicio:
 *   1. Configura FlatLaf con fuente algo mas grande
 *   2. Localiza el fichero cifrado (junto a la app; modo portatil)
 *   3. Pide la contrasena y descifra la base de datos (se pide en CADA arranque)
 *   4. Backup automatico y ventana principal maximizada
 */
public class App {

    public static void main(String[] args) {
        try {
            UIManager.put("Component.arc", 10);
            UIManager.put("Button.arc", 10);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("ScrollBar.trackArc", 999);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.width", 11);
            UIManager.put("TabbedPane.showTabSeparators", true);
            FlatIntelliJLaf.setup();
            enlargeDefaultFont(2f);
        } catch (Exception e) {
            System.err.println("Error al configurar look-and-feel: " + e.getMessage());
        }

        SwingUtilities.invokeLater(() -> {
            try {
                Path store = DatabaseManager.resolveStorePath();
                boolean firstRun = !Files.exists(store);

                // Bucle de login: repite si la contrasena es incorrecta
                String error = null;
                while (true) {
                    LoginDialog login = new LoginDialog(firstRun, error);
                    login.setVisible(true);
                    if (!login.isOk()) {
                        System.exit(0);
                        return;
                    }
                    try {
                        DatabaseManager.initialize(store, login.getPassword(), firstRun);
                        break; // contrasena correcta
                    } catch (DatabaseManager.BadPasswordException bad) {
                        error = "Contrasena incorrecta, intentalo de nuevo";
                        firstRun = false;
                    }
                }

                DatabaseManager.getInstance().createBackup();

                MainFrame mainFrame = new MainFrame();
                mainFrame.setVisible(true);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "Error al iniciar la aplicacion:\n" + e.getMessage(),
                        "Error Fatal",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                System.exit(1);
            }
        });
    }

    private static void enlargeDefaultFont(float delta) {
        Font base = UIManager.getFont("defaultFont");
        if (base == null) base = UIManager.getFont("Label.font");
        if (base == null) base = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
        Font bigger = base.deriveFont(base.getSize2D() + delta);
        UIManager.put("defaultFont", new FontUIResource(bigger));
    }
}
