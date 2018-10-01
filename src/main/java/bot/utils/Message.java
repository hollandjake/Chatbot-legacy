package bot.utils;

public class Message {
    private Human sender;
    private String message;

    public Message() {

    }

    public Message(String message, Human sender) {
        this.message = message;
        this.sender = sender;
    }
}
