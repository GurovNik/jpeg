import java.sql.*;

/**
 * Created by pivaso on 10.11.17.
 */
public class DB {

    private int ID;
    private ResultSet results;

    public DB(String name) {
        ID = selectID(name);
    }

    public static void main(String[] args) {
        DB app = new DB("ViPivaso");
        // insert three new rows
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

    private ResultSet getResult() {
        String sql = "SELECT * FROM messages";
        try (Connection conn = this.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public void makeSelection() {
        results = getResult();
    }

    public void next(String param) throws SQLException {
        if (results.next()) {

        }
    }

    public int selectID(String name) {
        String sql = "SELECT MAX(id) FROM messages WHERE user = ?";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // set the value
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            // loop through the result set
            while (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return -1;
    }

    public void insert(double size, double compressed, String user, byte compression, byte coding, String format, String content) {
        String sql =
                "INSERT INTO messages (id, size, compressed, user, compression, coding, format, content) VALUES (?,?,?,?,?,?,?,?)";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, ID);
            pstmt.setDouble(2, size);
            pstmt.setDouble(3, compressed);
            pstmt.setString(4, user);
            pstmt.setByte(5, compression);
            pstmt.setByte(6, coding);
            pstmt.setString(7, format);
            pstmt.setString(8, content);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
