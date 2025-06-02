package uz.project.auralife.config;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;

@Component
public class UserWebSocketHandler extends TextWebSocketHandler {

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        String token = message.getPayload();

        // Dummy token check
        if ("my-secret-token".equals(token)) {
            String userJson = "{\"id\": 1, \"name\": \"John Doe\"}";
            session.sendMessage(new TextMessage(userJson));
        } else {
            session.sendMessage(new TextMessage("Unauthorized"));
            session.close();
        }
    }
}
