package project;
import java.sql.*;

/* Project
 * Name : Janvi Patel
 * Dalhousie ID : B00863421 
 * Date : 14/12/2020
 */

//Connecting to SQL 
public class ConnectionManager {
	    private static String driverName = "com.mysql.cj.jdbc.Driver";   
	    private static Connection connection;
	    
	    protected static Connection getConnection(String database,String username,String password) {
	        try {
	            Class.forName(driverName);
	            try {
	                connection = DriverManager.getConnection(database, username, password);		//connecting to database
	            } catch (SQLException ex) {
	                System.out.println("Failed to create the database connection."); 
	            }
	        } catch (ClassNotFoundException ex) {
	            System.out.println("Driver not found."); 
	        }
	        return connection;
	    } 
}
