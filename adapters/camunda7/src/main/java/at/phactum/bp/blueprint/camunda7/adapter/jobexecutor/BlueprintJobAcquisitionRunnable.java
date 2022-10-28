package at.phactum.bp.blueprint.camunda7.adapter.jobexecutor;

import org.camunda.bpm.engine.impl.jobexecutor.JobAcquisitionStrategy;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.SequentialJobAcquisitionRunnable;

public class BlueprintJobAcquisitionRunnable extends SequentialJobAcquisitionRunnable {

    public BlueprintJobAcquisitionRunnable(
            final JobExecutor jobExecutor) {
        
        super(jobExecutor);
        
    }

    @Override
    protected JobAcquisitionStrategy initializeAcquisitionStrategy() {
        
        return new BlueprintBackoffJobAcquisitionStrategy(jobExecutor);
        
    }
    
}
