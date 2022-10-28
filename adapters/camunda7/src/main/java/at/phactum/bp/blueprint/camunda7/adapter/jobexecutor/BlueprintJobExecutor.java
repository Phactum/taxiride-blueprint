package at.phactum.bp.blueprint.camunda7.adapter.jobexecutor;

import org.camunda.bpm.engine.spring.components.jobexecutor.SpringJobExecutor;

public class BlueprintJobExecutor extends SpringJobExecutor {

    @Override
    protected void ensureInitialization() {

        super.ensureInitialization();
        acquireJobsRunnable = new BlueprintJobAcquisitionRunnable(this);
        
    }
    
}
