package bot.utils;

public interface Module {
    boolean process(Message message);

    String getMatch(Message message);
}