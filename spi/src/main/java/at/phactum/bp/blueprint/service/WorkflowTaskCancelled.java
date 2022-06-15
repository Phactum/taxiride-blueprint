package at.phactum.bp.blueprint.service;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * This annotation is used to define a method for processing the cancellation of
 * a certain asynchronous process-task (e.g. due to boundary events):
 * 
 * <pre>
 * &#64;WorkflowTaskCancelled(taskDefinition = "doSomeWorkload")
 * public void cancelWorkload(
 *         final MyDomainEntity entity,
 *         final &#64;TaskId String id) throws {@link TaskException} {
 *     ...
 * </pre>
 * 
 * <i>Hint:</i> This method is only called for workflow tasks which are marked
 * for asynchronous processing by having a method parameter annotated by
 * {@link TaskId}. Additionally the workflow system has to support notifying
 * about cancellations.
 * 
 * @see TaskId
 */
@Retention(RUNTIME)
@Target(METHOD)
@Inherited
@Documented
@Repeatable(WorkflowTasksCancelled.class)
public @interface WorkflowTaskCancelled {

    static String USE_METHOD_NAME = "";

    /**
     * @return The activity's BPMN id. Defaults to the annotated method's name.
     */
    String id() default USE_METHOD_NAME;

    /**
     * @return The task-definition as defined in the BPMN. Defaults to the annotated
     *         method's name.
     */
    String taskDefinition() default USE_METHOD_NAME;

    /**
     * Can be used to define certain versions or ranges of versions of a process for
     * which the annotated method should be used for.
     * <p>
     * Format:
     * <ul>
     * <li><i>*</i>: all versions
     * <li><i>1</i>: only version &quot;1&quot;
     * <li><i>1-3</i>: only versions &quot;1&quot;, &quot;2&quot; and &quot;3&quot;
     * <li><i>&gt;3</i>: only versions less than &quot;3&quot;
     * <li><i>&lt;3</i>: only versions higher than &quot;3&quot;</li>
     * </ul>
     * 
     * @throws RuntimeException If a process' version does not match any method
     *                          annotated.
     * @return The version of the process this method belongs to.
     */
    String[] version() default "*";

}
