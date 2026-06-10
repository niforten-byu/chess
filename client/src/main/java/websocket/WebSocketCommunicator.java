package websocket;

import com.google.gson.Gson;
import jakarta.websocket.*;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketCommunicator extends Endpoint {

    public Session session;
    private final ServerMessageObserver observer;

    public WebSocketCommunicator(String url, ServerMessageObserver observer) throws Exception {
        this.observer = observer;
        try {
            // convert http url to websocket ws url
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            // set up listener for incoming messages from server
            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    ServerMessage baseMessage = new Gson().fromJson(message, ServerMessage.class);
                    switch (baseMessage.getServerMessageType()) {
                        case LOAD_GAME -> observer.notify(new Gson().fromJson(message, websocket.messages.LoadGameMessage.class));
                        case NOTIFICATION -> observer.notify(new Gson().fromJson(message, websocket.messages.NotificationMessage.class));
                        case ERROR -> observer.notify(new Gson().fromJson(message, websocket.messages.ErrorMessage.class));
                    }
                }
            });
        } catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new Exception("WebSocket connection failed: " + ex.getMessage());
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }

    /**
     * send a JSON string to the server
     */
    public void send(String msg) throws IOException {
        this.session.getBasicRemote().sendText(msg);
    }
}