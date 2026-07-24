package com.joetr.lexikon.weblauncher;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.util.Locale;
import java.util.concurrent.Executors;

public final class Main {
    public static final int PORT = 10_001;
    public static final URI ORIGIN = URI.create("http://localhost:" + PORT + "/");
    public static final String MARKER = "lexikon-web-local-launcher";

    private Main() {}

    public static void main(String[] args) throws Exception {
        try {
            HttpServer server = start(PORT);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> server.stop(0), "lexikon-web-shutdown"));
            openBrowser(ORIGIN);
            System.out.println("Lexikon Web is running at " + ORIGIN);
        } catch (java.net.BindException occupied) {
            if (isLexikonRunning(ORIGIN.toURL())) {
                openBrowser(ORIGIN);
                System.out.println("Lexikon Web is already running at " + ORIGIN);
                return;
            }
            throw new IOException("Port 10001 is in use. Lexikon Web requires http://localhost:10001.", occupied);
        }
    }

    public static HttpServer start(int port) throws IOException {
        InetSocketAddress address = new InetSocketAddress(InetAddress.getByName("127.0.0.1"), port);
        HttpServer server = HttpServer.create(address, 0);
        server.createContext("/", Main::serve);
        server.setExecutor(Executors.newFixedThreadPool(2, runnable -> {
            Thread thread = new Thread(runnable, "lexikon-web-http");
            thread.setDaemon(false);
            return thread;
        }));
        server.start();
        return server;
    }

    private static void serve(HttpExchange exchange) throws IOException {
        try (exchange) {
            if (!"GET".equals(exchange.getRequestMethod()) && !"HEAD".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) path = "/index.html";
            if (path.contains("..")) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }
            InputStream input = Main.class.getResourceAsStream("/web" + path);
            if (input == null) {
                input = Main.class.getResourceAsStream("/web/index.html");
            }
            if (input == null) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }
            try (InputStream stream = input) {
                byte[] bytes = stream.readAllBytes();
                exchange.getResponseHeaders().set("Content-Type", mimeType(path));
                exchange.getResponseHeaders().set("X-Lexikon-Web", MARKER);
                exchange.getResponseHeaders().set("Cache-Control", "no-store, no-cache, must-revalidate");
                exchange.getResponseHeaders().set("Pragma", "no-cache");
                exchange.getResponseHeaders().set("Expires", "0");
                exchange.sendResponseHeaders(200, "HEAD".equals(exchange.getRequestMethod()) ? -1 : bytes.length);
                if (!"HEAD".equals(exchange.getRequestMethod())) exchange.getResponseBody().write(bytes);
            }
        }
    }

    static String mimeType(String path) {
        String value = path.toLowerCase(Locale.ROOT);
        if (value.endsWith(".html")) return "text/html; charset=utf-8";
        if (value.endsWith(".js") || value.endsWith(".mjs")) return "text/javascript; charset=utf-8";
        if (value.endsWith(".wasm")) return "application/wasm";
        return "application/octet-stream";
    }

    static boolean isLexikonRunning(URL url) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(800);
            connection.setReadTimeout(800);
            return connection.getResponseCode() == 200 &&
                MARKER.equals(connection.getHeaderField("X-Lexikon-Web"));
        } catch (IOException ignored) {
            return false;
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private static void openBrowser(URI uri) {
        if (!Desktop.isDesktopSupported()) return;
        try {
            Desktop.getDesktop().browse(uri);
        } catch (Exception error) {
            System.err.println("Open " + uri + " manually. Browser launch failed: " + error.getMessage());
        }
    }
}
