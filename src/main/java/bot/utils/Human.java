package bot.utils;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Human {
    //region Constants
    private final String name;
    private final int ID;
    //endregion

    //region Constructors
    public Human(String name, int id) {
        this.name = name;
        this.ID = id;
    }

    public Human(ResultSet resultSet) throws SQLException {
        this.ID = resultSet.getInt("H_ID");
        this.name = resultSet.getString("H_name");
    }
    //endregion

    //region Getters and Setters
    public String getName() {
        return name;
    }

    public int getID() {
        return ID;
    }
    //endregion

    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Human human = (Human) o;

        return ID == human.ID;
    }
}
