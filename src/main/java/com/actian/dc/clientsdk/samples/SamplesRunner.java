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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main driver used to manage execution of all of the samples
 */
public class SamplesRunner 
{
    private static final Logger logger = LogUtil.getLogger(SamplesRunner.class);
        
    static final String ARTIFACTS_PATH = new File("target/runtime/artifacts").getAbsolutePath();
     
    static String artifactPath(String name) {
        return ARTIFACTS_PATH+"/"+name;
    }
    
    static final String SAMPLE_PACKAGE_NAME = "Samples";
    static final String SAMPLE_PACKAGE_VERSION = "1.0";
    
    static String samplePackagePath() {
        String fullPackageName = SAMPLE_PACKAGE_NAME + "-" + SAMPLE_PACKAGE_VERSION + ".djar";
        return artifactPath(fullPackageName);
    }
    
    static final String SAMPLE_DATA_MACRO_NAME = "samples";
    
    static final String SAMPLE_DATA_MACRO_VALUE = new File("target/runtime/data").getAbsolutePath();
              
    private static final TaskBuilder taskBuilder;
    
    static {
        Map<String, String> macros = new HashMap<>();
        macros.put(SAMPLE_DATA_MACRO_NAME, SAMPLE_DATA_MACRO_VALUE);        
        taskBuilder = new TaskBuilder(SAMPLE_PACKAGE_NAME, SAMPLE_PACKAGE_VERSION, macros);
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
        List<ConnectionUser> samples = new ArrayList<>();   
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
            logger.log(Level.INFO, "Starting {0}", sampleName);
            boolean ok = sample.useConnection(cxnBuilder);
            String status = ok ? "OK" : "ERROR";
            logger.log(Level.INFO, "{0} finished {1}\n", new String[]{sampleName, status});
            if (!ok) {
                break;
            }
        }
    }
}
