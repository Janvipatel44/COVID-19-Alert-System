/*creating database*/
create database `ProjectSDC`;
use ProjectSDC;

/*showing existing tables*/
show tables;
/*dropping the tables*/
drop table devicePositiveResult;
drop table contactDetails;
drop table notifyDeviceDetails;
drop table mobileDeviceHashDetails;
drop table agencyTestResults;

/*creating the mobile Device hash details table*/
create table if not exists mobileDeviceHashDetails (
mobileDeviceHash varchar(11) not null primary key
);

/*creating the agency test results table*/
create table if not exists agencyTestResults
(testResultHash varchar(50) not null primary key,
resultDate int not null,
result boolean not null);

/*creating the contact Details table*/
create table if not exists contactDetails
(mobileDeviceHash varchar(11) not null, 
recordContactHash varchar(11) not null, 
recordDate int not null, 
recordTime int not null, 
primary key(mobileDeviceHash, recordContactHash, recordDate),
FOREIGN KEY (mobileDeviceHash) REFERENCES mobileDeviceHashDetails(mobileDeviceHash)
);				

/*creating the devicePositive Result table*/
create table if not exists devicePositiveResult
(mobileDeviceHash varchar(11) not null,
positiveHash varchar(50) not null primary key,
FOREIGN KEY (mobileDeviceHash) REFERENCES mobileDeviceHashDetails(mobileDeviceHash),
FOREIGN KEY (positiveHash) REFERENCES agencyTestResults(testResultHash));
 
/*creating the notifyDevice Details table*/
create table if not exists notifyDeviceDetails(
	mobileDeviceHash varchar(11),
    notificationDate int,
	coronacontactDevice varchar(11),
	positiveHash varchar(50),
primary key(mobileDeviceHash, positiveHash),
FOREIGN KEY (mobileDeviceHash) REFERENCES mobileDeviceHashDetails(mobileDeviceHash),
FOREIGN KEY (positiveHash) REFERENCES agencyTestResults(testResultHash)
);