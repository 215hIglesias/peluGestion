package com.pelugestion.ui;

import com.pelugestion.dao.ClienteDAO;
import com.pelugestion.dao.ProductoDAO;
import com.pelugestion.model.Cliente;
import com.pelugestion.util.CsvUtil;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Ventana principal de la aplicacion.
 * Barra superior (azul marino) con pestanas Clientas / Citas + CardLayout.
 */
public class MainFrame extends JFrame {

    private final ClienteDAO clienteDAO;
    private final ProductoDAO productoDAO;
    private ClienteListPanel listaPanel;
    private ClienteDetailPanel detallePanel;
    private VentasPanel ventasPanel;
    private StockPanel stockPanel;
    private JPanel bottomBar;

    private CardLayout centerCards;
    private JPanel centerPanel;
    private JButton tabClientas;
    private JButton tabCitas;
    private JButton tabVentas;
    private JButton tabStock;

    public MainFrame() {
        super("PeluGestion - Gestion de Clientes");
        this.clienteDAO = new ClienteDAO();
        this.productoDAO = new ProductoDAO();
        initComponents();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 650));
        setSize(1200, 750);
        setLocationRelativeTo(null);
        // Abrir maximizada (pantalla completa de escritorio)
        setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new MigLayout(
                "fill, wrap, insets 0, gap 0", "[grow]", "[shrink 0][grow][shrink 0]"));

        mainPanel.add(createHeader(), "growx, h 60!");

        // Panel central con CardLayout: "clientas" y "citas"
        centerCards = new CardLayout();
        centerPanel = new JPanel(centerCards);

        listaPanel = new ClienteListPanel(clienteDAO);
        detallePanel = new ClienteDetailPanel(clienteDAO);
        listaPanel.setOnClienteSelected(cliente -> detallePanel.showCliente(cliente));
        detallePanel.setOnClienteChanged(() -> listaPanel.refreshData());

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT, listaPanel, detallePanel);
        splitPane.setDividerLocation(340);
        splitPane.setDividerSize(4);
        splitPane.setBorder(BorderFactory.createEmptyBorder());

        centerPanel.add(splitPane, "clientas");
        centerPanel.add(new CitasPanel(), "citas");
        ventasPanel = new VentasPanel(productoDAO);
        centerPanel.add(ventasPanel, "ventas");
        stockPanel = new StockPanel(productoDAO);
        stockPanel.setOnProductoChanged(() -> ventasPanel.refreshData());
        centerPanel.add(stockPanel, "stock");
        mainPanel.add(centerPanel, "grow");

        ventasPanel.setOnProductoChanged(() -> stockPanel.refreshData());

        mainPanel.add(createBottomBar(), "growx, hidemode 2");

        setContentPane(mainPanel);

        mostrarCard("clientas");
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new MigLayout(
                "fill, insets 0 20 0 20", "[][][][][][grow][][][]")) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                        0, 0, UiFactory.HEADER_BG, getWidth(), 0, UiFactory.HEADER_BG_END);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setOpaque(false);

        JLabel titleLabel = new JLabel("PeluGestion");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 23f));
        titleLabel.setForeground(Color.WHITE);
        header.add(titleLabel, "gapright 25");

        // Pestanas de navegacion
        tabClientas = UiFactory.headerButton("Clientes");
        tabClientas.addActionListener(e -> mostrarCard("clientas"));
        header.add(tabClientas);

        tabCitas = UiFactory.headerButton("Citas");
        tabCitas.addActionListener(e -> mostrarCard("citas"));
        header.add(tabCitas);

        tabVentas = UiFactory.headerButton("Ventas");
        tabVentas.addActionListener(e -> mostrarCard("ventas"));
        header.add(tabVentas);

        tabStock = UiFactory.headerButton("Stock");
        tabStock.addActionListener(e -> mostrarCard("stock"));
        header.add(tabStock);

        header.add(new JLabel(), "growx"); // spacer

        JButton importBtn = UiFactory.headerButton("Importar CSV");
        importBtn.addActionListener(e -> importarCsv());
        header.add(importBtn);

        JButton exportBtn = UiFactory.headerButton("Exportar CSV");
        exportBtn.addActionListener(e -> exportarCsv());
        header.add(exportBtn);

        JButton logoutBtn = new JButton("\u23FB  Cerrar sesi\u00F3n");
        logoutBtn.setFont(logoutBtn.getFont().deriveFont(Font.PLAIN, 15f));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setBackground(new Color(0x7F, 0x8C, 0x8D));
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.putClientProperty("JButton.buttonType", "roundRect");
        logoutBtn.addActionListener(e -> cerrarSesion());
        header.add(logoutBtn, "gapleft 10");

        return header;
    }

    private JPanel createBottomBar() {
        JPanel bar = new JPanel(new MigLayout(
                "fillx, insets 8 20 10 20", "[][grow][]12[]"));

        JButton nuevoBtn = UiFactory.primaryButton("+ Nuevo Cliente");
        nuevoBtn.addActionListener(e -> listaPanel.createNewCliente());
        bar.add(nuevoBtn, "h 42!");

        bar.add(new JLabel(), "growx");

        JButton guardarBtn = UiFactory.primaryButton("Guardar");
        guardarBtn.addActionListener(e -> detallePanel.guardarCliente());
        bar.add(guardarBtn, "h 42!");

        JButton eliminarBtn = UiFactory.dangerButton("Eliminar cliente");
        eliminarBtn.addActionListener(e -> detallePanel.eliminarCliente());
        bar.add(eliminarBtn, "h 42!");

        bottomBar = bar;
        return bar;
    }

    /** Cambia de pestana y resalta la activa. */
    private void mostrarCard(String card) {
        centerCards.show(centerPanel, card);
        estiloTab(tabClientas, card.equals("clientas"));
        estiloTab(tabCitas, card.equals("citas"));
        estiloTab(tabVentas, card.equals("ventas"));
        estiloTab(tabStock, card.equals("stock"));

        bottomBar.setVisible(card.equals("clientas"));

        if (card.equals("ventas")) {
            ventasPanel.refreshData();
        }
    }

    private void estiloTab(JButton tab, boolean activo) {
        tab.setFont(tab.getFont().deriveFont(activo ? Font.BOLD : Font.PLAIN, 15f));
        tab.setForeground(activo ? Color.WHITE : new Color(255, 255, 255, 170));
    }

    // --- Cerrar sesion ---

    private void cerrarSesion() {
        int result = JOptionPane.showConfirmDialog(this,
                "Cerrar la aplicacion? La proxima vez se pedira la contrasena.",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            dispose();
            System.exit(0);
        }
    }

    // --- Exportar CSV ---

    private void exportarCsv() {
        List<Cliente> clientes = clienteDAO.findAll();
        if (clientes.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No hay clientes para exportar.",
                    "Exportar", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Guardar clientes como CSV");
        chooser.setSelectedFile(new java.io.File("clientes.csv"));
        chooser.setFileFilter(new FileNameExtensionFilter("Ficheros CSV (*.csv)", "csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        Path destino = chooser.getSelectedFile().toPath();
        if (!destino.getFileName().toString().toLowerCase().endsWith(".csv")) {
            destino = destino.resolveSibling(destino.getFileName() + ".csv");
        }

        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"Nombre completo", "Telefono", "Cumpleanos", "Direccion",
                "Ciudad", "Provincia", "Codigo postal", "Detalles y extras"});
        for (Cliente c : clientes) {
            rows.add(new String[]{
                    c.getNombre(),
                    c.getTelefono(),
                    com.pelugestion.util.DateUtil.toIso(c.getCumpleanos()),
                    c.getDireccion(),
                    c.getCiudad(),
                    c.getProvincia(),
                    c.getCodigoPostal(),
                    c.getDescripcion()});
        }

        try {
            CsvUtil.write(destino, rows);
            JOptionPane.showMessageDialog(this,
                    "Exportados " + clientes.size() + " clientes a:\n" + destino,
                    "Exportacion completada", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al exportar:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- Importar CSV ---

    private void importarCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Seleccionar CSV de clientes");
        chooser.setFileFilter(new FileNameExtensionFilter("Ficheros CSV (*.csv)", "csv"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        Path origen = chooser.getSelectedFile().toPath();
        List<String[]> rows;
        try {
            rows = CsvUtil.read(origen);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo leer el fichero:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (rows.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "El fichero esta vacio.",
                    "Importar", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ImportCsvDialog dialog = new ImportCsvDialog(this, rows);
        dialog.setVisible(true);

        List<Cliente> clientes = dialog.getResultado();
        if (clientes == null) return;

        try {
            int insertados = clienteDAO.insertAll(clientes);
            listaPanel.refreshData();
            JOptionPane.showMessageDialog(this,
                    "Importados " + insertados + " clientes correctamente.",
                    "Importacion completada", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al importar:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
