package at.phactum.bp.blueprint.camunda7.adapter.service;

import at.phactum.bp.blueprint.bpm.deployment.ProcessServiceImplementation;
import org.camunda.bpm.engine.ProcessEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.function.Function;

public class Camunda7ProcessService<DE>
        implements ProcessServiceImplementation<DE> {

    private static final Logger logger = LoggerFactory.getLogger(Camunda7ProcessService.class);
    
    private final ApplicationEventPublisher applicationEventPublisher;
    
    private final ProcessEngine processEngine;
    
    private final JpaRepository<DE, String> workflowDomainEntityRepository;
    
    private final Class<DE> workflowDomainEntityClass;
    
    private final Function<DE, String> getDomainEntityId;

    private String workflowModuleId;

    private String bpmnProcessId;

    public Camunda7ProcessService(
            final ApplicationEventPublisher applicationEventPublisher,
            final ProcessEngine processEngine,
            final Function<DE, String> getDomainEntityId,
            final JpaRepository<DE, String> workflowDomainEntityRepository,
            final Class<DE> workflowDomainEntityClass) {

        super();
        this.applicationEventPublisher = applicationEventPublisher;
        this.processEngine = processEngine;
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
    
    public boolean testForNotYetWired() {
        
        if (bpmnProcessId == null) {
            logger.error(
                    "The bean ProcessService<{}> was not wired to a BPMN process! "
                            + "It is likely that the BPMN is not part of the classpath.",
                    workflowDomainEntityClass.getName());
            return true;
        }
        
        return false;

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
        
        wakeupJobExecutorOnActivity();
        
        processEngine
                .getRuntimeService()
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
            final Object message) {
        
        return correlateMessage(
                domainEntity,
                message.getClass().getSimpleName());
        
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
    
    @Override
    public DE correlateMessage(
            final DE domainEntity,
            final Object message,
            final String correlationId) {
        
        return correlateMessage(
                domainEntity,
                message.getClass().getSimpleName(),
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
        
        final var correlation = processEngine
                .getRuntimeService()
                .createMessageCorrelation(messageName)
                .processInstanceBusinessKey(id);

        if (correlationIdLocalVariableName != null) {
            correlation.localVariableEquals(
                    correlationIdLocalVariableName,
                    correlationId);
        }

        wakeupJobExecutorOnActivity();

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
        
        wakeupJobExecutorOnActivity();
        
        processEngine
                .getTaskService()
                .complete(taskId);
        
        return attachedEntity;
        
    }

    @Override
    public DE completeTask(
            final DE domainEntity,
            final String taskId) {
        
        throw new UnsupportedOperationException();
        
    }
    
    @Override
    public DE cancelTask(
            final DE domainEntity,
            final String taskId,
            final String bpmnErrorCode) {
        
        throw new UnsupportedOperationException();
        
    }
    
    @Override
    public DE cancelUserTask(
            final DE domainEntity,
            final String taskId,
            final String errorCode) {
        
        final var attachedEntity = workflowDomainEntityRepository
                .saveAndFlush(domainEntity);

        wakeupJobExecutorOnActivity();
        
        processEngine
                .getTaskService()
                .handleBpmnError(taskId, errorCode);

        return attachedEntity;
        
    }
    
    private void wakeupJobExecutorOnActivity() {
        
        logger.debug("Wanna wake up job-executor");
        applicationEventPublisher.publishEvent(
                new WakupJobExecutorNotification(
                        this.getClass().getName()));
        
    }
    
}
