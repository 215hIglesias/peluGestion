package com.pelugestion.ui;

import com.pelugestion.model.Cliente;
import com.pelugestion.util.DateUtil;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialogo para importar clientes desde un CSV.
 * El usuario indica que columna del fichero corresponde a cada campo,
 * de modo que funciona con CSVs de cualquier procedencia.
 */
public class ImportCsvDialog extends JDialog {

    private static final String NINGUNA = "— (ninguna)";

    private final List<String[]> rows;

    private JCheckBox cabeceraCheck;
    private JComboBox<String> nombreCombo;
    private JComboBox<String> telefonoCombo;
    private JComboBox<String> cumpleanosCombo;
    private JComboBox<String> direccionCombo;
    private JComboBox<String> ciudadCombo;
    private JComboBox<String> provinciaCombo;
    private JComboBox<String> codigoPostalCombo;
    private JComboBox<String> descripcionCombo;
    private JLabel infoLabel;

    private List<Cliente> resultado = null;

    public ImportCsvDialog(Window owner, List<String[]> rows) {
        super(owner, "Importar clientes desde CSV", ModalityType.APPLICATION_MODAL);
        this.rows = rows;
        initComponents();
        pack();
        setMinimumSize(new Dimension(480, getHeight()));
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    private int numColumnas() {
        int max = 0;
        for (String[] r : rows) max = Math.max(max, r.length);
        return max;
    }

    private void initComponents() {
        JPanel panel = new JPanel(new MigLayout(
                "wrap 2, insets 20 25 20 25, gapy 8",
                "[right, 140!][grow, 260::]"));

        JLabel title = new JLabel("Asignar columnas del CSV");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        panel.add(title, "span, gapbottom 5");

        cabeceraCheck = new JCheckBox("La primera fila son los nombres de las columnas", true);
        cabeceraCheck.addActionListener(e -> refreshColumnOptions());
        panel.add(cabeceraCheck, "span, gapbottom 5");

        nombreCombo = addRow(panel, "Nombre completo *");
        telefonoCombo = addRow(panel, "Telefono");
        cumpleanosCombo = addRow(panel, "Cumpleanos");
        direccionCombo = addRow(panel, "Direccion");
        ciudadCombo = addRow(panel, "Ciudad");
        provinciaCombo = addRow(panel, "Provincia");
        codigoPostalCombo = addRow(panel, "Codigo postal");
        descripcionCombo = addRow(panel, "Detalles y extras");

        infoLabel = new JLabel();
        infoLabel.setForeground(UiFactory.TEXT_MUTED);
        infoLabel.setFont(infoLabel.getFont().deriveFont(Font.PLAIN, 14f));
        panel.add(infoLabel, "span, gaptop 5");

        panel.add(new JSeparator(), "span, growx, gaptop 10, gapbottom 5");

        JPanel buttons = new JPanel(new MigLayout("insets 0", "[grow][][]"));
        JButton cancelBtn = new JButton("Cancelar");
        cancelBtn.addActionListener(e -> dispose());
        JButton importBtn = UiFactory.primaryButton("Importar");
        importBtn.addActionListener(e -> importar());
        buttons.add(new JLabel(), "growx");
        buttons.add(cancelBtn);
        buttons.add(importBtn);
        panel.add(buttons, "span, growx");

        setContentPane(panel);
        getRootPane().setDefaultButton(importBtn);

        refreshColumnOptions();
    }

    private JComboBox<String> addRow(JPanel panel, String labelText) {
        JLabel l = new JLabel(labelText);
        l.setFont(l.getFont().deriveFont(Font.PLAIN, 15f));
        panel.add(l);
        JComboBox<String> combo = new JComboBox<>();
        panel.add(combo, "growx");
        return combo;
    }

    /** Reconstruye las opciones de los combos segun haya o no cabecera. */
    private void refreshColumnOptions() {
        int cols = numColumnas();
        boolean tieneCabecera = cabeceraCheck.isSelected();
        String[] headers = (tieneCabecera && !rows.isEmpty()) ? rows.get(0) : null;

        String[] etiquetas = new String[cols];
        String[] opcional = new String[cols + 1];
        opcional[0] = NINGUNA;
        for (int i = 0; i < cols; i++) {
            String etiqueta = (headers != null && i < headers.length && !headers[i].isBlank())
                    ? headers[i].trim() : ("Columna " + (i + 1));
            etiquetas[i] = etiqueta;
            opcional[i + 1] = etiqueta;
        }

        nombreCombo.setModel(new DefaultComboBoxModel<>(etiquetas));  // obligatorio
        telefonoCombo.setModel(new DefaultComboBoxModel<>(opcional));
        cumpleanosCombo.setModel(new DefaultComboBoxModel<>(opcional));
        direccionCombo.setModel(new DefaultComboBoxModel<>(opcional));
        ciudadCombo.setModel(new DefaultComboBoxModel<>(opcional));
        provinciaCombo.setModel(new DefaultComboBoxModel<>(opcional));
        codigoPostalCombo.setModel(new DefaultComboBoxModel<>(opcional));
        descripcionCombo.setModel(new DefaultComboBoxModel<>(opcional));

        // Adivinar el mapeo por el nombre de la cabecera
        if (headers != null) {
            preselect(nombreCombo, headers, false, "nombre", "name", "cliente", "clienta");
            preselect(telefonoCombo, headers, true, "tel", "phone", "movil", "móvil", "celular");
            preselect(cumpleanosCombo, headers, true, "cumple", "nacim", "birth", "fecha");
            preselect(direccionCombo, headers, true, "direc", "address", "domicil", "calle");
            preselect(ciudadCombo, headers, true, "ciudad", "city", "poblac", "localidad");
            preselect(provinciaCombo, headers, true, "provin", "state", "region", "región");
            preselect(codigoPostalCombo, headers, true, "postal", "zip", "c.p", "cp");
            preselect(descripcionCombo, headers, true, "detalle", "extra", "desc", "nota", "note", "obs", "coment");
        }

        int filasDatos = rows.size() - (tieneCabecera ? 1 : 0);
        infoLabel.setText("Se importaran hasta " + Math.max(0, filasDatos) + " filas.");
    }

    private void preselect(JComboBox<String> combo, String[] headers,
                           boolean opcional, String... claves) {
        for (int i = 0; i < headers.length; i++) {
            String h = headers[i].toLowerCase();
            for (String clave : claves) {
                if (h.contains(clave)) {
                    combo.setSelectedIndex(opcional ? i + 1 : i);
                    return;
                }
            }
        }
    }

    private void importar() {
        int idxNombre = nombreCombo.getSelectedIndex();
        int idxTelefono = telefonoCombo.getSelectedIndex() - 1;      // -1 = ninguna
        int idxCumple = cumpleanosCombo.getSelectedIndex() - 1;
        int idxDireccion = direccionCombo.getSelectedIndex() - 1;
        int idxCiudad = ciudadCombo.getSelectedIndex() - 1;
        int idxProvincia = provinciaCombo.getSelectedIndex() - 1;
        int idxCp = codigoPostalCombo.getSelectedIndex() - 1;
        int idxDescripcion = descripcionCombo.getSelectedIndex() - 1;

        boolean tieneCabecera = cabeceraCheck.isSelected();
        int inicio = tieneCabecera ? 1 : 0;

        List<Cliente> clientes = new ArrayList<>();
        int omitidos = 0;

        for (int i = inicio; i < rows.size(); i++) {
            String[] row = rows.get(i);
            String nombre = cell(row, idxNombre);
            if (nombre.isBlank()) { omitidos++; continue; }

            Cliente c = new Cliente(nombre, cell(row, idxTelefono), cell(row, idxDescripcion));
            c.setCumpleanos(DateUtil.parseFlexible(cell(row, idxCumple)));
            c.setDireccion(cell(row, idxDireccion));
            c.setCiudad(cell(row, idxCiudad));
            c.setProvincia(cell(row, idxProvincia));
            c.setCodigoPostal(cell(row, idxCp));
            clientes.add(c);
        }

        if (clientes.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No se encontro ninguna fila con nombre para importar.",
                    "Nada que importar", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String aviso = omitidos > 0
                ? "\n(" + omitidos + " fila(s) sin nombre se omitiran)" : "";
        int ok = JOptionPane.showConfirmDialog(this,
                "Se importaran " + clientes.size() + " cliente(s)." + aviso + "\n\nContinuar?",
                "Confirmar importacion",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) return;

        resultado = clientes;
        dispose();
    }

    private String cell(String[] row, int idx) {
        if (idx < 0 || idx >= row.length || row[idx] == null) return "";
        return row[idx].trim();
    }

    /** Lista de clientes a importar, o null si se cancelo. */
    public List<Cliente> getResultado() {
        return resultado;
    }
}
