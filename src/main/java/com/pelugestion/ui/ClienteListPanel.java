package com.pelugestion.ui;

import com.pelugestion.dao.ClienteDAO;
import com.pelugestion.model.Cliente;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Panel lateral izquierdo con la lista de clientes y campo de busqueda.
 * Equivalente conceptual a un componente de lista con filtro en un frontend web.
 */
public class ClienteListPanel extends JPanel {

    private final ClienteDAO clienteDAO;
    private JTextField searchField;
    private JTable table;
    private ClienteTableModel tableModel;
    private Consumer<Cliente> onClienteSelected;

    public ClienteListPanel(ClienteDAO clienteDAO) {
        this.clienteDAO = clienteDAO;
        initComponents();
        refreshData();
    }

    private void initComponents() {
        setLayout(new MigLayout("fill, wrap, insets 15", "[grow]", "[][grow]"));
        setPreferredSize(new Dimension(340, 0));

        // Campo de busqueda
        searchField = new JTextField();
        searchField.putClientProperty("JTextField.placeholderText",
                "Buscar cliente...");
        searchField.putClientProperty("JTextField.showClearButton", true);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterClients(); }
            public void removeUpdate(DocumentEvent e) { filterClients(); }
            public void changedUpdate(DocumentEvent e) { filterClients(); }
        });
        add(searchField, "growx, h 38!");

        // Tabla de clientes
        tableModel = new ClienteTableModel();
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(40);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFont(table.getFont().deriveFont(16f));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(
                table.getTableHeader().getFont().deriveFont(Font.BOLD, 15f));

        // Listener de seleccion
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && onClienteSelected != null) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    onClienteSelected.accept(tableModel.getClienteAt(row));
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, "grow");
    }

    private void filterClients() {
        String query = searchField.getText();
        List<Cliente> filtered = clienteDAO.search(query);
        tableModel.setClientes(filtered);
    }

    /** Recarga la lista completa de clientes desde la base de datos. */
    public void refreshData() {
        List<Cliente> clientes = clienteDAO.findAll();
        tableModel.setClientes(clientes);
    }

    /** Selecciona un cliente en la tabla por su ID. */
    public void selectCliente(Cliente cliente) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (tableModel.getClienteAt(i).getId() == cliente.getId()) {
                table.setRowSelectionInterval(i, i);
                table.scrollRectToVisible(table.getCellRect(i, 0, true));
                return;
            }
        }
    }

    public void createNewCliente() {
        Cliente nuevo = new Cliente();
        nuevo.setNombre("Nuevo Cliente");
        clienteDAO.insert(nuevo);
        refreshData();
        selectCliente(nuevo);
    }

    /** Registra el listener que se ejecuta al seleccionar un cliente. */
    public void setOnClienteSelected(Consumer<Cliente> listener) {
        this.onClienteSelected = listener;
    }

    // --- Modelo de tabla ---

    private static class ClienteTableModel extends AbstractTableModel {

        private List<Cliente> clientes = List.of();
        private final String[] columns = {"Nombre"};

        public void setClientes(List<Cliente> clientes) {
            this.clientes = clientes;
            fireTableDataChanged();
        }

        public Cliente getClienteAt(int row) {
            return clientes.get(row);
        }

        @Override
        public int getRowCount() { return clientes.size(); }

        @Override
        public int getColumnCount() { return columns.length; }

        @Override
        public String getColumnName(int col) { return columns[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            return clientes.get(row).getNombreCompleto();
        }
    }
}
