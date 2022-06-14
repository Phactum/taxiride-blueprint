package at.phactum.bp.blueprint.service;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * This annotation is used to define a parameter for processing a user-task
 * event.
 * 
 * <pre>
 * &#64;WorkflowTask(taskDefinition = "myFormKey")
 * public void setStatus(
 *         final MyDomainEntity entity,
 *         &#64;TaskEvent("ALL") TaskEvent event,
 *         &#64;UserTaskId String id) throws {@link TaskException} {
 * </pre>
 */
@Retention(RUNTIME)
@Target(ElementType.PARAMETER)
@Inherited
@Documented
public @interface UserTaskEvent {

    enum TaskEvent {
        CREATED, // on creating a user task
        COMPLETED, // on completing a user task
        CANCELED, // on canceling a user task (e.g. due to boundary event)
        ALL, // all events: CREATED, COMPLETED, CANCELED
        BPMS, // only events caused by the BPMS and not by the user: CREATED, CANCELED
    };

    public TaskEvent[] value() default { TaskEvent.BPMS };

}
