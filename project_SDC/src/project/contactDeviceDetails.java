package project;

/* Project
 * Name : Janvi Patel
 * Dalhousie ID : B00863421 
 * Date : 14/12/2020
 */

public class contactDeviceDetails {
		//Object for storing contact device details
		protected int date = 0,  duration = 0;			//to handle date and duration of contact device
		protected String individual_hashcode = null;	//to handle hash code of individual device
		protected contactDeviceDetails(String individual_hashcode, int date, int duration)
		{
			this.individual_hashcode = individual_hashcode;		//individual hash code
			this.date = date;	//date
			this.duration = duration;	//duration
		}
} 
