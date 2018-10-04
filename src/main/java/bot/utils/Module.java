package bot.utils;

public interface Module {
    void process(WebController webController, Message message);
}
