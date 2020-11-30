package project;

/* Project
 * Name : Janvi Patel
 * Dalhousie ID : B00863421 
 * Date : 30/12/2020
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Government {
	
	public Government( String configurationFile ) throws Exception
	{
		Connection connect = null;	//used for SQL connection 
		Statement statement = null; //declaration of statement  

		if(configurationFile.equals(null) || configurationFile.equals("")) {	//if configuration file is null or empty
        	throw new Exception();
		}
		FileReader deviceDetails = new FileReader(configurationFile);			//file reading 
        BufferedReader bfr_reader=new BufferedReader(deviceDetails);   			//buffer reading for handling line by line read
        String database = null;													//to store the value of database
        String user = null;														//to store the value of user
        String password = null;													//to store the value of password					
        List<String> content = new ArrayList<>();								//to store content of lines

        while (bfr_reader.ready()) {	
			String line = bfr_reader.readLine();				//reading from buffer reading   
	        content.add(line);									//adding lines to array list
        }
		bfr_reader.close();										//closing buffer reader

        if(content.size()>3 || content.isEmpty()) {				//if file contains more then 3 lines or file is empty
        	throw new Exception();
        }
        for(String read_detail: content)						//for every line in the array list
        {
        	int index = read_detail.indexOf("=");				//finding index of equal to
	        if(read_detail.contains("database")) {				//if string contains database
	        	database = read_detail.substring(index+1,read_detail.length());		//finding value of database by substring after equal to sign
	        }
	        else if(read_detail.contains("user")) {				//if string contains user
	        	user = read_detail.substring(index+1,read_detail.length());			//finding value of user by substring after equal to sign
	        }
	        else if(read_detail.contains("password")) {			//if string contains password
	        	password = read_detail.substring(index+1,read_detail.length());		//finding value of password by substring after equal to sign
	        }
	        else {												//if file contains any other details then throwing exception
	        	throw new Exception();
	        }
        }
        if(database==null || user==null || password == null) {	//if any value is null 
        	throw new Exception();
        }
		Class.forName("com.mysql.cj.jdbc.Driver");	//used for jdbc driver
		connect = DriverManager.getConnection(database, user, password);	//connection to database
		statement = connect.createStatement();		
		statement.execute("use janvi;");		//To select janvi database
	}
	public boolean mobileContact( String initiator, String contactInfo ) {
		System.out.print("\nInitiator" +initiator);
		System.out.print("\nContactInfo" +contactInfo);
		System.out.print("\n");
		return false;
	}
	protected boolean recordTestResult( String testHash, int date, boolean result ){
		
		return false;
	}
	protected int findGatherings( int date, int minSize, int minTime, float density ) {
		return 0;
	}
}