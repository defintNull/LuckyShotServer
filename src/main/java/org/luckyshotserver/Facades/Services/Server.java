package org.luckyshotserver.Facades.Services;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.luckyshotserver.Facades.LoginFacade;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

public class Server extends WebSocketServer {
    private final static int port = 8456;
    private static Server instance = null;
    private ConcurrentHashMap<WebSocket, String> loggedUser = new ConcurrentHashMap<>();

    private Server() {
        super(new InetSocketAddress(port));
    }

    public static Server getInstance() {
        if(instance == null) {
            instance = new Server();
        }
        return instance;
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        //Show Login Menu
        System.out.println("Client connected");
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        webSocket.send("addio");
    }

    @Override
    public void onMessage(WebSocket webSocket, String s) {
        String command = s.split(":")[0];
        switch (command) {
            case "LOGIN":
                String credentials = s.split(":")[1];
                String username = credentials.split("&")[0];
                String password = credentials.split("&")[1];
                LoginFacade loginFacade = new LoginFacade();
                if(loginFacade.login(webSocket, username, password)) {
                    loggedUser.put(webSocket, username);
                }
                break;
            case "REGISTER":
                credentials = s.split(":")[1];
                username = credentials.split("&")[0];
                password = credentials.split("&")[1];
                loginFacade = new LoginFacade();
                loginFacade.register(webSocket, username, password);
                break;
        }
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
        System.out.println(e.getMessage());
    }

    @Override
    public void onStart() {
        System.out.println("Connection Start");
    }

    public void sendError(WebSocket webSocket, String message) {
        sendMessage(webSocket, "ERROR: " + message);
    }

    public void sendOk(WebSocket webSocket, String message) {
        sendMessage(webSocket, "OK: " + message);
    }

    public void sendMessage(WebSocket webSocket, String message) {
        webSocket.send("START");
        webSocket.send(message);
        webSocket.send("STOP");
    }
}
