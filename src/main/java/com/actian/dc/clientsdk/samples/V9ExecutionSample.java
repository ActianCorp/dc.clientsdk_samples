/*
 * Copyright 2021 Actian Corporation
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

import com.pervasive.di.artifacts.runtimeconfig.RuntimeConfig;
import com.pervasive.di.artifacts.shared.NameValuePair;
import com.pervasive.di.client.sdk.ExecutionConnection;
import com.pervasive.di.client.sdk.Job;
import com.pervasive.di.client.sdk.SDKException;
import com.pervasive.di.client.sdk.Task;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Execute a v9 task synchronously.
 * 
 */
public class V9ExecutionSample extends ExecutionConnectionUser
{
           
    static final String PACKAGE_NAME = "V9Samples";
    static final String PACKAGE_VERSION = "1.0";
    static final String ENTRYPOINT = "m_SimpleMap.tf.xml";
    
    /**
     * This sample demonstrates execution of V9-style artifacts (XML).
     * This sample deviates from other samples in the following ways:
     * 1. Shows that a runtime configuration can be created dynamically and used
     * to populate a new Task instance.
     * 2. Creates its own Task instances rather than calling a method exposed
     * in the SamplesRunner.
     * @throws com.pervasive.di.client.sdk.SDKException
     * @see com.actian.dc.clientsdk.samples.ExecutionConnectionUser#useConnection(com.pervasive.di.client.sdk.ExecutionConnection) 
     */
    @Override
    public boolean useConnection(ExecutionConnection cxn) throws SDKException 
    {
        
        // Create tasks to execute sychronously
        List<Task> tasks = new ArrayList<>(3);
        
        // Configure a task that executes an artifacts in a package/djar
        RuntimeConfig config = new RuntimeConfig();
        config.setName("Execute packaged (in a djar) V9 artifacts");
        config.setPackageName(PACKAGE_NAME);
        config.setPackageVersion(PACKAGE_VERSION);
        config.setEntryPoint(ENTRYPOINT);
        config.addMacroDefinition(new NameValuePair(SamplesRunner.SAMPLE_DATA_MACRO_NAME, SamplesRunner.SAMPLE_DATA_MACRO_VALUE));
        Task tempTask = new Task();
        tempTask.populate(config);
        tasks.add(tempTask);
        
        // Configure a task which executes a V9 map directly (not in a package/djar)
        config = new RuntimeConfig();
        config.setName("Execute *.tf.xml directly (not in a package/djar)");
        config.setPackageName(null);  // Note that the package name is null
        config.setEntryPoint(SamplesRunner.artifactPath(ENTRYPOINT));
        config.addMacroDefinition(new NameValuePair(SamplesRunner.SAMPLE_DATA_MACRO_NAME, SamplesRunner.SAMPLE_DATA_MACRO_VALUE));
        tempTask = new Task();
        tempTask.populate(config);
        tasks.add(tempTask);
        
        for (Task task: tasks) {                
            LOGGER.log(Level.INFO, "Submitting task {0}", task.getTaskName());
            Job job = cxn.submit(task, false);
            switch (job.getJobStatus())
            {
            case FINISHED_OK:
                LOGGER.info("V9 Job Completed Successfully");
                break;

            case FINISHED_ERROR:
                LOGGER.info("V9 Job Completed unsuccessfully");
                if (job.getResult().getErrorMessage() != null)
                    if ( !job.getResult().getErrorMessage().isEmpty())
                    LOGGER.info(job.getResult().getErrorMessage());
                break;
            default:
                LOGGER.log(Level.INFO, "V9 Job Status: {0}", job.getJobStatus().toString());
                break;
            }

            if (!reportResult(job, cxn))
                return false;
        }
        
        return true;
    }
}
