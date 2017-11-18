import java.io.*;
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
    private Connection conn;

    public DB() {
        connect();
        System.out.println("conn open");
        ID = selectID();
    }

    public static void main(String[] args) throws SQLException {
        File file = new File("Server/file");

    }

    public Connection connect() {
        // SQLite connection string
        String url = "jdbc:sqlite:messenger";
        conn = null;
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
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, recipient);
            pstmt.setString(3, recipient);
            pstmt.setString(4, name);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Hashtable<String, String> temp = new Hashtable<>(10);
                temp.put("user", rs.getString(2));
                temp.put("recepient", rs.getString(3));
                temp.put("format", rs.getString(4));
                temp.put("content", rs.getString(12));
                temp.put("compression", Integer.toString(rs.getInt(9)));
                temp.put("coding", Integer.toString(rs.getInt(6)));
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

    public void insert(Object size, Object compressed, int noiseTimes, Object encoded, Object encodedTime, Object compressedTime,
                       Object user, Object recipient, Object compression, Object coding,
                       Object format, Object content) {
        if (ID == -1) {
            selectID();
        }
        System.out.println("Inserting");
        String sql =                   //1  //2         //3         //4         //5       6     7           8           9       10              11       12       13
                "INSERT INTO messages (id, intial_size, compressed, encoded, coding_time, user, recepient, compression, coding, compres_time, format, content, noises) " +
                        "VALUES (?,?,?, ?,?,?, ?,?,?, ?,?,?, ?)";

        Double dTime = new Double((int) encodedTime);
        Double cTime = new Double((int) compressedTime);

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, ++ID);                           //id
            pstmt.setInt(2, Integer.parseInt((String) size));                     //initial_size
            pstmt.setInt(3, Integer.parseInt((String) compressed));                //compressed
//            pstmt.setInt(4, Integer.parseInt((String) encoded));                   //encoded
            pstmt.setInt(4, (int) encoded);
            pstmt.setDouble(5, dTime);                          //coding_time
            pstmt.setString(6, (String) user);                  //user
            pstmt.setString(7, (String) recipient);             //recepient
            pstmt.setInt(8, Integer.parseInt((String) compression));               //compression
            pstmt.setInt(9, Integer.parseInt((String) coding));                    //coding
            pstmt.setDouble(10, cTime);                         //compres_time
            pstmt.setString(11, (String) format);               //format
            pstmt.setString(12, (String) content);              //message
            pstmt.setInt(13, noiseTimes);                    //noises

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void close() {
        try {
            conn.close();
            System.out.println("conn closed");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
