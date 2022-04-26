package at.phactum.bp.blueprint.camunda7.adapter.service;

import java.util.Objects;
import java.util.function.Function;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import at.phactum.bp.blueprint.bpm.deployment.ProcessServiceImplementation;

public class Camunda7ProcessService<DE>
        implements ProcessServiceImplementation<DE> {

    private static final Logger logger = LoggerFactory.getLogger(Camunda7ProcessService.class);
    
    private final RuntimeService runtimeService;
    
    private final TaskService taskService;
    
    private final JpaRepository<DE, String> workflowDomainEntityRepository;
    
    private final Class<DE> workflowDomainEntityClass;
    
    private final Function<DE, String> getDomainEntityId;

    private String workflowModuleId;

    private String bpmnProcessId;

    public Camunda7ProcessService(
            final RuntimeService runtimeService,
            final TaskService taskService,
            final RepositoryService repositoryService,
            final Function<DE, String> getDomainEntityId,
            final JpaRepository<DE, String> workflowDomainEntityRepository,
            final Class<DE> workflowDomainEntityClass) {

        super();
        this.runtimeService = runtimeService;
        this.taskService = taskService;
        this.workflowDomainEntityRepository = workflowDomainEntityRepository;
        this.workflowDomainEntityClass = workflowDomainEntityClass;
        this.getDomainEntityId = getDomainEntityId;

    }

    public void wire(
            final String workflowModuleId,
            final String bpmnProcessId) {
        
        this.workflowModuleId = workflowModuleId;
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

        final var attachedEntity = workflowDomainEntityRepository.saveAndFlush(domainEntity);

        final var id = getDomainEntityId.apply(attachedEntity).toString();
        
        runtimeService
                .createProcessInstanceByKey(bpmnProcessId)
                .businessKey(id)
                .processDefinitionTenantId(workflowModuleId)
                .execute();
        
        return workflowDomainEntityRepository.saveAndFlush(domainEntity);

    }

    @Override
    @Transactional
    public DE correlateMessage(
            final DE domainEntity,
            final String messageName) {

        return correlateMessage(
                domainEntity,
                messageName,
                null,
                null);

    }

    @Override
    public DE correlateMessage(
            final DE domainEntity,
            final String messageName,
            final String correlationId) {
        
        final var correlationIdLocalVariableName =
                bpmnProcessId
                + "-"
                + messageName;

        return correlateMessage(
                domainEntity,
                messageName,
                correlationIdLocalVariableName,
                correlationId);

    }

    private DE correlateMessage(
            final DE domainEntity,
            final String messageName,
            final String correlationIdLocalVariableName,
            final String correlationId) {

        final var originalId = getDomainEntityId.apply(domainEntity);
        final var isNewEntity = Objects.isNull(originalId);

        final var attachedEntity = workflowDomainEntityRepository
                .saveAndFlush(domainEntity);
        
        final var id = (isNewEntity ? getDomainEntityId.apply(attachedEntity) : originalId).toString();
        
        final var correlation = runtimeService
                .createMessageCorrelation(messageName)
                .processInstanceBusinessKey(id);

        if (correlationIdLocalVariableName != null) {
            correlation.localVariableEquals(
                    correlationIdLocalVariableName,
                    correlationId);
        }

        if (isNewEntity) {
            
            final var result = correlation.correlateStartMessage();
            logger.trace("Started process '{}#{}' by message-correlation '{}' (tenant: {})",
                    bpmnProcessId,
                    result.getProcessInstanceId(),
                    messageName,
                    result.getTenantId());
            
        } else {
        
            final var result = correlation
                    .correlateWithResult()
                    .getExecution();
            
            logger.trace("Correlated message '{}' using correlation-id '{}' for process '{}#{}' and execution '{}' (tenant: {})",
                    messageName,
                    correlationId,
                    bpmnProcessId,
                    result.getProcessInstanceId(),
                    result.getId(),
                    result.getTenantId());

        }
        
        return attachedEntity;

    }

    @Override
    public DE completeUserTask(
            final DE domainEntity,
            final String taskId) {

        final var attachedEntity = workflowDomainEntityRepository
                .saveAndFlush(domainEntity);
        
        taskService.complete(taskId);
        
        return attachedEntity;
        
    }
    
    @Override
    public DE completeUserTaskByError(
            final DE domainEntity,
            final String taskId,
            final String errorCode) {
        
        final var attachedEntity = workflowDomainEntityRepository
                .saveAndFlush(domainEntity);

        taskService.handleBpmnError(taskId, errorCode);

        return attachedEntity;
        
    }
    
}
