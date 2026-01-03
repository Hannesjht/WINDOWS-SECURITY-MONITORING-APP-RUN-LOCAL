package com.security.services.scanner;

import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class AdvancedPortScanner {
    private static final int THREAD_POOL_SIZE = 100;
    private static final int TIMEOUT = 1000;
    private static final int[] COMMON_PORTS = {
        21, 22, 23, 25, 53, 80, 110, 111, 135, 139, 143, 443,
        445, 993, 995, 1723, 3306, 3389, 5900, 8080
    };
    
    public Map<String, List<PortScanResult>> scanNetworkRange(String networkPrefix, int start, int end) {
        Map<String, List<PortScanResult>> results = new ConcurrentHashMap<>();
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        List<Future<?>> futures = new ArrayList<>();
        
        for (int i = start; i <= end; i++) {
            // Create final variable for lambda
            final String ip = networkPrefix + i;
            futures.add(executor.submit(() -> {
                List<PortScanResult> portResults = scanHost(ip);
                if (!portResults.isEmpty()) {
                    results.put(ip, portResults);
                }
            }));
        }
        
        // Wait for all scans to complete
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        executor.shutdown();
        return results;
    }
    
    public List<PortScanResult> scanHost(String ip) {
        List<PortScanResult> results = new ArrayList<>();
        
        // First check if host is alive
        if (!isHostAlive(ip)) {
            return results;
        }
        
        ExecutorService executor = Executors.newFixedThreadPool(50);
        List<Future<PortScanResult>> futures = new ArrayList<>();
        
        // Scan common ports
        for (int port : COMMON_PORTS) {
            final int currentPort = port; // Make final for lambda
            futures.add(executor.submit(() -> scanPort(ip, currentPort)));
        }
        
        // Optional: Scan port ranges
        for (int port = 1; port <= 1024; port++) {
            if (!contains(COMMON_PORTS, port)) {
                final int currentPort = port; // Make final for lambda
                futures.add(executor.submit(() -> scanPort(ip, currentPort)));
            }
        }
        
        for (Future<PortScanResult> future : futures) {
            try {
                PortScanResult result = future.get(TIMEOUT, TimeUnit.MILLISECONDS);
                if (result != null && result.isOpen()) {
                    results.add(result);
                }
            } catch (Exception e) {
                // Port is closed or filtered
            }
        }
        
        executor.shutdown();
        return results;
    }
    
    public PortScanResult scanPort(String ip, int port) {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(ip, port), TIMEOUT);
            socket.close();
            
            String service = identifyService(port);
            return new PortScanResult(ip, port, "TCP", "open", service);
        } catch (Exception e) {
            // Try UDP scan for specific ports
            if (port == 53 || port == 123 || port == 161) {
                if (scanUDPPort(ip, port)) {
                    String service = identifyService(port);
                    return new PortScanResult(ip, port, "UDP", "open", service);
                }
            }
        }
        return null;
    }
    
    private boolean scanUDPPort(String ip, int port) {
        try {
            DatagramSocket socket = new DatagramSocket();
            socket.setSoTimeout(TIMEOUT);
            
            InetAddress address = InetAddress.getByName(ip);
            byte[] buffer = new byte[1024];
            
            // Send empty packet
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
            socket.send(packet);
            
            // Try to receive response
            socket.receive(packet);
            socket.close();
            return true;
        } catch (Exception e) {
            // Port might be open but not responding
        }
        return false;
    }
    
    private boolean isHostAlive(String ip) {
        try {
            InetAddress address = InetAddress.getByName(ip);
            return address.isReachable(TIMEOUT);
        } catch (Exception e) {
            return false;
        }
    }
    
    private String identifyService(int port) {
        Map<Integer, String> services = new HashMap<>();
        services.put(21, "FTP");
        services.put(22, "SSH");
        services.put(23, "Telnet");
        services.put(25, "SMTP");
        services.put(53, "DNS");
        services.put(80, "HTTP");
        services.put(110, "POP3");
        services.put(135, "MSRPC");
        services.put(139, "NetBIOS");
        services.put(143, "IMAP");
        services.put(443, "HTTPS");
        services.put(445, "SMB");
        services.put(993, "IMAPS");
        services.put(995, "POP3S");
        services.put(1723, "PPTP");
        services.put(3306, "MySQL");
        services.put(3389, "RDP");
        services.put(5900, "VNC");
        services.put(8080, "HTTP-Proxy");
        
        return services.getOrDefault(port, "Unknown");
    }
    
    private boolean contains(int[] array, int value) {
        for (int item : array) {
            if (item == value) return true;
        }
        return false;
    }
    
    public static class PortScanResult {
        private String ip;
        private int port;
        private String protocol;
        private String state;
        private String service;
        private String banner;
        private Date timestamp;
        
        public PortScanResult(String ip, int port, String protocol, String state, String service) {
            this.ip = ip;
            this.port = port;
            this.protocol = protocol;
            this.state = state;
            this.service = service;
            this.timestamp = new Date();
        }
        
        // Getters and setters
        public String getIp() { return ip; }
        public int getPort() { return port; }
        public String getProtocol() { return protocol; }
        public String getState() { return state; }
        public String getService() { return service; }
        public String getBanner() { return banner; }
        public Date getTimestamp() { return timestamp; }
        
        public void setBanner(String banner) { this.banner = banner; }
        public boolean isOpen() { return "open".equalsIgnoreCase(state); }
        
        @Override
        public String toString() {
            return String.format("%s:%d [%s] - %s - %s", 
                ip, port, protocol, service, state);
        }
    }
    
    public void performBannerGrabbing(String ip, int port) {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(ip, port), TIMEOUT);
            socket.setSoTimeout(2000);
            
            // Send some data to trigger response
            java.io.OutputStream out = socket.getOutputStream();
            out.write("\r\n".getBytes());
            out.flush();
            
            // Read banner
            java.io.InputStream in = socket.getInputStream();
            byte[] buffer = new byte[1024];
            int bytesRead = in.read(buffer);
            
            if (bytesRead > 0) {
                String banner = new String(buffer, 0, bytesRead).trim();
                System.out.println("Banner for " + ip + ":" + port + ": " + banner);
            }
            
            socket.close();
        } catch (Exception e) {
            // Banner grabbing failed
        }
    }
}