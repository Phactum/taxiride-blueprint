package at.phactum.bp.blueprint.bpm.deployment;

public class TaskDefinition {

    private String bpmnProcessId;

    private String taskDefinition;

    public TaskDefinition(String bpmnProcessId, String taskDefinition) {
        this.bpmnProcessId = bpmnProcessId;
        this.taskDefinition = taskDefinition;
    }

    public String getBpmnProcessId() {
        return bpmnProcessId;
    }

    public String getTaskDefinition() {
        return taskDefinition;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == null) {
            return false;
        }
        if (!(obj instanceof TaskDefinition)) {
            return false;
        }
        final var other = (TaskDefinition) obj;
        if (!bpmnProcessId.equals(other.bpmnProcessId)) {
            return false;
        }
        return taskDefinition.equals(other.taskDefinition);

    }

    @Override
    public int hashCode() {

        var result = 7;

        result = 31 * result + bpmnProcessId.hashCode();
        result = 31 * result + taskDefinition.hashCode();

        return result;

    }

}
