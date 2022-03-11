package at.phactum.bp.blueprint.camunda8.adapter.service;

import java.util.concurrent.TimeUnit;

import org.springframework.data.jpa.repository.JpaRepository;

import at.phactum.bp.blueprint.domain.WorkflowDomainEntity;
import at.phactum.bp.blueprint.process.ProcessService;
import io.camunda.zeebe.client.ZeebeClient;

public class Camunda8ProcessService<DE extends WorkflowDomainEntity>
        implements ProcessService<DE> {

    private final JpaRepository<DE, String> workflowDomainEntityRepository;

    private final Class<DE> workflowDomainEntityClass;

    private ZeebeClient client;
    
    private String bpmnProcessId;
    
    public Camunda8ProcessService(
            final JpaRepository<DE, String> workflowDomainEntityRepository,
            final Class<DE> workflowDomainEntityClass) {
        
        super();
        this.workflowDomainEntityRepository = workflowDomainEntityRepository;
        this.workflowDomainEntityClass = workflowDomainEntityClass;
                
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

    public JpaRepository<DE, String> getWorkflowDomainEntityRepository() {

        return workflowDomainEntityRepository;

    }

    @Override
    public DE startWorkflow(
            final DE domainEntity) throws Exception {
        
        final var processInstance = client
                .newCreateInstanceCommand()
                .bpmnProcessId(bpmnProcessId)
                .latestVersion()
                .variables(domainEntity)
                .send()
                .get(10, TimeUnit.SECONDS);

        domainEntity.setBpmnProcessId(bpmnProcessId);
        domainEntity.setWorkflowId(Long.toHexString(processInstance.getProcessInstanceKey()));
        domainEntity.setRootWorkflowId(domainEntity.getWorkflowId());
        domainEntity.setDeployedProcessId(Long.toHexString(processInstance.getProcessDefinitionKey()));
        
        try {
            return workflowDomainEntityRepository.saveAndFlush(domainEntity);
        } catch (RuntimeException exception) {
            // HibernateH2PostgresIdempotency.ignoreDuplicatesExceptionAndRethrow(exception,
            // processEntity);
            throw exception;
        }
        
    }
    
}
