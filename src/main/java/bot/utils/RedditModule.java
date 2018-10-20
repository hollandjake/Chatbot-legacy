package bot.utils;

import java.util.List;

public interface RedditModule extends Module {
    List<String> getSubreddits();
}