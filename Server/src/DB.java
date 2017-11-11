import java.sql.*;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Created by pivaso on 10.11.17.
 */
public class DB {

    private int ID;
    private ArrayList<Hashtable<String, String>> results;
    private int cursor = 0;

    public DB() {
        ID = selectID();
    }

//    public static void main(String[] args) throws SQLException {
//        DB app = new DB();
//        app.makeSelection("texet", "evgerher");
//        System.out.println(app.results.size());
//        System.out.println(app.hasNext());
//        System.out.println(app.get("user"));
//        System.out.println(app.get("recepient"));
//        app.next();
//        System.out.println(app.get("user"));
//        System.out.println(app.get("recepient"));
//        app.next();
//        System.out.println(app.hasNext());
//        System.out.println(app.get("user"));
//        System.out.println(app.get("recepient"));
//        app.reset();
//        // insert three new rows
//    }

    public Connection connect() {
        // SQLite connection string
        String url = "jdbc:sqlite:messenger";
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
        ArrayList<Hashtable<String, String>> result = new ArrayList<>();
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ;
            pstmt.setString(1, name);
            pstmt.setString(2, recipient);
            pstmt.setString(3, recipient);
            pstmt.setString(4, name);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Hashtable<String, String> temp = new Hashtable<>(10);
                temp.put("user", rs.getString(2));
                temp.put("recepient", rs.getString(3));
                temp.put("format", rs.getString(5));
                temp.put("content", rs.getString(10));
                result.add(temp);
            }
            results = result;
        } catch (SQLException e) {
            System.out.println("null");
            System.out.println(e.getMessage());
        }

    }

    public void makeSelection(String name, String recepient) {
        getResult(name, recepient);
    }


    public String get(String param) {
        return results.get(cursor).get(param);
    }

    public void next() {
        ++cursor;
    }

    public void reset() {
        cursor = 0;
        results = null;
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
        return cursor < results.size();
    }

    public void insert(double size, double compressed, String user, String recipient, byte compression, byte coding,
                       String format, String content) {
        if (ID == -1) {
            selectID();
        }
        System.out.println("Inserting");
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
