package project;

/* Project
 * Name : Janvi Patel
 * Dalhousie ID : B00863421 
 * Date : 30/12/2020
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
 
/* ************** To handle mobile devices' following details: *************** */
//Generating hash code of mobile device using address, deviceName provided by User
//Record Contact Details of mobile device: contact hash, day, time
//Record Positive Hash details of mobile device
//Synchronize with Government Class

public class MobileDevice {			

	//hash table to include mobile device hash code as key and object of contact details of device including hash code, date, time
	private static Hashtable<String, List<contactDeviceDetails>> recentContact = new Hashtable<String, List<contactDeviceDetails> >();  
	//hash table to include mobile device hash code detail as key and positive test hash as value
    private static Hashtable<String, List<String>> positiveTestdetails = new Hashtable<String, List<String> >(); 
    //creating new generic array list for array list of contact device details for a particular device 
  	private List<contactDeviceDetails> collection_contactDeviceDetails=new ArrayList<contactDeviceDetails>();
    //creating new array list for storing details of positive hash list 
  	private List<String>collection_positiveTestDetails = new ArrayList<String>();
  	
    private contactDeviceDetails contactDeviceDetails;				// declaration of contactDevicedetails
	private String devicehashcode = null;							// to use same mobile device while entering the details
	private Government contactTracer;								//declaration of government object
	private File xmlObj; 											//xml file object
	//mobile device constructor to handle configurationFile reading and handling government object for synchronizeData 
	protected MobileDevice( String configurationFile, Government contactTracer )  throws Exception{		
		
		//checks configurationFile name is null or empty
		if(configurationFile.equals(null) || configurationFile.equals("")) {
			System.out.print("Invalid Configuration File name\n");
			return;
		}
		else {
			devicehashcode = mobileHashGenerator(configurationFile);	//Calling mobileHashGenerator to read the file and generate hash code
			System.out.print("Generated Mobile Device hash Code\n");
			this.setContactTracer(contactTracer);							//calling government object
		}
	}
	
	//Mobile Hash Generator reads configuration File with address and device name details and generating hash code for mobileDevice
	private String mobileHashGenerator( String configurationFile) throws Exception{
		
		FileReader deviceDetails = null;		//Declaration of FileReader
		try {
			deviceDetails = new FileReader(configurationFile);		//Storing the details of file into deviceDetails
		} catch (FileNotFoundException e) {		//If does not exist
			System.out.print("\n"+e);
		}		
        BufferedReader bfr_reader=new BufferedReader(deviceDetails);   		//buffer reader to handle reading of line by line
        String address = null;												//to store address details
        String deviceName = null;											//to store device name details
    	List<String> content = new ArrayList<>();							//this array list is used to store lines of file 

        try {
			while (bfr_reader.ready()) {	
				String line = bfr_reader.readLine();						//reading from buffer reading   
			    content.add(line);											//adding line to array list
			}
		} catch (IOException e) {											
			System.out.print("\n"+e);										//Message passing
		}
		try {
			bfr_reader.close();		//closing buffer reader
			deviceDetails.close();
		} catch (IOException e) {
			System.out.print("\n"+e);										//Message passing
		}
		
        if(content.size()>2 || content.isEmpty() || content.size()<2) {							//if file contains more than 2 lines or no content found
			System.out.print("Invalid Content in Configuration File\n");	//Message passing
			throw new Exception();											//throwing the Exception
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
				System.out.print("Please check content format\n");			//Message passing
				throw new Exception();										
	        }
	        if(!read_detail.contains("=")) {
				System.out.print("Please check content format\n");			//Message passing
				throw new Exception();										
	        }
        }
        if(address.equals(null) || deviceName.equals(null) || address.equals(" ") || deviceName.equals(" ") ) {								//address and deviceName contains has no value 
			System.out.print("Null value found in mobileDevice detail\n");
			throw new Exception();
        }
        
        //generate concat operation of address and device name and then finding hashcode
        devicehashcode = Integer.toString(address.concat(deviceName).hashCode());				
        return devicehashcode; 		//returning device hash code to constructor
	}
	 
	//recording the contact to hash table where Key: Hash code of mobile device
											//  Value: List of ContactDeviceDetails details: individual, date, duration 
	protected boolean recordContact( String individual, int date, int duration )		//record contact is used to enter details of contacted device, date and duration
	{
		if(individual.equals(null) || individual.equals(""))	{	//individual is null or empty 
			System.out.print("Null value found for 'individual'\n");
			return false;	//returning false due to Null value of 'individual' 
		}
		if(date<=0 ) {
			System.out.print("Invalid value(zero or negative value) found for 'date'\n");
			return false;	//returning false due to date is zero or negative value
		}
		if(duration<=0 && duration>1140) {		//considering 1140 because there are only 1140 minutes in one days
			System.out.print("Invalid value found for 'duration'\n");
			return false;	//returning false due to duration is zero or negative value
		}
		if(!individual.matches("[0-9-]+")) {		//considering only negative or positive integer value as individual
			System.out.print("Invalid value(alphabetes or special characters) found for 'Individual'\n");
			return false;	//returning false due to individual contains numbers or minus'-' value 
		}
		
		//Generating contactDeviceDetails object using individual, date and duration details
		contactDeviceDetails = new contactDeviceDetails(individual,date,duration);			

		if(recentContact.get(devicehashcode)!=null)											//if it is not the first contact to mobile device (key-value pair is present in hash table)
			collection_contactDeviceDetails = recentContact.get(devicehashcode);			//collecting all the details of contacted device in array list
		else
			collection_contactDeviceDetails.clear();										//if it is the first entry then clearing the array list 
																							//(here we need to clear it because it might be the case when we have values in 																			
		collection_contactDeviceDetails.add(contactDeviceDetails);		//if not exist already in array list then adding it to array list 
		recentContact.put(devicehashcode,collection_contactDeviceDetails);			//put array list into hash table by key-value 
		
		return true;		//return true after successful insertion
	}
	
	//recording the positive hash to hash table where Key: Hash code of mobile device
												  //  Value: List of positive hash
	protected boolean positiveTest(String testHash)						//adding details of positive test hash value of mobile device
	{	
		if(testHash.equals(null) || testHash.equals("")) {			//test hash is null or empty
			System.out.print("\nNull value found\n");				
			return false;
		}
		
		//if testHash only contains alphabets or numbers as hash code
		if(testHash.matches("[0-9]+") || testHash.matches("[a-z]+") || testHash.matches("[A-Z]+")) {
			System.out.print("\nInvalidate testHash... contains either numbers only or alphabets only\n");
			return false;
		}
		if(!testHash.matches("[A-Za-z0-9]+")) {					//if provided hash code contains special characters
			System.out.print("\nInvalidate testHash... contains special characters\n");
			return false;
		}
			
		if(positiveTestdetails.get(devicehashcode)!=null)			//if it is not the first hash code of mobile device (key-value pair is present in hash table)
			collection_positiveTestDetails = positiveTestdetails.get(devicehashcode);			//collecting all the details of contacted device in array list
		else
			collection_positiveTestDetails.clear();										//if it is the first entry then clearing the array list 
			
		if(collection_positiveTestDetails.contains(testHash) ) {				//Duplicate hash device entry for same mobile device
				System.out.print("Duplicate testHash for same device\n");
				return false;			//it is not allowed so returning false from here
		}
		collection_positiveTestDetails.add(testHash);		//if not exist already in array list then adding it to array list 
		positiveTestdetails.put(devicehashcode,collection_positiveTestDetails);			//put array list into hash table by key-value 																				
		return true;	//returning true indicating the successful insertion
	}
	
	//Calls MobileDevice class in Government and returns back the COVID-19 contact with device in last 14 days
	//Internally sub functions are used to generate formatted String and handling operations of XML file 
	//XML file and mobileDevice hash are being passed to mobile Contact
	protected boolean synchronizeData()  {		//synchronize data method is used convert the hash table
																										//to XML format and then passing details to mobile device
        boolean result = false;
		String file_content=null;										//file content to store detail of data structure and then add string to xml file
		String contactInfo = "contactInfo.xml";							//name of XML file
		
		file_content = xml_contentFormation();							//calling XML contentFormation to store the data from data structure to string format
		if(xml_FileOperations(file_content)==true) {					//if file content has been written successfully
			  //calling mobileContact which handles insertion of data to database and returning the notification about 
			  //contact with COVID-19 person in within last 14 days
			  result = contactTracer.mobileContact(devicehashcode,contactInfo);
			  xmlObj.delete(); 						   //deleting XML file of mobileDevice
			  collection_contactDeviceDetails.clear(); //clearing list of contact Device details value for further use
			  recentContact.remove(devicehashcode);	   //Removing value (array list) of key (mobile device) once entered details into Government 
			  collection_positiveTestDetails.clear();  //clearing list of positive hash value for further use
			  positiveTestdetails.remove(devicehashcode);	//Removing value (array list) of key (mobile device) once entered details into Government 
			  if(result==true)	{ //return true if device has been near with COVID-19 contact in last 14 days   
				  return true;												
			  }
			  else {			//otherwise false
				  return false;
			  }
		}
		else {
			return false;
		}
	}
	
	//Generating String of XML format
	private String xml_contentFormation()
	{
		String file_content=null;					//file content to store detail of data structure and then add string to xml file

		//commands for XML file
		file_content = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" + "\n";		
		file_content += "<mobiledevicedetails>" + "\n";
        
		if(recentContact.get(devicehashcode)!=null) {			//if there is no contact for object of mobile device  
        	for(contactDeviceDetails detail : recentContact.get(devicehashcode))				//for each contact details
        	{
        		//detail of individual hash code , date, duration and positive test hash code appending into a string
        		file_content += "\t" + "<initiator>" + "\n";		//mobile device as initiator 
        		file_content += "\t\t" +"<individual>"+detail.individual_hashcode+"</individual>" + "\n";
        		file_content += "\t\t" +"<date>"+detail.date+"</date>" + "\n";
        		file_content += "\t\t" +"<duration>"+detail.duration+"</duration>" + "\n";
        		file_content += "\t" + "</initiator>" +"\n";
        	}
		}
        if(positiveTestdetails.get(devicehashcode)!=null) {		//if there is no positive hash for object of mobile device 
	        	for(String test: positiveTestdetails.get(devicehashcode))
	        	{
	        		file_content += "\t" +"<positivetest>"+test+"</positivetest>" + "\n";
	        	}
        }
        file_content += "</mobiledevicedetails>" + "\n";		//closure of mobile device tag
                
        return file_content;		//returning file content as string which is going to be stored in the XML file
	}
	
	//XML file creating and writing the content for the mobile device entry into database
	private boolean xml_FileOperations(String file_content)
	{
		String contactInfo = "contactInfo.xml";							//name of XML file
		File file=new File(contactInfo); 		//file creation using contactInfo name
		String filePath=file.getAbsolutePath(); //find path of configuration file 
		xmlObj  = new File(filePath); //create a contact Info file on this particular path
		  
		try {
			if (xmlObj.createNewFile()) //creating new XML file 
			{ 
				xmlObj.setWritable(true);	//making the file writable 
			}
			else { 
				xmlObj.delete();
				xmlObj.createNewFile();
				xmlObj.setWritable(true);
				return false; //if file already exist }
			}
		} catch (IOException e) {
    		System.out.print(e);  
		}
		FileWriter myWriter = null;
		try {
			myWriter = new FileWriter(xmlObj);	//FileWriter object creation
			myWriter.write(file_content); //writing string to the file 
			myWriter.close();			  //writer close
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}
	public Government getContactTracer() {		//getter
		return contactTracer;
	}
	public void setContactTracer(Government contactTracer) {	//setter
		this.contactTracer = contactTracer;
	}
}