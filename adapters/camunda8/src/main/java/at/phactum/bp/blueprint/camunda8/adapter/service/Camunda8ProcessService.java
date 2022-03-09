package at.phactum.bp.blueprint.camunda8.adapter.service;

import java.util.concurrent.TimeUnit;

import at.phactum.bp.blueprint.domain.WorkflowDomainEntity;
import at.phactum.bp.blueprint.process.ProcessService;
import io.camunda.zeebe.client.ZeebeClient;

public class Camunda8ProcessService<DE extends WorkflowDomainEntity>
        implements ProcessService<DE> {

    private final Class<DE> workflowDomainEntityClass;

    private ZeebeClient client;
    
    private String bpmnProcessId;
    
    public Camunda8ProcessService(
            final Class<DE> workflowDomainEntityClass) {
        
        super();
        this.workflowDomainEntityClass = workflowDomainEntityClass;
                
//        this.client = client;
//        this.bpmnProcessId = bpmnProcessId;
        
    }
    
    public void wire(
            final ZeebeClient client,
            final String bpmnProcessId) {
        
        this.client = client;
        this.bpmnProcessId = bpmnProcessId;
        
    }

    public Class<DE> getWorkflowDomainEntityClass() {
        return workflowDomainEntityClass;
    }

    @Override
    public String startWorkflow(
            final DE domainEntity) throws Exception {
        
        return Long.toHexString(
                client
                        .newCreateInstanceCommand()
                        .bpmnProcessId(bpmnProcessId)
                        .latestVersion()
                        .variables(domainEntity)
                        .send()
                        .get(10, TimeUnit.SECONDS)
                        .getProcessInstanceKey());
        
    }
    
    
    
}
