CS643
Programming Assignment Project 1
Author: Christian Carpena

---1. Git Repo

https://github.com/vkchadha12/cs643 

---2. AWS Create EC2 Instances 

$ksh -x ec2.create_instances

Keep the instance name 
example :- "i-0e942fb0ddc723789", "i-0519149bc27b26646"

--3. AWS Console Steps

create PEM file, via the ec2 console on AWS

--Run the following command to set the correct permissions for the .pem file:

$ chmod 400 i-0e942fb0ddc723789.pem

$ chmod 400 i-0519149bc27b26646.pem

--4. Connect to each instance using "Public IPv4 DNS"

install java

$sudo yum install java-17-amazon-corretto-headless
$sudo yum install java-17-amazon-corretto
$sudo yum install java-17-amazon-corretto-devel
$sudo yum install java-17-amazon-corretto-jmods

$ sudo /usr/sbin/alternatives --config java
$sudo yum install git
$sudo yum install ksh

-- 5. Git Clone 

$git clone https://github.com/vkchadha12/cs643

---6. AWS Setup

On each of the ec2 instances  create .aws/credentials

$ksh -x ec2.create_cred_template

Credentials file should be of the following format
$vi $
[default]
aws_access_key_id=
aws_secret_access_key=
aws_session_token=

--7 Setting up the application config file

Set the following variables 

cs_643/project_1/CS643_Project_1/config.json
{
"AWS_QUEUE_NAME" : "vc35_queue_1.fifo",
"AWS_S3_BUCKET" : "cs643-njit-project1",
"AWS_DEFAULT_REGION" :"Region.US_EAST_1",
"OUTPUT_FILE" : "output.txt",
"THRESHOLD" : 80.0
}


--8 Running the application 
Application runs in 2 mode publisher mode & receive mode

on first instance "i-0e942fb0ddc723789" 
run in publisher mode using the following scripts. 

$ksh -x CS643_Project_1/run_publisher.sh
java -jar target/com.njit.cs643.project1-1.0-SNAPSHOT-jar-with-dependencies.jar publisher config.json

In this mode the application will read the images from the s3 bucket and classify the images of car and publish to the queue define on QUEUE

on second  instance "i-0519149bc27b26646"

Run the application in receiver mode 

$ksh -x CS643_Project_1/run_receiver.sh
java -jar target/com.njit.cs643.project1-1.0-SNAPSHOT-jar-with-dependencies.jar receiver config.json

In this mode the application will read the images from the queue which are cars , read the text on them
& write it to the output file 

