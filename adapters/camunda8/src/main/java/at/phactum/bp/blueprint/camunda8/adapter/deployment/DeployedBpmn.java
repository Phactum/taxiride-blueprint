package at.phactum.bp.blueprint.camunda8.adapter.deployment;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("BPMN")
public class DeployedBpmn extends DeploymentResource {

}
