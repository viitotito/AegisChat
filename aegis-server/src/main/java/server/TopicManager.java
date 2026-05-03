package server;

import model.Message;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TopicManager {

    private final Map<String, Set<ClientHandler>> topics = new ConcurrentHashMap<>();
    private final Set<String> connectedUsers = new HashSet<>();

    public synchronized boolean create(String topic, ClientHandler owner) {
        if (topics.containsKey(topic)) {
            return false;
        }

        Set<ClientHandler> subscribers = new HashSet<>();
        subscribers.add(owner); 
        topics.put(topic, subscribers);
        return true;
    }

    public synchronized boolean subscribe(String topic, ClientHandler client) {
        if (!topics.containsKey(topic)) {
            return false;
        }

        topics.get(topic).add(client);
        return true;
    }

    public synchronized void unsubscribe(String topic, ClientHandler client) {
        if (!topics.containsKey(topic)) {
            return;
        }

        topics.get(topic).remove(client);

        if (topics.get(topic).isEmpty()) {
            topics.remove(topic);
        }
    }

    public synchronized boolean delete(String topic, ClientHandler requester) {
        if (!topics.containsKey(topic)) {
            return false;
        }

        Set<ClientHandler> clients = topics.get(topic);

        if (clients.size() == 1 && clients.contains(requester)) {
            topics.remove(topic);
            return true;
        }

        return false;
    }

    public synchronized void publish(String topic, String sender, String content) {
        if (!topics.containsKey(topic)) {
            return;
        }

        for (ClientHandler client : topics.get(topic)) {
            client.send(Message.fromString("MESSAGE;" + topic + ";" + sender + ";" + content));
        }
    }

    public synchronized boolean registerUser(String name) {
        if (connectedUsers.contains(name)) {
            return false;
        }
        connectedUsers.add(name);
        return true;
    }

    public synchronized void removeUser(String name) {
        connectedUsers.remove(name);
    }

    public synchronized boolean isSubscribed(String topic, ClientHandler client) {
        return topics.containsKey(topic) && topics.get(topic).contains(client);
    }

    public synchronized void removeClientFromAllTopics(ClientHandler client) {
        for (String topic : topics.keySet()) {
            topics.get(topic).remove(client);
        }
    }
}
