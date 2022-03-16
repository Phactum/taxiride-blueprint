package at.phactum.bp.blueprint.bpm.deployment;

import java.lang.reflect.Method;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import at.phactum.bp.blueprint.bpm.deployment.MethodParameter.Type;
import at.phactum.bp.blueprint.domain.WorkflowDomainEntity;

public class TaskHandlerBase {

    protected final JpaRepository<WorkflowDomainEntity, String> workflowDomainEntityRepository;

    protected final List<MethodParameter> parameters;

    protected final Object bean;

    protected final Method method;

    public TaskHandlerBase(
            final JpaRepository<WorkflowDomainEntity, String> workflowDomainEntityRepository,
            final Object bean,
            final Method method, final List<MethodParameter> parameters) {
        
        this.workflowDomainEntityRepository = workflowDomainEntityRepository;
        this.bean = bean;
        this.method = method;
        this.parameters = parameters;

    }
    
    protected Object execute(
            final String workflowDomainEntityId) throws Exception {
        
        final var domainEntity = new WorkflowDomainEntity[] { null };

        final var args = new Object[parameters.size()];
        final var index = new int[] { -1 };
        parameters
                .stream()
                .peek(param -> ++index[0])
                .filter(param -> {
                    if (param.getType() != Type.DOMAINENTITY) {
                        return false;
                    }
                    domainEntity[0] = (WorkflowDomainEntity) workflowDomainEntityRepository
                            .getById(workflowDomainEntityId);
                    args[index[0]] = domainEntity[0];
                    return true;
                })
                // ignore unknown parameters, but they should be filtered as part of validation
                .forEach(param -> { /* */ });
        
        final var result = method.invoke(bean, args);

        if (domainEntity[0] != null) {
            workflowDomainEntityRepository.saveAndFlush(domainEntity[0]);
        }

        return result;
        
    }
    
}
