package com.example.demo;

import java.io.IOException;
import java.util.logging.Level;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.stereotype.Component;

/**
 * WebSocket endpoint for handling drawing actions via WebSocket protocol.
 * This component manages WebSocket sessions, message processing, and error handling.
 */
@Component
@ServerEndpoint("/bbService")
public class BBEndpoint {
    private static final Logger logger = Logger.getLogger(BBEndpoint.class.getName());
    /* Queue for all open WebSocket sessions */
    static Queue<Session> queue = new ConcurrentLinkedQueue<>();
    Session ownSession = null;

    /**
     * Sends a message to all connected WebSocket clients except the sender.
     *
     * @param msg The message to send.
     */
    public void send(String msg) {
        try {
            /* Send updates to all open WebSocket sessions */
            for (Session session : queue) {
                if (!session.equals(this.ownSession)) {
                    session.getBasicRemote().sendText(msg);
                }
                logger.log(Level.INFO, "Sent: {0}", msg);
            }
        } catch (IOException e) {
            logger.log(Level.INFO, e.toString());
        }
    }

    /**
     * Handles incoming messages from WebSocket clients.
     *
     * @param message The incoming message.
     * @param session The session from which the message originated.
     */
    @OnMessage
    public void processPoint(String message, Session session) {
        System.out.println("Point received:" + message + ". From session: " + session);
        this.send(message);
    }

     /**
     * Handles opening of WebSocket connections.
     *
     * @param session The newly opened WebSocket session.
     */
    @OnOpen
    public void openConnection(Session session) {
        /* Register this connection in the queue */
        queue.add(session);
        ownSession = session;
        logger.log(Level.INFO, "Connection opened.");
        try {
            session.getBasicRemote().sendText("Connection established.");
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Handles closure of WebSocket connections.
     *
     * @param session The closed WebSocket session.
     */
    @OnClose
    public void closedConnection(Session session) {
        /* Remove this connection from the queue */
        queue.remove(session);
        logger.log(Level.INFO, "Connection closed.");
    }

    /**
     * Handles WebSocket errors.
     *
     * @param session The session in which the error occurred.
     * @param t       The Throwable representing the error.
     */
    @OnError
    public void error(Session session, Throwable t) {
        /* Remove this connection from the queue */
        queue.remove(session);
        logger.log(Level.INFO, t.toString());
        logger.log(Level.INFO, "Connection error.");
    }
}
