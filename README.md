
# DataConnect Client SDK Samples
---

This project provides code samples that demonstrate how to use the DataConnect Client SDK. 

The DataConnect Client SDK assumes you have already developed, tested and packaged an integration using the [DataConnect Studio IDE](https://www.actian.com/data-integration/dataconnect-integration/). In addition to using the Runtime Engine command line interface or Integration Manager to execute integrations, you can use the Client SDK to dynamically submit jobs synchronously or asynchronously, monitor run-time events, and get job status via the Runtime Engine. 

All samples in this project use the same provided DataConnect integration package and demonstrate:

* Synchronous execution
* Asynchronous execution using polling to determine completion
* Asynchronous execution using wait/notify to determine completion
* Asynchronous execution with a progress listener

---
## Prerequisites 

Before running these samples, you will need:

1. Apache Maven 3.8.0 or higher [maven.apache.org](https://maven.apache.org/)
2. JDK version 11. Note: a JRE is not sufficient to build the samples 
3. Licensed DataConnect 12.0.0 installation with either the DataConnect Studio IDE or a standalone DataConnect Runtime Engine
   
Note: the sample code must be on the same machine as the DataConnect install.

---
## Preparing Your Environment

The Client SDK samples project builds and executes using Apache Maven.

1. Start a command shell
1. Ensure that the Apache Maven bin directory is in the path using the command **`mvn -v`** to print the maven version, Java version, and other relevant info
1. Add the **`runtime/di9`** directory relative to the DataConnect engine installation to your **`PATH`** environment variable
1. Set the **`DJLIB`** environment variable to the location of the directory that contains your **`cosmos.ini`** file.  This will typically be in **`\ProgramData\Actian\DataConnect\dc-rcp-64-bit-<version>`** on Windows
1. From the command shell, run `djengine -?` to verify a valid license file and to see command line Help

Note: for more information about installing and configuring the DataConnect Runtime Engine, see the Help topic of the same name in the [DataConnect User Guide on-line documentation](http://docs.actian.com/).

---
## Building And Running the Samples

After you have completed the setup above, open a command shell and change directory to the root of the dc.clientsdk_samples project.

To build and execute all samples, run the following command: **`mvn verify`**
	
To build and execute a single sample, specify the name of the sample: **`mvn verify -Dsample.to.run=SyncExecutionSample`**

---
## Verifying Sample Results

* The console will display the integration project name, FINISHED_OK, and BUILD SUCCESSFUL messages
* A target file created by the integration package is created under the folder  **`target/runtime/data`** 
* A Client SDK log is created under **`target/work/log`** 
* An Engine execution log is created under **`target/work/log/ec`**

---
## Key Concepts

### ExecutionConnection Object

The ExecutionConnection object represents the service itself. The ExecutionConnection interface, whether remote or local, is the main point of contact with the service.  Use the ExecutionConnection object to submit or schedule a Task for execution. A Task is typically a package containing a process or transformation.

### Task Object

Information about the package is contained in the Task object. Minimally, a Task must contain all of the execution parameters required to execute the process or transformation, such as the package name, a package version, and an entry point. Some of the parameters are part of the Task object itself, while other information is in separate classes referenced by the Task.  
  
A Task may contain information which augments or overrides settings within the package. For example, a run-time configuraton may not have a package name or version set. Alternately, you may need to override some of the information in the runtime configuration. In either case, you must populate the task from the run-time configuration and then make any additions or changes with the Task object.  After you populate the Task object, submit it to the Execution Service using one of the Connection object's submit methods.  
  
There are two overloads for the Connection's submit methods, both of which return a Job object.  The first overload takes a Task object and a Boolean flag to indicate whether the Task should be submitted asynchronously (true) or synchronously (false). The second overload takes a Task object and a JobListener object through which notifications are sent from the Execution Service.

### Job Object 

The Job object represents a submitted Task. The Job object provides the execution status, ID, access to the log, profile output, execution results, and other statistics.

### Threading Applications

The execution features are multithreaded. In particular, each task is submitted asynchronously, and all of the progress updates happen on a single thread.

### Synchronous Execution

If you submit a task for synchronous execution, the method call does not block other tasks. However, it does block that particular thread in the application. Because execution is inherently asynchronous, the Connection waits for the notification that the job is complete.
 
Some tasks might execute very quickly, while others might require more time. For this reason, the interval at which the Connection polls the service for status is configurable through the get and set methods of the Task object.

### Asynchronous Execution

When you submit a task for asynchronous execution, the method returns as soon as the service has acknowledged that the task is submitted. If you like, create a JobListener to receive status updates on jobs as they occur.  
  
You can create an object that implements the JobListener interface, and pass the listener in with the Task to the Connection's submit method. In this scenario, the Listener's jobProgress method is called by the Execution Service passing in a JobProgress object.  
  
Listeners are useful when submitting asynchronous tasks which may take varying amounts of time to complete.  Check the JobStatusCode associated with the JobProgress object to determine if the job has completed.

### Execution Specifics

The main interface is ExecutionConnection. This class and the other classes and exceptions are reusable without needing extension. Other helper classes, such as enumerations and JobProgress, are reusable for the execution features.  
  
The Task class specifies the request and is used by the Connection classes to provide the engine, through an engine and engine service class, with all data needed to return a Job. Create a Task object and submit it through the Connections object.   
  
A Job object returns the status of the task execution.  Depending on the whether the task was submitted synchronously or asynchronously, either the task is completed and the job is returned, or a job is returned once the task starts processing.   
  
The submit function is overloaded in order to provide this functionality and attach a JobListener interface to a Job.  In order to receive notifications from the Execution Service, the client application must implement the JobListener interface.   
  
Job status updates are attached to each engine instance and the engine uses those callbacks to provide updates through the Engine Service class. Job Status Codes are returned as one of the following strings: 

* QUEUED
* STARTED
* FINISHED_OK
* FINISHED_ERROR
* ABORTED
* UNKOWN

### Exceptions

Exceptions contain important error information useful for debugging. The execution exception types are based on the action being performed and include the following:
* ConnectException
* SubmitTaskException
* SDKException

---
## Project Structure
```
README.TXT:  This file
LICENSE:  Apache License 2.0
pom.xml:  Maven build script
src/main/artifacts:
  Samples-1.0.djar:  Package containing maps and processes used by the samples
  Samples.process.rtc:  Runtime configuration used to configure and load a process within the package
  Samples.map.rtc:  Runtime configuration used to configure and load a map within the package
  V9Samples-1.0.djar:  Package containing artifacts used by the V9 sample
  m_SimpleMap.map.xml:  Free-standing V9 map file used by the V9 sample
  m_SimpleMap.tf.xml:  Free-standing V9 transformation file used by the V9 sample
src/main/assemblies:
  stage-artifacts-and-data.xml:  Maven assembly used to stage sample artifacts and data for execution
src/main/data:
  invoices_src.txt:  Source data used by sample artifacts
  Accounts.txt:  Source data used by the sample artifacts
src/main/java/com/actian/dc/clientsdk/samples:
  SamplesRunner.java:  Main class used to execute all of the samples
  ConnectionBuilder.java:  Helper class used to build a Connection
  TaskBuilder.java:  Helper class used to build a task
  ConnectionUser.java: Provides type safety for the SamplesRunner to submit the samples
  ExecutionConnectionUser.java:  Implements the ConnectionUser interface and provides base behavior for the samples
  LogUtil.java:  Utility class used to implement logging for the samples
  SimpleJobListener.java:  Used by samples to demonstrate job progress events
  AsyncExecutionSample.java:  Sample which executes a task asynchronously, using polling to determine when task is complete
  ExecutionListenerSample.java:  Executes multiple tasks asynchronously.  Uses inactivity on a shared queue of job progress events to determine when to shut down.
  SyncExecutionSample.java:  Executes tasks synchronously.  
                             Shows different kinds of runnable entry points.
                             Maps require a runtime configuration to supply source and target datasets.
                             Processes can be run using a runtime configuration, but can also be used directly.
  ThreadedAsyncExecutionSample.java:  Executes a task asynchronously, using wait/notify to determine when task is complete.
  V9ExecutionSample.java:  Sample which demonstrates execution of V9 artifacts.  Also demonstrates configuration of the Task using a dynamically-created runtime configuration.
```
---
## Support

Free support is available for registered users of the [Actian Community](https://communities.actian.com/s/?_ga=2.42990309.1976004892.1553019528-1750363259.1548967681)  
Paid plans are also available at [Actian Support Services](https://www.actian.com/support-services/)



---
## Contributing

Actian Corporation welcomes contributions to the DataConnect Client SDK Samples project.  To contribute, please follow these steps:

* When submitting your pull request, please provide full contact info (Name, company, email, phone)
* Submit your pull request to the dev branch. We will review and test the requested change.  
* Once approved, we will perform the merge to dev.  Your change will be available immediately after our next merge to master.
