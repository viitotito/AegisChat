package server;

import model.Message;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TopicManager {

    private final Map<String, Set<ClientHandler>> topics = new ConcurrentHashMap<>();

    public synchronized boolean createTopic(String topic, ClientHandler owner) {
        if (topics.containsKey(topic)) {
            return false;
        }

        Set<ClientHandler> subscribers = new HashSet<>();
        subscribers.add(owner); // entra automaticamente
        topics.put(topic, subscribers);
        return true;
    }

    public synchronized boolean subscribeTopic(String topic, ClientHandler client) {
        if (!topics.containsKey(topic)) {
            return false;
        }

        topics.get(topic).add(client);
        return true;
    }

    public synchronized void unsubscribeTopic(String topic, ClientHandler client) {
        if (!topics.containsKey(topic)) {
            return;
        }

        topics.get(topic).remove(client);

        if (topics.get(topic).isEmpty()) {
            topics.remove(topic);
        }
    }

    public synchronized boolean deleteTopic(String topic, ClientHandler requester) {
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

    public synchronized void publishTopic(String topic, String sender, String content) {
        if (!topics.containsKey(topic)) {
            return;
        }

        for (ClientHandler client : topics.get(topic)) {
            client.send(Message.fromString("MESSAGE;" + topic + ";" + sender + ";" + content));
        }
    }
}