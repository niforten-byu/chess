package server.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {

    // map gameID to a map of authentication tokens -> session
    public final ConcurrentHashMap<Integer, ConcurrentHashMap<String, Session>> connections = new ConcurrentHashMap<>();

    public void add(int gameID, String authenticationToken, Session session) {
        // if the game doesn't exist in our map, create a map with it
        var gameConnections = connections.computeIfAbsent(gameID, k -> new ConcurrentHashMap<>());
        gameConnections.put(authenticationToken, session);
    }

    public void remove(int gameID, String authenticationToken) {
        var gameConnections = connections.get(gameID);
        if (gameConnections != null) {
            // remove authentication token for player
            gameConnections.remove(authenticationToken);
            if (gameConnections.isEmpty()) {
                // if no one is in the game, remove it from the map
                connections.remove(gameID);
            }
        }
    }

    public void broadcast(int gameID, String excludeAuthenticationToken, ServerMessage message) throws IOException {
        var gameConnections = connections.get(gameID);
        if (gameConnections != null) {
            // convert message object to JSON string
            String jsonMessage = new Gson().toJson(message);

            // send the message to everyone in the game except the excluded user(s)
            for (var entry : gameConnections.entrySet()) {
                String authToken = entry.getKey();
                Session session = entry.getValue();

                if (session.isOpen() && !authToken.equals(excludeAuthenticationToken)) {
                    session.getRemote().sendString(jsonMessage);
                }
            }
        }
    }
}