// Francisco Lou Gardenberg - 2211275
// Vinicius Barros Pessoa de Araujo - 2210392

package Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ExecutionException;

public class Database {

    public Connection connection = null;

    private static Database database = null;

    private Database() {

    }

    public static Database getInstance() throws Exception {
        if(database != null)
            return database;

        database = new Database();

        String url = "jdbc:sqlite:C:\\Users\\Francisco\\IdeaProjects\\CofreDigital\\Code\\database\\cofre.db";


        database.connection = DriverManager.getConnection(url);

        return database;
    }

    public static void log(int registro) {
        try {
            String sql = "INSERT INTO Registros(MID) VALUES (?)";

            PreparedStatement preparedStatement = Database.getInstance().connection.prepareStatement(sql);
            preparedStatement.setInt(1, registro);
            preparedStatement.executeUpdate();
        } catch (Exception e) {}
    }

    public static void log(int registro, String usuario) {
        try {
            String sql = "INSERT INTO Registros(MID, usuario) VALUES (?, ?)";

            PreparedStatement preparedStatement = Database.getInstance().connection.prepareStatement(sql);
            preparedStatement.setInt(1, registro);
            preparedStatement.setString(2, usuario);
            preparedStatement.executeUpdate();
        } catch (Exception e) {}
    }

    public static void log(int registro, String usuario, String arquivo) throws Exception {
        try {
            String sql = "INSERT INTO Registros(MID, usuario, arquivo) VALUES (?, ?, ?)";

            PreparedStatement preparedStatement = Database.getInstance().connection.prepareStatement(sql);
            preparedStatement.setInt(1, registro);
            preparedStatement.setString(2, usuario);
            preparedStatement.setString(3, arquivo);
            preparedStatement.executeUpdate();
        } catch (Exception e) {}
    }
}
