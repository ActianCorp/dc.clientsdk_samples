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
import java.util.logging.Level;

/**
 * Executes a task asynchronously, using polling to determine when task is complete.
 */
public class AsyncExecutionSample extends ExecutionConnectionUser
{
    /**
     * Execute a job asynchronously and determine completion by polling a JobListener
     * instance every 3 seconds for completion
     * @param cxn ExecutionConnection instance 
     * @return true if the job completed successfully, false otherwise
     * @throws SDKException if an unexpected error occurs
     */
    @Override
    public boolean useConnection(ExecutionConnection cxn) throws SDKException
    {
        // Create a new Task and feed it the RTC
        Task task = SamplesRunner.sampleTask("Samples.map.rtc");

        // Execute the task asynchronously.
        // The SimpleJobListener will set the finished boolean to true when the job is done.
        SimpleJobListener listener = new SimpleJobListener(LOGGER);
        LOGGER.log(Level.INFO, "Submitting task {0}", task.getTaskName());
        Job job = cxn.submit(task, listener);

        try {
            // Wait until the listener says it's ok to proceed
            while (!listener.isFinished())
                Thread.sleep(3000);
        } catch(InterruptedException e) {
            LOGGER.severe(e.getMessage());
            return false;   
        }
        
        // Report results
        return reportResult(job, cxn);       
    }
}
