package com.pelugestion.ui;

import com.pelugestion.model.Ficha;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

/**
 * Dialogo para crear o editar una ficha de visita.
 */
public class FichaDialog extends JDialog {

    private static final Color PRIMARY = new Color(0x9B, 0x59, 0xB6);

    private JComboBox<String> servicioCombo;
    private JTextField productoField;
    private JTextField colorFormulaField;
    private JTextField precioField;
    private JTextArea observacionesArea;

    private Ficha resultado = null;
    private final Ficha fichaEditar;

    public FichaDialog(Window owner) {
        this(owner, null);
    }

    public FichaDialog(Window owner, Ficha fichaEditar) {
        super(owner, fichaEditar == null ? "Nueva Ficha" : "Editar Ficha",
                ModalityType.APPLICATION_MODAL);
        this.fichaEditar = fichaEditar;
        initComponents();
        if (fichaEditar != null) {
            loadFicha(fichaEditar);
        }
        pack();
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    private void initComponents() {
        JPanel panel = new JPanel(new MigLayout(
                "wrap 2, insets 20 25 20 25, gapy 8",
                "[right, 130!][grow, 260::]"));

        panel.add(label("Servicio *"));
        servicioCombo = new JComboBox<>(Ficha.SERVICIOS_PREDEFINIDOS);
        servicioCombo.setEditable(true);
        panel.add(servicioCombo, "growx");

        panel.add(label("Producto"));
        productoField = new JTextField();
        productoField.putClientProperty("JTextField.placeholderText", "Ej: L'Oreal Majirel");
        panel.add(productoField, "growx");

        panel.add(label("Color / Formula"));
        colorFormulaField = new JTextField();
        colorFormulaField.putClientProperty("JTextField.placeholderText", "Ej: 7.1 + 8.3 (40vol)");
        panel.add(colorFormulaField, "growx");

        panel.add(label("Precio (EUR)"));
        precioField = new JTextField();
        precioField.putClientProperty("JTextField.placeholderText", "0.00");
        panel.add(precioField, "growx");

        panel.add(label("Observaciones"), "top");
        observacionesArea = new JTextArea(3, 20);
        observacionesArea.setLineWrap(true);
        observacionesArea.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(observacionesArea);
        panel.add(scroll, "growx, growy");

        panel.add(new JSeparator(), "span, growx, gaptop 10, gapbottom 5");

        JPanel buttons = new JPanel(new MigLayout("insets 0", "[grow][][]"));

        JButton cancelBtn = new JButton("Cancelar");
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = new JButton("Guardar");
        saveBtn.setBackground(PRIMARY);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.putClientProperty("JButton.buttonType", "roundRect");
        saveBtn.addActionListener(e -> guardar());

        buttons.add(new JLabel(), "growx");
        buttons.add(cancelBtn);
        buttons.add(saveBtn);

        panel.add(buttons, "span, growx");
        setContentPane(panel);

        getRootPane().setDefaultButton(saveBtn);
    }

    private void loadFicha(Ficha f) {
        servicioCombo.setSelectedItem(f.getServicio());
        productoField.setText(f.getProducto());
        colorFormulaField.setText(f.getColorFormula());
        precioField.setText(f.getPrecio() > 0
                ? String.format("%.2f", f.getPrecio()) : "");
        observacionesArea.setText(f.getObservaciones());
    }

    private void guardar() {
        String servicio = servicioCombo.getSelectedItem() != null
                ? servicioCombo.getSelectedItem().toString().trim() : "";

        if (servicio.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "El servicio es obligatorio",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double precio = 0.0;
        String precioText = precioField.getText().trim().replace(",", ".");
        if (!precioText.isEmpty()) {
            try {
                precio = Double.parseDouble(precioText);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                        "El precio no es valido. Usa formato: 25.50",
                        "Aviso", JOptionPane.WARNING_MESSAGE);
                precioField.requestFocusInWindow();
                return;
            }
        }

        resultado = fichaEditar != null ? fichaEditar : new Ficha();
        resultado.setServicio(servicio);
        resultado.setProducto(productoField.getText().trim());
        resultado.setColorFormula(colorFormulaField.getText().trim());
        resultado.setPrecio(precio);
        resultado.setObservaciones(observacionesArea.getText().trim());

        dispose();
    }

    public Ficha getResultado() {
        return resultado;
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
         l.setFont(l.getFont().deriveFont(Font.PLAIN, 14f));
        return l;
    }
}
