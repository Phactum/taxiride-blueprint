package at.phactum.bp.blueprint.camunda7.adapter.service;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import at.phactum.bp.blueprint.bpm.deployment.ProcessServiceImplementation;
import at.phactum.bp.blueprint.domain.WorkflowDomainEntity;

public class Camunda7ProcessService<DE extends WorkflowDomainEntity>
        implements ProcessServiceImplementation<DE> {

    private static final Logger logger = LoggerFactory.getLogger(Camunda7ProcessService.class);
    
    private final RuntimeService runtimeService;
    
    private final JpaRepository<DE, String> workflowDomainEntityRepository;
    
    private final Class<DE> workflowDomainEntityClass;

    private String bpmnProcessId;

    public Camunda7ProcessService(
            final RuntimeService runtimeService,
            final RepositoryService repositoryService,
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
    public DE startWorkflow(
            final DE domainEntity) throws Exception {

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

    @Override
    @Transactional
    public void correlateMessage(
            final DE domainEntity,
            final String messageName) {

        correlateMessage(
                domainEntity,
                messageName,
                null,
                null);

    }

    @Override
    public void correlateMessage(
            final DE domainEntity,
            final String messageName,
            final String correlationId) {
        
        final var correlationIdLocalVariableName =
                domainEntity.getBpmnProcessId()
                + "-"
                + messageName;

        correlateMessage(
                domainEntity,
                messageName,
                correlationIdLocalVariableName,
                correlationId);

    }

    private void correlateMessage(
            final DE domainEntity,
            final String messageName,
            final String correlationIdLocalVariableName,
            final String correlationId) {

        workflowDomainEntityRepository.saveAndFlush(domainEntity);

        final var correlation = runtimeService
                .createMessageCorrelation(messageName)
                .processInstanceBusinessKey(domainEntity.getId());

        if (correlationIdLocalVariableName != null) {
            correlation.localVariableEquals(
                    correlationIdLocalVariableName,
                    correlationId);
        }

        final var result = correlation.correlateWithResult();
        
        logger.trace("Correlated message '{}' using correlation-id '{}' for process '{}#{}' and execution '{}' (tenant: {})",
                messageName,
                correlationId,
                bpmnProcessId, 
                result.getExecution().getProcessInstanceId(),
                result.getExecution().getId(),
                result.getExecution().getTenantId());

    }
    
}
