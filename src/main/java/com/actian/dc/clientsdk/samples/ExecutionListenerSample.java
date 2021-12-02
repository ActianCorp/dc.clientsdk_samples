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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Executes multiple tasks asynchronously.
 * Uses inactivity on a shared queue of job progress events to determine when to shut down.
 */
public class ExecutionListenerSample extends ExecutionConnectionUser
{
    
    /**
     * @throws com.pervasive.di.client.sdk.SDKException
     * @see com.actian.dc.clientsdk.samples.ExecutionConnectionUser#useConnection(com.pervasive.di.client.sdk.ExecutionConnection) 
     */
    @Override
    public boolean useConnection(ExecutionConnection cxn) throws SDKException
    {
        try
        {
            // Instantiate the listener and start the listening thread
            QueueListener listener = new QueueListener(LOGGER);
            Thread thread = new Thread(listener);
            // this prevents the main thread from exiting before the listener is done
            thread.setDaemon(false);
            // fire it up before we start submitting tasks
            thread.start();

            // submit the job a whole bunch of times
            // you could also submit a bunch of different tasks
            for (int i=0; i<25; i++) {
                // for this example we are submitting the same task repeatedly
                // but you could also load a set of tasks to submit,
                // or change the datasets or runtime configurations
                Task task = SamplesRunner.sampleTask("Samples.process.rtc");
                Job job = cxn.submit(task, listener);
                listener.addJob(job);
            }
            
            // once submissions are done just idle
            // so we don't disconnect before the listener is finished listening
            while (!listener.isFinished())
                Thread.sleep(1000);
            return true;
        }
        catch (InterruptedException e) {
            LOGGER.severe(e.getMessage());
        }
        return false;
    }
    
    private static class QueueListener implements JobListener, Runnable
    {
        private final Map<String, Job> jobmap = new HashMap<>();
        private final BlockingQueue<JobProgress> myqueue = new LinkedBlockingQueue<>();
        private int queuesize=0;
        private boolean finished = false;
        
        private final Logger logger;
        
        QueueListener(Logger logger) {
            this.logger = logger;
        }

        // the callback for the JobListner class
        // called by the connection when a STOMP message is receieved
        @Override
        public synchronized void jobProgress(JobProgress progress)
        {
            // just add it to the queue
            myqueue.add(progress);
        }
        
        // called by the parent app to place the job into the listener's job map
        synchronized void addJob(Job job)
        {
            logger.log(Level.INFO, "Adding job {0} to queue [{1}]", new Object[]{job.getJobId(), ++queuesize});
            jobmap.put(job.getJobId(), job);
        }
        
        /**
         * The following method encapsulates the code which implements the listening thread.  
         * This gets started before the jobs are added
         */
        @Override
        public void run()
        {
            JobProgress progress = null;
            while (!isFinished())
            {
                try
                {
                    // this is the place where you configure the timeout
                    progress = pollRepeatedly(5, 12); // 5*12 = 60 seconds - max one minute of waiting
                }
                catch (InterruptedException e)
                {
                    break;
                }
            
                // if it breaks out of the timeout it will get here with no progress object
                if (progress == null) {
                    markFinished();
                }
                
                // just a sanity check to make sure we don't get a null pointer exception
                else if (progress.getEventName() != null)
                {
                    // if the event says the job has ended remove it from the queue
                    if (progress.getEventName() == JobEventName.JOB_ENDED)
                    {
                        jobmap.remove(progress.getJobId());
                        logger.log(Level.INFO, "Removing job {0} from queue [{1}]: {2}",
                                new Object[]{progress.getJobId(), --queuesize, progress.getJobStatusCode().toString()});
                    }
                }
            }

            // if we exited before the job map was completely cleared
            // then there are jobs still in the queue that didn't get reported as finished
            if (!jobmap.isEmpty())
                for (Map.Entry pairs : jobmap.entrySet())
                    logger.log(Level.INFO, "TIMEOUT: Job {0} did not finish [{1}]",
                            new Object[]{pairs.getKey(), --queuesize});
            myqueue.clear();
        }
        
        private JobProgress pollRepeatedly(int secondsToWait, int retries) throws InterruptedException {
            for (int i=0; i<retries; ++i) {
                JobProgress progress = myqueue.poll(secondsToWait, TimeUnit.SECONDS);
                if (progress != null) {
                    return progress;
                }
                else {
                    int remaining = secondsToWait * (retries-i);
                    logger.log(Level.INFO, "Countdown to shutdown: {0} seconds", remaining);
                }
            }
            return null;
        }

        // called by the parent thread to see if we're done yet
        private synchronized void markFinished() {
            finished = true;
        } 
        
        // called by the parent thread to see if we're done yet
        synchronized boolean isFinished() {
            return finished;
        }
    }
}
