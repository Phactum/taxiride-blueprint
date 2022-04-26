package at.phactum.bp.blueprint.camunda8.adapter.service;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;

import at.phactum.bp.blueprint.bpm.deployment.ProcessServiceImplementation;
import io.camunda.zeebe.client.ZeebeClient;

public class Camunda8ProcessService<DE>
        implements ProcessServiceImplementation<DE> {

    private static final Logger logger = LoggerFactory.getLogger(Camunda8ProcessService.class);
    
    private final JpaRepository<DE, String> workflowDomainEntityRepository;

    private final Class<DE> workflowDomainEntityClass;

    private final Function<DE, String> getDomainEntityId;

    private ZeebeClient client;
    
    private String workflowModuleId;

    private String bpmnProcessId;
    
    public Camunda8ProcessService(
            final JpaRepository<DE, String> workflowDomainEntityRepository,
            final Function<DE, String> getDomainEntityId,
            final Class<DE> workflowDomainEntityClass) {
        
        super();
        this.workflowDomainEntityRepository = workflowDomainEntityRepository;
        this.workflowDomainEntityClass = workflowDomainEntityClass;
        this.getDomainEntityId = getDomainEntityId;
                
    }
    
    public void wire(
            final ZeebeClient client,
            final String workflowModuleId,
            final String bpmnProcessId) {
        
        this.client = client;
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
        
        client
                .newCreateInstanceCommand()
                .bpmnProcessId(bpmnProcessId)
                .latestVersion()
                .variables(domainEntity)
                .send()
                .get(10, TimeUnit.SECONDS);

        try {
            return workflowDomainEntityRepository.saveAndFlush(domainEntity);
        } catch (RuntimeException exception) {
            // HibernateH2PostgresIdempotency.ignoreDuplicatesExceptionAndRethrow(exception,
            // processEntity);
            throw exception;
        }
        
    }

    @Override
    public DE correlateMessage(
            final DE domainEntity,
            final String messageName) {
        
        final var attachedEntity = workflowDomainEntityRepository
                .saveAndFlush(domainEntity);
        final var id = getDomainEntityId.apply(domainEntity);

        final var messageKey = client
                .newPublishMessageCommand()
                .messageName(messageName)
                .correlationKey(id)
                .variables(domainEntity)
                .send()
                .join()
                .getMessageKey();
        
        logger.trace("Correlated message '{}' using correlation-id '{}' for process '{}' as '{}'",
                messageName, id, bpmnProcessId, messageKey);
        
        return attachedEntity;
        
    }

    @Override
    public DE correlateMessage(
            final DE domainEntity,
            final String messageName,
            final String correlationId) {
            
        final var attachedEntity = workflowDomainEntityRepository
                .saveAndFlush(domainEntity);
        
        final var messageKey = client
                .newPublishMessageCommand()
                .messageName(messageName)
                .correlationKey(correlationId)
                .variables(domainEntity)
                .send()
                .join()
                .getMessageKey();
        
        logger.trace("Correlated message '{}' using correlation-id '{}' for process '{}' as '{}'",
                messageName, correlationId, bpmnProcessId, messageKey);
        
        return attachedEntity;
        
    }

    @Override
    public DE completeUserTask(
            final DE domainEntity,
            final String taskId) {
        
        final var attachedEntity = workflowDomainEntityRepository
                .saveAndFlush(domainEntity);
        
        client
                .newCompleteCommand(Long.parseLong(taskId, 16))
                .variables(domainEntity)
                .send()
                .join();

        logger.trace("Complete usertask '{}' for process '{}'",
                taskId, bpmnProcessId);
        
        return attachedEntity;
        
    }
    
    @Override
    public DE completeUserTaskByError(
            final DE domainEntity,
            final String taskId,
            final String errorCode) {
        
        final var attachedEntity = workflowDomainEntityRepository
                .saveAndFlush(domainEntity);
        
        client
                .newThrowErrorCommand(Long.parseLong(taskId))
                .errorCode(errorCode)
                .send()
                .join();

        logger.trace("Complete usertask '{}' for process '{}'",
                taskId, bpmnProcessId);
        
        return attachedEntity;
        
    }
    
}
