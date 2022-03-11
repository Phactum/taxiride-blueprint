package org.blueprint.bp.blueprint.test1;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.blueprint.bp.blueprint.config.ApplicationProperties;

import at.phactum.bp.blueprint.domain.WorkflowDomainEntity;

@Entity
@Table(name = "TEST1")
public class Test1DomainEntity extends WorkflowDomainEntity {

    @Override
    public String getWorkflowModuleId() {

        return ApplicationProperties.WORKFLOW_MODULE_ID;

    }

}
