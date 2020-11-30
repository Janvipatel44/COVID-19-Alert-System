package project;

/* Project
 * Name : Janvi Patel
 * Dalhousie ID : B00863421 
 * Date : 30/12/2020
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.sql.*;

public class MobileDevice {			//To handle mobile device details

		//hash table to include mobile device hash code as key and object of contact details of device including hash code, date, 
	    private static Hashtable<Integer, ArrayList<contactDeviceDetails>> recentContact = new Hashtable<Integer, ArrayList<contactDeviceDetails> >(); 
	 
	    //hash table to include mobile device hash code detail as key and positive test hash as value
	    private static Hashtable<Integer, String> positiveTestdetails = new Hashtable<Integer, String >(); 
	    private contactDeviceDetails contactDeviceDetails;				// declaration of contactDevicedetails
		private int devicehashcode = 0;											// to use same mobile device while entering the details
		private ArrayList<contactDeviceDetails> collection_contactDeviceDetails=new ArrayList<contactDeviceDetails>();//creating new generic arraylist for arraylist of contact device details for a particular device 
		private Government contactTracer;								//declaration of government object
		
		protected MobileDevice( String configurationFile, Government contactTracer ) throws Exception {		//mobile device constructor 
			
			//checks configurationFile name is null or empty
			if(configurationFile.equals(null) || configurationFile.equals("")) {
	        	throw new Exception();
			}
			
			FileReader deviceDetails = new FileReader(configurationFile);		//reading the configuration files
	        BufferedReader bfr_reader=new BufferedReader(deviceDetails);   		//buffer reader to handle reading of line by line
	        String address = null;												//to store address details
	        String deviceName = null;											//to store device name details
	    	List<String> content = new ArrayList<>();							//this array list is used to store lines of file 
	
	        while (bfr_reader.ready()) {	
				String line = bfr_reader.readLine();							//reading from buffer reading   
		        content.add(line);												//adding line to array list
	        }
			bfr_reader.close();													//closing buffer reader
			
	        if(content.size()>2 || content.isEmpty()) {							//if file contains more than 2 lines or no content found
	        	throw new Exception();
	        }
	        for(String read_detail: content)									//read string from array list
	        {	
	        	int index = read_detail.indexOf("=");							//find index of equals
		        if(read_detail.contains("address")) {							//if string contains address 
		        	address = read_detail.substring(index+1,read_detail.length());		//get the value of substring from line which is the value of address
		        }
		        else if(read_detail.contains("deviceName")) {					//if string contains device Name
		        	deviceName = read_detail.substring(index+1,read_detail.length());	//get the value of substring from line which is value of deviceName 
		        }
		        else {
		        	throw new Exception();										//if found anything other than address and deviceName keyword then throwing exception
		        }
	        }
	        if(address==null || deviceName==null) {								//address and deviceName contains has no value 
	        	throw new Exception();
	        }
	        System.out.print("Address: " +address);
	        System.out.print("\nDevicename" +deviceName);
	
	        devicehashcode = address.concat(deviceName).hashCode();				//generate concat operation of address and device name and then finding hashcode
	        System.out.print("\ndevicehashcode" +devicehashcode);
	
			this.setContactTracer(contactTracer);								//calling government object
	}
	
	protected int recordContact( String individual, int date, int duration )		//record contact is used to enter details of contacted device, date and duration
	{
		
		if(individual.equals(null) || individual.equals("") || date<=0 || duration<=0 )		//individual is null or empty 
			return 0;																		//date and duration is zero or negative value
		
		contactDeviceDetails = new contactDeviceDetails(individual,date,duration);			//passing values to contactDeviceDetails object
		
		if(recentContact.get(devicehashcode)!=null)											//if it is not the first contact to mobile device (key-value pair is present in hash table)
			collection_contactDeviceDetails = recentContact.get(devicehashcode);			//collecting all the details of contacted device in array list
		else
			collection_contactDeviceDetails.clear();										//if it is the first entry then clearing the array list 
																							//(here we need to clear it because it might be the case when we have values in 
																							//array list from previous mobile device
		
		for(contactDeviceDetails detail: collection_contactDeviceDetails ) {				//checks contact device details one by one
			if(detail.individual_hashcode.equals(contactDeviceDetails.individual_hashcode) 
					&& detail.duration == contactDeviceDetails.duration && 
					   detail.date == contactDeviceDetails.date)
			{
				return 0;			//if device details, duration, date matches with previous details
			}
		}
		collection_contactDeviceDetails.add(contactDeviceDetails);		//if not exist already in array list then adding it to array list 
		recentContact.put(devicehashcode,collection_contactDeviceDetails);			//put array list into hash table by key-value 
		
		return 1;
	}
	
	protected int positiveTest(String testHash)						//adding details of positive test hash value of mobile device
	{		
		//if(positiveTestdetails.get(devicehashcode)!=null)			/////check with professor what if someone enters 2nd test hash of a particular device
		if(testHash.equals(null) || testHash.equals(""))			//test hash is null or empty
			positiveTestdetails.put(devicehashcode, testHash);		//key as mobile device and device hash code as value in positiveTest details in hash table
																						
		return 1;
	}
	
	protected boolean synchronizeData() throws IOException, SQLException, ClassNotFoundException {		//synchronize data method is used convert the hash table
																										//to XML format and then passing details to mobile device
        System.out.print(recentContact);
        System.out.print(positiveTestdetails);

        if(recentContact.isEmpty() && positiveTestdetails.isEmpty())	//if there is no entry in hash table meaning user directly calling synchronizeData function
        	return false;
        
		Set<Integer> setOfCountries = recentContact.keySet();			//gathering key set of hash table
		String file_content=null;										//file content to store detail of data structure and then add string to xml file
		
		String contactInfo = "contactInfo.xml";							//name of XML file
		
		//commands for XML file
		file_content = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" + "\n";		
		file_content += "<mobiledevicedetails>" + "\n";
        for(Integer mobile : setOfCountries)		//for every mobile device in key set
        {
    		file_content += "\t" + "<initiator>" + mobile + "</initiator>" +"\n";		//mobile device as initiator 
        	for(contactDeviceDetails detail : recentContact.get(mobile))				//for each contact details
        	{
        		//detail of individual hash code , date, duration and positive test hash code appending into a string
        		file_content += "\t" +"<individual> "+ detail.individual_hashcode +" </individual>" + "\n";
        		file_content += "\t" +"<date> "+ detail.date +" </date>" + "\n";
        		file_content += "\t" +"<duration> "+ detail.duration +" </duration>" + "\n";
        		file_content += "\t" +"<positivetest> "+ positiveTestdetails.get(mobile) +" </positivetest>" + "\n";
        	}
    		file_content += "</mobiledevicedetails>" + "\n";	//terminating the mobile device details tag
    		System.out.print(file_content);
        	File file=new File(contactInfo);				
    	    String filePath=file.getAbsolutePath();			//find path of configuration file
            File myObj = new File(filePath); 				//create a contact Info file on this particular path
    		  
    		if (myObj.createNewFile()) 	//creating new XML file
    		{ 
    			myObj.setWritable(true); //making the file writable
    	    }
    		else 
    		{ 
    			return false; 	//if file already exist 
    		}
    		FileWriter myWriter = new FileWriter(myObj); 		//creating writer for XML file
    		try {
    			myWriter.write(file_content);			//writing string to the file
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();	
    		}							
    		myWriter.close();			//closing the writer instance
    		contactTracer.mobileContact(mobile.toString(),contactInfo);		//calling mobile contact with initiator and government details
    		myObj.delete();				//deleting the file 
        }
		
        collection_contactDeviceDetails.clear();		//clearing array list after inserting to database
        recentContact.clear();							//clearing hash tables after inserting details to database
        positiveTestdetails.clear();
		return true;
	}

	public Government getContactTracer() {		//getter
		return contactTracer;
	}
	
	public void setContactTracer(Government contactTracer) {	//setter
		this.contactTracer = contactTracer;
	}
}