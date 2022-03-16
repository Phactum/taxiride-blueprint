package at.phactum.bp.blueprint.camunda7.adapter.service;

import org.camunda.bpm.engine.RuntimeService;
import org.springframework.data.jpa.repository.JpaRepository;

import at.phactum.bp.blueprint.bpm.deployment.ProcessServiceImplementation;
import at.phactum.bp.blueprint.domain.WorkflowDomainEntity;

public class Camunda7ProcessService<DE extends WorkflowDomainEntity>
        implements ProcessServiceImplementation<DE> {

    private final RuntimeService runtimeService;

    private final JpaRepository<DE, String> workflowDomainEntityRepository;
    
    private final Class<DE> workflowDomainEntityClass;

    private String bpmnProcessId;

    public Camunda7ProcessService(
            final RuntimeService runtimeService,
            final JpaRepository<DE, String> workflowDomainEntityRepository,
            final Class<DE> workflowDomainEntityClass) {

        super();
        this.runtimeService = runtimeService;
        this.workflowDomainEntityRepository = workflowDomainEntityRepository;
        this.workflowDomainEntityClass = workflowDomainEntityClass;

    }

    public void wire(
            final String bpmnProcessId) {
        
        this.bpmnProcessId = bpmnProcessId;
        
    }

    @Override
    public String getBpmnProcessId() {

        return bpmnProcessId;

    }

    @Override
    public Class<DE> getWorkflowDomainEntityClass() {

        return workflowDomainEntityClass;

    }

    @Override
    public JpaRepository<DE, String> getWorkflowDomainEntityRepository() {

        return workflowDomainEntityRepository;

    }

    @Override
    public DE startWorkflow(DE domainEntity) throws Exception {

        final var processInstance =
                runtimeService
                        .createProcessInstanceByKey(bpmnProcessId)
                        .businessKey(domainEntity.getId())
                        .processDefinitionTenantId(domainEntity.getWorkflowModuleId())
                        .execute();

        domainEntity.setBpmnProcessId(bpmnProcessId);
        domainEntity.setWorkflowId(processInstance.getId());
        domainEntity.setRootWorkflowId(processInstance.getId());
        domainEntity.setDeployedProcessId(processInstance.getProcessDefinitionId());
        
        try {
            return workflowDomainEntityRepository.saveAndFlush(domainEntity);
        } catch (RuntimeException exception) {
            // HibernateH2PostgresIdempotency.ignoreDuplicatesExceptionAndRethrow(exception,
            // processEntity);
            throw exception;
        }

    }

}
