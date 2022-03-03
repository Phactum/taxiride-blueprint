package at.phactum.bp.blueprint.bpm.deployment;

import java.lang.reflect.Method;

public class TaskBeanMethod {

    private Object bean;

    private Method method;

    public TaskBeanMethod(Object bean, Method method) {
        this.bean = bean;
        this.method = method;
    }

    public Object getBean() {
        return bean;
    }

    public Method getMethod() {
        return method;
    }

}
