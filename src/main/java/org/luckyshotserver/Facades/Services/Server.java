package org.luckyshotserver.Facades.Services;

import com.sun.source.tree.Tree;
import kotlin.Pair;
import org.apache.logging.log4j.message.Message;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.luckyshotserver.Facades.LoginFacade;
import org.luckyshotserver.Facades.Services.Converters.ObjectConverter;
import org.luckyshotserver.Facades.UserFacade;
import org.luckyshotserver.Models.Enums.MessageEnum;
import org.luckyshotserver.Models.User;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Server extends WebSocketServer {
    private final static int port = 8456;
    private static Server instance = null;
    private ConcurrentHashMap<WebSocket, User> loggedUser = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ArrayList<WebSocket>> gameRooms = new ConcurrentHashMap<>();
    private final HashSet<String> rooms = new HashSet<>();

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
        loggedUser.remove(webSocket);
        System.out.println("Client disconnected");
    }

    @Override
    public void onMessage(WebSocket webSocket, String s) {
        ArrayList<String> message = new ArrayList<>(Arrays.asList(s.split(":")));
        String command = message.removeFirst();
        String params = String.join(":", message);
        if(command.equals("LOGIN")) {
            String username = params.split("&")[0];
            String password = params.split("&")[1];
            LoginFacade loginFacade = new LoginFacade();
            User user = loginFacade.login(webSocket, username, password);
            if(user == null) {
                sendError(webSocket, "NOT_FOUND");
                return;
            }
            if(loggedUser.containsValue(user)) {
                sendError(webSocket, "ALREADY_LOGGED");
                return;
            }
            ObjectConverter converter = new ObjectConverter();
            String userJSON = converter.userToJson(user);
            sendOk(webSocket, userJSON);
            loggedUser.put(webSocket, user);
        }
        else if (command.equals("REGISTER")) {
            String username = params.split("&")[0];
            String password = params.split("&")[1];
            LoginFacade loginFacade = new LoginFacade();
            loginFacade.register(webSocket, username, password);
        }
        else if (command.equals("UPDATE_USER")) {
            UserFacade userFacade = new UserFacade();
            User user = loggedUser.get(webSocket);

            if (!userFacade.updateUser(user, params)) {
                sendError(webSocket, "FATAL");
            } else {
                sendOk(webSocket, "OK");
            }
        }
        else if(command.equals("CREATE_ROOM")) {
            ArrayList<WebSocket> players = new ArrayList<>();
            players.add(webSocket);
            String code = generateRoomCode();
            gameRooms.put(code, players);
            sendOk(webSocket, code);
        }
        else if(command.equals("JOIN_ROOM")) {
            if(gameRooms.containsKey(params) && gameRooms.get(params).size() < 2) {
                gameRooms.get(params).add(webSocket);
                ArrayList<Pair<MessageEnum, String>> messages = new ArrayList<>();
                for (int i = 0; i < gameRooms.get(params).size(); i++) {
                    Pair<MessageEnum, String> m = new Pair<>(MessageEnum.OK, loggedUser.get(gameRooms.get(params).get(i)).getUsername());
                    messages.add(m);
                }
                for (int i = 0; i < gameRooms.get(params).size(); i++) {
                    sendMessage(gameRooms.get(params).get(i), messages);
                }
            } else {
                sendError(webSocket, "NOT_VALID_ROOM");
            }
        }
        else if(command.equals("LEAVE_ROOM")) {
            if(webSocket.equals(gameRooms.get(params).getFirst())) {
                sendOk(gameRooms.get(params).getLast(), "ROOM_CLOSED");
                gameRooms.remove(params);
            } else {
                gameRooms.get(params).remove(webSocket);
                ArrayList<Pair<MessageEnum, String>> messages = new ArrayList<>();
                for (int i = 0; i < gameRooms.get(params).size(); i++) {
                    Pair<MessageEnum, String> m = new Pair<>(MessageEnum.OK, loggedUser.get(gameRooms.get(params).get(i)).getUsername());
                    messages.add(m);
                }
                sendMessage(gameRooms.get(params).getFirst(), messages);
            }
            sendOk(webSocket, "ROOM_LEFT");
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
        sendMessage(webSocket, MessageEnum.ERROR + ":" + message);
    }

    public void sendOk(WebSocket webSocket, String message) {
        sendMessage(webSocket, MessageEnum.OK + ":" + message);
    }

    private void sendMessage(WebSocket webSocket, String s) {
        webSocket.send("START");
        webSocket.send(s);
        webSocket.send("STOP");
    }

    public void sendMessage(WebSocket webSocket, ArrayList<Pair<MessageEnum, String>> message) {
        webSocket.send("START");
        for (Pair<MessageEnum, String> messageEnumStringPair : message) {
            webSocket.send(messageEnumStringPair.getFirst().getMessage() + ":" + messageEnumStringPair.getSecond());
        }
        webSocket.send("STOP");
    }

    private String generateRoomCode() {
        String uniqueString;
        do {
            uniqueString = generateUniqueString();
        } while (rooms.contains(uniqueString)); // Assicura l'unicità

        rooms.add(uniqueString);
        return uniqueString;
    }

    private String generateUniqueString() {
        String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        int STRING_LENGTH = 6;
        StringBuilder sb = new StringBuilder(STRING_LENGTH);
        Random random = new Random();
        for (int i = 0; i < STRING_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        return sb.toString();
    }
}
