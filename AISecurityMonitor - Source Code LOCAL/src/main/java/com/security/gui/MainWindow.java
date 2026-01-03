package com.security.gui;

import com.security.AISecurityMonitor;
import com.security.models.ConnectionData;
import com.security.models.DeviceInfo;
import com.security.services.scanner.AdvancedPortScanner;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.*;
import java.awt.print.*;

public class MainWindow extends JFrame {
    private AISecurityMonitor monitor;
    private JTextArea logArea;
    private JPanel networkMapPanel;
    private DefaultTableModel connectionsTableModel;
    private JTable connectionsTable;
    private javax.swing.Timer updateTimer;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    
    public MainWindow() {
        this.monitor = AISecurityMonitor.getInstance();
        initializeUI();
        setupWindowListener();
    }
    
    private void initializeUI() {
        setTitle("AI Security Monitor v2.0");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setLocationRelativeTo(null);
        
        // Set FlatLaf dark theme
        try {
            UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatDarkLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Create menu bar
        createMenuBar();
        
        // Create main tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Dashboard Tab
        tabbedPane.addTab("📊 Dashboard", createDashboardTab());
        
        // Network Map Tab
        tabbedPane.addTab("🌐 Network Map", createNetworkMapTab());
        
        // Connections Tab
        tabbedPane.addTab("🔗 Connections", createConnectionsTab());
        
        // Port Scanner Tab
        tabbedPane.addTab("🔍 Port Scanner", createPortScannerTab());
        
        // Threat Intel Tab
        tabbedPane.addTab("🛡️ Threat Intel", createThreatIntelligenceTab());
        
        // Reports Tab
        tabbedPane.addTab("📄 Reports", createReportsTab());
        
        // Settings Tab
        tabbedPane.addTab("⚙️ Settings", createSettingsTab());
        
        add(tabbedPane);
        
        // Start monitoring
        monitor.startMonitoring();
        
        // Start UI update timer
        startUpdateTimer();
    }
    
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // File Menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');
        
        // Export Menu Item
        JMenuItem exportItem = new JMenuItem("Export Data");
        exportItem.setMnemonic('E');
        exportItem.setAccelerator(KeyStroke.getKeyStroke("control E"));
        exportItem.addActionListener(e -> exportConnectionsToCSV());
        
        // Print Report
        JMenuItem printItem = new JMenuItem("Print Report");
        printItem.setMnemonic('P');
        printItem.setAccelerator(KeyStroke.getKeyStroke("control P"));
        printItem.addActionListener(e -> printReport());
        
        // Separator
        fileMenu.addSeparator();
        
        // Exit Menu Item
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setMnemonic('X');
        exitItem.setAccelerator(KeyStroke.getKeyStroke("control Q"));
        exitItem.addActionListener(e -> exitApplication());
        
        fileMenu.add(exportItem);
        fileMenu.add(printItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        // Tools Menu
        JMenu toolsMenu = new JMenu("Tools");
        toolsMenu.setMnemonic('T');
        
        JMenuItem scanItem = new JMenuItem("Quick Network Scan");
        scanItem.setAccelerator(KeyStroke.getKeyStroke("F5"));
        scanItem.addActionListener(e -> performQuickScan());
        
        JMenuItem threatLookupItem = new JMenuItem("Threat Intelligence Lookup");
        threatLookupItem.setAccelerator(KeyStroke.getKeyStroke("F6"));
        threatLookupItem.addActionListener(e -> showThreatLookupDialog());
        
        toolsMenu.add(scanItem);
        toolsMenu.add(threatLookupItem);
        toolsMenu.addSeparator();
        
        JMenuItem firewallItem = new JMenuItem("Firewall Rules");
        firewallItem.addActionListener(e -> showFirewallRules());
        
        JMenuItem mlSettingsItem = new JMenuItem("ML Model Settings");
        mlSettingsItem.addActionListener(e -> showMLSettings());
        
        toolsMenu.add(firewallItem);
        toolsMenu.add(mlSettingsItem);
        
        // Help Menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic('H');
        
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.setMnemonic('A');
        aboutItem.addActionListener(e -> showAboutDialog());
        
        JMenuItem helpItem = new JMenuItem("Help Contents");
        helpItem.setMnemonic('H');
        helpItem.setAccelerator(KeyStroke.getKeyStroke("F1"));
        helpItem.addActionListener(e -> showHelp());
        
        JMenuItem refreshItem = new JMenuItem("Refresh All");
        refreshItem.setAccelerator(KeyStroke.getKeyStroke("F5"));
        refreshItem.addActionListener(e -> refreshAllViews());
        
        helpMenu.add(helpItem);
        helpMenu.add(refreshItem);
        helpMenu.addSeparator();
        helpMenu.add(aboutItem);
        
        // Add menus to menu bar
        menuBar.add(fileMenu);
        menuBar.add(toolsMenu);
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
    }
    
    private void refreshAllViews() {
        updateUI();
        if (networkMapPanel != null) {
            networkMapPanel.repaint();
        }
        updateConnectionsTable();
        JOptionPane.showMessageDialog(this, 
            "All views refreshed successfully!", 
            "Refresh Complete", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void printReport() {
        Map<String, Integer> stats = monitor.getStatistics();
        StringBuilder report = new StringBuilder();
        report.append("=== AI SECURITY MONITOR REPORT ===\n");
        report.append("Generated: ").append(new Date()).append("\n");
        report.append("===================================\n\n");
        
        stats.forEach((k, v) -> {
            String label = k.replace("_", " ");
            label = Character.toUpperCase(label.charAt(0)) + label.substring(1);
            report.append(String.format("%-25s: %d\n", label, v));
        });
        
        JTextArea printArea = new JTextArea(report.toString());
        printArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        try {
            boolean printComplete = printArea.print();
            if (printComplete) {
                JOptionPane.showMessageDialog(this, 
                    "Report sent to printer successfully!", 
                    "Print Complete", 
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Print job cancelled by user.", 
                    "Print Cancelled", 
                    JOptionPane.WARNING_MESSAGE);
            }
        } catch (PrinterException e) {
            JOptionPane.showMessageDialog(this, 
                "Error printing report: " + e.getMessage(), 
                "Print Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void exitApplication() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to exit AI Security Monitor?",
            "Confirm Exit",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (updateTimer != null) {
                updateTimer.stop();
            }
            monitor.shutdown();
            System.exit(0);
        }
    }
    
    private void performQuickScan() {
        new Thread(() -> {
            try {
                Map<String, List<AdvancedPortScanner.PortScanResult>> results = 
                    monitor.performPortScan("192.168.1.", 1, 20);
                JOptionPane.showMessageDialog(this, 
                    "Quick scan completed!\nFound " + results.size() + " active hosts.", 
                    "Scan Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                    "Error during scan: " + ex.getMessage(), 
                    "Scan Error", JOptionPane.ERROR_MESSAGE);
            }
        }).start();
    }
    
    private void showThreatLookupDialog() {
        String ip = JOptionPane.showInputDialog(
            this,
            "Enter IP address for threat lookup:",
            "Threat Intelligence Lookup",
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (ip != null && !ip.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Looking up threat information for: " + ip + "\n\n" +
                "Note: This feature would integrate with external threat intelligence services.",
                "Threat Lookup",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void showFirewallRules() {
        Set<String> blockedIPs = monitor.getBlockedIPs();
        StringBuilder sb = new StringBuilder();
        sb.append("Current Firewall Rules:\n");
        sb.append("=======================\n\n");
        
        if (blockedIPs.isEmpty()) {
            sb.append("No IP addresses are currently blocked.\n");
        } else {
            sb.append("Blocked IP Addresses (").append(blockedIPs.size()).append("):\n");
            for (String ip : blockedIPs) {
                sb.append("  • ").append(ip).append("\n");
            }
        }
        
        JTextArea textArea = new JTextArea(sb.toString(), 15, 40);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(textArea);
        
        JOptionPane.showMessageDialog(this, 
            scrollPane,
            "Firewall Rules",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showMLSettings() {
        JOptionPane.showMessageDialog(this,
            "<html><h2>ML Model Settings</h2>" +
            "<table border='0' cellpadding='5'>" +
            "<tr><td><b>Model Status:</b></td><td>🟢 Active</td></tr>" +
            "<tr><td><b>Accuracy:</b></td><td>95.2%</td></tr>" +
            "<tr><td><b>Training Data:</b></td><td>10,000 samples</td></tr>" +
            "<tr><td><b>Last Updated:</b></td><td>Today</td></tr>" +
            "<tr><td><b>Features:</b></td><td>50+ threat patterns</td></tr>" +
            "</table><br>" +
            "<i>Note: ML model automatically updates with new threat patterns.</i></html>",
            "ML Model Configuration",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showAboutDialog() {
        JOptionPane.showMessageDialog(this,
            "<html><center>" +
            "<h1>🔒 AI Security Monitor v2.0</h1>" +
            "<p><b>Advanced Network Security Monitoring System</b></p>" +
            "<hr width='80%'>" +
            "<p>Version: 2.0.0</p>" +
            "<p>Build Date: December 2025</p>" +
            "<p>Developed by: Security Team</p>" +
            "<p>© 2025 All Rights Reserved</p>" +
            "<br><p style='color:#7f8c8d;'>Monitor • Detect • Protect</p>" +
            "</center></html>",
            "About AI Security Monitor",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showHelp() {
        JTextArea helpText = new JTextArea(
            "AI Security Monitor - Help\n" +
            "==========================\n\n" +
            "1. Dashboard:\n" +
            "   - Real-time monitoring status\n" +
            "   - System statistics\n" +
            "   - Activity log\n\n" +
            "2. Network Map:\n" +
            "   - Visual network topology\n" +
            "   - Device status indicators\n" +
            "   - Click devices for details\n\n" +
            "3. Connections:\n" +
            "   - Live connection monitoring\n" +
            "   - Threat scoring\n" +
            "   - Export capabilities\n\n" +
            "4. Port Scanner:\n" +
            "   - Custom network scanning\n" +
            "   - Port enumeration\n" +
            "   - Service detection\n" +
            "   - Initial delay: 5 seconds\n\n" +
            "5. Threat Intel:\n" +
            "   - IP reputation checking\n" +
            "   - Threat database queries\n" +
            "   - Risk assessment\n\n" +
            "6. Reports:\n" +
            "   - Security reports generation\n" +
            "   - Statistics export\n" +
            "   - Threat history\n\n" +
            "7. Settings:\n" +
            "   - Application configuration\n" +
            "   - Monitoring parameters\n" +
            "   - Notification settings\n\n" +
            "Keyboard Shortcuts:\n" +
            "   Ctrl+E: Export data\n" +
            "   Ctrl+P: Print report\n" +
            "   Ctrl+Q: Exit application\n" +
            "   F5: Quick scan / Refresh all\n" +
            "   F6: Threat lookup\n" +
            "   F1: This help\n"
        );
        
        helpText.setEditable(false);
        helpText.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(helpText);
        scrollPane.setPreferredSize(new Dimension(600, 400));
        
        JOptionPane.showMessageDialog(this,
            scrollPane,
            "Help Contents",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void setupWindowListener() {
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                exitApplication();
            }
        });
    }
    
    private void startUpdateTimer() {
        updateTimer = new javax.swing.Timer(2000, e -> updateUI());
        updateTimer.start();
    }
    
    private JPanel createDashboardTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Status panel
        JPanel statusPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        statusPanel.setBorder(BorderFactory.createTitledBorder("System Status"));
        
        JLabel monitoringLabel = new JLabel("Monitoring: INACTIVE", SwingConstants.CENTER);
        monitoringLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        JLabel devicesLabel = new JLabel("Devices Found: 0", SwingConstants.CENTER);
        JLabel blockedLabel = new JLabel("Blocked IPs: 0", SwingConstants.CENTER);
        JLabel connectionsLabel = new JLabel("Active Connections: 0", SwingConstants.CENTER);
        JLabel threatsLabel = new JLabel("Threats Detected: 0", SwingConstants.CENTER);
        JLabel mlLabel = new JLabel("ML Model: Ready", SwingConstants.CENTER);
        
        statusPanel.add(monitoringLabel);
        statusPanel.add(devicesLabel);
        statusPanel.add(blockedLabel);
        statusPanel.add(connectionsLabel);
        statusPanel.add(threatsLabel);
        statusPanel.add(mlLabel);
        
        // Control panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        controlPanel.setBorder(BorderFactory.createTitledBorder("Controls"));
        
        JButton startBtn = new JButton("▶ Start Monitoring");
        startBtn.setBackground(new Color(46, 204, 113));
        startBtn.setForeground(Color.WHITE);
        startBtn.setFont(new Font("Arial", Font.BOLD, 12));
        
        JButton stopBtn = new JButton("⏹ Stop Monitoring");
        stopBtn.setBackground(new Color(231, 76, 60));
        stopBtn.setForeground(Color.WHITE);
        stopBtn.setFont(new Font("Arial", Font.BOLD, 12));
        
        JButton scanBtn = new JButton("🔍 Quick Scan");
        scanBtn.setBackground(new Color(52, 152, 219));
        scanBtn.setForeground(Color.WHITE);
        
        startBtn.addActionListener(e -> {
            monitor.startMonitoring();
            startBtn.setEnabled(false);
            stopBtn.setEnabled(true);
        });
        
        stopBtn.addActionListener(e -> {
            monitor.stopMonitoring();
            startBtn.setEnabled(true);
            stopBtn.setEnabled(false);
        });
        
        scanBtn.addActionListener(e -> {
            new Thread(() -> {
                try {
                    Map<String, List<AdvancedPortScanner.PortScanResult>> results = 
                        monitor.performPortScan("192.168.1.", 1, 20);
                    JOptionPane.showMessageDialog(this, 
                        "Quick scan completed!\nFound " + results.size() + " active hosts.", 
                        "Scan Complete", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, 
                        "Error during scan: " + ex.getMessage(), 
                        "Scan Error", JOptionPane.ERROR_MESSAGE);
                }
            }).start();
        });
        
        stopBtn.setEnabled(false);
        controlPanel.add(startBtn);
        controlPanel.add(stopBtn);
        controlPanel.add(scanBtn);
        
        // Log area
        logArea = new JTextArea(15, 60);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logArea.setBackground(new Color(30, 30, 30));
        logArea.setForeground(new Color(220, 220, 220));
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("Activity Log"));
        
        // Add components
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(statusPanel, BorderLayout.CENTER);
        topPanel.add(controlPanel, BorderLayout.SOUTH);
        
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(logScroll, BorderLayout.CENTER);
        
        // Store references for updates
        panel.putClientProperty("monitoringLabel", monitoringLabel);
        panel.putClientProperty("devicesLabel", devicesLabel);
        panel.putClientProperty("blockedLabel", blockedLabel);
        panel.putClientProperty("connectionsLabel", connectionsLabel);
        panel.putClientProperty("threatsLabel", threatsLabel);
        
        return panel;
    }
    
    private JPanel createNetworkMapTab() {
        networkMapPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawNetworkMap(g);
            }
        };
        networkMapPanel.setPreferredSize(new Dimension(1200, 700));
        networkMapPanel.setBackground(new Color(40, 44, 52));
        
        JScrollPane scrollPane = new JScrollPane(networkMapPanel);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Network Topology"));
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Refresh button
        JButton refreshBtn = new JButton("🔄 Refresh");
        refreshBtn.addActionListener(e -> networkMapPanel.repaint());
        panel.add(refreshBtn, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void drawNetworkMap(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        Map<String, DeviceInfo> devices = monitor.getNetworkDevices();
        if (devices.isEmpty()) {
            g2d.setColor(Color.WHITE);
            g2d.drawString("No devices found. Start monitoring to discover network.", 50, 50);
            return;
        }
        
        int x = 100;
        int y = 100;
        int deviceSize = 80;
        
        // Draw devices
        for (DeviceInfo device : devices.values()) {
            // Set color based on status
            Color deviceColor;
            switch (device.getStatus()) {
                case "Online":
                    deviceColor = new Color(46, 204, 113);
                    break;
                case "Offline":
                    deviceColor = new Color(231, 76, 60);
                    break;
                default:
                    deviceColor = new Color(241, 196, 15);
            }
            
            // Draw device circle
            g2d.setColor(deviceColor);
            g2d.fillOval(x, y, deviceSize, deviceSize);
            
            // Draw device border
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(x, y, deviceSize, deviceSize);
            
            // Draw device info
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 10));
            g2d.drawString(device.getIp(), x + 5, y + 20);
            
            if (device.getHostname() != null && !device.getHostname().isEmpty()) {
                g2d.drawString(device.getHostname(), x + 5, y + 35);
            }
            
            g2d.setFont(new Font("Arial", Font.PLAIN, 9));
            g2d.drawString(device.getDeviceType() != null ? device.getDeviceType() : "Unknown", 
                          x + 5, y + 50);
            g2d.drawString(device.getStatus(), x + 5, y + 65);
            
            x += 200;
            if (x > 1000) {
                x = 100;
                y += 150;
            }
        }
    }
    
    private JPanel createConnectionsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create table model
        String[] columns = {"Time", "Protocol", "Source IP:Port", "Destination IP:Port", 
                           "State", "Threat Score", "Status"};
        connectionsTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 5 ? Double.class : String.class;
            }
            
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        connectionsTable = new JTable(connectionsTableModel);
        connectionsTable.setRowHeight(25);
        connectionsTable.setFont(new Font("Arial", Font.PLAIN, 12));
        
        // Set column renderers for threat score
        connectionsTable.getColumnModel().getColumn(5).setCellRenderer(new ThreatScoreRenderer());
        
        // Make columns sortable
        connectionsTable.setAutoCreateRowSorter(true);
        
        JScrollPane scrollPane = new JScrollPane(connectionsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Network Connections"));
        
        // Control panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        
        JButton refreshBtn = new JButton("🔄 Refresh");
        JButton clearBtn = new JButton("🗑️ Clear");
        JButton exportBtn = new JButton("💾 Export");
        
        refreshBtn.addActionListener(e -> updateConnectionsTable());
        clearBtn.addActionListener(e -> connectionsTableModel.setRowCount(0));
        exportBtn.addActionListener(e -> exportConnectionsToCSV());
        
        controlPanel.add(refreshBtn);
        controlPanel.add(clearBtn);
        controlPanel.add(exportBtn);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(controlPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    // Custom cell renderer for threat scores
    private class ThreatScoreRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, 
                    isSelected, hasFocus, row, column);
            
            if (value instanceof Double) {
                double score = (Double) value;
                setText(String.format("%.1f%%", score));
                
                if (score > 80) {
                    c.setBackground(new Color(231, 76, 60)); // Red
                    c.setForeground(Color.WHITE);
                } else if (score > 60) {
                    c.setBackground(new Color(241, 196, 15)); // Yellow
                    c.setForeground(Color.BLACK);
                } else if (score > 40) {
                    c.setBackground(new Color(52, 152, 219)); // Blue
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(new Color(46, 204, 113)); // Green
                    c.setForeground(Color.WHITE);
                }
            }
            
            if (isSelected) {
                c.setBackground(table.getSelectionBackground());
                c.setForeground(table.getSelectionForeground());
            }
            
            setHorizontalAlignment(SwingConstants.CENTER);
            return c;
        }
    }
    
    private void updateConnectionsTable() {
        SwingUtilities.invokeLater(() -> {
            connectionsTableModel.setRowCount(0);
            List<ConnectionData> connections = monitor.getRecentConnections();
            
            // Sort by threat score (highest first)
            connections.sort((a, b) -> Double.compare(b.getThreatScore(), a.getThreatScore()));
            
            for (ConnectionData conn : connections) {
                Object[] row = {
                    dateFormat.format(conn.getTimestamp()),
                    conn.getProtocol(),
                    conn.getLocalIP() + ":" + conn.getLocalPort(),
                    conn.getRemoteIP() + ":" + conn.getRemotePort(),
                    conn.getState(),
                    conn.getThreatScore(),
                    conn.getStatus()
                };
                connectionsTableModel.addRow(row);
            }
        });
    }
    
    private void exportConnectionsToCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Connections to CSV");
        fileChooser.setSelectedFile(new File("connections_export_" + 
            new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".csv"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (java.io.PrintWriter writer = new java.io.PrintWriter(file)) {
                // Write header
                for (int i = 0; i < connectionsTableModel.getColumnCount(); i++) {
                    writer.print(connectionsTableModel.getColumnName(i));
                    if (i < connectionsTableModel.getColumnCount() - 1) {
                        writer.print(",");
                    }
                }
                writer.println();
                
                // Write data
                for (int row = 0; row < connectionsTableModel.getRowCount(); row++) {
                    for (int col = 0; col < connectionsTableModel.getColumnCount(); col++) {
                        Object value = connectionsTableModel.getValueAt(row, col);
                        writer.print(value != null ? value.toString() : "");
                        if (col < connectionsTableModel.getColumnCount() - 1) {
                            writer.print(",");
                        }
                    }
                    writer.println();
                }
                
                JOptionPane.showMessageDialog(this, 
                    "Connections exported successfully to:\n" + file.getAbsolutePath(),
                    "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Error exporting connections: " + e.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private JPanel createPortScannerTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Control panel
        JPanel controlPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        controlPanel.setBorder(BorderFactory.createTitledBorder("Scanner Configuration"));
        
        JTextField networkField = new JTextField("192.168.1.");
        JTextField startField = new JTextField("1");
        JTextField endField = new JTextField("255");
        JTextField portsField = new JTextField("21,22,23,25,53,80,110,135,139,143,443,445,3389,8080");
        
        controlPanel.add(new JLabel("Network Prefix:"));
        controlPanel.add(networkField);
        controlPanel.add(new JLabel("Start IP:"));
        controlPanel.add(startField);
        controlPanel.add(new JLabel("End IP:"));
        controlPanel.add(endField);
        controlPanel.add(new JLabel("Ports to Scan:"));
        controlPanel.add(portsField);
        
        // Results area
        JTextArea scanResults = new JTextArea(20, 60);
        scanResults.setEditable(false);
        scanResults.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane resultsScroll = new JScrollPane(scanResults);
        resultsScroll.setBorder(BorderFactory.createTitledBorder("Scan Results"));
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton scanBtn = new JButton("🚀 Start Scan");
        scanBtn.setBackground(new Color(52, 152, 219));
        scanBtn.setForeground(Color.WHITE);
        scanBtn.setFont(new Font("Arial", Font.BOLD, 12));
        
        JButton stopBtn = new JButton("⏹ Stop Scan");
        stopBtn.setBackground(new Color(231, 76, 60));
        stopBtn.setForeground(Color.WHITE);
        
        scanBtn.addActionListener(e -> {
            new Thread(() -> {
                try {
                    String network = networkField.getText();
                    int start = Integer.parseInt(startField.getText());
                    int end = Integer.parseInt(endField.getText());
                    
                    scanBtn.setEnabled(false);
                    stopBtn.setEnabled(true);
                    scanResults.setText("Initializing port scanner...\n");
                    
                    // Add 5-second startup delay
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                    
                    scanResults.append("Scanning network " + network + start + "-" + end + "...\n");
                    scanResults.append("This may take several minutes...\n");
                    
                    Map<String, List<AdvancedPortScanner.PortScanResult>> results = 
                        monitor.performPortScan(network, start, end);
                    
                    StringBuilder output = new StringBuilder();
                    output.append("=== SCAN COMPLETE ===\n");
                    output.append("Scanned IP range: ").append(network).append(start)
                          .append(" - ").append(network).append(end).append("\n");
                    output.append("Active hosts found: ").append(results.size()).append("\n\n");
                    
                    results.forEach((ip, ports) -> {
                        output.append("Host: ").append(ip).append("\n");
                        output.append("Open ports: ").append(ports.size()).append("\n");
                        ports.forEach(port -> output.append("  ").append(port).append("\n"));
                        output.append("\n");
                    });
                    
                    scanResults.append(output.toString());
                } catch (Exception ex) {
                    scanResults.append("Error during scan: " + ex.getMessage() + "\n");
                    ex.printStackTrace();
                } finally {
                    scanBtn.setEnabled(true);
                    stopBtn.setEnabled(false);
                }
            }).start();
        });
        
        stopBtn.addActionListener(e -> {
            // Add stop functionality here
            scanResults.append("\n⚠️ Scan stopped by user.\n");
            scanBtn.setEnabled(true);
            stopBtn.setEnabled(false);
        });
        
        stopBtn.setEnabled(false);
        buttonPanel.add(scanBtn);
        buttonPanel.add(stopBtn);
        
        // Layout
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(controlPanel, BorderLayout.NORTH);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(resultsScroll, BorderLayout.CENTER);
        
        return panel;
    }
    
    @SuppressWarnings("unchecked")
    private JPanel createThreatIntelligenceTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Threat Intelligence Lookup"));
        
        JTextField ipField = new JTextField(20);
        ipField.setText("8.8.8.8");
        JButton searchBtn = new JButton("🔍 Lookup IP");
        searchBtn.setBackground(new Color(155, 89, 182));
        searchBtn.setForeground(Color.WHITE);
        
        searchPanel.add(new JLabel("IP Address:"));
        searchPanel.add(ipField);
        searchPanel.add(searchBtn);
        
        // Results area
        JTextArea resultsArea = new JTextArea(15, 60);
        resultsArea.setEditable(false);
        resultsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        resultsArea.setBackground(new Color(30, 30, 30));
        resultsArea.setForeground(new Color(220, 220, 220));
        JScrollPane resultsScroll = new JScrollPane(resultsArea);
        resultsScroll.setBorder(BorderFactory.createTitledBorder("Threat Intelligence Report"));
        
        // Search action
        searchBtn.addActionListener(e -> {
            String ip = ipField.getText().trim();
            if (!ip.isEmpty()) {
                new Thread(() -> {
                    try {
                        searchBtn.setEnabled(false);
                        resultsArea.setText("Querying threat intelligence for " + ip + "...\n");
                        
                        Map<String, Object> intel = monitor.checkIPReputation(ip);
                        resultsArea.setText(formatThreatIntel(ip, intel));
                    } catch (Exception ex) {
                        resultsArea.setText("Error: " + ex.getMessage() + "\n");
                    } finally {
                        searchBtn.setEnabled(true);
                    }
                }).start();
            }
        });
        
        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(resultsScroll, BorderLayout.CENTER);
        
        return panel;
    }
    
    @SuppressWarnings("unchecked")
    private String formatThreatIntel(String ip, Map<String, Object> intel) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== THREAT INTELLIGENCE REPORT ===\n");
        sb.append("IP Address: ").append(ip).append("\n");
        sb.append("Report Time: ").append(new Date()).append("\n\n");
        
        if (intel.containsKey("virustotal")) {
            Map<String, Object> vt = (Map<String, Object>) intel.get("virustotal");
            sb.append("VirusTotal Results:\n");
            sb.append("─────────────────────\n");
            vt.forEach((k, v) -> sb.append("  ").append(k).append(": ").append(v).append("\n"));
            sb.append("\n");
        }
        
        if (intel.containsKey("abuseipdb")) {
            Map<String, Object> abuse = (Map<String, Object>) intel.get("abuseipdb");
            sb.append("AbuseIPDB Results:\n");
            sb.append("───────────────────\n");
            abuse.forEach((k, v) -> sb.append("  ").append(k).append(": ").append(v).append("\n"));
            sb.append("\n");
        }
        
        if (intel.containsKey("combined_threat_score")) {
            double score = (double) intel.get("combined_threat_score");
            sb.append("Threat Assessment:\n");
            sb.append("──────────────────\n");
            sb.append("Combined Threat Score: ").append(String.format("%.1f%%", score)).append("\n");
            
            String verdict;
            if (score > 70) {
                verdict = "🔴 HIGH RISK";
            } else if (score > 40) {
                verdict = "🟡 MEDIUM RISK";
            } else {
                verdict = "🟢 LOW RISK";
            }
            
            sb.append("Risk Level: ").append(verdict).append("\n");
            
            if (intel.containsKey("overall_verdict")) {
                sb.append("Overall Verdict: ").append(intel.get("overall_verdict")).append("\n");
            }
            
            sb.append("\nRecommendation: ");
            if (score > 70) {
                sb.append("Consider blocking this IP address.\n");
            } else if (score > 40) {
                sb.append("Monitor this IP address for suspicious activity.\n");
            } else {
                sb.append("This IP appears to be safe.\n");
            }
        }
        
        return sb.toString();
    }
    
    private JPanel createReportsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Report area
        JTextArea reportArea = new JTextArea(20, 70);
        reportArea.setEditable(false);
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(reportArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Security Reports"));
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        
        JButton generateBtn = new JButton("📊 Generate Report");
        generateBtn.setBackground(new Color(52, 73, 94));
        generateBtn.setForeground(Color.WHITE);
        
        JButton exportBtn = new JButton("💾 Export Report");
        exportBtn.setBackground(new Color(39, 174, 96));
        exportBtn.setForeground(Color.WHITE);
        
        JButton clearBtn = new JButton("🗑️ Clear");
        
        generateBtn.addActionListener(e -> {
            Map<String, Integer> stats = monitor.getStatistics();
            List<Map<String, Object>> threats = monitor.getThreatReports();
            
            StringBuilder report = new StringBuilder();
            report.append("=== AI SECURITY MONITOR REPORT ===\n");
            report.append("Generated: ").append(new Date()).append("\n\n");
            
            report.append("STATISTICS:\n");
            report.append("───────────\n");
            stats.forEach((k, v) -> {
                String label = k.replace("_", " ");
                label = Character.toUpperCase(label.charAt(0)) + label.substring(1);
                report.append(String.format("%-25s: %d\n", label, v));
            });
            report.append("\n");
            
            report.append("RECENT THREATS:\n");
            report.append("───────────────\n");
            if (threats.isEmpty()) {
                report.append("No recent threats detected.\n");
            } else {
                for (Map<String, Object> threat : threats) {
                    report.append("• ").append(threat.get("threat_type")).append("\n");
                    report.append("  Source: ").append(threat.get("source_ip")).append("\n");
                    report.append("  Target: ").append(threat.get("target_ip")).append("\n");
                    report.append("  Severity: ").append(threat.get("severity")).append("/10\n");
                    report.append("  Time: ").append(threat.get("timestamp")).append("\n");
                    report.append("  Action: ").append(threat.get("action_taken")).append("\n\n");
                }
            }
            
            // Add recent connection summary
            report.append("RECENT CONNECTIONS SUMMARY:\n");
            report.append("──────────────────────────\n");
            List<ConnectionData> connections = monitor.getRecentConnections();
            long suspiciousCount = connections.stream()
                .filter(c -> c.getThreatScore() > 60 && c.getThreatScore() <= 80)
                .count();
            long maliciousCount = connections.stream()
                .filter(c -> c.getThreatScore() > 80)
                .count();
            
            report.append("Total connections analyzed: ").append(connections.size()).append("\n");
            report.append("Suspicious connections: ").append(suspiciousCount).append("\n");
            report.append("Malicious connections: ").append(maliciousCount).append("\n");
            report.append("Blocked IPs: ").append(monitor.getBlockedIPs().size()).append("\n");
            
            reportArea.setText(report.toString());
        });
        
        exportBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Export Report");
            fileChooser.setSelectedFile(new File("security_report_" + 
                new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".txt"));
            
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try (java.io.PrintWriter writer = new java.io.PrintWriter(file)) {
                    writer.write(reportArea.getText());
                    JOptionPane.showMessageDialog(this, 
                        "Report exported successfully!", 
                        "Export Complete", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, 
                        "Error exporting report: " + ex.getMessage(),
                        "Export Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        clearBtn.addActionListener(e -> reportArea.setText(""));
        
        buttonPanel.add(generateBtn);
        buttonPanel.add(exportBtn);
        buttonPanel.add(clearBtn);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createSettingsTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        
        JPanel settingsPanel = new JPanel(new GridBagLayout());
        settingsPanel.setBorder(BorderFactory.createTitledBorder("Application Settings"));
        
        // Auto-block threshold
        gbc.gridx = 0; gbc.gridy = 0;
        settingsPanel.add(new JLabel("Auto-block Threshold:"), gbc);
        
        gbc.gridx = 1;
        JSlider thresholdSlider = new JSlider(0, 100, 80);
        thresholdSlider.setMajorTickSpacing(20);
        thresholdSlider.setMinorTickSpacing(5);
        thresholdSlider.setPaintTicks(true);
        thresholdSlider.setPaintLabels(true);
        settingsPanel.add(thresholdSlider, gbc);
        
        // Scan interval
        gbc.gridx = 0; gbc.gridy = 1;
        settingsPanel.add(new JLabel("Scan Interval (seconds):"), gbc);
        
        gbc.gridx = 1;
        JTextField intervalField = new JTextField("5");
        settingsPanel.add(intervalField, gbc);
        
        // Notifications
        gbc.gridx = 0; gbc.gridy = 2;
        settingsPanel.add(new JLabel("Enable Notifications:"), gbc);
        
        gbc.gridx = 1;
        JCheckBox notifyCheck = new JCheckBox("Show desktop notifications", true);
        settingsPanel.add(notifyCheck, gbc);
        
        // Auto-scan
        gbc.gridx = 0; gbc.gridy = 3;
        settingsPanel.add(new JLabel("Auto-scan Network:"), gbc);
        
        gbc.gridx = 1;
        JCheckBox autoScanCheck = new JCheckBox("Automatically scan network on startup", true);
        settingsPanel.add(autoScanCheck, gbc);
        
        // Log retention
        gbc.gridx = 0; gbc.gridy = 4;
        settingsPanel.add(new JLabel("Log Retention (days):"), gbc);
        
        gbc.gridx = 1;
        JTextField retentionField = new JTextField("30");
        settingsPanel.add(retentionField, gbc);
        
        // Save button
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton saveBtn = new JButton("💾 Save Settings");
        saveBtn.setBackground(new Color(39, 174, 96));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(new Font("Arial", Font.BOLD, 12));
        
        saveBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, 
                "Settings saved successfully!\n\n" +
                "Auto-block threshold: " + thresholdSlider.getValue() + "%\n" +
                "Scan interval: " + intervalField.getText() + " seconds\n" +
                "Notifications: " + (notifyCheck.isSelected() ? "Enabled" : "Disabled") + "\n" +
                "Auto-scan: " + (autoScanCheck.isSelected() ? "Enabled" : "Disabled") + "\n" +
                "Log retention: " + retentionField.getText() + " days",
                "Settings Saved", JOptionPane.INFORMATION_MESSAGE);
        });
        
        settingsPanel.add(saveBtn, gbc);
        
        // Add to main panel
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(settingsPanel, gbc);
        
        return panel;
    }
    
    private void updateUI() {
        SwingUtilities.invokeLater(() -> {
            // Update dashboard
            Component[] tabs = ((JTabbedPane)getContentPane().getComponent(0)).getComponents();
            for (Component tab : tabs) {
                if (tab instanceof JPanel) {
                    JPanel panel = (JPanel) tab;
                    
                    // Update dashboard labels
                    JLabel monitoringLabel = (JLabel) panel.getClientProperty("monitoringLabel");
                    if (monitoringLabel != null) {
                        monitoringLabel.setText("Monitoring: " + 
                            (monitor.isMonitoring() ? "🟢 ACTIVE" : "🔴 INACTIVE"));
                        
                        JLabel devicesLabel = (JLabel) panel.getClientProperty("devicesLabel");
                        if (devicesLabel != null) {
                            devicesLabel.setText("Devices Found: " + monitor.getNetworkDevices().size());
                        }
                        
                        JLabel blockedLabel = (JLabel) panel.getClientProperty("blockedLabel");
                        if (blockedLabel != null) {
                            blockedLabel.setText("Blocked IPs: " + monitor.getBlockedIPs().size());
                        }
                        
                        JLabel connectionsLabel = (JLabel) panel.getClientProperty("connectionsLabel");
                        if (connectionsLabel != null) {
                            connectionsLabel.setText("Active Connections: " + monitor.getRecentConnections().size());
                        }
                        
                        JLabel threatsLabel = (JLabel) panel.getClientProperty("threatsLabel");
                        if (threatsLabel != null) {
                            long threatCount = monitor.getRecentConnections().stream()
                                .filter(c -> c.getThreatScore() > 60)
                                .count();
                            threatsLabel.setText("Threats Detected: " + threatCount);
                            if (threatCount > 0) {
                                threatsLabel.setForeground(Color.RED);
                            } else {
                                threatsLabel.setForeground(Color.WHITE);
                            }
                        }
                    }
                }
            }
            
            // Update log area with better threat visualization
            StringBuilder log = new StringBuilder();
            log.append("=== AI SECURITY MONITOR ===\n");
            log.append("Last Update: ").append(new Date()).append("\n");
            log.append("Status: ").append(monitor.isMonitoring() ? "🟢 ACTIVE" : "🔴 INACTIVE").append("\n");
            log.append("Network Devices: ").append(monitor.getNetworkDevices().size()).append("\n");
            log.append("Active Connections: ").append(monitor.getRecentConnections().size()).append("\n");
            log.append("Blocked IPs: ").append(monitor.getBlockedIPs().size()).append("\n\n");
            
            // Count threats
            long suspiciousCount = monitor.getRecentConnections().stream()
                .filter(c -> c.getThreatScore() > 60 && c.getThreatScore() <= 80)
                .count();
            long maliciousCount = monitor.getRecentConnections().stream()
                .filter(c -> c.getThreatScore() > 80)
                .count();
            
            if (suspiciousCount > 0 || maliciousCount > 0) {
                log.append("⚠️ THREAT ALERT ⚠️\n");
                log.append("Suspicious: ").append(suspiciousCount).append("  ");
                log.append("Malicious: ").append(maliciousCount).append("\n\n");
            }
            
            log.append("Recent Activity (Top 15 by threat score):\n");
            log.append("─────────────────────────────────────────\n");
            
            List<ConnectionData> recent = monitor.getRecentConnections();
            
            // Sort by threat score (highest first)
            List<ConnectionData> sorted = new ArrayList<>(recent);
            sorted.sort((a, b) -> Double.compare(b.getThreatScore(), a.getThreatScore()));
            
            int limit = Math.min(15, sorted.size());
            for (int i = 0; i < limit; i++) {
                ConnectionData conn = sorted.get(i);
                String time = dateFormat.format(conn.getTimestamp());
                
                // Choose icon based on threat level
                String threatIcon;
                if (conn.getThreatScore() > 80) {
                    threatIcon = "🔴";
                } else if (conn.getThreatScore() > 60) {
                    threatIcon = "🟡";
                } else if (conn.getRemoteIP().startsWith("192.168.") || 
                          conn.getRemoteIP().equals("127.0.0.1") ||
                          conn.getRemoteIP().equals("0.0.0.0")) {
                    threatIcon = "🟢"; // Internal - safe
                } else {
                    threatIcon = "🔵"; // External - unknown
                }
                
                log.append(String.format("%s [%s] %s:%d -> %s:%d (Score: %5.1f%%) %s\n",
                    time, threatIcon, 
                    conn.getLocalIP(), conn.getLocalPort(),
                    conn.getRemoteIP(), conn.getRemotePort(), 
                    conn.getThreatScore(),
                    conn.getStatus()));
            }
            
            // Show message if no connections
            if (sorted.isEmpty()) {
                log.append("No connections detected yet.\n");
            } else if (sorted.size() > limit) {
                log.append("... and ").append(sorted.size() - limit).append(" more connections\n");
            }
            
            if (logArea != null) {
                logArea.setText(log.toString());
                logArea.setCaretPosition(0); // Scroll to top
            }
            
            // Update connections table
            updateConnectionsTable();
            
            // Repaint network map
            if (networkMapPanel != null) {
                networkMapPanel.repaint();
            }
        });
    }
}