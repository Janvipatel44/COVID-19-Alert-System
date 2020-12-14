package project;

/* Project
 * Name : Janvi Patel
 * Dalhousie ID : B00863421 
 * Date : 14/12/2020
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
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

/* ************** To handle Government's following details with SQL database: *************** */
//Reading the configurationFile and providing details of user name,password and database to ConnectionManager class
//Inserting contact details of initiator into SQL database
//Inserting positive hash details of initiator into SQL database 
//Finding notification about initiator has been in contact with COVID-19 per in the 14 days duration of test
//Inserting record Test result provided by agency into SQL database
//Find gathering of a particular date with minimum size and minimum density
 
public class Government {			
	 
	private Statement statement = null; 	//declaration of statement  
	private Connection connect = null;		//used for SQL connection 
	private List<String> individual_list = new ArrayList<String>();		//list of contacts which is being used to provide information about 
																		//this mobile device has been notified by this contact previously or not

	protected Government( String configurationFile ) 		//Constructor to read the configuration File to get information while database connection
	{
		if(configurationFile.equals(null) || configurationFile.equals("")) {	//if configuration file is null or empty
			System.out.print("Invalid Configuration File name");
			return;
		}
		FileReader deviceDetails = null;										//File reader for configurationFile
		try {
			deviceDetails = new FileReader(configurationFile);					//File reader object
		} catch (FileNotFoundException e) {
			System.out.print(e);
		}			
        BufferedReader bfr_reader=new BufferedReader(deviceDetails);   			//buffer reading for handling line by line read
        String database = null;													//to store the value of database
        String user = null;														//to store the value of user
        String password = null;													//to store the value of password					
        List<String> content = new ArrayList<>();								//to store content of lines

        try {
			while (bfr_reader.ready()) {	
				String line = bfr_reader.readLine();				//reading from buffer reading   
			    content.add(line);									//adding lines to array list
			}
		} catch (IOException e) {
			System.out.print(e);
		}
		try {
			bfr_reader.close();		//closing buffer reader
			deviceDetails.close();	//closing the file
		} catch (IOException e) {
			System.out.print(e);
		}										

        if(content.size()>3 || content.isEmpty() || content.size()<3) {				//if file contains more then or less then 3 lines or file is empty
			System.out.print("Invalid Content in Configuration File");
			return;
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
				System.out.print("Please check content format");
				return;
	        }
	        if(!read_detail.contains("=")) {					//if line does not contain "=" sign
				System.out.print("Please check content format");
				return;
	        }
        }
        if(database.equals(null) || user.equals(null) || password.equals(null) || database.equals(" ") || user.equals(" ") || password.equals(" ")) {	//if any value is null 
			System.out.print("Null/Empty value found in SQL connection details");
			return;
        }
        
        //use the details of database, user and password from configuration file in ConnectionManager
        connect = ConnectionManager.getConnection(database, user, password); 	//calling ConnectionManager to establish connections 
		try {
			statement = connect.createStatement();			//statement creation
		} catch (SQLException e) {
			e.printStackTrace();
		}		
	}
	
	//When mobileContact is being called by synchronizeData, contact Details and positive hash details are being stored in SQL database
	//Handling XML read file operations
	//Also at last it notify the device about contact with COVID-19 positive person 
	//Sub Classed used: readXML,notify_mobiledevice
	protected boolean mobileContact( String initiator, String contactInfo )  
	{		
		Document doc = readXML(contactInfo);			//Calling readXML method and that returns read document 
		ResultSet resultSet = null;						//declaring resultSet for SQL query output 
		boolean resultNotify = false;					//for resultNotify to check notify device method has returned false or true
		int success=0;									//to check whether SQL query is successfully performed or not
		int total_successContact=0;							//to get idea on final count of multiple contact details are stored successfully
		int total_successpositiveHash = 0;					//to keep track of positiveHash's successful insertion in SQL database

		if(initiator.equals(null) || initiator.equals(""))	{	//initiator is null or empty 
			System.out.print("Please perform addDevice first\n");
			return false;	//returning false due to Null value of 'initiator' 
		}
		//Table MobileDevice hash contains hash code of those device whose contact details/ positive hash details we are going to insert
		try {
			//insert ignore is used to avoid inserting same details again into database
			statement.execute("insert ignore into mobileDeviceHashDetails (mobileDeviceHash) values('"+initiator+"');");
		} catch (SQLException e) {
			System.out.print(e);
		}
	
		NodeList nodeList = doc.getElementsByTagName("initiator");  //Creating node list for each contact starting with initiator tag

		// nodeList is not iterable, so I am using for loop  
		for (int itr = 0; itr < nodeList.getLength(); itr++)   	//for every block of initiator
		{  
			Node node = nodeList.item(itr);  					//getting items from nodeList
			if (node.getNodeType() == Node.ELEMENT_NODE)   		//if node type is element node
			{  
				Element eElement = (Element) node;  			//finding the element 
				String individual = eElement.getElementsByTagName("individual").item(0).getTextContent(); //content of individual named element
				int date =  Integer.parseInt(eElement.getElementsByTagName("date").item(0).getTextContent()); //content of date named element
				int duration = Integer.parseInt(eElement.getElementsByTagName("duration").item(0).getTextContent().toString()); //content of duration named element
				individual_list.add(individual);	//adding the contact device name to individual list for notify user method 
				
				//Table contactDetails contains recordTime, date, mobiledevice hash and contactHash
				try {	
					//Checking if there is already data present for the same date, same individual and same initiator 
					resultSet = statement.executeQuery("select recordTime from contactDetails "
							+ "where mobileDeviceHash = '"+initiator+"' and recordContactHash = '"+individual+"' and recordDate = '"+date+"';");
				} catch (SQLException e) {
					System.out.print(e);
				}

				try {
					if(resultSet.next() == false) {		//if there is no data found as this element's details
						
						//Inserting into contactDetails table
						success = statement.executeUpdate("insert ignore into contactDetails values('"+initiator+"', '"+individual+"', '"+date+"', '"+duration+"');");
						if(success==1)		//if successfully executed query then incrementing a variable 
							total_successContact++;	//after terminating the for loop we will look number of successful returns 
					}
					else {		//If there is already an entry then adding the time 
						duration = duration + resultSet.getInt("recordTime");		//adding duration to a value which is alreay there in database
						//updating the contactDetails' recordTime with updated duration
						success = statement.executeUpdate("update contactDetails set recordTime = '"+duration+"' where mobileDeviceHash = '"+initiator+"' "
														+ "and recordContactHash = '"+individual+"' and recordDate = '"+date+"';");
						if(success==1)		//if successfully executed query then incrementing a variable 
							total_successContact++;	//after terminating the for loop we will look number of successful returns
					}
					resultSet.close();			//closing result set
				} catch (SQLException e) {
					System.out.print(e);
				}
			}  
		} 
		if(total_successContact!=nodeList.getLength()) {			//checking whether all the elements are successfully inserted into database
			System.out.println("\nContact detail insertion unsucessful");
		} 
		
		//Gathering node list of positive test
		nodeList = doc.getElementsByTagName("positivetest");  
		if(nodeList.getLength()!=0) {		//if node list is not empty
			for (int itr = 0; itr < nodeList.getLength(); itr++)   	//for every value of node list
			{  
				//content of positive test named element
				String positiveHash = doc.getElementsByTagName("positivetest").item(itr).getTextContent();	
				try {
					//Checking if there is already data present for the same initiator and positive test hash 
					success = statement.executeUpdate("insert ignore into devicePositiveResult values('"+initiator+"', '"+positiveHash+"');");
					//If user wants to enter positive hash result before the agency then details will not be successfully added
					if(success==1) {			//if successfully inserted into SQL database
						total_successpositiveHash++;	//after terminating the for loop we will look number of successful returns 
					}
				} catch (SQLException e) {
					e.printStackTrace();
				} 
			}
		}
		if(total_successpositiveHash!=nodeList.getLength())		//checking whether all the elements are successfully inserted into database
			System.out.println("\nPositive hash detail insertion unsuccessful");
		
		try {		
			resultNotify = notify_mobiledevice(initiator);		//Calling notify mobile device with initiator as passing parameter
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		if(resultNotify==true)			//if resultNotify is true then returning true: mobileDevice was in contact with new COVID-19 person 
			return true;
		else							//if not then returning false
			return false;
	}
	
	//To read XML file of mobile Device
	private Document readXML(String contactInfo) 
	{
		File myObj= new File(contactInfo);
		String filePath=myObj.getAbsolutePath();			//find path of configuration file
		File file= new File(filePath); 				//create a contact Info file on this particular path
		  
		//an instance of factory that gives a document builder  
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();  
		//an instance of builder to parse the specified xml file  
		DocumentBuilder db = null;
		try {
			db = dbf.newDocumentBuilder();		//newDocumentBuilder 
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		}  
		Document doc = null;
		try {
			doc = db.parse(file);				//document is generated of XML file
		} catch (SAXException | IOException e) {
			e.printStackTrace();
		}  
		doc.getDocumentElement().normalize(); 	
		return doc;				//returning the document to mobileContact Class
	}
	
	//Notify mobileDevice follows below steps:
	//Step 1: fetching the contacts, record date and for a particular device and with the consideration of difference between today's Data and
	//		  recordDate is not more then 14 days 
	//Step 2: for this recordContact fetching the positive hash values from Table devicePositiveResult
	//Step 3: matching with agency's result table and finding the result date & positiveHash value
	//checking result date provided by agency and record Date's difference is between 14 to -14 (14 and -14 excluding)
	//Step 4: checking if the initiator, positive hash, record contact hash already present in notifydeviceDetails
	//Step 5: if not then adding it to notify device details and increasing the flag
	//Step 6: if present then checking it with the current contact details to make sure that I am not going to provide a notification to user
	//		  till 14 days if I have already provided one. But If current device is contacted with other devices then I am going to provide notification
	private boolean notify_mobiledevice(String initiator) throws SQLException{
		
		//resultSet to store the outcome of query, statement to run query
		//Note: I have used different statement because of working on the same statement at the same time is not allowed
		ResultSet resultSet= null;
		Statement statement_1 =null;
		Statement statement_2 =null;
		Statement statement_3 =null;
		Statement statement_4 =null;
		statement_1 = connect.createStatement();		
		statement_2 = connect.createStatement();		
		statement_3 = connect.createStatement();		
		statement_4 = connect.createStatement();
		int flag = 0;

		//Table contactDetails with mobile device hash, recordcontactHash, date, time
		
		//Step 1: fetching the contacts, record date and for a particular device and with the consideration of difference between today's Data and
		//		  recordDate is not more then 14 days 
		resultSet = statement.executeQuery("select recordContactHash, recordDate "
				+ "from contactDetails where contactDetails.mobileDeviceHash='"+initiator+"' and \r\n" + 
				"  DATEDIFF(CAST(sysdate() As Date), '2020-01-01')-recordDate<14;");

		//For every record Contact repeating the same process and if result set is not empty
		while(resultSet.next())  //loop to check every contact results
		{
			//resultSets to store the outcome of query
			ResultSet resultSubSet_1= null;
			ResultSet resultSubSet_2= null;
			ResultSet resultSubSet_3= null;
			
			String recordDate = resultSet.getString("recordDate");		//fetching recordDate from this row
			String recordContactHash = resultSet.getString("recordContactHash");	//fetching record ContactHash from this row
			
			//Table devicePositiveResult contains mobileDevice Hash and positive Hash details
			//Step 2: for this recordContact fetching the positive hash values from Table devicePositiveResult
			resultSubSet_1 = statement_1.executeQuery("select devicePositiveResult.positiveHash"
					+ " from devicePositiveResult where devicePositiveResult.mobileDeviceHash = '"+recordContactHash+"';");
			
			while(resultSubSet_1.next()) {
				
				String positiveHash = resultSubSet_1.getString("positiveHash");

				//Table devicePositiveResult contains mobileDevice Hash and positive Hash details
				//Table agencyTestResults contains the results provided by agency: Positive Hash, result Date, result value
				//Step 3: matching with agency's result table and finding the result date & positiveHash value
				//checking result date provided by agency and record Date's difference is between 14 to -14 (14 and -14 excluding)
				resultSubSet_2 = statement_2.executeQuery("select devicePositiveResult.positiveHash,agencyTestResults.resultDate from devicePositiveResult,agencyTestResults \r\n" + 
						"where agencyTestResults.testResultHash = '"+positiveHash+"' and agencyTestResults.result = true"+ 
						" and agencyTestResults.resultDate-'"+recordDate+"' <14 and \r\n" + 
						"agencyTestResults.resultDate-"+recordDate+">-14;");

				//if result set is not empty then for every positive hash and result date of the particular mobile device
				while(resultSubSet_2.next()) {
					
					//Table notifyDeviceDetails is used for storing the device entries into the table for keeping track on next input
					//Table notifyDeviceDetails contains: mobileDevice, notificationDate, coronacontactDevice, positiveHash
					//Step 4: checking if the initiator, positive hash, record contact hash already present in notifydeviceDetails
					resultSubSet_3 = statement_3.executeQuery("select notificationDate,coronacontactDevice,positiveHash "
							+ "from notifyDeviceDetails where mobileDeviceHash = '"+initiator+"' and positiveHash = '"+positiveHash+"' and "
									+ "coronacontactDevice ='"+recordContactHash+"' ;");

					//if result set is not empty
					if(resultSubSet_3.next()==false) {
							
							//Inserting details into notifyDeviceDetails
							statement_4.execute("insert ignore notifyDeviceDetails values('"+initiator+"',DATEDIFF(CAST(sysdate() As Date), '2020-01-01'),"
									+ "'"+recordContactHash+"','"+positiveHash+"') ; ");
							flag++;		//increasing the flag value
					}
					else {		
						if(individual_list.contains(recordContactHash)) {	//if device is again being contacted with COVID-19 patient
							flag++;		//increasing the flag value
						}
					}
				}
			}
		}
		resultSet.close();			//result set closing
		individual_list.clear();	//cleaning the list
		if(flag >0)			//if flag is greater then 0 then returning true
			return true;
		else
			return false;
	}
	
	//Agency provides test results, result date and result values and inserting into SQL table
	protected boolean recordTestResult( String testHash, int date, boolean result ) {
		int success=0;

		if(testHash.equals(null) || testHash.equals(""))			//test hash is null or empty
			return false;
		
		if(testHash.matches("[0-9]+") || testHash.matches("[a-z]+") || testHash.matches("[A-Z]+")) {	//test hash is numbers or alphabets only
			System.out.print("\nInvalidate testHash... contains either numbers only or alphabets only");
			return false;
		}
		if(!testHash.matches("[A-Za-z0-9]+")) {				//test hash contains special characters
			System.out.print("\nInvalidate testHash... contins special characters");
			return false;
		}
		if(date<=0 ) {
			System.out.print("Invalid value(zero or negative value) found for 'date'\n");
			return false;	//returning false due to date is zero or negative value
		}
		try {
			//Table agencyTestResult : testHash, date, result 
			//Inserting the details if not already present into data table
			success = statement.executeUpdate("insert ignore into agencyTestResults values('"+testHash+"','"+date+"', "+result+");");
		} catch (SQLException e) {
			System.out.print(e);
		}
		if(success==0) {		//if there are any unsuccessful insertion
			System.out.print("Unsuccessful insertion of TestResult provided by agency");
			return false;
		}
		return true;
	}

	//To Get information on number of gatherings on a particular date by considering minimum gathering of no of people, minimum time and minimum density
	//Step 1: Hash table key-value insertion for contacted devices 
	//Step 2: Generate pair of key set from hashMap for example, having 5 keys we will end up with 10 pairs
	//Step 3: Find Set S for A,B and consider all the devices present in A,B both including A,B
	//Step 4: Filter with minSize of group need and find connections in Set S
	//Step 5: Maximum Possible Connection and find density, increase the gathering count if density is greater then the inputed density
	//Step 6: Removing the subset S from superSet to avoid considering the same gathering with less number of people again and decreasing the gathering count
	//Step 7: Returning the gathering count 
	protected int findGatherings( int date, int minSize, int minTime, float density ) 
	{	
		if(date<=0) {
			System.out.print("Invalid value(zero or negative value) found for 'date'\n");
			return 0;	//returning false due to date is zero or negative value
		}
		if(minSize<=1) {
			System.out.print("Invalid value(zero or negative value) found for 'minSize'\n");
			return 0;	//returning false due to size is less than 2 value
		}
    	if(minTime<=0 ) {
			System.out.print("Invalid value(zero or negative value) found for 'minTime'\n");
			return 0;	//returning false due to minTime is zero or negative value
		}
    	if(density<=0 ) {
			System.out.print("Invalid value(zero or negative value) found for 'density'\n");
			return 0;	//returning false due to density is zero or negative value
		}
		ResultSet resultSet= null;			//to store the result of query
		Map<String, List<String>> adj_list = new HashMap<String, List<String>>();	// Adjacency list map to store key value pair
		List<List<String>> duplicateHashSet = new ArrayList<List<String>>();
		List<List<String>> final_Set = new ArrayList<List<String>>();
		int gathering = 0;

		try {
			//Fetch the data from contactDetails by checking minimum time and date
			resultSet = statement.executeQuery("select * from contactDetails where recordTime >= '"+minTime+"' and recordDate = '"+date+"'; ");
		} catch (SQLException e) {
			System.out.print(e);
		}
		try {
			//if result set is not empty then for each entry of result set
			while(resultSet.next())
			{
				String mobileDeviceHash = resultSet.getString("mobileDeviceHash");		//fetching and storing mobileDeviceHash value from current row
				String recordContactHash = resultSet.getString("recordContactHash");	//fetching and storing recordContactHash value from current row
			
				//If A is Mobile device and B is contactHash then storing 
				//Hash Map 1) Key A: Value B and 2) Key B: Value A
				if(adj_list.get(mobileDeviceHash)==null)		{		//if there is no value present for mobileDevice
					adj_list.put(mobileDeviceHash, new ArrayList<String>());	//adding new array list 
				}
				if(adj_list.get(mobileDeviceHash).contains(recordContactHash)==false) {		//if mobileDeviceHash does not contain record Contact Hash
					adj_list.get(mobileDeviceHash).add(recordContactHash);		//adding contact device with values of key mobileDevice 
				}
				if(adj_list.get(recordContactHash)==null)		{		//if there is no value present for recordContact
					adj_list.put(recordContactHash, new ArrayList<String>());	//adding new array list 
				}
				if(adj_list.get(recordContactHash).contains(mobileDeviceHash)==false) {
					adj_list.get(recordContactHash).add(mobileDeviceHash);	//adding contact device with values of key recordContact
				}
			}
			System.out.print("\n" +adj_list);

			//Step 2: Generate pair of key set from hashMap for example, having 5 keys we will end up with 10 pairs

	        Set<String> hashSet_KeyValue =adj_list.keySet() ; 		//To generate pairs for every 
			int total_pair = (hashSet_KeyValue.size() * (hashSet_KeyValue.size()-1))/2;		//total pairs possible
			int pair_count = 2;
		    String[][] myNumbers = new String[total_pair][pair_count];	//2D Array for pair column 1: element1 from pair, column 2: element2 from pair 
		    int flag =0;
		    int i=0,j=0;
		    
		    //If A,B,C,D,E in set (A,B),(A,C),(A,D),(A,E),(B,C)
		    //note: not considering (B,A) so till we find the same element from outer loop into inner loop not changing the flag
			for (String pair1 : hashSet_KeyValue) {		//for every value in set
				for (String pair2 : hashSet_KeyValue) {	//to find pair
					if(pair1.equals(pair2)) {			//flag for keeping track of same element found from the outer loop
						flag = 1;
					}
					if(!pair1.equals(pair2) && flag == 1)	//if not same element then adding element to array
					{
						myNumbers[i][j] = pair1;		//adding outer loop element into column 0
						j++;							//increasing the j value by 1 
						myNumbers[i][j] = pair2;		//adding inner loop element into column 1
						j--;							//decreasing the j value by 0 for further use
						i++;							//increasing i 
					}
				}
				flag = 0;								//making flag as 0 for further use
			}
			
			flag = 0;
			
			//Generating of Set S, Finding connection, maximum connection and density check is performed by this loop
			for(i=0;i<total_pair;i++)
			{
				float connection = 0;
				float max_connection = 0;
				float cal_density = 0;
				int numberOfelements = 0;
				List<String> hash_Set = new ArrayList<String>(); 		//to store values of Set S	
				hash_Set.add(myNumbers[i][0]);			//adding Device A to set S 
				hash_Set.add(myNumbers[i][1]);			//adding Device B to set S
				for (String pair2 : hashSet_KeyValue) {		 //for every device from Set hashSet key list 
					if(!pair2.equals(myNumbers[i][0]) && !pair2.equals(myNumbers[i][0])) {		//avoid adding device A,B into Set S
						if(adj_list.get(myNumbers[i][0]).contains(pair2) && adj_list.get(myNumbers[i][1]).contains(pair2))	//if A and B both contains value
						{
							hash_Set.add(pair2);		//adding Device to set S
						}
					}
				}
				for(List<String> temp_String: duplicateHashSet) {		//To remove duplicate hashSet & also removing some subset gathering which was already being considered
					numberOfelements=0;									
					for(String hash_value: hash_Set) {					//Checking if every Set 
						if(temp_String.contains(hash_value)) {			//check each string matches
							numberOfelements++;							//increasing the count 
						}
					}
					if(numberOfelements==hash_Set.size())				//if it exactly the same as Set s size
						break;
				}
				if(numberOfelements<hash_Set.size() || i==0) {			//if number of elements less then hash set size && first hash set

						if(hash_Set.size()>=minSize) {					//if Set s meets the condition of minimum size of group in gathering
							//finding the connections
							for(String pair: hash_Set) {				//for every set value
								for(String pair_temp: hash_Set) {		
									if(pair.equals(pair_temp)) {		//to avoid checking the same pair
										flag = 1;
									}
									if(!pair.equals(pair_temp) && flag == 1)	
									{
										if(adj_list.get(pair).contains(pair_temp))	//for each pair checking the connecting is there
										{
											connection++;			//increasing the connection
										}
									}
								}
								flag = 0;
						}
						max_connection = (hash_Set.size()*(hash_Set.size()-1))/2;	//(n*n-1)/2 to find number of connections
						cal_density = connection/max_connection;		//c/(n*n-1)/2
						if(cal_density>density)						//to check the density 
						{
							final_Set.add(hash_Set);				//final_Set to get idea about gathering details
							gathering++;							//increasing gathering count
						}
						duplicateHashSet.add(hash_Set);				//added to duplicateHashSet

					}
				}
			}

			//Removing subset from list because it is already being covered by superSet 
			//Note: performing this because there were some subset those were left out while checking subset previously
			int subSet=0;
			for(List<String> temp_String: final_Set) {
				subSet=0;
				for(List<String> temp_String2: final_Set) {
					subSet=0;
					if(temp_String.size()<temp_String2.size()) {		//considering subset
						for(String hash_value: temp_String) {			//matches every string value in each set
							if(temp_String2.contains(hash_value)) {
								subSet++;
							}
						}
						if(subSet==temp_String.size())		//if sub set count is same as size of string then decreasing count
							gathering--;
					}
				}
			}
		} catch (SQLException e) {
			System.out.print(e);
		}
		return gathering;		//returning gatherings
	}
}