# flume-spark-weblog
Log analysis using Flume, Spark streaming &amp; saving results to S3

## Objective

To build an application which can monitor user browsing history continously using Spark and save results to S3

## Data description

1. Data is a streaming log data which comprises of login, browsing details of an e-commerce website.

2. Consists information of IP address, categories, browser used, operating system specifications etc

   ![alt text](images/output1.png)
   
## Tools & Technologies used

Best suited technologies:

1. Apache Flume

2. Apache Spark

3. Amazon S3

## EMR Cluster details

EMR version: 5.10.0

Cluster name: Bootcamp_Spark2

DNS: ec2-34-219-135-32.us-west-2.compute.amazonaws.com

Key pair: ******.ppk, ******.pem

## Installation steps and commands

### Flume installation

1. Download flume using below command:

   wget http://archive.apache.org/dist/flume/1.7.0/apache-flume-1.7.0-bin.tar.gz

2. Untar the downloaded file

   tar -xzvf apache-flume-1.7.0-bin.tar.gz

3. Move the extracted file to /opt

   sudo mv apache-flume-1.7.0-bin /opt

4. Add Flume to Path in user bash profile file

   sudo nano ~/.bash_profile

   export FLUME_HOME="/opt/apache-flume-1.7.0-bin"
   export PATH=$PATH:$FLUME_HOME/bin

   // Use source command to the update the values of environment variables

   source ~/.bash_profile

5. Navigate to flume home directory

   cd /opt/apache-flume-1.7.0-bin/

6. Copy sample template flume environment file to "flume-env.sh" file for putting some custom environment configurations

   cp conf/flume-env.sh.template conf/flume-env.sh

7. Open flume-env.sh and configure Java variables

   sudo nano conf/flume-env.sh

   // Uncomment the below statements and add JDK path to JAVA_HOME variable

   JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk

   JAVA_OPTS="-Xms100m -Xmx200m -Dcom.sun.management.jmxremote"

8. Confirm flume installation using below command:

   flume-ng version

### Git installation

sudo yum install git

git --version

### Logs utility installation

git clone https://github.com/NavyaSreeKanakala/generating_logs.git

cd generating_logs/logs_utility/

ls -lrt

sudo mv -f gen_logs/ /opt

cd

rm -rf generating_logs  //remove git hub repository

ls -lrt /opt

ls -lrt /opt/gen_logs

// Creation of soft links to avoid directory navigation

sudo ln -s /opt/gen_logs/start_logs.sh /usr/bin/start_logs.sh

sudo ln -s /opt/gen_logs/stop_logs.sh /usr/bin/stop_logs.sh

sudo ln -s /opt/gen_logs/tail_logs.sh /usr/bin/tail_logs.sh

// Now one can start logs generating script from any location

### SBT installation

1. Download SBT .tgz file from the command below

   wget https://piccolo.link/sbt-1.1.5.tgz
 
2. Extract it using the following command

   tar -zxvf sbt-1.1.5.tgz

3. Move the extracted folder to local directory
 
   sudo mv sbt /usr/local

4. Edit the profile file by typing

   sudo vi /etc/profile

5. Add the following line
 
   export PATH=$PATH:/usr/local/sbt/bin

6. Exit from VI editor and type the following command to apply changes
 
   source /etc/profile 

7. Use below command to check whether SBT has been installed successfully or not
 
   sbt about

## Flume and Spark integration

JARs link: 

https://mvnrepository.com/artifact/org.apache.spark/spark-streaming-flume-sink_2.11/2.2.0
           
https://mvnrepository.com/artifact/org.apache.spark/spark-streaming-flume_2.11/2.2.0
           
http://search.maven.org/remotecontent?filepath=org/scala-lang/scala-library/2.11.8/scala-library-2.11.8.jar
           
http://search.maven.org/remotecontent?filepath=org/apache/commons/commons-lang3/3.5/commons-lang3-3.5.jar
	

// Open a new terminal and follow below instructions

1. Copy below jars to default flume location

```
cp scala-library-2.11.8.jar /opt/apache-flume-1.7.0-bin/lib/
   
cp spark-streaming-flume_2.11-2.2.0.jar /opt/apache-flume-1.7.0-bin/lib/
   
cp spark-streaming-flume-sink_2.11-2.2.0.jar /opt/apache-flume-1.7.0-bin/lib/	
   
cp commons-lang3-3.5.jar /opt/apache-flume-1.7.0-bin/lib/ 
```

2. vi flume.conf

```
# sdc.conf: A multiplex flume configuration
# Source: log file
# Sink 1: Unprocessed data to HDFS
# Sink 2: Spark

# Name the components on this agent
sdc.sources = ws
sdc.sinks = hd spark
sdc.channels = hdmem sparkmem

# Describe/configure the source
sdc.sources.ws.type = exec
sdc.sources.ws.command = tail -F /opt/gen_logs/logs/access.log

# Describe the sink
sdc.sinks.hd.type = hdfs
sdc.sinks.hd.hdfs.path = /user/hadoop/flume_demo

sdc.sinks.hd.hdfs.filePrefix = FlumeDemo
sdc.sinks.hd.hdfs.fileSuffix = .txt
sdc.sinks.hd.hdfs.rollInterval = 120
sdc.sinks.hd.hdfs.rollSize = 1048576
sdc.sinks.hd.hdfs.rollCount = 100
sdc.sinks.hd.hdfs.fileType = DataStream

sdc.sinks.spark.type = org.apache.spark.streaming.flume.sink.SparkSink
sdc.sinks.spark.hostname = ip-172-31-19-34
sdc.sinks.spark.port = 8123

# Use a channel sdc which buffers events in memory
sdc.channels.hdmem.type = memory
sdc.channels.hdmem.capacity = 1000
sdc.channels.hdmem.transactionCapacity = 100

sdc.channels.sparkmem.type = memory
sdc.channels.sparkmem.capacity = 1000
sdc.channels.sparkmem.transactionCapacity = 200


# Bind the source and sink to the channel
sdc.sources.ws.channels = hdmem sparkmem
sdc.sinks.hd.channel = hdmem
sdc.sinks.spark.channel = sparkmem
```

3. Run logs generation script

   sudo sh start_logs.sh

4. Run your flume agent

   flume-ng agent -n sdc -f flume.conf


// Now open another new terminal and create a directory structure to build SBT application

1. sudo -i > mkdir retail > cd retail

   Place all the 4 jars in /root directory

2. vi build.sbt

```

name := "retail"
version := "1.0"
scalaVersion := "2.11.8"

libraryDependencies += "org.apache.spark" % "spark-core_2.11" % "2.2.0"
libraryDependencies += "org.apache.spark" % "spark-streaming_2.11" % "2.2.0"
libraryDependencies += "org.apache.spark" % "spark-streaming-flume_2.11" % "2.2.0"
libraryDependencies += "org.apache.spark" % "spark-streaming-flume-sink_2.11" % "2.2.0"
libraryDependencies += "org.scala-lang" % "scala-library" % "2.11.8"
libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.5"

```
press [esc] > :wq

3. mkdir src > cd src > mkdir main > cd main > mkdir scala > cd scala

Q1. Count the number of department categories accessed in every 10 secs of time	

Q2. Display number of times hosts who made requests to the server in every 10 secs

Q3. Find Total count of different response codes returned by the server per 10 secs

vi FlumeStreamingDepartmentCount.scala

```
FlumeStreamingDepartmentCount.scala
```
press [esc] > :wq

4. cd .. > cd .. > cd..

pwd

/root/retail/

5. Run below command to package the application

   sbt package

6. Now run below spark submit command after packaging the jar

   spark-submit --class FlumeStreamingDepartmentCount --master yarn --jars spark-streaming-flume-sink_2.11-2.2.0.jar,
   commons-lang3-3.5.jar,scala-library-2.11.8.jar --packages org.apache.spark:spark-streaming-flume_2.11:2.2.0 
   /home/ec2-user/retail/target/scala-2.11/retail_2.11-1.0.jar yarn-client ip-172-31-19-34 8123

## Screenshots

1. Output saved to S3

   ![alt text](images/output2.png)
   
   ![alt text](images/output3.png)

1. Count the number of department categories accessed in every 10 secs of time	

   ![alt text](images/output4.png)
   
   ![alt text](images/output5.png)

2. Display number of times hosts who made requests to the server in every 10 secs

   ![alt text](images/output6.png)

3. Find Total count of different response codes returned by the server per 10 secs

   ![alt text](images/output7.png)

## Future Possibilities

As output data is being stored continuously in S3, 
 
  1. We can connect this data to Web UIs like Splunk and generate dashboards
  
  2. We can send product recommendations to customers based on their search history

## Most common errors

// Issue 1

ERROR ReceiverTracker: Deregistered receiver for stream 0: Error starting receiver 0 - java.io.IOException: Error connecting to /127.0.0.1:8123

// Solution 1: hostname must be ip-172-31-18-163 instead of localhost/127.0.0.1

// Issue 2

Unable to find Sink org.apache.spark.streaming.flume.sink.SparkSink

// Solution 2: Copy the jars to default flume location

cp scala-library-2.11.8.jar /opt/apache-flume-1.7.0-bin/lib/
cp spark-streaming-flume_2.11-2.2.0.jar /opt/apache-flume-1.7.0-bin/lib/
cp spark-streaming-flume-sink_2.11-2.2.0.jar /opt/apache-flume-1.7.0-bin/lib/	
cp commons-lang3-3.3.2.jar /opt/apache-flume-1.7.0-bin/lib/

## References

https://spark.apache.org/docs/2.2.0/streaming-flume-integration.html
