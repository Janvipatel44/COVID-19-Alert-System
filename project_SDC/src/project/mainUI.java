package project;

/* Project
 * Name : Janvi Patel
 * Dalhousie ID : B00863421 
 * Date : 14/12/2020
 */

import java.util.*;

public class mainUI{
		//mainUI is designed to handle operations
	    public static void main (String[] args)  {
	    	
	    	//details of commands
			String addDevice = "addDevice";
			String contactDevice = "contactDevice";
			String testresult = "testResult";
			String synchronizeData = "synchronizeData";
			String recordTestResult = "recordTestResult";
			String findGatherings = "findGatherings";
			String quit = "quit";
			
			String userCommand = "";
			Scanner userInput = new Scanner( System.in );
			String userArgument = "";

			String configurationFile = "ConfigurationFile_Government";		//configuration file name for government class
			MobileDevice device = null;
	        
			//commands menu for user
			System.out.println("Commands available:");
			System.out.println("  " + addDevice + " <space configuration file names>");
			System.out.println("  " + contactDevice + " <individual> <date> <duration>");
			System.out.println("  " + testresult + "<Positive test hashcode");
			System.out.println("  " + synchronizeData);
			System.out.println("  " + recordTestResult + "<testHash> <date> <result>");
			System.out.println("  " + findGatherings + "<date> <minsize> <minTime> <density>");
			System.out.println("  " + quit );
			
			Government gov = new Government(configurationFile);		//new instance of gov class

			do {
				// Find out what the user wants to do
				userCommand = userInput.next();
					
				/* Do what the user asked for.  If condition for each command.  Since each command
				   has a different number of parameters, we do separate handling of each command. */
 
				if (userCommand.equalsIgnoreCase(addDevice)) {		//add details of new device
					String filename;		
 
					// Get the file name list
					userArgument = getEndingString( userInput );	//scanning file name
					filename = userArgument;
				 	try {
						device = new MobileDevice(filename,gov);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						System.out.print(e);
					}	//creating instance of mobileDevice using filename and government object
				}
				else if(userCommand.equalsIgnoreCase(contactDevice))	//placing puzzle at provided location
				{
					userArgument = getEndingString( userInput );		//scanning detail of contacted details
					String[] inputs;	
					ArrayList<String> contactdeviceDetail = new ArrayList<>();
					if (userArgument == null) {
						contactdeviceDetail = null;
					} else {
						// build the array list for the parameter from each of the files given
						inputs = userArgument.split(" ");

						for (String Detail : inputs) {			//inserting details to array list
							contactdeviceDetail.add( Detail );
						}
					}
					System.out.print(device.recordContact(contactdeviceDetail.get(0),Integer.parseInt(contactdeviceDetail.get(1)), 
							Integer.parseInt(contactdeviceDetail.get(2))));			//calling recordContact function in device
				}
				else if(userCommand.equalsIgnoreCase(testresult))	//entering positive test result hash code
				{
					userArgument = getEndingString( userInput );	//scanning user input
					System.out.print(device.positiveTest(userArgument));				//calling positive test function in device class
				}
				else if(userCommand.equalsIgnoreCase(synchronizeData))		//to provide data to government class to enter details
				{
					System.out.print(device.synchronizeData());		//calling synchronizeData function in device class
				}
				//currently developing this two methods
				else if(userCommand.equalsIgnoreCase(recordTestResult))		//to enter details of COVID-19 test 
				{
					userArgument = getEndingString( userInput );		//scanning detail of contacted details
					String[] inputs;	
					ArrayList<String> contactdeviceDetail = new ArrayList<>();
					if (userArgument == null) {
						contactdeviceDetail = null;
					} else {
						// build the array list for the parameter from each of the files given
						inputs = userArgument.split(" ");

						for (String Detail : inputs) {			//inserting details to array list
							contactdeviceDetail.add( Detail );
						}
					}
					System.out.print(gov.recordTestResult(contactdeviceDetail.get(0), Integer.parseInt(contactdeviceDetail.get(1)), Boolean.parseBoolean(contactdeviceDetail.get(2))));
				}
				else if(userCommand.equalsIgnoreCase(findGatherings))		//to find gathering for government
				{
					userArgument = getEndingString( userInput );		//scanning detail of contacted details
					String[] inputs;	
					ArrayList<String> contactdeviceDetail = new ArrayList<>();
					if (userArgument == null) {
						contactdeviceDetail = null;
					} else {
						// build the array list for the parameter from each of the files given
						inputs = userArgument.split(" ");

						for (String Detail : inputs) {			//inserting details to array list
							contactdeviceDetail.add( Detail );
						}
					}
					System.out.print(gov.findGatherings(Integer.parseInt(contactdeviceDetail.get(0)),
							Integer.parseInt(contactdeviceDetail.get(1)),
							Integer.parseInt(contactdeviceDetail.get(2)),
							Float.parseFloat(contactdeviceDetail.get(3))));
				}

			} while (!userCommand.equalsIgnoreCase("quit"));  	//to quit 
    }
	private static String getEndingString(Scanner userInput ) {
				String userArgument = null;
	
				userArgument = userInput.nextLine();
				userArgument = userArgument.trim();
	
				// Include a "hack" to provide null and empty strings for testing
				if (userArgument.equalsIgnoreCase("empty")) {
					userArgument = "";
				} else if (userArgument.equalsIgnoreCase("null")) {
					userArgument = null;
				}
				return userArgument;
	}
}