package at.phactum.bp.blueprint.service;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @see {@link WorkflowService}
 */
@Retention(RUNTIME)
@Target(TYPE)
@Inherited
@Documented
public @interface WorkflowServices {

    WorkflowService[] value();

}
