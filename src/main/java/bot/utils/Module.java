package bot.utils;

public interface Module {
    boolean process(WebController webController, Message message);
}
