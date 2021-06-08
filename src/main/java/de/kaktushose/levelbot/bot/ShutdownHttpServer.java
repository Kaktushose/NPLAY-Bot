package de.kaktushose.levelbot.bot;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class ShutdownHttpServer {

    private static final Logger log = LoggerFactory.getLogger(ShutdownHttpServer.class);
    private final Levelbot levelbot;
    private HttpServer server;

    public ShutdownHttpServer(Levelbot levelbot, int port) {
        this.levelbot = levelbot;
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/api/v1/shutdown", new ShutdownHandler());
        } catch (IOException e) {
            log.error("Unable to create http server!", e);
        }
    }

    public void start() {
        server.start();
    }

    private class ShutdownHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            log.info("Received shutdown request! Shutting down bot...");
            String response = "OK";
            exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(response.getBytes());
            outputStream.close();
            new Thread(() -> {
                levelbot.stop();
                levelbot.terminate(0);
            }).start();
            server.stop(0);
        }
    }
}
