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

import com.pervasive.di.client.sdk.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Executes a task asynchronously, using wait/notify to determine when the task is complete.
 */
public class ThreadedAsyncExecutionSample extends ExecutionConnectionUser
{
    /**
     * @throws com.pervasive.di.client.sdk.SDKException
     * @see com.actian.dc.clientsdk.samples.ExecutionConnectionUser#useConnection(com.pervasive.di.client.sdk.ExecutionConnection) 
     */
    @Override
    public boolean useConnection(ExecutionConnection cxn) throws SDKException
    {
        // Create a new Task and feed it the RTC
        Task task = SamplesRunner.sampleTask("Samples.process.rtc");
        LOGGER.log(Level.INFO, "Submitting task {0}", task.getTaskName());
        JobListener listener = new Listener(LOGGER);
        Job job = null;

        // Wait until the listener says it's ok to proceed
        try {
            synchronized(this) {
                job = cxn.submit(task, listener);
                wait();
            }
        } catch (InterruptedException e) {
        }

        // Report results
        return reportResult(job, cxn);
    }
    
    /**
     * Custom com.pervasive.di.client.sdk.JobListener instance which overrides
     * the jobProgress method to notify the waiting thread that the job has finished.
     */
    public class Listener extends SimpleJobListener
    { 
        public Listener(Logger logger) {
            super(logger);
        }

        /**
         * Overridden to notify the waiting thread to wake up and finish.
         * @see com.actian.dc.clientsdk.samples.SimpleJobListener#jobProgress(com.pervasive.di.client.sdk.JobProgress) 
         */
        @Override
        public void jobProgress(JobProgress progress) {
            super.jobProgress(progress);
            if (isFinished()) {                
                synchronized(ThreadedAsyncExecutionSample.this) {
                    ThreadedAsyncExecutionSample.this.notify();
                }
            }
        }
    }
}
