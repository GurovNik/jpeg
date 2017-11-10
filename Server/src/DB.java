import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by pivaso on 10.11.17.
 */
public class DB {

    private int ID;

    public DB() {

    }

    private Connection connect() {
        // SQLite connection string
        String url = "jdbc:sqlite:./messenger";
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public void insert() {
        String sql =
        "INSERT INTO messages (id, size, compressed, user, compression, coidng, format, content) VALUES (?,?,?,?,?,?)";

        try (Connection conn = this.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            pstmt.setString(1, name);
//            pstmt.setDouble(2, capacity);
//            pstmt.executeUpdate();
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    public static void main(String[] args) {

        DB app = new DB();
        // insert three new rows
    }
}
