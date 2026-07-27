package com.pelugestion.ui;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import com.pelugestion.dao.ClienteDAO;
import com.pelugestion.model.Cliente;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Locale;

/**
 * Panel de detalle del cliente seleccionado.
 * Muestra los datos del cliente ocupando toda la parte derecha.
 */
public class ClienteDetailPanel extends JPanel {

    private static final Color GRIS_CLARO = new Color(0xF0, 0xF1, 0xF3);

    private final ClienteDAO clienteDAO;

    private Cliente clienteActual;

    // Campos del formulario
    private JTextField nombreField;
    private JTextField telefonoField;
    private DatePicker cumpleanosPicker;
    private JTextField direccionField;
    private JTextField ciudadField;
    private JComboBox<String> provinciaCombo;
    private JTextField codigoPostalField;
    private JTextArea descripcionPreview; // vista previa (solo lectura); se edita en un dialogo

    // Callback cuando se modifica un cliente
    private Runnable onClienteChanged;

    // CardLayout: "empty" / "content"
    private CardLayout cardLayout;

    public ClienteDetailPanel(ClienteDAO clienteDAO) {
        this.clienteDAO = clienteDAO;
        initComponents();
    }

    private void initComponents() {
        cardLayout = new CardLayout();
        setLayout(cardLayout);

        add(createEmptyPanel(), "empty");
        add(createContentPanel(), "content");

        cardLayout.show(this, "empty");
    }

    private JPanel createEmptyPanel() {
        JPanel panel = new JPanel(new MigLayout("fill", "[center]", "[center]"));
        JLabel label = new JLabel("Selecciona un cliente para ver su ficha");
        label.setFont(label.getFont().deriveFont(Font.PLAIN, 18f));
        label.setForeground(UiFactory.TEXT_MUTED);
        panel.add(label);
        return panel;
    }

    private JPanel createContentPanel() {
        // Filas: [titulo][formulario que crece][botones]
        JPanel panel = new JPanel(new MigLayout(
                "fill, wrap, insets 22 20 22 22",
                "[grow]",
                "[][grow]"));

        JLabel clienteTitle = new JLabel("Datos del Cliente");
        clienteTitle.setFont(clienteTitle.getFont().deriveFont(Font.BOLD, 21f));
        panel.add(clienteTitle, "gapbottom 10");

        // --- Formulario ---
        // gapy uniforme entre todas las filas; etiquetas pegadas a la izquierda.
        JPanel formPanel = new JPanel(new MigLayout(
                "fill, wrap 2, insets 12 10 12 12, gapx 12, gapy 10",
                "[left, 135!][grow]",
                ""));
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE0, 0xE0, 0xE0)),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)));

        formPanel.add(label("Nombre completo *"));
        nombreField = new JTextField();
        nombreField.putClientProperty("JTextField.placeholderText", "Ej: Maria Lopez Garcia");
        formPanel.add(nombreField, "growx, h 32!");

        formPanel.add(label("Telefono"));
        telefonoField = new JTextField();
        telefonoField.putClientProperty("JTextField.placeholderText", "Ej: 600 123 456");
        formPanel.add(telefonoField, "growx, h 32!");

        formPanel.add(label("Cumpleanos"));
        cumpleanosPicker = crearDatePicker();
        formPanel.add(cumpleanosPicker, "growx, h 32!");

        formPanel.add(label("Direccion"));
        direccionField = new JTextField();
        formPanel.add(direccionField, "growx, h 32!");

        formPanel.add(label("Ciudad"));
        ciudadField = new JTextField();
        formPanel.add(ciudadField, "growx, h 32!");

        formPanel.add(label("Provincia"));
        provinciaCombo = new JComboBox<>(Cliente.PROVINCIAS);
        provinciaCombo.setEditable(true);
        provinciaCombo.setSelectedIndex(-1);
        formPanel.add(provinciaCombo, "growx, h 32!");

        formPanel.add(label("Codigo postal"));
        codigoPostalField = new JTextField();
        formPanel.add(codigoPostalField, "growx, h 32!");

        formPanel.add(label("Detalles y extras"), "top, gaptop 4");
        formPanel.add(createDescripcionBox(), "grow, pushy, hmin 110"); // ocupa el espacio restante

        panel.add(formPanel, "grow");

        return panel;
    }

    private DatePicker crearDatePicker() {
        DatePickerSettings settings = new DatePickerSettings(new Locale("es", "ES"));
        settings.setFormatForDatesCommonEra("dd/MM/yyyy");
        settings.setAllowEmptyDates(true);
        settings.setFontValidDate(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
        DatePicker picker = new DatePicker(settings);
        return picker;
    }

    /**
     * Caja gris clarita para "Detalles y extras": muestra una vista previa del
     * texto, un boton rojo en cursiva abajo a la izquierda, y al hacer clic en
     * cualquier parte del fondo abre un dialogo con el editor ampliado.
     */
    private JPanel createDescripcionBox() {
        JPanel box = new JPanel(new MigLayout("fill, insets 8", "[grow]", "[grow][]"));
        box.setBackground(GRIS_CLARO);
        box.setBorder(BorderFactory.createLineBorder(new Color(0xD5, 0xD8, 0xDC)));
        box.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        descripcionPreview = new JTextArea();
        descripcionPreview.setEditable(false);
        descripcionPreview.setLineWrap(true);
        descripcionPreview.setWrapStyleWord(true);
        descripcionPreview.setOpaque(false); // deja ver el fondo gris
        descripcionPreview.setFont(descripcionPreview.getFont().deriveFont(16f));
        descripcionPreview.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JScrollPane sc = new JScrollPane(descripcionPreview);
        sc.setOpaque(false);
        sc.getViewport().setOpaque(false);
        sc.setBorder(BorderFactory.createEmptyBorder());
        box.add(sc, "grow, wrap");

        JButton clicaBtn = new JButton("Clica aquí!");
        clicaBtn.setForeground(UiFactory.DANGER);
        clicaBtn.setFont(clicaBtn.getFont().deriveFont(Font.ITALIC, 15f));
        clicaBtn.setContentAreaFilled(false);
        clicaBtn.setBorderPainted(false);
        clicaBtn.setFocusPainted(false);
        clicaBtn.setMargin(new Insets(0, 0, 0, 0));
        clicaBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        clicaBtn.addActionListener(e -> abrirDescripcionDialog());
        box.add(clicaBtn, "left");

        // Clic en cualquier parte del fondo (o del texto) abre el editor ampliado
        MouseAdapter abrir = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                abrirDescripcionDialog();
            }
        };
        box.addMouseListener(abrir);
        descripcionPreview.addMouseListener(abrir);
        sc.getViewport().addMouseListener(abrir);

        return box;
    }

    /** Ventana emergente con el texto ampliado de "Detalles y extras". */
    private void abrirDescripcionDialog() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, "Detalles y extras",
                Dialog.ModalityType.APPLICATION_MODAL);

        JPanel p = new JPanel(new MigLayout(
                "fill, wrap, insets 18", "[grow]", "[][grow][]"));

        JLabel titulo = new JLabel("Detalles y extras");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 19f));
        p.add(titulo, "gapbottom 6");

        JTextArea editor = new JTextArea(descripcionPreview.getText());
        editor.setLineWrap(true);
        editor.setWrapStyleWord(true);
        editor.setFont(editor.getFont().deriveFont(16f));
        JScrollPane es = new JScrollPane(editor);
        p.add(es, "grow");

        JPanel botones = new JPanel(new MigLayout("insets 0", "[grow][][]"));
        JButton cancelar = new JButton("Cancelar");
        cancelar.addActionListener(e -> dialog.dispose());
        JButton guardar = UiFactory.primaryButton("Guardar");
        guardar.addActionListener(e -> {
            descripcionPreview.setText(editor.getText());
            descripcionPreview.setCaretPosition(0);
            dialog.dispose();
        });
        botones.add(new JLabel(), "growx");
        botones.add(cancelar);
        botones.add(guardar, "h 36!");
        p.add(botones, "growx, gaptop 10");

        dialog.setContentPane(p);
        dialog.setSize(580, 470);
        dialog.setMinimumSize(new Dimension(420, 320));
        dialog.setLocationRelativeTo(owner);
        editor.requestFocusInWindow();
        dialog.setVisible(true);
    }

    /** Carga y muestra los datos de un cliente. Null muestra el estado vacio. */
    public void showCliente(Cliente cliente) {
        this.clienteActual = cliente;
        if (cliente == null) {
            cardLayout.show(this, "empty");
            return;
        }

        nombreField.setText(cliente.getNombre());
        telefonoField.setText(cliente.getTelefono());
        cumpleanosPicker.setDate(cliente.getCumpleanos()); // acepta null
        direccionField.setText(cliente.getDireccion());
        ciudadField.setText(cliente.getCiudad());
        provinciaCombo.setSelectedItem(cliente.getProvincia());
        codigoPostalField.setText(cliente.getCodigoPostal());
        descripcionPreview.setText(cliente.getDescripcion());
        descripcionPreview.setCaretPosition(0);

        cardLayout.show(this, "content");
    }

    public void guardarCliente() {
        if (clienteActual == null) return;

        String nombre = nombreField.getText().trim();
        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "El nombre es obligatorio",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            nombreField.requestFocusInWindow();
            return;
        }

        clienteActual.setNombre(nombre);
        clienteActual.setTelefono(telefonoField.getText().trim());
        clienteActual.setCumpleanos(cumpleanosPicker.getDate()); // null si vacio
        clienteActual.setDireccion(direccionField.getText().trim());
        clienteActual.setCiudad(ciudadField.getText().trim());
        Object sel = provinciaCombo.getSelectedItem();
        clienteActual.setProvincia(sel != null ? sel.toString().trim() : "");
        clienteActual.setCodigoPostal(codigoPostalField.getText().trim());
        clienteActual.setDescripcion(descripcionPreview.getText().trim());

        clienteDAO.update(clienteActual);

        if (onClienteChanged != null) {
            onClienteChanged.run();
        }

        JOptionPane.showMessageDialog(this,
                "Cliente guardado correctamente",
                "Guardado", JOptionPane.INFORMATION_MESSAGE);
    }

    public void eliminarCliente() {
        if (clienteActual == null) return;

        int result = JOptionPane.showConfirmDialog(this,
                "Seguro que quieres eliminar a " + clienteActual.getNombreCompleto() + "?",
                "Confirmar eliminacion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            clienteDAO.delete(clienteActual.getId());
            clienteActual = null;
            cardLayout.show(this, "empty");

            if (onClienteChanged != null) {
                onClienteChanged.run();
            }
        }
    }

    /** Registra callback que se ejecuta al guardar/eliminar un cliente. */
    public void setOnClienteChanged(Runnable listener) {
        this.onClienteChanged = listener;
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(Font.PLAIN, 16f));
        return l;
    }
}
