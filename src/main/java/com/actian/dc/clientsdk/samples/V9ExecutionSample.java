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

/**
 * Execute a v9 task synchronously.  
 */
public class V9ExecutionSample extends ExecutionConnectionUser
{
           
    static final String PACKAGE_NAME = "V9Samples";
    static final String PACKAGE_VERSION = "1.0";
    static final String ENTRYPOINT = "V9Samples-1.0/m_SimpleMap.tf.xml";
    
    /**
     * @throws com.pervasive.di.client.sdk.SDKException
     * @see com.actian.dc.clientsdk.samples.ExecutionConnectionUser#useConnection(com.pervasive.di.client.sdk.ExecutionConnection) 
     */
    @Override
    public boolean useConnection(ExecutionConnection cxn) throws SDKException 
    {
        
        // Demonstrates that a runtime configuration can be created dynamically
        // and used to populate a new Task instance.
        RuntimeConfig config = new RuntimeConfig();
        config.setName("Package With V9 Artifacts");
        config.setPackageName(PACKAGE_NAME);
        config.setPackageVersion(PACKAGE_VERSION);
        config.setEntryPoint(ENTRYPOINT);
        
        // Deviates from other samples in that this one creates its own Task
        // instance rather than calling a method exposed in the SamplesRunner.
        Task task = new Task();
        task.populate(config);
        task.addMacro(new NameValuePair(SamplesRunner.sampleDataMacroName, SamplesRunner.sampleDataMacroValue));

        Job job = cxn.submit(task, false);
        switch (job.getJobStatus())
        {
        case FINISHED_OK:
            logger.info("V9 Job Completed Successfully");
            break;

        case FINISHED_ERROR:
            logger.info("V9 Job Completed unsuccessfully");
            if (job.getResult().getErrorMessage() != null)
                if ( !job.getResult().getErrorMessage().isEmpty())
                logger.info(job.getResult().getErrorMessage());
            break;
        default:
            logger.info("V9 Job Status: "+job.getJobStatus().toString());
            break;
        }
        
        return reportResult(job, cxn);
    }
}
