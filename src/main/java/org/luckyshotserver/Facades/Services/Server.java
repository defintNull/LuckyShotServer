package org.luckyshotserver.Facades.Services;

import com.sun.source.tree.Tree;
import kotlin.Pair;
import org.apache.logging.log4j.message.Message;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.luckyshotserver.Facades.LoginFacade;
import org.luckyshotserver.Facades.MultiplayerGameFacade;
import org.luckyshotserver.Facades.Services.Converters.ObjectConverter;
import org.luckyshotserver.Facades.UserFacade;
import org.luckyshotserver.Models.Enums.MessageEnum;
import org.luckyshotserver.Models.Room;
import org.luckyshotserver.Models.User;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Server extends WebSocketServer {
    private final static int port = 8456;
    private static Server instance = null;
    private ConcurrentHashMap<WebSocket, User> loggedUser = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Room> gameRooms = new ConcurrentHashMap<>();
    private final HashSet<String> rooms = new HashSet<>();
    private final int MAX_ROOM_PLAYERS = 2;

    private Server() {
        super(new InetSocketAddress(port));
        launchEmptyRoomCollector();
        HibernateService.getInstance();
    }

    public static Server getInstance() {
        if(instance == null) {
            instance = new Server();
        }
        return instance;
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        System.out.println("Client connected");
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        loggedUser.remove(webSocket);
        for (Map.Entry<String, Room> entry : gameRooms.entrySet()) {
            Room room = entry.getValue();
            if(room.getMembers().contains(webSocket)) {
                if(room.getOwner().equals(webSocket)) {
                    room.removeMember(webSocket);
                    for(WebSocket member : room.getMembers()) {
                        sendOk(member, "ROOM_CLOSED");
                    }
                    gameRooms.remove(entry.getKey());
                    break;
                }
                room.removeMember(webSocket);
                ArrayList<Pair<MessageEnum, String>> messages = new ArrayList<>();
                ArrayList<WebSocket> roomMembers = room.getMembers();
                for (WebSocket member : roomMembers) {
                    Pair<MessageEnum, String> m = new Pair<>(MessageEnum.OK, loggedUser.get(member).getUsername());
                    messages.add(m);
                }
                for (WebSocket roomMember : roomMembers) {
                    sendMessage(roomMember, messages);
                }
            }
        }
        System.out.println("Client disconnected");
    }

    @Override
    public void onMessage(WebSocket webSocket, String s) {
        System.out.println(s);
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
            String code = generateRoomCode();
            Room room = new Room(webSocket);
            gameRooms.put(code, room);
            sendOk(webSocket, code);
        }
        else if(command.equals("JOIN_ROOM")) {
            if(gameRooms.containsKey(params) && gameRooms.get(params).getMembers().size() < MAX_ROOM_PLAYERS) {
                gameRooms.get(params).addMember(webSocket);
                ArrayList<Pair<MessageEnum, String>> messages = new ArrayList<>();
                ArrayList<WebSocket> roomMembers = gameRooms.get(params).getMembers();
                for (WebSocket member : roomMembers) {
                    Pair<MessageEnum, String> m = new Pair<>(MessageEnum.OK, loggedUser.get(member).getUsername());
                    messages.add(m);
                }
                for (WebSocket roomMember : roomMembers) {
                    sendMessage(roomMember, messages);
                }
            } else {
                sendError(webSocket, "ROOM_NOT_VALID");
            }
        }
        else if(command.equals("LEAVE_ROOM")) {
            System.out.println(params);

            Room currentRoom = gameRooms.get(params);
            if(webSocket.equals(currentRoom.getOwner())) {
                currentRoom.removeMember(webSocket);
                if(!currentRoom.getMembers().isEmpty()) {
                    for(WebSocket member : currentRoom.getMembers()) {
                        sendOk(member, "ROOM_CLOSED");
                    }
                }
                gameRooms.remove(params);
                rooms.remove(params);
            } else {
                currentRoom.removeMember(webSocket);
                ArrayList<Pair<MessageEnum, String>> messages = new ArrayList<>();
                for (int i = 0; i < currentRoom.getMembers().size(); i++) {
                    Pair<MessageEnum, String> m = new Pair<>(MessageEnum.OK, loggedUser.get(currentRoom.getMembers().get(i)).getUsername());
                    messages.add(m);
                    sendMessage(currentRoom.getMembers().get(i), messages);
                }
            }
            sendOk(webSocket, "ROOM_LEFT");
        }
        else if(command.equals("START_GAME")) {
            Room currentRoom = gameRooms.get(params);
            MultiplayerGameFacade multiplayerGameFacade = new MultiplayerGameFacade(MAX_ROOM_PLAYERS);
            multiplayerGameFacade.start(currentRoom.getMembers());
        }
    }

    public User getUserFromWebSocket(WebSocket webSocket) {
        return loggedUser.get(webSocket);
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
        System.out.println(s);
    }

    public void sendMessage(WebSocket webSocket, ArrayList<Pair<MessageEnum, String>> message) {
        webSocket.send("START");
        for (Pair<MessageEnum, String> messageEnumStringPair : message) {
            webSocket.send(messageEnumStringPair.getFirst().getMessage() + ":" + messageEnumStringPair.getSecond());
        }
        webSocket.send("STOP");
        System.out.println(message);
    }

    private String generateRoomCode() {
        String uniqueString;
        do {
            uniqueString = generateUniqueString();
        } while (rooms.contains(uniqueString));

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

    public void launchEmptyRoomCollector() {
        Thread emptyRoomCollector = new Thread(() -> {
            while(true) {
                try {
                    Thread.sleep(1000);
                    System.out.println(gameRooms);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                for (Map.Entry<String, Room> entry : gameRooms.entrySet()) {
                    if (entry.getValue().getMembers().isEmpty()) {
                        gameRooms.remove(entry.getKey());
                        rooms.remove(entry.getKey());
                        System.out.println(entry.getKey() + " has been deleted");
                    }
                }
            }
        });
        emptyRoomCollector.start();
    }
}
