package project;
import java.sql.*;

public class ConnectionManager {
	    private static String driverName = "com.mysql.cj.jdbc.Driver";   
	    private static Connection connection;

	    protected static Connection getConnection(String database,String username,String password) {
	        try {
	            Class.forName(driverName);
	            try {
	                connection = DriverManager.getConnection(database, username, password);
	            } catch (SQLException ex) {
	                // log an exception. fro example:
	                System.out.println("Failed to create the database connection."); 
	            }
	        } catch (ClassNotFoundException ex) {
	            // log an exception. for example:
	            System.out.println("Driver not found."); 
	        }
	        return connection;
	    }
}
