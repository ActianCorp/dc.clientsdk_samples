/*
 * Copyright 2019 Actian Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.actian.dc.clientsdk.samples;

import com.pervasive.di.client.sdk.SDKException;
import com.pervasive.di.client.sdk.Task;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Main driver used to manage execution of all of the samples
 */
public class SamplesRunner 
{
    private static final Logger logger = LogUtil.getLogger(SamplesRunner.class);
        
    static final String artifactsPath = new File("target/runtime/artifacts").getAbsolutePath();
     
    static String artifactPath(String name) {
        return artifactsPath+"/"+name;
    }
    
    static final String samplePackageName = "Samples";
    static final String samplePackageVersion = "1.0";
    
    static String samplePackagePath() {
        String fullPackageName = samplePackageName + "-" + samplePackageVersion + ".djar";
        return artifactPath(fullPackageName);
    }
    
    static final String sampleDataMacroName = "samples";
    
    static final String sampleDataMacroValue = new File("target/runtime/data").getAbsolutePath();
              
    private static TaskBuilder taskBuilder;
    
    static {
        Map<String, String> macros = new HashMap<String, String>();
        macros.put(sampleDataMacroName, sampleDataMacroValue);        
        taskBuilder = new TaskBuilder(samplePackageName, samplePackageVersion, macros);
    }
    
    /**
     * Convenience method which creates a new Task using the runtime configuration file
     * referenced by the string argument
     * @param rtcName Name of the source runtime configuration
     * @return com.pervasive.di.client.sdk.Task instance
     * @throws com.pervasive.di.client.sdk.SDKException if an error occurs while creating the task
     */
    static Task sampleTask(String rtcName) throws SDKException {
        if (rtcName == null) {
            return taskBuilder.buildTask();
        }
        else {
            File rtcFile = new File(artifactPath(rtcName));                
            Task task = taskBuilder.buildTask(rtcFile);
            task.setName("Run sample project using configuration "+rtcName);
            return task;
        }        
    }
    
    /**
     * The entry point used to drive execution of the samples.  Optionally accepts
     * a single command line argument which represents the name of a single sample
     * class.
     * @param args container of the command line arguments
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception
    {
        List<ConnectionUser> samples = new ArrayList<ConnectionUser>();   
        if (args!=null && args.length > 0 && args[0].trim().length() > 0) {
            Class<?> clazz = null;
            String sampleClassToRun = args[0].trim();
            try {
                clazz = Class.forName(sampleClassToRun);
            } catch (ClassNotFoundException ex) {
                // didn't find the class using the name provided.  Maybe the
                // package was left off.  Check to see if any dots are in the
                // name.  If not, add the current package and try again.
                if (!sampleClassToRun.contains(".")) {
                    String pkgName = SamplesRunner.class.getPackage().getName();
                    clazz = Class.forName(pkgName + "." + sampleClassToRun);
                }
                else
                    throw ex;
            }
            // Add only the provided sample for execution
            // Assumes instance of ConnectionUser and null public constructor
            samples.add((ConnectionUser)clazz.newInstance());
        }
        else {
            // Queue all samples for execution
            samples.add(new V9ExecutionSample());
            samples.add(new SyncExecutionSample());
            samples.add(new AsyncExecutionSample());
            samples.add(new ThreadedAsyncExecutionSample());
            samples.add(new ExecutionListenerSample());
        }
        
        // Create a ConnectionBuilder and then execute each by calling the
        // sample's useConnection() method.
        ConnectionBuilder cxnBuilder = new ConnectionBuilder();        
        for (ConnectionUser sample : samples) {
            String sampleName = sample.getClass().getSimpleName();
            logger.info("Starting " + sampleName);
            boolean ok = sample.useConnection(cxnBuilder);
            String status = ok ? "OK" : "ERROR";
            logger.info(sampleName + " finished " +status+"\n");
            if (!ok) {
                break;
            }
        }
    }
}
