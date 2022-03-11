package at.phactum.bp.blueprint.camunda7.adapter.wiring;

import java.lang.reflect.Method;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.data.jpa.repository.JpaRepository;

public class Camunda7TaskHandler implements JavaDelegate {

    private final JpaRepository<?, String> workflowDomainEntityRepository;

    private final Object bean;

    private final Method method;

    private Object result;

    public Camunda7TaskHandler(
            final JpaRepository<?, String> workflowDomainEntityRepository,
            final Object bean,
            final Method method) {
        
        this.workflowDomainEntityRepository = workflowDomainEntityRepository;
        this.bean = bean;
        this.method = method;
        
    }

    @Override
    public void execute(
            final DelegateExecution execution) throws Exception {
        
        result = method.invoke(bean);

    }
    
    public Object getResult() {

        return result;

    }

}
