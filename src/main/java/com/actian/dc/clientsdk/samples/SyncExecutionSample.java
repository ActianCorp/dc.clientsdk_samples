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

import com.pervasive.di.client.sdk.ExecutionConnection;
import com.pervasive.di.client.sdk.Job;
import com.pervasive.di.client.sdk.SDKException;
import com.pervasive.di.client.sdk.Task;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Executes tasks synchronously.  
 * Shows different kinds of runnable entry points.
 * Maps require a runtime configuration to supply source and target datasets.
 * Processes can be run using a runtime configuration, but can also be 
 * used directly.
 */
public class SyncExecutionSample extends ExecutionConnectionUser
{
    /**
     * @throws com.pervasive.di.client.sdk.SDKException
     * @see com.actian.dc.clientsdk.samples.ExecutionConnectionUser#useConnection(com.pervasive.di.client.sdk.ExecutionConnection) 
     */
    @Override
    public boolean useConnection(ExecutionConnection cxn) throws SDKException 
    {
        // Create tasks to execute sychronously
        List<Task> tasks = new ArrayList<>(3);        
        // Add task for sample map, using a runtime configuration      
        tasks.add(SamplesRunner.sampleTask("Samples.map.rtc"));
        // Add task for sample process, using a runtime configuration
        tasks.add(SamplesRunner.sampleTask("Samples.process.rtc"));
        // Add task for sample process, specifying entry point directly
        tasks.add(sampleTaskNoConfig()); 
        
        for (Task task: tasks) {
            LOGGER.log(Level.INFO, "Submitting task {0}", task.getTaskName());
            Job job = cxn.submit(task, false);
            switch (job.getJobStatus())
            {
            case FINISHED_OK:
                LOGGER.info("Job Completed Successfully");
                break;

            case FINISHED_ERROR:
                LOGGER.info("Job Completed unsuccessfully");
                if (job.getResult().getErrorMessage() != null)
                    if ( !job.getResult().getErrorMessage().isEmpty())
                    LOGGER.info(job.getResult().getErrorMessage());
                break;
            default:
                LOGGER.log(Level.INFO, "Job Status: {0}", job.getJobStatus().toString());
                break;
            }
            
            // Report results
            boolean ok = reportResult(job, cxn);
            if (!ok) return false;
        }
        return true;        
    }
    
    private Task sampleTaskNoConfig() throws SDKException {
        Task task = SamplesRunner.sampleTask(null);
        task.setName("Run sample process using entry point");
        task.setEntryPoint("Samples-1.0/process_map_invoices_ascii_to_ascii_p.process");
        return task;
    }
}
