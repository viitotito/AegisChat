package model;

public class Message {

    private String type;
    private String topic;
    private String sender;
    private String content;

    public Message(String type, String topic, String sender, String content) {
        this.type = type;
        this.topic = topic;
        this.sender = sender;
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public String getTopic() {
        return topic;
    }

    public String getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return type + ";" + topic + ";" + sender + ";" + content;
    }

    public static Message fromString(String text) {
        String[] parts = text.split(";", 4);

        String type = parts.length > 0 ? parts[0] : "";
        String topic = parts.length > 1 ? parts[1] : "";
        String sender = parts.length > 2 ? parts[2] : "";
        String content = parts.length > 3 ? parts[3] : "";

        return new Message(type, topic, sender, content);
    }
}