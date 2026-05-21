package server;

import java.io.*;
import java.net.*;

public class ClientHandler extends Thread {
    
    Socket socket;
    BufferedReader in;
    PrintWriter out;
    String username;
    
    public ClientHandler(Socket socket) throws Exception {
        this.socket = socket;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }
    
    public void run() {
        try {
            username = in.readLine();
            System.out.println("✅ " + username + " joined");
            ChatServer.broadcastUsers();
            
            String msg;
            while ((msg = in.readLine()) != null) {
                System.out.println("💬 " + username + ": " + msg);
                
                if (msg.startsWith("FILE:")) {
                    // Forward file to recipient
                    String[] parts = msg.split(":", 5);
                    if (parts.length >= 5) {
                        String recipient = parts[1];
                        for (ClientHandler c : ChatServer.clients) {
                            if (c.username.equals(recipient)) {
                                c.out.println(msg);
                                out.println("✅ File sent to " + recipient);
                                break;
                            }
                        }
                    }
                }
                else if (msg.startsWith("@")) {
                    // Private message
                    int space = msg.indexOf(" ");
                    if (space > 0) {
                        String target = msg.substring(1, space);
                        String privateMsg = msg.substring(space + 1);
                        for (ClientHandler c : ChatServer.clients) {
                            if (c.username.equals(target)) {
                                c.out.println("🔒 [Private from " + username + "]: " + privateMsg);
                                out.println("🔒 [Private to " + target + "]: " + privateMsg);
                                break;
                            }
                        }
                    }
                }
                else {
                    // Broadcast to all
                    for (ClientHandler c : ChatServer.clients) {
                        c.out.println(username + ": " + msg);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("❌ " + username + " disconnected");
        } finally {
            ChatServer.clients.remove(this);
            ChatServer.broadcastUsers();
        }
    }
}