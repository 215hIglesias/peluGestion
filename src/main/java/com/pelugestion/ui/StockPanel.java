package com.pelugestion.ui;

import com.pelugestion.dao.ProductoDAO;
import com.pelugestion.model.Producto;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class StockPanel extends JPanel {

    private static final Color PLACEHOLDER_COLOR = new Color(0xAA, 0xAA, 0xAA);

    private final ProductoDAO productoDAO;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final List<Producto> productos;
    private final JTextField nombreField;

    private Runnable onProductoChanged;

    public StockPanel(ProductoDAO productoDAO) {
        this.productoDAO = productoDAO;
        this.productos = new ArrayList<>();

        setLayout(new MigLayout("fill, insets 20", "[grow]", "[][][grow][shrink 0]"));

        JLabel titulo = new JLabel("Stock");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 23f));
        add(titulo, "wrap, gapbottom 10");

        JPanel addPanel = new JPanel(new MigLayout("insets 0", "[grow]12[]"));
        nombreField = new JTextField();
        nombreField.putClientProperty("JTextField.placeholderText", "Nombre del producto");
        addPanel.add(nombreField, "growx, h 46!");
        JButton addBtn = UiFactory.primaryButton("+ A\u00F1adir a stock");
        addBtn.setFont(addBtn.getFont().deriveFont(Font.BOLD, 18f));
        addBtn.addActionListener(e -> addProducto());
        addPanel.add(addBtn, "h 46!");
        add(addPanel, "growx, wrap, gapbottom 12");

        String[] columnas = {"Producto", "Cantidad"};
        tableModel = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 1;
            }

            @Override
            public Class<?> getColumnClass(int col) {
                return col == 0 ? String.class : Integer.class;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(38);
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD, 16f));
        table.setFont(table.getFont().deriveFont(Font.PLAIN, 17f));
        table.setShowGrid(true);
        table.setGridColor(new Color(0xE0, 0xE0, 0xE0));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        TableColumn cantidadCol = table.getColumnModel().getColumn(1);
        cantidadCol.setPreferredWidth(120);
        cantidadCol.setMaxWidth(160);
        cantidadCol.setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                if (value == null || (value instanceof Number n && n.intValue() == 0)) {
                    setText("INTRODUZCA STOCK DISPONIBLE");
                    setForeground(PLACEHOLDER_COLOR);
                    setFont(getFont().deriveFont(Font.ITALIC));
                } else {
                    setForeground(isSelected ? t.getSelectionForeground() : t.getForeground());
                    setFont(t.getFont());
                }
                return c;
            }
        });

        tableModel.addTableModelListener(e -> {
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int col = e.getColumn();
                if (row >= 0 && row < productos.size() && col == 1) {
                    Producto p = productos.get(row);
                    Object val = tableModel.getValueAt(row, col);
                    p.setCantidad(val instanceof Number n ? n.intValue() : 0);
                    productoDAO.update(p);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        add(scroll, "grow, wrap");

        JPanel botones = new JPanel(new MigLayout("insets 10 0 0 0", "[]12[]"));
        JButton delBtn = UiFactory.dangerButton("Eliminar producto");
        delBtn.setFont(delBtn.getFont().deriveFont(Font.BOLD, 18f));
        delBtn.addActionListener(e -> deleteProducto());
        botones.add(delBtn);
        add(botones, "growx");

        nombreField.addActionListener(e -> addProducto());

        refreshData();
    }

    private void addProducto() {
        String nombre = nombreField.getText().trim();
        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Escribe un nombre para el producto.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            nombreField.requestFocusInWindow();
            return;
        }

        Producto existente = productoDAO.findByName(nombre);
        if (existente != null) {
            JOptionPane.showMessageDialog(this,
                    "Ya existe un producto con el nombre \"" + nombre + "\".",
                    "Producto duplicado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Producto p = new Producto(nombre);
        productoDAO.insert(p);
        nombreField.setText("");
        nombreField.requestFocusInWindow();
        refreshData();

        if (onProductoChanged != null) {
            onProductoChanged.run();
        }
    }

    private void deleteProducto() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= productos.size()) {
            JOptionPane.showMessageDialog(this,
                    "Selecciona un producto para eliminar.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Producto p = productos.get(row);
        int result = JOptionPane.showConfirmDialog(this,
                "Eliminar \"" + p.getNombre() + "\" de stock?",
                "Confirmar eliminaci\u00F3n", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            productoDAO.delete(p.getId());
            refreshData();

            if (onProductoChanged != null) {
                onProductoChanged.run();
            }
        }
    }

    public void refreshData() {
        productos.clear();
        productos.addAll(productoDAO.findAll());
        tableModel.setRowCount(0);
        for (Producto p : productos) {
            tableModel.addRow(new Object[]{p.getNombre(), p.getCantidad()});
        }
    }

    public void setOnProductoChanged(Runnable callback) {
        this.onProductoChanged = callback;
    }
}
