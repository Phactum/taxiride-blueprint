package at.phactum.bp.blueprint.camunda8.adapter.deployment;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("PROCESS")
public class DeployedProcess extends Deployment {
    
    /** the BPMN process id of the process */
    @Column(name = "BPMN_PROCESS_ID")
    private String bpmnProcessId;

    public String getBpmnProcessId() {
        return bpmnProcessId;
    }

    public void setBpmnProcessId(String bpmnProcessId) {
        this.bpmnProcessId = bpmnProcessId;
    }

}
