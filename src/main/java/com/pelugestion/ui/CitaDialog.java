package com.pelugestion.ui;

import com.pelugestion.dao.ClienteDAO;
import com.pelugestion.model.Cita;
import com.pelugestion.model.Cliente;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CitaDialog extends JDialog {

    private final ClienteDAO clienteDAO;
    private final Cita citaEditar;
    private final LocalDate fecha;

    private JTextField clienteField;
    private JComboBox<String> horaCombo;
    private JComboBox<String> tiempoEstimadoCombo;
    private JTextField servicioField;
    private JTextField notasField;

    private JWindow popup;
    private JList<String> suggestionList;
    private DefaultListModel<String> suggestionModel;
    private List<Cliente> allClientes;

    private Cita resultado = null;

    public CitaDialog(Window owner, Cita citaEditar, LocalDate fecha, ClienteDAO clienteDAO) {
        super(owner, citaEditar == null ? "Nueva Cita" : "Editar Cita",
                ModalityType.APPLICATION_MODAL);
        this.clienteDAO = clienteDAO;
        this.citaEditar = citaEditar;
        this.fecha = fecha;
        initComponents();
        if (citaEditar != null) {
            loadCita(citaEditar);
        }
        pack();
        setLocationRelativeTo(owner);
        setMinimumSize(new Dimension(440, getHeight()));
        setResizable(false);
    }

    private void initComponents() {
        JPanel panel = new JPanel(new MigLayout(
                "wrap 2, insets 24 28 24 28, gapy 10",
                "[right, 90!][grow, 280::]"));

        panel.add(label("Fecha"));
        JLabel fechaLabel = new JLabel(fecha.format(
                java.time.format.DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' yyyy",
                        new java.util.Locale("es", "ES"))));
        fechaLabel.setFont(fechaLabel.getFont().deriveFont(Font.BOLD, 14f));
        panel.add(fechaLabel, "growx");

        panel.add(label("Clienta"));
        clienteField = new JTextField();
        clienteField.putClientProperty("JTextField.placeholderText", "Nombre de la clienta");
        clienteField.setFont(clienteField.getFont().deriveFont(Font.PLAIN, 14f));
        panel.add(clienteField, "growx");

        allClientes = clienteDAO.findAll();
        suggestionModel = new DefaultListModel<>();
        suggestionList = new JList<>(suggestionModel);
        suggestionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        suggestionList.setFont(suggestionList.getFont().deriveFont(Font.PLAIN, 13f));
        suggestionList.setFocusable(false);

        popup = new JWindow(this);
        popup.setAlwaysOnTop(true);
        JScrollPane popupScroll = new JScrollPane(suggestionList);
        popupScroll.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        popup.add(popupScroll);

        clienteField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filtrarSugerencias(); }
            @Override public void removeUpdate(DocumentEvent e) { filtrarSugerencias(); }
            @Override public void changedUpdate(DocumentEvent e) { filtrarSugerencias(); }
        });

        clienteField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    if (popup.isVisible() && suggestionModel.getSize() > 0) {
                        suggestionList.setSelectedIndex(0);
                        suggestionList.requestFocus();
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    popup.setVisible(false);
                }
            }
        });

        suggestionList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    aplicarSugerencia();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    popup.setVisible(false);
                    clienteField.requestFocus();
                }
            }
        });

        suggestionList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    aplicarSugerencia();
                }
            }
        });

        panel.add(label("Hora"));
        horaCombo = new JComboBox<>(Cita.HORAS);
        horaCombo.setSelectedItem("10:00");
        horaCombo.setFont(horaCombo.getFont().deriveFont(Font.PLAIN, 14f));
        panel.add(horaCombo, "growx");

        panel.add(label("Tiempo est."));
        tiempoEstimadoCombo = new JComboBox<>(Cita.TIEMPOS_ESTIMADOS);
        tiempoEstimadoCombo.setSelectedItem("30 min");
        tiempoEstimadoCombo.setFont(tiempoEstimadoCombo.getFont().deriveFont(Font.PLAIN, 14f));
        panel.add(tiempoEstimadoCombo, "growx");

        panel.add(label("Servicio"));
        servicioField = new JTextField();
        servicioField.putClientProperty("JTextField.placeholderText", "Ej: Corte + Tinte");
        servicioField.setFont(servicioField.getFont().deriveFont(Font.PLAIN, 14f));
        panel.add(servicioField, "growx");

        panel.add(label("Notas"));
        notasField = new JTextField();
        notasField.putClientProperty("JTextField.placeholderText", "Ej: traer foto de referencia");
        notasField.setFont(notasField.getFont().deriveFont(Font.PLAIN, 14f));
        panel.add(notasField, "growx");

        panel.add(new JSeparator(), "span, growx, gaptop 14, gapbottom 6");

        JPanel buttons = new JPanel(new MigLayout("insets 0", "[grow][][]"));

        JButton cancelBtn = new JButton("Cancelar");
        cancelBtn.setFont(cancelBtn.getFont().deriveFont(Font.PLAIN, 14f));
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = UiFactory.primaryButton("Guardar");
        saveBtn.setFont(saveBtn.getFont().deriveFont(Font.BOLD, 14f));
        saveBtn.addActionListener(e -> guardar());

        buttons.add(new JLabel(), "growx");
        buttons.add(cancelBtn);
        buttons.add(saveBtn);

        panel.add(buttons, "span, growx");
        setContentPane(panel);

        getRootPane().setDefaultButton(saveBtn);
    }

    private void loadCita(Cita c) {
        clienteField.setText(c.getClienteNombre() != null ? c.getClienteNombre() : "");
        horaCombo.setSelectedItem(c.getHora());
        tiempoEstimadoCombo.setSelectedItem(c.getTiempoEstimado() != null ? c.getTiempoEstimado() : "30 min");
        servicioField.setText(c.getServicio() != null ? c.getServicio() : "");
        notasField.setText(c.getNotas() != null ? c.getNotas() : "");
    }

    private void guardar() {
        String nombre = clienteField.getText().trim();
        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Escribe el nombre de la clienta.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String servicio = servicioField.getText().trim();
        if (servicio.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Escribe el servicio.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int clienteId = resolverClienteId(nombre);

        resultado = citaEditar != null ? citaEditar : new Cita();
        resultado.setClienteId(clienteId);
        resultado.setClienteNombre(nombre);
        resultado.setFecha(fecha);
        resultado.setHora(horaCombo.getSelectedItem().toString());
        resultado.setServicio(servicio);
        resultado.setTiempoEstimado(tiempoEstimadoCombo.getSelectedItem().toString());
        resultado.setNotas(notasField.getText().trim());

        dispose();
    }

    private int resolverClienteId(String nombre) {
        List<Cliente> matches = clienteDAO.search(nombre);
        for (Cliente c : matches) {
            if (c.getNombre().equalsIgnoreCase(nombre)) {
                return c.getId();
            }
        }
        Cliente nuevo = new Cliente(nombre, "", "");
        return clienteDAO.insert(nuevo);
    }

    public Cita getResultado() {
        return resultado;
    }

    private void filtrarSugerencias() {
        String text = clienteField.getText().trim().toLowerCase();
        suggestionModel.clear();

        if (text.isEmpty()) {
            popup.setVisible(false);
            return;
        }

        java.util.List<String> matches = new ArrayList<>();
        for (Cliente c : allClientes) {
            if (c.getNombre() != null && c.getNombre().toLowerCase().contains(text)) {
                matches.add(c.getNombre());
            }
        }

        if (matches.isEmpty()) {
            popup.setVisible(false);
            return;
        }

        for (String m : matches) {
            suggestionModel.addElement(m);
        }

        Point p = clienteField.getLocationOnScreen();
        popup.setSize(clienteField.getWidth() - 2, Math.min(matches.size() * 22 + 4, 180));
        popup.setLocation(p.x + 1, p.y + clienteField.getHeight());
        popup.setVisible(true);
        suggestionList.setSelectedIndex(0);
    }

    private void aplicarSugerencia() {
        String selected = suggestionList.getSelectedValue();
        if (selected != null) {
            clienteField.setText(selected);
        }
        popup.setVisible(false);
        clienteField.requestFocus();
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(Font.PLAIN, 14f));
        return l;
    }
}
