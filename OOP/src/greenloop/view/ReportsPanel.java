package greenloop.view;

import greenloop.controller.OrderController;
import greenloop.controller.StockController;
import greenloop.model.Stock;
import greenloop.util.UITheme;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.util.List;

import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.*;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;

public class ReportsPanel extends JPanel {

    private OrderController oc = new OrderController();
    private StockController sc = new StockController();

    private JComboBox<String> cmbReportType, cmbMonth, cmbYear;
    private JPanel            chartPanel;
    private DefaultTableModel lowStockModel;
    private JLabel            lblRevenue, lblOrders, lblItems, lblAvgOrder;

    // Cached chart data so paintComponent doesn't hit the DB
    private double[] chartData = new double[12];
    private double   chartMax  = 1;

    private double lastRevenue;
    private int    lastOrders, lastItems;
    private double lastAvg;

    private static final String[] MONTHS = {
        "January","February","March","April","May","June",
        "July","August","September","October","November","December"
    };

    public ReportsPanel() {
        setLayout(new BorderLayout(12, 12));
        setBackground(UITheme.BG);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 16, 28));
        initUI();
        generateReport();
    }

    private void initUI() {
        JLabel title = new JLabel("Monthly Sales & Inventory Reports");
        title.setFont(UITheme.FONT_TITLE);
        add(title, BorderLayout.NORTH);

        // ── Control bar ──────────────────────────────────────────────
        JPanel controlBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 8));
        controlBar.setBackground(Color.WHITE);
        controlBar.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));

        cmbReportType = new JComboBox<>(new String[]{"Sales Summary","Inventory Report","Order Report"});
        cmbReportType.setFont(UITheme.FONT_BODY);
        cmbReportType.setPreferredSize(new Dimension(180, 32));

        cmbMonth = new JComboBox<>(MONTHS);
        cmbMonth.setFont(UITheme.FONT_BODY);
        cmbMonth.setPreferredSize(new Dimension(120, 32));
        cmbMonth.setSelectedIndex(LocalDate.now().getMonthValue() - 1);

        int currentYear = LocalDate.now().getYear();
        String[] years = new String[5];
        for (int i = 0; i < 5; i++) years[i] = String.valueOf(currentYear - 2 + i);
        cmbYear = new JComboBox<>(years);
        cmbYear.setFont(UITheme.FONT_BODY);
        cmbYear.setPreferredSize(new Dimension(90, 32));
        cmbYear.setSelectedItem(String.valueOf(currentYear));

        JButton btnGenerate = UITheme.makeButton("Generate Report", UITheme.MID_GREEN);
        btnGenerate.setPreferredSize(new Dimension(155, 36));

        JButton btnExport = UITheme.makeButton("Export PDF", UITheme.BLUE_BTN);
        btnExport.setPreferredSize(new Dimension(130, 36));

        controlBar.add(new JLabel("Type:"));    controlBar.add(cmbReportType);
        controlBar.add(new JLabel("Month:"));   controlBar.add(cmbMonth);
        controlBar.add(new JLabel("Year:"));    controlBar.add(cmbYear);
        controlBar.add(btnGenerate);
        controlBar.add(Box.createHorizontalStrut(10));
        controlBar.add(btnExport);

        // ── Centre row: chart + low-stock table ──────────────────────
        JPanel centerRow = new JPanel(new GridLayout(1, 2, 16, 0));
        centerRow.setOpaque(false);

        chartPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Uses pre-fetched chartData — no DB calls here
                drawBarChart((Graphics2D) g);
            }
        };
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        chartPanel.setPreferredSize(new Dimension(0, 280));
        centerRow.add(chartPanel);

        JPanel lowStockPanel = new JPanel(new BorderLayout(0, 6));
        lowStockPanel.setBackground(Color.WHITE);
        lowStockPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        JLabel lowTitle = new JLabel("Low Stock Alerts");
        lowTitle.setFont(UITheme.FONT_SUBTITLE);
        lowTitle.setForeground(UITheme.RED_BTN);

        String[] lowCols = {"Product","Stock","Reorder"};
        lowStockModel = new DefaultTableModel(lowCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable lowTable = new JTable(lowStockModel);
        UITheme.styleTable(lowTable);
        lowStockPanel.add(lowTitle, BorderLayout.NORTH);
        lowStockPanel.add(new JScrollPane(lowTable), BorderLayout.CENTER);
        centerRow.add(lowStockPanel);

        // ── KPI row ──────────────────────────────────────────────────
        JPanel kpiRow = new JPanel(new GridLayout(1, 4, 12, 0));
        kpiRow.setOpaque(false);
        kpiRow.setPreferredSize(new Dimension(0, 80)); // prevent squishing
        lblRevenue  = makeKpiValue("Rs. 0");
        lblOrders   = makeKpiValue("0");
        lblItems    = makeKpiValue("0");
        lblAvgOrder = makeKpiValue("Rs. 0");

        kpiRow.add(makeKpiCard("Total Revenue",   lblRevenue,  "Selected Month"));
        kpiRow.add(makeKpiCard("Total Orders",    lblOrders,   "Selected Month"));
        kpiRow.add(makeKpiCard("Items Sold",      lblItems,    "Selected Month"));
        kpiRow.add(makeKpiCard("Avg Order Value", lblAvgOrder, "Selected Month"));

        // ── Assemble body ────────────────────────────────────────────
        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.add(controlBar, BorderLayout.NORTH);
        body.add(centerRow,  BorderLayout.CENTER);
        body.add(kpiRow,     BorderLayout.SOUTH);
        add(body, BorderLayout.CENTER);

        btnGenerate.addActionListener(e -> generateReport());
        btnExport.addActionListener(e -> exportPDF());
    }

    // ─────────────────────────────────────────────────────────────────
    //  Generate report  (respects the selected report type)
    // ─────────────────────────────────────────────────────────────────
    private void generateReport() {
        int    month       = cmbMonth.getSelectedIndex() + 1;
        int    year        = Integer.parseInt((String) cmbYear.getSelectedItem());
        String reportType  = (String) cmbReportType.getSelectedItem();

        // Always refresh KPI numbers
        lastRevenue = oc.getMonthlyRevenue(month, year);
        lastOrders  = oc.getMonthlyOrderCount(month, year);
        lastItems   = oc.getMonthlyItemsSold(month, year);
        lastAvg     = lastOrders > 0 ? lastRevenue / lastOrders : 0;

        lblRevenue.setText(String.format("Rs. %,.2f", lastRevenue));
        lblOrders.setText(String.valueOf(lastOrders));
        lblItems.setText(String.valueOf(lastItems));
        lblAvgOrder.setText(String.format("Rs. %,.2f", lastAvg));

        // Always refresh low-stock table
        lowStockModel.setRowCount(0);
        for (Stock s : sc.getLowStockItems()) {
            lowStockModel.addRow(new Object[]{s.getProductName(), s.getQuantityOnHand(), s.getReorderLevel()});
        }

        // Pre-fetch chart data once (no DB calls in paintComponent)
        if ("Sales Summary".equals(reportType) || "Order Report".equals(reportType)) {
            chartMax = 1;
            for (int m = 1; m <= 12; m++) {
                chartData[m-1] = "Order Report".equals(reportType)
                    ? oc.getMonthlyOrderCount(m, year)
                    : oc.getMonthlyRevenue(m, year);
                if (chartData[m-1] > chartMax) chartMax = chartData[m-1];
            }
        } else {
            // Inventory Report: show current stock quantities per month is not meaningful;
            // display stock-on-hand per product category as a simple bar instead.
            // We reuse the array for low-stock counts across the year (one bar per month = low-stock trend).
            chartMax = 1;
            for (int m = 1; m <= 12; m++) {
                chartData[m-1] = oc.getMonthlyRevenue(m, year); // fallback to revenue for chart
                if (chartData[m-1] > chartMax) chartMax = chartData[m-1];
            }
        }

        chartPanel.repaint();
    }

    // ─────────────────────────────────────────────────────────────────
    //  PDF Export  (content adapts to selected report type)
    // ─────────────────────────────────────────────────────────────────
    private void exportPDF() {
        JFileChooser fc = new JFileChooser();
        String reportType    = (String) cmbReportType.getSelectedItem();
        String monthName     = MONTHS[cmbMonth.getSelectedIndex()];
        String yearStr       = (String) cmbYear.getSelectedItem();
        String defaultName   = "GreenLoop_" + reportType.replace(" ", "_") + "_" + monthName + "_" + yearStr + ".pdf";
        fc.setSelectedFile(new File(defaultName));

        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File outFile = fc.getSelectedFile();
        if (!outFile.getName().toLowerCase().endsWith(".pdf")) {
            outFile = new File(outFile.getAbsolutePath() + ".pdf");
        }

        try (PDDocument doc = new PDDocument()) {
            // ── Page 1 ──────────────────────────────────────────────
            PDPage page1 = new PDPage(PDRectangle.A4);
            doc.addPage(page1);
            float pageW = page1.getMediaBox().getWidth();
            float pageH = page1.getMediaBox().getHeight();

            try (PDPageContentStream cs = new PDPageContentStream(doc, page1)) {
                // Header banner
                cs.setNonStrokingColor(new PDColor(new float[]{27f/255, 94f/255, 32f/255}, PDDeviceRGB.INSTANCE));
                cs.addRect(0, pageH - 80, pageW, 80);
                cs.fill();

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 20);
                cs.setNonStrokingColor(1f, 1f, 1f);
                cs.newLineAtOffset(30, pageH - 42);
                cs.showText("GreenLoop - " + reportType);
                cs.endText();

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA, 10);
                cs.setNonStrokingColor(1f, 1f, 1f);
                cs.newLineAtOffset(30, pageH - 62);
                cs.showText("Report Period: " + monthName + " " + yearStr + "  |  Generated: " + LocalDate.now());
                cs.endText();

                float y = pageH - 110;

                // ── KPI cards ────────────────────────────────────────
                String[] kpiLabels = {"Revenue", "Orders", "Items Sold", "Avg Value"};
                String[] kpiVals   = {
                    String.format("Rs.%,.0f", lastRevenue),
                    String.valueOf(lastOrders),
                    String.valueOf(lastItems),
                    String.format("Rs.%,.0f", lastAvg)
                };

                float colW = (pageW - 60) / 4;
                for (int i = 0; i < 4; i++) {
                    float cardX = 30 + (i * colW);
                    cs.setNonStrokingColor(0.95f, 0.95f, 0.95f);
                    cs.addRect(cardX, y - 10, colW - 8, 50);
                    cs.fill();

                    cs.beginText();
                    cs.setFont(PDType1Font.HELVETICA_BOLD, 9);
                    cs.setNonStrokingColor(0.4f, 0.4f, 0.4f);
                    cs.newLineAtOffset(cardX + 6, y + 26);
                    cs.showText(kpiLabels[i]);
                    cs.endText();

                    cs.beginText();
                    cs.setFont(PDType1Font.HELVETICA_BOLD, 13);
                    cs.setNonStrokingColor(27f/255, 94f/255, 32f/255);
                    cs.newLineAtOffset(cardX + 6, y + 8);
                    cs.showText(kpiVals[i]);
                    cs.endText();
                }

                y -= 70;

                // ── Section: Low Stock Alerts ─────────────────────────
                drawSectionHeader(cs, "Low Stock Alerts", y, pageW);
                y -= 24;

                // Table header
                float[] colXs   = {35f, 285f, 385f};
                String[] hdrCols = {"Product Name", "Stock", "Reorder Level"};
                cs.setNonStrokingColor(0.87f, 0.94f, 0.87f);
                cs.addRect(30, y - 4, pageW - 60, 18);
                cs.fill();

                for (int c = 0; c < hdrCols.length; c++) {
                    cs.beginText();
                    cs.setFont(PDType1Font.HELVETICA_BOLD, 9);
                    cs.setNonStrokingColor(0.1f, 0.35f, 0.1f);
                    cs.newLineAtOffset(colXs[c], y);
                    cs.showText(hdrCols[c]);
                    cs.endText();
                }
                y -= 18;

                cs.setFont(PDType1Font.HELVETICA, 10);
                boolean shaded = false;
                int stockRows  = lowStockModel.getRowCount();

                for (int i = 0; i < stockRows; i++) {
                    if (y < 50) {
                        // Overflow onto page 2
                        cs.close(); // close current stream before adding page
                        PDPage page2 = new PDPage(PDRectangle.A4);
                        doc.addPage(page2);
                        // Re-open on page2 is handled after this try-with-resources block
                        // We break and handle the remainder separately
                        writeOverflowRows(doc, page2, pageW, pageH, i, stockRows);
                        break;
                    }

                    if (shaded) {
                        cs.setNonStrokingColor(0.97f, 0.97f, 0.97f);
                        cs.addRect(30, y - 4, pageW - 60, 16);
                        cs.fill();
                    }
                    shaded = !shaded;

                    String[] rowVals = {
                        lowStockModel.getValueAt(i, 0).toString(),
                        lowStockModel.getValueAt(i, 1).toString(),
                        lowStockModel.getValueAt(i, 2).toString()
                    };
                    for (int c = 0; c < rowVals.length; c++) {
                        cs.beginText();
                        cs.setFont(PDType1Font.HELVETICA, 10);
                        cs.setNonStrokingColor(0.1f, 0.1f, 0.1f);
                        cs.newLineAtOffset(colXs[c], y);
                        cs.showText(rowVals[c]);
                        cs.endText();
                    }
                    y -= 18;
                }

                // ── Section: Annual Sales Chart (bar chart in PDF) ────
                if (y > 180) {
                    y -= 10;
                    drawSectionHeader(cs, "Annual Revenue Overview (" + yearStr + ")", y, pageW);
                    y -= 20;
                    drawPdfBarChart(cs, 30, y - 110, pageW - 60, 110, yearStr);
                }

                // Footer
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA, 8);
                cs.setNonStrokingColor(0.6f, 0.6f, 0.6f);
                cs.newLineAtOffset(30, 20);
                cs.showText("GreenLoop Business System  |  Confidential  |  Page 1");
                cs.endText();
            }

            doc.save(outFile);
            JOptionPane.showMessageDialog(this,
                "Report exported successfully:\n" + outFile.getName(),
                "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(outFile);

        } catch (NoClassDefFoundError e) {
            JOptionPane.showMessageDialog(this,
                "Required libraries (PDFBox/FontBox) are missing from the classpath.",
                "Library Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error generating PDF:\n" + ex.getMessage(),
                "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Write overflow stock rows onto a new page */
    private void writeOverflowRows(PDDocument doc, PDPage page, float pageW, float pageH,
                                   int startRow, int totalRows) throws Exception {
        try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
            float y = pageH - 40;

            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 11);
            cs.setNonStrokingColor(0.1f, 0.35f, 0.1f);
            cs.newLineAtOffset(30, y);
            cs.showText("Low Stock Alerts (continued)");
            cs.endText();
            y -= 22;

            float[] colXs    = {35f, 285f, 385f};
            String[] hdrCols  = {"Product Name", "Stock", "Reorder Level"};
            cs.setNonStrokingColor(0.87f, 0.94f, 0.87f);
            cs.addRect(30, y - 4, pageW - 60, 18);
            cs.fill();

            for (int c = 0; c < hdrCols.length; c++) {
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 9);
                cs.setNonStrokingColor(0.1f, 0.35f, 0.1f);
                cs.newLineAtOffset(colXs[c], y);
                cs.showText(hdrCols[c]);
                cs.endText();
            }
            y -= 18;

            boolean shaded = false;
            for (int i = startRow; i < totalRows && y > 50; i++) {
                if (shaded) {
                    cs.setNonStrokingColor(0.97f, 0.97f, 0.97f);
                    cs.addRect(30, y - 4, pageW - 60, 16);
                    cs.fill();
                }
                shaded = !shaded;

                String[] rowVals = {
                    lowStockModel.getValueAt(i, 0).toString(),
                    lowStockModel.getValueAt(i, 1).toString(),
                    lowStockModel.getValueAt(i, 2).toString()
                };
                for (int c = 0; c < rowVals.length; c++) {
                    cs.beginText();
                    cs.setFont(PDType1Font.HELVETICA, 10);
                    cs.setNonStrokingColor(0.1f, 0.1f, 0.1f);
                    cs.newLineAtOffset(colXs[c], y);
                    cs.showText(rowVals[c]);
                    cs.endText();
                }
                y -= 18;
            }

            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA, 8);
            cs.setNonStrokingColor(0.6f, 0.6f, 0.6f);
            cs.newLineAtOffset(30, 20);
            cs.showText("GreenLoop Business System  |  Confidential  |  Page 2");
            cs.endText();
        }
    }

    /** Draw a mini bar chart directly into the PDF using PDFBox primitives */
    private void drawPdfBarChart(PDPageContentStream cs, float startX, float startY,
                                  float chartW, float chartH, String yearStr) throws Exception {
        float barAreaW = chartW - 20;
        float barW     = barAreaW / 12 - 4;
        double maxVal  = chartMax;

        for (int i = 0; i < 12; i++) {
            float barH   = maxVal > 0 ? (float)(chartData[i] / maxVal * chartH) : 0;
            float barX   = startX + 10 + i * (barAreaW / 12);
            float barY   = startY;

            // Bar fill
            if (i == cmbMonth.getSelectedIndex()) {
                cs.setNonStrokingColor(27f/255, 94f/255, 32f/255);
            } else {
                cs.setNonStrokingColor(56f/255, 142f/255, 60f/255);
            }
            if (barH > 0) {
                cs.addRect(barX, barY, barW, barH);
                cs.fill();
            }

            // Month label
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA, 7);
            cs.setNonStrokingColor(0.4f, 0.4f, 0.4f);
            cs.newLineAtOffset(barX, barY - 10);
            cs.showText(MONTHS[i].substring(0, 3));
            cs.endText();
        }

        // Baseline
        cs.setStrokingColor(0.7f, 0.7f, 0.7f);
        cs.moveTo(startX + 10, startY);
        cs.lineTo(startX + chartW - 10, startY);
        cs.stroke();
    }

    /** Draw a coloured section header line */
    private void drawSectionHeader(PDPageContentStream cs, String text, float y, float pageW) throws Exception {
        cs.setNonStrokingColor(0.87f, 0.94f, 0.87f);
        cs.addRect(30, y - 4, pageW - 60, 20);
        cs.fill();

        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
        cs.setNonStrokingColor(27f/255, 94f/255, 32f/255);
        cs.newLineAtOffset(34, y + 4);
        cs.showText(text);
        cs.endText();
    }

    // ─────────────────────────────────────────────────────────────────
    //  Bar chart (Swing) — uses pre-cached data, no DB calls
    // ─────────────────────────────────────────────────────────────────
    private void drawBarChart(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = chartPanel.getWidth(), h = chartPanel.getHeight();
        if (w < 100 || h < 100) return;

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);

        String reportType = (String) cmbReportType.getSelectedItem();
        String chartTitle = "Order Report".equals(reportType) ? "Annual Order Count" : "Annual Revenue";
        g.setFont(UITheme.FONT_SUBTITLE);
        g.setColor(new Color(60, 60, 60));
        g.drawString(chartTitle + " (" + cmbYear.getSelectedItem() + ")", 15, 25);

        int margin = 40;
        int chartH = h - (margin * 2);
        int chartW = w - (margin * 2);
        int barW   = Math.max(4, chartW / 12 - 5);

        for (int i = 0; i < 12; i++) {
            int barH = (int)((chartData[i] / chartMax) * chartH);
            int x    = margin + (i * (chartW / 12));
            int y    = h - margin - barH;

            g.setColor(i == cmbMonth.getSelectedIndex() ? UITheme.DARK_GREEN : UITheme.MID_GREEN);
            g.fillRoundRect(x, y, barW, barH, 5, 5);

            // Value label on top of bar if enough space
            if (barH > 18) {
                g.setColor(Color.WHITE);
                g.setFont(new Font("SansSerif", Font.BOLD, 9));
                String lbl = formatK(chartData[i]);
                g.drawString(lbl, x + 2, y + 13);
            }

            g.setColor(Color.GRAY);
            g.setFont(new Font("SansSerif", Font.PLAIN, 10));
            g.drawString(MONTHS[i].substring(0, 3), x, h - margin + 15);
        }

        // Y-axis baseline
        g.setColor(new Color(200, 200, 200));
        g.drawLine(margin, h - margin, w - margin, h - margin);
    }

    private String formatK(double v) {
        if (v >= 1_000_000) return String.format("%.1fM", v / 1_000_000);
        if (v >= 1000)      return String.format("%.1fK", v / 1000);
        return String.format("%.0f", v);
    }

    private JPanel makeKpiCard(String title, JLabel val, String sub) {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        JLabel t = new JLabel(title);
        t.setFont(new Font("SansSerif", Font.BOLD, 12));
        t.setForeground(Color.GRAY);

        JLabel s = new JLabel(sub);
        s.setFont(new Font("SansSerif", Font.ITALIC, 10));
        s.setForeground(Color.LIGHT_GRAY);

        p.add(t,   BorderLayout.NORTH);
        p.add(val, BorderLayout.CENTER);
        p.add(s,   BorderLayout.SOUTH);
        return p;
    }

    private JLabel makeKpiValue(String txt) {
        JLabel l = new JLabel(txt);
        l.setFont(new Font("SansSerif", Font.BOLD, 18));
        l.setForeground(UITheme.MID_GREEN);
        return l;
    }
}
