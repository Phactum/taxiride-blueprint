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
 *         &#64;UserTaskId String id) throws {@link TaskException} {
 * </pre>
 */
@Retention(RUNTIME)
@Target(ElementType.PARAMETER)
@Inherited
@Documented
public @interface UserTaskId {

}
