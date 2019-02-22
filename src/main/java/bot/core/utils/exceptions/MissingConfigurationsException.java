package bot.core.utils.exceptions;

import java.util.Arrays;

public class MissingConfigurationsException extends Exception {
	public MissingConfigurationsException(String... args) {
		super("There are missing core config parameters (" + Arrays.toString(args) + ")");
	}
}
