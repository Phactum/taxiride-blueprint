package at.phactum.bp.blueprint.service;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Used to wire workflow-services to the processes they are responsible for.
 * 
 * @see {@link WorkflowDomainEntityAdapter}
 */
@Retention(RUNTIME)
@Target(TYPE)
@Inherited
@Documented
@Repeatable(WorkflowServices.class)
public @interface WorkflowService {

    static String USE_BEAN_NAME = "";

    /**
     * @return The process-id for which the annotated service is responsible for.
     *         Defaults to the bean name of the service.
     */
    String processId() default USE_BEAN_NAME;

    /**
     * Can be used to define certain versions or ranges of versions of a process for
     * which the annotated type should be used for.
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
     * @throws RuntimeException If a process' version does not match service
     *                          annotated
     * @return The version of the process this method belongs to
     */
    String[] version() default "*";

}
