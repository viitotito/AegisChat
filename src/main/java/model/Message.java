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

    public static Message fromString(String message){
        String[] partsOfMessage = message.split(";",4);
        
        String type = partsOfMessage.length > 0 ? partsOfMessage[0] : "";
        String topic = partsOfMessage.length > 1 ? partsOfMessage[1] : "";
        String sender = partsOfMessage.length > 2 ? partsOfMessage[2] : "";
        String content = partsOfMessage.length > 3 ? partsOfMessage[3] : "";
        
        return new Message(type, topic, sender, content);
    }
}
