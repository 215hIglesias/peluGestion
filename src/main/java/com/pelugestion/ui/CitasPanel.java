package com.pelugestion.ui;

import com.github.lgooddatepicker.components.CalendarPanel;
import com.github.lgooddatepicker.components.DatePickerSettings;
import com.github.lgooddatepicker.optionalusertools.CalendarListener;
import com.github.lgooddatepicker.zinternaltools.CalendarSelectionEvent;
import com.github.lgooddatepicker.zinternaltools.YearMonthChangeEvent;
import com.pelugestion.dao.CitaDAO;
import com.pelugestion.dao.ClienteDAO;
import com.pelugestion.model.Cita;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

public class CitasPanel extends JPanel {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern(
            "EEEE, d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));

    private final CitaDAO citaDAO;
    private final ClienteDAO clienteDAO;

    private CalendarPanel calendario;
    private LocalDate selectedDate;

    private JPanel citasListPanel;
    private JLabel dateLabel;

    public CitasPanel() {
        this.citaDAO = new CitaDAO();
        this.clienteDAO = new ClienteDAO();
        this.selectedDate = LocalDate.now();
        initComponents();
    }

    private void initComponents() {
        setLayout(new MigLayout("fill, insets 0, gap 0", "[50%][50%]", "[grow]"));

        DatePickerSettings settings = new DatePickerSettings(new Locale("es", "ES"));
        settings.setFontValidDate(new Font(Font.SANS_SERIF, Font.PLAIN, 36));
        settings.setFontMonthAndYearMenuLabels(new Font(Font.SANS_SERIF, Font.BOLD, 32));
        settings.setFontTodayLabel(new Font(Font.SANS_SERIF, Font.BOLD, 34));
        settings.setFontCalendarWeekdayLabels(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        settings.setFontCalendarDateLabels(new Font(Font.SANS_SERIF, Font.PLAIN, 30));
        settings.setSizeDatePanelMinimumHeight(6 * 60);
        settings.setSizeDatePanelMinimumWidth(7 * 90);

        calendario = new CalendarPanel(settings);
        calendario.setSelectedDate(selectedDate);
        calendario.addCalendarListener(new CalendarListener() {
            @Override
            public void selectedDateChanged(CalendarSelectionEvent event) {
                if (event.getNewDate() != null) {
                    selectedDate = event.getNewDate();
                    refreshCitas();
                }
            }
            @Override
            public void yearMonthChanged(YearMonthChangeEvent event) {
            }
        });

        JPanel calendarWrap = new JPanel(new MigLayout("fill, insets 12", "[grow]", "[grow]"));
        calendarWrap.setBackground(UIManager.getColor("Panel.background"));
        calendarWrap.add(calendario, "align center");

        JScrollPane leftScroll = new JScrollPane(calendarWrap);
        leftScroll.setBorder(BorderFactory.createEmptyBorder());
        leftScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(leftScroll, "grow");

        JPanel rightPanel = new JPanel(new MigLayout("fill, insets 15, wrap", "[grow]", "[][][grow]"));

        dateLabel = new JLabel();
        dateLabel.setFont(dateLabel.getFont().deriveFont(Font.BOLD, 18f));
        rightPanel.add(dateLabel, "growx, gapbottom 10");

        JButton nuevaCitaBtn = UiFactory.primaryButton("+ Nueva Cita");
        nuevaCitaBtn.addActionListener(e -> crearCita());
        JPanel btnPanel = new JPanel(new MigLayout("insets 0", "[grow][]"));
        btnPanel.add(new JLabel(), "growx");
        btnPanel.add(nuevaCitaBtn);
        rightPanel.add(btnPanel, "growx, gapbottom 10");

        citasListPanel = new JPanel(new MigLayout("fillx, insets 0, gapy 5", "[grow]", ""));
        citasListPanel.setBackground(UIManager.getColor("Panel.background"));

        JScrollPane citasScroll = new JScrollPane(citasListPanel);
        citasScroll.setBorder(BorderFactory.createEmptyBorder());
        citasScroll.getVerticalScrollBar().setUnitIncrement(16);
        rightPanel.add(citasScroll, "grow");

        add(rightPanel, "grow");

        refreshCitas();
    }

    private void refreshCitas() {
        dateLabel.setText("Citas del " + selectedDate.format(DATE_FMT));
        citasListPanel.removeAll();

        List<Cita> citas = citaDAO.findByFecha(selectedDate);

        if (citas.isEmpty()) {
            JLabel emptyLabel = new JLabel("No hay citas para este dia.");
            emptyLabel.setForeground(UiFactory.TEXT_MUTED);
            emptyLabel.setFont(emptyLabel.getFont().deriveFont(Font.ITALIC, 14f));
            citasListPanel.add(emptyLabel, "gapleft 5, gaptop 20");
        } else {
            for (Cita cita : citas) {
                citasListPanel.add(createCitaRow(cita), "growx, wrap");
            }
        }

        citasListPanel.revalidate();
        citasListPanel.repaint();
    }

    private JPanel createCitaRow(Cita cita) {
        JPanel row = new JPanel(new MigLayout("fillx, insets 10 14 10 14, gap 0",
                "[60!][grow]", "[][]"));
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE0, 0xE0, 0xE0), 1),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)));
        row.putClientProperty("JComponent.outline", "roundRect 8");

        JLabel horaLabel = new JLabel(cita.getHora());
        horaLabel.setFont(horaLabel.getFont().deriveFont(Font.BOLD, 18f));
        horaLabel.setForeground(UiFactory.HEADER_BG);
        row.add(horaLabel, "spany 2, gapright 14, aligny center");

        JLabel nombreLabel = new JLabel(cita.getClienteNombre());
        nombreLabel.setFont(nombreLabel.getFont().deriveFont(Font.BOLD, 15f));
        row.add(nombreLabel, "wrap");

        JPanel servicioBox = new JPanel(new MigLayout("insets 6 10 6 10, gap 0", "[grow]", ""));
        servicioBox.setBackground(new Color(0xF2, 0xF2, 0xF2));
        servicioBox.putClientProperty("JComponent.outline", "roundRect 6");

        StringBuilder sb = new StringBuilder("<html><b>");
        sb.append(escapeHtml(cita.getServicio()));
        sb.append("</b>");
        if (cita.getTiempoEstimado() != null && !cita.getTiempoEstimado().isEmpty()) {
            sb.append(" &nbsp;<font color='#888888'>(");
            sb.append(escapeHtml(cita.getTiempoEstimado()));
            sb.append(")</font>");
        }
        if (cita.getNotas() != null && !cita.getNotas().isEmpty()) {
            sb.append(" &nbsp;\u2014&nbsp; ");
            sb.append(escapeHtml(cita.getNotas()));
        }
        sb.append("</html>");

        JLabel detalleLabel = new JLabel(sb.toString());
        detalleLabel.setFont(detalleLabel.getFont().deriveFont(Font.PLAIN, 13f));
        servicioBox.add(detalleLabel, "growx");
        row.add(servicioBox, "growx, gaptop 2");

        JPanel btnGroup = new JPanel(new MigLayout("insets 0, gap 4"));
        btnGroup.setOpaque(false);

        JButton editBtn = new JButton("Editar");
        editBtn.setFont(editBtn.getFont().deriveFont(Font.PLAIN, 12f));
        editBtn.putClientProperty("JButton.buttonType", "roundRect");
        editBtn.setFocusPainted(false);
        editBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        editBtn.addActionListener(e -> editarCita(cita));
        btnGroup.add(editBtn);

        JButton delBtn = new JButton("Eliminar");
        delBtn.setFont(delBtn.getFont().deriveFont(Font.PLAIN, 12f));
        delBtn.setForeground(UiFactory.DANGER);
        delBtn.putClientProperty("JButton.buttonType", "roundRect");
        delBtn.setFocusPainted(false);
        delBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        delBtn.addActionListener(e -> eliminarCita(cita));
        btnGroup.add(delBtn);

        JPanel rightActions = new JPanel(new GridBagLayout());
        rightActions.setOpaque(false);
        rightActions.add(btnGroup);
        row.add(rightActions, "spany 2, aligny center, gapleft 10, east");

        return row;
    }

    private String escapeHtml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void crearCita() {
        CitaDialog dialog = new CitaDialog(SwingUtilities.getWindowAncestor(this),
                null, selectedDate, clienteDAO);
        dialog.setVisible(true);
        Cita resultado = dialog.getResultado();
        if (resultado != null) {
            citaDAO.insert(resultado);
            refreshCitas();
        }
    }

    private void editarCita(Cita cita) {
        CitaDialog dialog = new CitaDialog(SwingUtilities.getWindowAncestor(this),
                cita, cita.getFecha(), clienteDAO);
        dialog.setVisible(true);
        Cita resultado = dialog.getResultado();
        if (resultado != null) {
            resultado.setId(cita.getId());
            citaDAO.update(resultado);
            refreshCitas();
        }
    }

    private void eliminarCita(Cita cita) {
        int result = JOptionPane.showConfirmDialog(this,
                "Eliminar la cita de " + cita.getClienteNombre()
                        + " del " + cita.getHora() + "?",
                "Confirmar eliminacion", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            citaDAO.delete(cita.getId());
            refreshCitas();
        }
    }
}
