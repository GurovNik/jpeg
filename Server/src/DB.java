import java.sql.*;

/**
 * Created by pivaso on 10.11.17.
 */
public class DB {

    private int ID;
    private ResultSet results;

    public DB() {
        ID = selectID();
    }

    public static void main(String[] args) throws SQLException {
        DB app = new DB();
        app.makeSelection("ViPivaso", "evgerher");
        app.next();
        System.out.println(app.get("string", "user"));
        // insert three new rows
    }

    private Connection connect() {
        // SQLite connection string
        String url = "jdbc:sqlite:Server/messenger";
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    private void getResult(String name, String recipient) {
        String sql = "SELECT * FROM messages WHERE (user = ? AND recepient = ?)OR (user = ? AND recepient = ?) ORDER BY id ASC";
        Connection conn = this.connect();
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setString(2, recipient);
            pstmt.setString(3, recipient);
            pstmt.setString(4, name);
            results = pstmt.executeQuery();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void makeSelection(String name, String recepient) {
        getResult(name, recepient);
    }


    public Object get(String format, String param) throws SQLException {
        try {
            switch (format.toLowerCase()) {
                case "int":
                    return results.getInt(param);
                case "string":
                    return results.getString(param);
                case "float":
                    return results.getFloat(param);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "hui";
        }
        return null;
    }

    public void next() throws SQLException {
        results.next();
        if (results.isAfterLast()) {
            results.close();
        }
    }

    public int selectID() {
        String sql = "SELECT MAX(id) FROM messages";

        try {
            Connection conn = this.connect();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return -1;
    }

    public boolean hasNext() {
        if (results != null) {
            try {
                return results.isLast();
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    public void insert(double size, double compressed, String user, String recipient, byte compression, byte coding,
                       String format, String content) {
        if (ID == -1) {
            selectID();
        }
        String sql =
                "INSERT INTO messages (id, size, compressed, user, recepient, compression, coding, format, content) " +
                        "VALUES (?,?,?,?,?,?,?,?,?)";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, ++ID);
            pstmt.setDouble(2, size);
            pstmt.setDouble(3, compressed);
            pstmt.setString(4, user);
            pstmt.setString(5, recipient);
            pstmt.setByte(6, compression);
            pstmt.setByte(7, coding);
            pstmt.setString(8, format);
            pstmt.setString(9, content);
            pstmt.executeUpdate();
            conn.commit();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }
}
