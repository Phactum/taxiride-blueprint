package at.phactum.bp.blueprint.camunda7.adapter.wiring;

import java.lang.reflect.Method;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class Camunda7TaskHandler implements JavaDelegate {

    private Object bean;

    private Method method;

    private Object result;

    public Camunda7TaskHandler(
            final Object bean,
            final Method method) {
        
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
