package server;

import model.Message;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TopicManager {

    private final Map<String, Set<String>> topics = new ConcurrentHashMap<>();
    private final Map<String, List<Message>> messageBuffers = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Integer>> pendingOffsets = new ConcurrentHashMap<>();
    private final Map<String, ClientHandler> activeHandlers = new ConcurrentHashMap<>();
    private final Set<String> connectedUsers = new HashSet<>();

    public synchronized boolean create(String topic, String ownerName, ClientHandler handler) {
        if (topics.containsKey(topic)) {
            return false;
        }

        Set<String> subscribers = new HashSet<>();
        subscribers.add(ownerName);

        topics.put(topic, subscribers);
        messageBuffers.put(topic, new ArrayList<>());

        Map<String, Integer> offsets = new HashMap<>();
        offsets.put(ownerName, 0);
        pendingOffsets.put(topic, offsets);

        connectedUsers.add(ownerName);
        activeHandlers.put(ownerName, handler);

        return true;
    }

    public synchronized boolean subscribe(String topic, String userName) {
        if (!topics.containsKey(topic)) {
            return false;
        }

        Set<String> subscribers = topics.get(topic);
        boolean added = subscribers.add(userName);

        Map<String, Integer> offsets = pendingOffsets.computeIfAbsent(topic, t -> new HashMap<>());
        offsets.putIfAbsent(userName, messageBuffers.getOrDefault(topic, Collections.emptyList()).size());

        if (added && activeHandlers.containsKey(userName)) {
            sendPendingMessages(topic, userName);
        }

        return true;
    }

    public synchronized void unsubscribe(String topic, String userName) {
        if (!topics.containsKey(topic)) {
            return;
        }

        topics.get(topic).remove(userName);
        Map<String, Integer> offsets = pendingOffsets.get(topic);
        if (offsets != null) {
            offsets.remove(userName);
        }

        if (topics.get(topic).isEmpty()) {
            removeTopic(topic);
        }
    }

    public synchronized boolean delete(String topic, String requesterName) {
        if (!topics.containsKey(topic)) {
            return false;
        }

        Set<String> subscribers = topics.get(topic);
        if (subscribers.size() == 1 && subscribers.contains(requesterName)) {
            removeTopic(topic);
            return true;
        }

        return false;
    }

    public synchronized void publish(String topic, String sender, String content) {
        if (!topics.containsKey(topic)) {
            return;
        }

        List<Message> buffer = messageBuffers.computeIfAbsent(topic, t -> new ArrayList<>());
        Message message = new Message("MESSAGE", topic, sender, content);
        buffer.add(message);

        Map<String, Integer> offsets = pendingOffsets.computeIfAbsent(topic, t -> new HashMap<>());
        for (String userName : topics.get(topic)) {
            if (!offsets.containsKey(userName)) {
                offsets.put(userName, buffer.size() - 1);
            }

            ClientHandler handler = activeHandlers.get(userName);
            if (handler != null) {
                handler.send(message);
                offsets.put(userName, buffer.size());
            }
        }

        cleanupTopicBuffer(topic);
    }

    public synchronized boolean registerUser(String name, ClientHandler handler) {
        if (connectedUsers.contains(name)) {
            return false;
        }
        connectedUsers.add(name);
        activeHandlers.put(name, handler);
        return true;
    }

    public synchronized void userDisconnected(String name) {
        connectedUsers.remove(name);
        activeHandlers.remove(name);
    }

    public synchronized boolean isSubscribed(String topic, String userName) {
        return topics.containsKey(topic) && topics.get(topic).contains(userName);
    }

    public synchronized void deliverPendingMessages(String userName) {
        for (String topic : topics.keySet()) {
            Set<String> subscribers = topics.get(topic);
            if (subscribers.contains(userName)) {
                sendPendingMessages(topic, userName);
            }
        }
    }

    private void sendPendingMessages(String topic, String userName) {
        ClientHandler handler = activeHandlers.get(userName);
        if (handler == null) {
            return;
        }

        List<Message> buffer = messageBuffers.get(topic);
        if (buffer == null || buffer.isEmpty()) {
            return;
        }

        Map<String, Integer> offsets = pendingOffsets.computeIfAbsent(topic, t -> new HashMap<>());
        int currentOffset = offsets.getOrDefault(userName, buffer.size());

        for (int i = currentOffset; i < buffer.size(); i++) {
            handler.send(buffer.get(i));
        }

        offsets.put(userName, buffer.size());
        cleanupTopicBuffer(topic);
    }

    private void cleanupTopicBuffer(String topic) {
        List<Message> buffer = messageBuffers.get(topic);
        if (buffer == null || buffer.isEmpty()) {
            return;
        }

        Map<String, Integer> offsets = pendingOffsets.get(topic);
        if (offsets == null || offsets.isEmpty()) {
            return;
        }

        int minOffset = Integer.MAX_VALUE;
        for (int offset : offsets.values()) {
            minOffset = Math.min(minOffset, offset);
        }

        if (minOffset <= 0) {
            return;
        }

        buffer.subList(0, minOffset).clear();
        for (Map.Entry<String, Integer> entry : offsets.entrySet()) {
            entry.setValue(entry.getValue() - minOffset);
        }
    }

    private void removeTopic(String topic) {
        topics.remove(topic);
        messageBuffers.remove(topic);
        pendingOffsets.remove(topic);
    }
}
