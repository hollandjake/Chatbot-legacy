package bot.utils;

import bot.Chatbot;

public interface Module {
    boolean process(Chatbot chatbot, Message message);
}
