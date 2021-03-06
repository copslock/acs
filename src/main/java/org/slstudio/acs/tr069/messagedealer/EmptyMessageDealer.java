package org.slstudio.acs.tr069.messagedealer;

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slstudio.acs.tr069.databinding.TR069Message;
import org.slstudio.acs.tr069.exception.TR069Exception;
import org.slstudio.acs.tr069.fault.TR069Fault;
import org.slstudio.acs.tr069.job.IDeviceJob;
import org.slstudio.acs.tr069.session.context.ITR069MessageContext;
import org.slstudio.acs.util.Tuple;
import org.slstudio.acs.util.Tuple.Tuple2;

/**
 * Created with IntelliJ IDEA.
 * User: chandler
 * Date: 13-4-30
 * Time: ����12:51
 */
public class EmptyMessageDealer extends AbstractMessageDealer {
    private static final Log log = LogFactory.getLog(EmptyMessageDealer.class);

    @Override
    protected TR069Message convertToTR069Message(SOAPEnvelope envelope) throws TR069Exception {
        return null;
    }

    @Override
    protected TR069Message dealMessage(ITR069MessageContext context, TR069Message request) throws TR069Fault {
        //get device key
        String deviceKey = getDeviceKey(context.getTR069SessionContext());

        //first handing running job
        IDeviceJob currentJob = findRunningJob(deviceKey);

        TR069Message jobRequest = null;
        //handle running job
        if(currentJob != null){
            jobRequest = handleRunningJob(context, currentJob);
        }
        //if get request then return it
        if(jobRequest != null){
            return jobRequest;
        }

        //running job is waiting for some request(it is not finished and no request to send) then skip follow on checking
        if(currentJob != null && (!currentJob.isFinished())){
            log.debug("after handle empty message, job:" + currentJob.getJobID() + " is not finished, and no request to send ");
            return null;
        }


        //then handing system jobs one by one until get some request on some job which is running
        //(null, null) means no job in dealing and no request to send
        //(notnull, null) means there is some job is running and waiting for some message(request) checking
        //(notnull, notnull) means there is some job is running and need send request for response
        //(null, notnull) means no job in dealing but has request to send, it is impossible
        Tuple2<IDeviceJob, TR069Message> result = fetchSystemJobAndExecute(context, deviceKey);
        currentJob = result._1();
        jobRequest = result._2();

        if(jobRequest != null){
            return jobRequest;
        }
        if(currentJob != null && currentJob.isRunning()){
            //currentJob is not isFinished, then do not need to beginRun next job;
            return null;
        }

        //then handing user jobs one by one until get some request on some job which is running
        //(null, null) means no job in dealing and no request to send
        //(notnull, null) means there is some job is running and waiting for some message(request) checking
        //(notnull, notnull) means there is some job is running and need send request for response
        //(null, notnull) means no job in dealing but has request to send, it is impossible
        Tuple2<IDeviceJob, TR069Message> result2 = fetchUserJobAndExecute(context, deviceKey);
        currentJob = result2._1();
        jobRequest = result2._2();

        if(jobRequest != null){
            return jobRequest;
        }
        if(currentJob != null && currentJob.isRunning()){
            //currentJob is not isFinished, then do not need to beginRun next job;
            return null;
        }
        //nothing to do
        return null;
    }

    private Tuple2<IDeviceJob, TR069Message> fetchSystemJobAndExecute(ITR069MessageContext context,String deviceKey) {
        IDeviceJob currentJob = getJobManager().fetchSystemJob(deviceKey);
        //no job to exectue
        if(currentJob == null){
            return Tuple.tuple(null,null);
        }
        //handle job and get request
        TR069Message jobRequest = handleJob(currentJob, context, deviceKey);

        //has request, then return
        if(jobRequest != null){
            log.debug("after handle empty message, job:"+ currentJob.getJobID() + " has request to send");
            return Tuple.tuple(currentJob, jobRequest);
        }
        //no request, and job is running -- means waiting for some request
        if(currentJob.isRunning()){
            log.debug("after handle empty message, job:"+ currentJob.getJobID() + " is waiting for some request");
            return Tuple.tuple(currentJob, null);
        }

        log.debug("after handle empty message, job:"+ currentJob.getJobID() + " has finished with no request to send, then fetch next one");
        //fetch next one
        return fetchSystemJobAndExecute(context, deviceKey);
    }

    private Tuple2<IDeviceJob, TR069Message> fetchUserJobAndExecute(ITR069MessageContext context,String deviceKey) {
        IDeviceJob currentJob = getJobManager().fetchUserJob(deviceKey);
        //no job to exectue
        if(currentJob == null){
            return Tuple.tuple(null,null);
        }
        //handle job and get request
        TR069Message jobRequest = handleJob(currentJob, context, deviceKey);

        //has request, then return
        if(jobRequest != null){
            log.debug("after handle empty message, job:"+ currentJob.getJobID() + " has request to send");
            return Tuple.tuple(currentJob, jobRequest);
        }
        //no request, and job is running -- means waiting for some request
        if(currentJob.isRunning()){
            log.debug("after handle empty message, job:"+ currentJob.getJobID() + " is waiting for some request");
            return Tuple.tuple(currentJob, null);
        }

        //fetch next one
        log.debug("after handle empty message, job:"+ currentJob.getJobID() + " has finished with no request to send, then fetch next one");
        return fetchUserJobAndExecute(context, deviceKey);
    }

    private TR069Message handleJob(IDeviceJob currentJob, ITR069MessageContext context, String deviceKey) {
        TR069Message result = null;
        try{
            if(currentJob.isReady()){
                log.debug("job:" + currentJob.getJobID() + " for device:" + deviceKey + " is ready, begin run it");
                result = getJobRunner().beginRun(currentJob,context);
            }else if(currentJob.isRunning()){
                log.debug("job:" + currentJob.getJobID() + " for device:" + deviceKey +" is running, continue run it");
                result = getJobRunner().continueRun(currentJob,context);
            } if(currentJob.isFinished()){
                //impossible, should not happened
                log.error("job:" + currentJob.getJobID() + "should not be finished status for device:" + deviceKey + " when handle empty message");
            }
            log.debug("after handle empty message for job:"+ currentJob.getJobID() + ", get request:" + (result == null?"null":result.toSOAPString()));
            if(currentJob.isFinished()){
                log.debug("after handle empty message, job:" + currentJob.getJobID() + " has finished");
                getJobManager().removeJob(currentJob);
            }
        }catch(Exception exp){
            log.error("when handle empty message,job:" + currentJob.getJobID() + " failed for execution",exp);
            getJobRunner().failOnException(currentJob, exp);
            getJobManager().removeJob(currentJob);
        }
        return result;
    }

    //handle running job , let it continue beginRun
    private TR069Message handleRunningJob(ITR069MessageContext context,IDeviceJob job) {
        TR069Message request = null;
        try{
            log.debug("continue running job:" + job.getJobID() + " with empty message");
            request = getJobRunner().continueRun(job, context);
            if(job.isFinished()){
                log.debug("after continue running, job:"+ job.getJobID() + " has finished");
                getJobManager().removeJob(job);
            }
            log.debug("after handle empty message,job:"+ job.getJobID() + " get request:" + (request == null?"null":request.toSOAPString()));
        }catch(Exception exp){
            log.error("when handle empty message,job:" + job.getJobID() + " failed for execution", exp);
            getJobRunner().failOnException(job, exp);
            getJobManager().removeJob(job);
        }
        return request;
    }

    //first find running job, the priority is:
    // 1. running system job
    // 2. running user job
    private IDeviceJob findRunningJob(String deviceKey) {
        if(deviceKey == null){
            return null;
        }
        IDeviceJob currentSystemJob = getJobManager().fetchSystemJob(deviceKey);
        IDeviceJob currentUserJob = getJobManager().fetchUserJob(deviceKey);
        if(currentSystemJob == null || currentSystemJob.isReady()){
            if(currentUserJob!=null && currentUserJob.isRunning()){
                //no system job or system is not running
                //current job is running
                return currentUserJob;
            }else{
                //no system job or system is not running
                // no user job or user job is not running
                return null;
            }
        }else{
            //system job is running, no matter user job status
            return currentSystemJob;
        }
    }
}
