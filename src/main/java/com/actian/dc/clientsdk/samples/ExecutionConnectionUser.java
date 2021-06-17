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
import com.pervasive.di.client.sdk.JobServiceReturnCode;
import com.pervasive.di.client.sdk.SDKException;
import java.util.logging.Logger;

/**
 * Common base type that all execution samples extend.
 * Implements the base ConnectionUser interface.
 * Handles connection setup and cleanup
 */
abstract class ExecutionConnectionUser implements ConnectionUser
{
    protected Logger logger;
    
    /**
     * Returns true if the sample supports local execution.  The default value
     * is true.
     * @return true if local execution is supported, false otherwise.
     */
    @Override
    public boolean supportsLocal() {
        return true;
    }
    
    /**
     * Called to create, and track completion of a job using the provided ExecutionConnection
     * instance
     * @param cxn instance of ExecutionConnection
     * @return true if the job completed successfully, false otherwise
     */
    abstract boolean useConnection(ExecutionConnection cxn) throws SDKException;

    /**
     * 
     * @see com.actian.dc.clientsdk.samples.ConnectionUser#useConnection(com.actian.dc.clientsdk.samples.ConnectionBuilder) 
     */
    @Override
    public boolean useConnection(ConnectionBuilder cxnBuilder)
    {
        logger = LogUtil.getLogger(getClass());

        ExecutionConnection cxn = null;
        try
        {
            cxn = cxnBuilder.createExecutionConnection();
            return useConnection(cxn);
        }
        catch (SDKException e) {
            logger.severe(e.getMessage());
            return false;
        }
        finally {
            // disconnect from the service
            if (cxn != null) {
                cxn.disconnect();
            }
        }
    }
    
    /**
     * Called to log job results and return true if the job completed successfully
     * @param job com.pervasive.di.client.sdk.Job instance representing the job that was executed
     * @param cxn com.pervasive.di.client.sdk.ExecutionConnection instance through which the job was executed
     * @return true if the job succeeded, false otherwise
     */
    protected boolean reportResult(Job job, ExecutionConnection cxn) 
    {
        // Display any Error Message
        if (job.getResult().getErrorMessage() != null)
            logger.info(job.getResult().getErrorMessage());

        try {
            // Get the job log
            logger.info(cxn.getLog(job));
            return (job.getResult().getServiceReturnCode() == JobServiceReturnCode.SUCCEEDED);
        } catch (Exception e) {
            logger.severe(e.getMessage());
            return false;
        }
    }
}
