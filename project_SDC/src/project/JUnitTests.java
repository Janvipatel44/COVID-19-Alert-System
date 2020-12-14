package project;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.jupiter.api.Test;

//Note: I had performed Some queries mentioned in Government_findGatherings before running this file's testcase together

class JUnitTests {

	String configurationFile = "ConfigurationFile_Government";		//configuration file name for government class
	Government gov = new Government(configurationFile);		//new instance of gov class
    private MobileDevice c;
    
    @Test
    public void filename_null_empty(){		//Empty or null file entered as configurationFile
		String filename = "";
        try {
			c = new MobileDevice(filename, gov);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
     
    @Test
    public void configurationFile_notexist(){		//ConfigurationFile does not exist
		String filename = "Conf_mobileDevice.txt";
        try {
			c = new MobileDevice(filename, gov);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    @Test
    public void filename_valid(){			//Valid congurationFile entered
		String filename = "ConfigurationFile_MobileDevice";
        try {
			c = new MobileDevice(filename, gov);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    @Test
    public void recordContact_inputvalidation(){		
		String filename = "ConfigurationFile_MobileDevice";
        try {
			c = new MobileDevice(filename, gov);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}				//Object creation

        //null or empty value passed as individual - false
		assertFalse("Fail",c.recordContact("", 343, 10));
		
		//valid value for individual, date and time - true
    	assertTrue("True", c.recordContact("987452",127,50));
    	
    	//date as value 0 - false
    	assertFalse("Fail", c.recordContact("987452",0,10));
    	
    	//date as negative value - false
    	assertFalse("Fail", c.recordContact("987452",-10,10));
    	
    	//duration as 0 - false
    	assertFalse("Fail", c.recordContact("987452", 340,0));
    	
    	//duration as negative - false
    	assertFalse("Fail", c.recordContact("987452", 340,-10));
    	
    	//Individual contains only numeric values - true
    	assertFalse("Fail", c.recordContact("9A7a52", 340,-10));

    	//duplicate entry of contact - true
    	assertTrue("True", c.recordContact("987452", 340,10));
    	assertTrue("True(duplicate value)", c.recordContact("987452", 340,10));		
    	
    	//Call when there is an entry with different duration and date but same individual
    	assertTrue("True", c.recordContact("76767", 342,70));		
    	assertTrue("True(Same individual)", c.recordContact("76767", 341,20));	
    	
    	//Call when there is an entry with the same duration and date but different individual. (multiple contacts to one mobile device)
    	assertTrue("True", c.recordContact("54329", 342,70));		
    	assertTrue("True(Same duration and date)", c.recordContact("12321", 342,70));	
    	
    	//Call when there are multiple contacts to one mobile device with different duration and time.
    	assertTrue("True", c.recordContact("543329", 330,70));		
    	assertTrue("True(multiple contacts)", c.recordContact("112321", 312,20));	
    }
    
    @Test
    public void positiveTest_inputvalidation(){
    	
    	String filename = "ConfigurationFile_MobileDevice";	
        try {
			c = new MobileDevice(filename, gov);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}     				//Object creation

        // Null/Empty value as testHash. - false
        assertFalse("Fail", c.positiveTest(""));
        
        // testHash contains only numeric values. - false
    	assertFalse("Fail", c.positiveTest("102"));
        
    	//testHash contains special characters. - false
    	assertFalse("Fail", c.positiveTest("A1@"));
    	
    	//testHash contains only alphabets. - false
    	assertFalse("Fail", c.positiveTest("ABC"));
    	
        //testHash is an alphanumeric string. - true
    	assertTrue("True", c.positiveTest("Dd1"));
    	
    	//String testHash consists of a combination of 1 character and 1 number.  
    	assertTrue("True", c.positiveTest("AW2m89"));

    	//Long value of teshHash consists of a combination of multiple characters and numbers .
    	assertTrue("True", c.positiveTest("AIU769OIGWE"));

    	//Call positiveTest when there is already a testHash value present for a mobile device. (multiple test of COVID-19 for single user)
    	assertTrue("True", c.positiveTest("A3"));
    	assertTrue("True", c.positiveTest("A4"));

    	//Duplicate positiveTest for the same user.
    	assertFalse("Fail", c.positiveTest("A4"));
    }
    
    @Test
    public void synchronize()
    {	
    	String filename = "ConfigurationFile_MobileDevice";
        try {
			c = new MobileDevice(filename, gov);
		} catch (Exception e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}     			//Object creation
		//Call when contact device or positive hash does not exist but there is only detail of mobile device.
		assertFalse("False", c.synchronizeData());		//there is no COVID-19 contact device of input mobile
		// TODO Auto-generated catch block
			
		
		//Input Steps
		//Step 1: Agency reported positive hash Y1, 342 day and true COVID-19 result
		//Step 2: address=10.0.0.10, deviceName=SAMSUNG is contacted with contacted hash -1878734511, day 340 and for 70 minutes
		//Step 3: synchronized with system (Here synchronize device's address=10.0.0.10, deviceName=SAMSUNG)
		//Step 4: address=10.0.0.20, deviceName=SAMSUNG inserted positive test Y1 
		//Step 5: synchronized with government and provided result
		//Step 6: address=10.0.0.10, deviceName=SAMSUNG wants to check on day 348 about COVID-19 contact so calling synchronizeData method
		
		assertTrue("True",gov.recordTestResult("Y1", 342, true));		//Step 1: Agency reported positive hash Y1, 342 day and true COVID-19 result
		
		assertTrue("True",c.recordContact("-1878734511", 340,70));			//Step 2: address=10.0.0.10, deviceName=SAMSUNG is contacted with contacted hash -1878734511, day 340 and for 70 minutes
		
		//Step 3: synchronized with system (Here synchronize device's address=10.0.0.10, deviceName=SAMSUNG)
		assertFalse("False",c.synchronizeData());
				
	
		//To switch to another mobileDevice input
		File file=new File(filename); 			
		String filePath=file.getAbsolutePath(); //find path of configuration file 
		File myObj  = new File(filePath); //create a contact Info file on this particular path
		FileWriter myWriter = null;
		try {
			 myWriter = new FileWriter(myObj);		//FileWriter creation
			 myWriter.write("address=10.0.0.20");	//Changing device address
			 myWriter.write("\ndeviceName=SAMSUNG");	//Changing device name
			 myWriter.close(); //closing the writer instance
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} //creating writer for XML file
        try {
			c = new MobileDevice(filename, gov);
		} catch (Exception e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}     
		assertTrue("True",c.positiveTest("Y1"));			//Step 4: address=10.0.0.20, deviceName=SAMSUNG inserted positive test Y1 

		//Step 5: synchronized with government and provided result
		assertFalse("False",c.synchronizeData());		//Using assertFalse because no contact with covid-19 person 
		
		
		//To re-switch to previous mobileDevice input
		file=new File(filename); 
		filePath=file.getAbsolutePath(); //find path of configuration file 
		myObj  = new File(filePath); //create a contact Info file on this particular path
		myWriter = null;
		try {
			 myWriter = new FileWriter(myObj);		//FileWriter creation
			 myWriter.write("address=10.0.0.10");	//Changing device address
			 myWriter.write("\ndeviceName=SAMSUNG");	//Changing device name
			 myWriter.close(); //closing the writer instance
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} //creating writer for XML file
        try {
			c = new MobileDevice(filename, gov);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		//Step 6: address=10.0.0.10, deviceName=SAMSUNG wants to check on day 348 about COVID-19 contact so calling synchronizeData method
		assertTrue("True",c.synchronizeData());
		
    }
    
    @Test
    public void Government_recordTestResult()
    {	
    	// testHash contains only numeric values. - false
    	assertFalse("Fail", gov.recordTestResult("102",347,true));
        
    	//testHash contains special characters. - false
    	assertFalse("Fail", gov.recordTestResult("A1@",347,true));
    	
    	//testHash contains only alphabets. - false
    	assertFalse("Fail", gov.recordTestResult("ABC",347,true));
    	
        //testHash is an alphanumeric string. - true
    	assertTrue("True", gov.recordTestResult("A1",347,true));
    	
    	//Null/Empty value passed as testHash. - false
        assertFalse("Fail", gov.recordTestResult("",347,true));

    	//date is equal to zero. - false
        assertFalse("Fail", gov.recordTestResult("B1",0,true));

    	//date as a negative value. - false
        assertFalse("Fail", gov.recordTestResult("B1",-348,true));

    	//Valid date passed as positive integer value. - true
        //String teshHash consists of a combination of 1 character and 1 number.  
        //date passed as 1.

        assertTrue("True", gov.recordTestResult("B1",1,true));

    	//False or True passed as result. - true
        //Long value of teshHash consists of a combination of multiple characters and numbers.
        //date passed as greater than 1. 

        assertTrue("Fail", gov.recordTestResult("CA1",348,false));

    }
    @Test
    public void Government_findGatherings()
    {
    	//date is equal to zero. - false
        assertEquals(0, gov.findGatherings(0,3,10,1));

    	//date as a negative value. - false
        assertEquals(0, gov.findGatherings(-5,3,10,1));

    	//Valid date passed as positive integer value. - true
    	//minSize as positive integer. - true
    	//minTime as a positive integer. - true
    	//density is greater than zero. - true
        //date passed as greater than 1. 
        //Passed minTime as more than 1 minutes.
        //Inputed density is greater than zero.
        //Passed minSize as more than 2 members of the gathering.
        //When only one no of gathering.
        //One of the conditions or all the conditions followed to find gathering.
        
        //Note: I had performed Below queries into mySQL before running this function.
        /*
        insert into mobileDeviceHashDetails values(1);
        insert into mobileDeviceHashDetails values(2);
		insert into contactDetails values(123, 234,10,10);
		insert into contactDetails values(123, 345,10,20);	
		insert into contactDetails values(123, 456,10,13);
		insert into contactDetails values(234,345,10,25);
		insert into contactDetails values(234,123,10,80);
		insert into contactDetails values(234,567,10,40);*/
        
        assertEquals(1, gov.findGatherings(10,3,10,(float) 0.7));

    	//minSize is equal to zero/one. - false
        assertEquals(0, gov.findGatherings(10,0,10,(float) 0.6));

    	//minSize as a negative value. - false
        assertEquals(0, gov.findGatherings(10,-2,10,(float) 0.6));

    	//minTime is equal to zero. - false
        assertEquals(0, gov.findGatherings(10,2,0,(float) 0.6));

    	//minTime as a negative value. - false 
        assertEquals(0, gov.findGatherings(10,2,-7,(float) 0.6));
 
    	//density is less than or equal to zero. - false
        assertEquals(0, gov.findGatherings(10,2,7,0));

        //date passed as 1.
        assertEquals(0, gov.findGatherings(1,3,11,(float) 0.6));

        //Passed minTime as 1 minutes.
        //Details of a particular date do not exist. (no gatherings)
        assertEquals(0, gov.findGatherings(10,1,1,(float) 0.6));

        //Passed minSize as 2 members of the gathering.
        //When more than one gathering for the same day.
        assertEquals(4, gov.findGatherings(10,2,10,(float) 0.6));
    }
}
