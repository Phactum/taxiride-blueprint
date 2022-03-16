package at.phactum.bp.blueprint.service;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import at.phactum.bp.blueprint.domain.WorkflowDomainEntity;

@Retention(RUNTIME)
@Target(PARAMETER)
@Inherited
@Documented
public @interface MultiInstanceElement {

    /**
     * @return The bean-name of the resolver used to determine the current element
     */
    Class<? extends MultiInstanceElementResolver<? extends WorkflowDomainEntity, ?>> resolverBean() default NoResolver.class;

}
