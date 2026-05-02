package server;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import model.Message;

public class TopicManager {

    private final Map<String, Set<ClientHandler>> topics = new ConcurrentHashMap<>();

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

    public synchronized boolean delete(String topic, ClientHandler client) {
        if (!topics.containsKey(topic)) {
            return false;
        }

        Set<ClientHandler> clients = topics.get(topic);

        if (clients.size() == 1 && clients.contains(client)) {
            topics.remove(topic);
            return true;
        }
        return false;
    }

    public synchronized void publish(String topic, ClientHandler sender, String content) {
        if (!topics.containsKey(topic)) {
            return;
        }

        Set<ClientHandler> subscribers = topics.get(topic);

        if (!subscribers.contains(sender)) {
            sender.send(Message.fromString("ERROR;" + topic + ";BROKER;Você não está inscrito neste tópico"));
            return;
        }

        for (ClientHandler client : subscribers) {
            client.send(Message.fromString("MESSAGE;" + topic + ";" + sender.getClientName() + ";" + content));
        }
    }
}
