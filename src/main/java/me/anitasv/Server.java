package me.anitasv;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;

public class Server {

    record FileHandler(String fileName,
                       String contentType) implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                File file = new File(fileName);
                byte[] bytes = Files.readAllBytes(file.toPath());

                Headers headers = exchange.getResponseHeaders();
                headers.add("Content-Type", contentType);
                exchange.sendResponseHeaders(200, bytes.length);
                exchange.getResponseBody().write(bytes);
                exchange.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
    public static void main(String[] args) throws IOException {
        HttpServer httpServer = HttpServer.create(
                new InetSocketAddress("127.0.0.1", 8080)
        , 0);

        httpServer.createContext("/jigsaw.html",
                new FileHandler("public/jigsaw.html",
                        "text/html; charset=utf-8"));
        httpServer.createContext("/jigsaw.js",
                new FileHandler("public/jigsaw.js",
                        "text/javascript; charset=utf-8"));
        httpServer.createContext("/favicon.ico",
                new FileHandler("public/favicon.ico",
                        "image/x-icon"));

        httpServer.start();
    }
}
