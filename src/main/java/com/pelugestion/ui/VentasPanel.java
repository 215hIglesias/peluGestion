package com.pelugestion.ui;

import com.pelugestion.dao.ProductoDAO;
import com.pelugestion.model.Producto;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class VentasPanel extends JPanel {

    private final ProductoDAO productoDAO;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final List<Producto> productos;

    private Runnable onProductoChanged;

    public VentasPanel(ProductoDAO productoDAO) {
        this.productoDAO = productoDAO;
        this.productos = new ArrayList<>();

        setLayout(new MigLayout("fill, insets 20", "[grow]", "[][grow][shrink 0]"));

        JLabel titulo = new JLabel("Ventas");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 23f));
        add(titulo, "wrap, gapbottom 10");

        String[] columnas = {"Producto", "Precio compra", "Precio venta"};
        tableModel = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return true;
            }

            @Override
            public Class<?> getColumnClass(int col) {
                return col == 0 ? String.class : Double.class;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(38);
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD, 16f));
        table.setFont(table.getFont().deriveFont(Font.PLAIN, 17f));
        table.setShowGrid(true);
        table.setGridColor(new Color(0xE0, 0xE0, 0xE0));
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        tableModel.addTableModelListener(e -> {
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int col = e.getColumn();
                if (row >= 0 && row < productos.size()) {
                    Producto p = productos.get(row);
                    Object val = tableModel.getValueAt(row, col);
                    switch (col) {
                        case 0 -> p.setNombre(val != null ? val.toString() : "");
                        case 1 -> p.setPrecioCompra(val instanceof Number n ? n.doubleValue() : 0.0);
                        case 2 -> p.setPrecioVenta(val instanceof Number n ? n.doubleValue() : 0.0);
                    }
                    productoDAO.update(p);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        add(scroll, "grow, wrap");

        JPanel botones = new JPanel(new MigLayout("insets 10 0 0 0", "[]12[]12[]"));
        JButton addBtn = UiFactory.primaryButton("+ A\u00F1adir producto");
        addBtn.setFont(addBtn.getFont().deriveFont(Font.BOLD, 18f));
        addBtn.addActionListener(e -> {
            Producto p = new Producto();
            productoDAO.insert(p);
            refreshData();
            if (onProductoChanged != null) {
                onProductoChanged.run();
            }
        });

        JButton delBtn = UiFactory.dangerButton("Eliminar producto");
        delBtn.setFont(delBtn.getFont().deriveFont(Font.BOLD, 18f));
        delBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0 && row < productos.size()) {
                Producto p = productos.get(row);
                int result = JOptionPane.showConfirmDialog(this,
                        "Eliminar \"" + p.getNombre() + "\"?",
                        "Confirmar eliminaci\u00F3n", JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    productoDAO.delete(p.getId());
                    refreshData();
                    if (onProductoChanged != null) {
                        onProductoChanged.run();
                    }
                }
            }
        });

        botones.add(addBtn);
        botones.add(delBtn);
        add(botones, "growx");

        refreshData();
    }

    public void refreshData() {
        productos.clear();
        productos.addAll(productoDAO.findAll());
        tableModel.setRowCount(0);
        for (Producto p : productos) {
            tableModel.addRow(new Object[]{p.getNombre(), p.getPrecioCompra(), p.getPrecioVenta()});
        }
    }

    public void setOnProductoChanged(Runnable callback) {
        this.onProductoChanged = callback;
    }
}
