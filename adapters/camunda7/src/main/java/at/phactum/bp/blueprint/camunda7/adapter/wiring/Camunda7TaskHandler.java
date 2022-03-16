package at.phactum.bp.blueprint.camunda7.adapter.wiring;

import java.lang.reflect.Method;
import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.data.jpa.repository.JpaRepository;

import at.phactum.bp.blueprint.bpm.deployment.MethodParameter;
import at.phactum.bp.blueprint.bpm.deployment.TaskHandlerBase;
import at.phactum.bp.blueprint.domain.WorkflowDomainEntity;

public class Camunda7TaskHandler extends TaskHandlerBase implements JavaDelegate {

    private Object result;

    public Camunda7TaskHandler(
            final JpaRepository<WorkflowDomainEntity, String> workflowDomainEntityRepository,
            final Object bean,
            final Method method,
            final List<MethodParameter> parameters) {
        
        super(workflowDomainEntityRepository, bean, method, parameters);
        
    }

    @Override
    public void execute(
            final DelegateExecution execution) throws Exception {
        
        result = super.execute(execution.getBusinessKey());

    }
    
    public Object getResult() {

        return result;

    }

}
