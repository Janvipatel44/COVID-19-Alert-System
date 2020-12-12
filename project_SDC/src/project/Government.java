package project;

/* Project
 * Name : Janvi Patel
 * Dalhousie ID : B00863421 
 * Date : 30/12/2020
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;  
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.w3c.dom.Node;  
import org.w3c.dom.Element;  


public class Government {
	
	private Statement statement = null; //declaration of statement  
	private Connection connect = null;	//used for SQL connection 

	protected Government( String configurationFile ) throws Exception
	{

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
        
        connect = ConnectionManager.getConnection(database, user, password); 
		statement = connect.createStatement();		
		statement.execute("use janvi;");		//To select janvi database
	}
	protected boolean mobileContact( String initiator, String contactInfo ) throws ParserConfigurationException, SAXException, IOException, SQLException {		
		System.out.print("\nInitiator" +initiator);
		System.out.print("\nContactInfo" +contactInfo);
		System.out.print("\n");
		Document doc = readXML(contactInfo);
		
		System.out.println("Root element: " + doc.getDocumentElement().getNodeName());  
		NodeList nodeList = doc.getElementsByTagName("initiator");  
		ResultSet resultSet= null;		
		statement.execute("insert ignore into mobileDeviceHashDetails values('"+initiator+"');");
		
		// nodeList is not iterable, so we are using for loop  
		for (int itr = 0; itr < nodeList.getLength(); itr++)   
		{  
			Node node = nodeList.item(itr);  
			System.out.println("\nNode Name :" + node.getNodeName());  
			if (node.getNodeType() == Node.ELEMENT_NODE)   
			{  
				Element eElement = (Element) node;  		
				String individual = eElement.getElementsByTagName("individual").item(0).getTextContent(); 
				int date =  Integer.parseInt(eElement.getElementsByTagName("date").item(0).getTextContent());
				int duration = Integer.parseInt(eElement.getElementsByTagName("duration").item(0).getTextContent().toString());  
				
				System.out.print(individual);
				System.out.print(date);
				System.out.print(duration);
				resultSet = statement.executeQuery("select recordTime from contactDetails "
						+ "where mobileDeviceHash = '"+initiator+"' and recordContactHash = '"+individual+"' and recordDate = '"+date+"';");

				if(resultSet.next() == false) {
					statement.execute("insert ignore into contactDetails values('"+initiator+"', '"+individual+"', '"+date+"', '"+duration+"');");
				}
				else {
					duration = duration + resultSet.getInt("recordTime");
					System.out.print(duration);
					statement.execute("update contactDetails set recordTime = '"+duration+"' where mobileDeviceHash = '"+initiator+"' "
													+ "and recordContactHash = '"+individual+"' and recordDate = '"+date+"';");
				}
				resultSet.close();
			}  
		}
		nodeList = doc.getElementsByTagName("positivetest");  
		if(nodeList.getLength()!=0) {
			for (int itr = 0; itr < nodeList.getLength(); itr++)   
			{  
				String positiveHash = doc.getElementsByTagName("positivetest").item(itr).getTextContent();	
				System.out.print("PositiveHash" +positiveHash);
				statement.execute("insert ignore into devicePositiveResult values('"+initiator+"', '"+positiveHash+"');");
			}
		}
		if(notify_mobiledevice(statement,initiator)==true)
			return true;
		else
			return false;
	}
	private boolean notify_mobiledevice(Statement statement,String initiator) throws SQLException{
		ResultSet resultSet= null;
		Statement statement_1 =null;
		Statement statement_2 =null;
		statement_1 = connect.createStatement();		
		statement_2 = connect.createStatement();		

		resultSet = statement.executeQuery("select recordContactHash,recordDate from contactDetails "
				+ "where contactDetails.mobileDeviceHash='"+initiator+"' and \r\n" + 
				"DATEDIFF(CAST(sysdate() As Date), '2020-01-01')-recordDate<14;");
		while(resultSet.next())  //loop to store every row
		{
			ResultSet resultSubSet_1= null;
			ResultSet resultSubSet_2= null;
			String recordDate = resultSet.getString("recordDate");
			String recordContactHash = resultSet.getString("recordContactHash");
			System.out.print(recordContactHash);
			resultSubSet_1 = statement_1.executeQuery("select devicePositiveResult.positiveHash"
					+ " from devicePositiveResult where devicePositiveResult.mobileDeviceHash = '"+recordContactHash+"';");
			while(resultSubSet_1.next()) {
				String positiveHash = resultSubSet_1.getString("positiveHash");
				System.out.print(positiveHash);
				resultSubSet_2 = statement_2.executeQuery("select devicePositiveResult.positiveHash from devicePositiveResult,agencyTestResults \r\n" + 
						"where agencyTestResults.testResultHash = '"+positiveHash+"' and agencyTestResults.resultDate-'"+recordDate+"' <14 and \r\n" + 
						"agencyTestResults.resultDate-"+recordDate+">-14;\r\n" + 
						"");
				if(resultSubSet_2.next() == true) {
					System.out.print(resultSubSet_1.getString("positiveHash"));
					resultSet.close();
					resultSubSet_1.close();
					resultSubSet_2.close();
					return true;
				}
			}
		} 
		return false;
	}
	
	private Document readXML(String contactInfo) {
		File myObj= new File(contactInfo);
		String filePath=myObj.getAbsolutePath();			//find path of configuration file
		File file= new File(filePath); 				//create a contact Info file on this particular path
		  
		//an instance of factory that gives a document builder  
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();  
		//an instance of builder to parse the specified xml file  
		DocumentBuilder db = null;
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}  
		Document doc = null;
		try {
			doc = db.parse(file);
		} catch (SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		doc.getDocumentElement().normalize(); 
		return doc;
	}
	protected boolean recordTestResult( String testHash, int date, boolean result ) throws SQLException{
		
		if(testHash.equals(null) || testHash.equals(""))			//test hash is null or empty
			return false;
		//if(!testHash.matches("[A-Za-z0-9]+"))
			//return false;
	
		statement.execute("insert ignore into agencyTestResults values('"+testHash+"','"+date+"', "+result+");");
		return true;
	}
	protected int findGatherings( int date, int minSize, int minTime, float density ) {
		ResultSet resultSet= null;
		Map<String, List<String>> adj_list = new HashMap<String, List<String>>();	// Adjacency list map to store key value pair
		int count = 0;
		try {
			resultSet = statement.executeQuery("select * from contactDetails where recordTime >= '"+minTime+"' and recordDate = '"+date+"'; ");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			while(resultSet.next())
			{
				String mobileDeviceHash = resultSet.getString("mobileDeviceHash");
				String recordContactHash = resultSet.getString("recordContactHash");
			
				if(adj_list.get(mobileDeviceHash)==null)		{
					adj_list.put(mobileDeviceHash, new ArrayList<String>());
				}
				if(adj_list.get(mobileDeviceHash).contains(recordContactHash)==false) {
					adj_list.get(mobileDeviceHash).add(recordContactHash);
				}
				if(adj_list.get(recordContactHash)==null)		{
					adj_list.put(recordContactHash, new ArrayList<String>());
				}
				if(adj_list.get(recordContactHash).contains(mobileDeviceHash)==false) {
					adj_list.get(recordContactHash).add(mobileDeviceHash);
				}
			}
			 Set<String> hashSet_KeyValue =adj_list.keySet() ; 
				System.out.print("\nHashset key value: " +hashSet_KeyValue);

				int total_pair = (hashSet_KeyValue.size() * hashSet_KeyValue.size()-1)/2;
			    String[][] myNumbers = new String[total_pair][2];
			    int flag =0;
			    int i=0,j=0;
				for (String pair1 : hashSet_KeyValue) {
					for (String pair2 : hashSet_KeyValue) {
						if(pair1.equals(pair2)) {
							flag = 1;
						}
						if(!pair1.equals(pair2) && flag == 1)
						{
							myNumbers[i][j] = pair1;
							j++;
							myNumbers[i][j] = pair2;
							j--;
							i++;
						}
					}
					flag = 0;
				}
			System.out.print("\n" +adj_list);
		}
		catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.print("\n");
		return 0;
	}
	
}