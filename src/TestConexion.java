import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class TestConexion {
    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        props.load(new FileInputStream("config.properties"));
        String url  = props.getProperty("db.url");
        String user = props.getProperty("db.user");
        String pass = props.getProperty("db.password");

        System.out.println("Conectando a: " + url);
        System.out.println("Usuario: " + user);

        Class.forName("org.postgresql.Driver");
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            System.out.println("✅ Conexión exitosa! " + conn.getMetaData().getDatabaseProductVersion());
        }
    }
}
