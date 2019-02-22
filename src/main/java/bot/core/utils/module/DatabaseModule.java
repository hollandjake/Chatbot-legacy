package bot.core.utils.module;

import java.sql.Connection;
import java.sql.SQLException;

public interface DatabaseModule {
	void prepareStatements(Connection connection) throws SQLException;
}