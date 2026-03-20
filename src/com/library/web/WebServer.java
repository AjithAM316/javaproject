package com.library.web;

import com.library.service.LibraryService;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class WebServer {

    private final LibraryService libraryService;
    private HttpServer server;

    public WebServer(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    public void start(int port) {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            
            // Register API Handlers
            server.createContext("/api/", new ApiHandler(libraryService));
            
            // Register Static File Handler
            server.createContext("/", new StaticFileHandler("web"));
            
            server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
            server.start();
            System.out.println("[Web] Server started securely on http://localhost:" + port);
            System.out.println("[Web] Press Ctrl+C in console to stop the server.");
        } catch (IOException e) {
            System.err.println("[Web] Failed to start server: " + e.getMessage());
        }
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("[Web] Server stopped.");
        }
    }
}
