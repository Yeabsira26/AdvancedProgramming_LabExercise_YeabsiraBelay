package server;

import java.net.*;
import java.util.concurrent.*;

public class ChatServer {
    
    public static CopyOnWriteArrayList<ClientHandler> clients = new CopyOnWriteArrayList<>();
    
    public static void main(String[] args) throws Exception {
        ServerSocket server = new ServerSocket(5000);
        System.out.println("🌸 Chat Server started on port 5000");
        System.out.println("Waiting for clients...\n");
        
        while (true) {
            Socket socket = server.accept();
            System.out.println("✅ New client connected");
            ClientHandler client = new ClientHandler(socket);
            clients.add(client);
            client.start();
            
            // Broadcast updated user list to everyone
            broadcastUsers();
        }
    }
    
    public static void broadcastUsers() {
        StringBuilder sb = new StringBuilder("USERS:");
        for (ClientHandler c : clients) {
            sb.append(c.username).append(",");
        }
        String msg = sb.toString();
        for (ClientHandler c : clients) {
            c.out.println(msg);
        }
    }
}