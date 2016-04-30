package com.testdatadesigner.tdalloy.core.io;

import java.io.InputStream;
import java.io.Reader;
import java.nio.ByteBuffer;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;

@ClientEndpoint
@ServerEndpoint(value="/events/")
public class EventSocket
{
    @OnOpen
    public void onWebSocketConnect(Session sess)
    {
        System.out.println("Socket Connected: " + sess);
    }
    
    @OnMessage
    public void onWebSocketText(String message)
    {
        System.out.println("Received TEXT message: " + message);
    }
    
    @OnMessage
    public void onTextFrame(ByteBuffer buffer, boolean fin) {
        System.out.println("Received Stream message: ");
    }
    
    @OnClose
    public void onWebSocketClose(CloseReason reason)
    {
        System.out.println("Socket Closed: " + reason);
    }
    
    @OnError
    public void onWebSocketError(Throwable cause)
    {
        cause.printStackTrace(System.err);
    }

    @OnWebSocketMessage
    public void onTextMethod(Reader stream) {
       // TEXT message received, and reported to your socket as a
       // Reader. (can handle 1 message, regardless of size or 
       // number of frames)
        System.out.println("...stream: ");
    }
   
//    @OnWebSocketMessage
//    public void onTextMethod(WebSocketConnection connection, 
//                             Reader stream) {
//       // TEXT message received, and reported to your socket as a
//       // Reader. (can handle 1 message, regardless of size or 
//       // number of frames).  Connection that message occurs
//       // on is reported as well.
//    }
   
    @OnWebSocketMessage
    public void onBinaryMethod(InputStream stream) {
       // BINARY message received, and reported to your socket
       // as a InputStream. (can handle 1 message, regardless
       // of size or number of frames).
        System.out.println("...binary stream: ");
    }
   
//    @OnWebSocketMessage
//    public void onBinaryMethod(WebSocketConnection connection, 
//                               InputStream stream) {
//       // BINARY message received, and reported to your socket
//       // as a InputStream. (can handle 1 message, regardless
//       // of size or number of frames).  Connection that 
//       // message occurs on is reported as well.
//    }
}
