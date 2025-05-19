package uz.project.jorasoft.config;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.stereotype.Component;

@Component
public class MyWebSocketHandler extends TextWebSocketHandler {

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("New WebSocket connection: " + session.getId());
        session.sendMessage(new TextMessage("Welcome! Connection established."));
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String receivedText = message.getPayload();
        System.out.println("Received message: " + receivedText + " from " + session.getId());

        // Echo the message back
        session.sendMessage(new TextMessage("Echo: " + receivedText));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) throws Exception {
        System.out.println("WebSocket connection closed: " + session.getId());
    }
}
