package at.phactum.bp.blueprint.camunda7.adapter.wiring;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "camunda.blueprint")
public class Camunda7BlueprintProperties {

    private boolean useBpmnAsyncDefinitions = false;
    
    private List<BpmnAsyncDefinition> bpmnAsyncDefinitions = List.of();

    public static class BpmnAsyncDefinition {
        
        private String workflowModuleId;
        
        private String bpmnProcessId;
        
        public String getBpmnProcessId() {
            return bpmnProcessId;
        }
        
        public void setBpmnProcessId(String bpmnProcessId) {
            this.bpmnProcessId = bpmnProcessId;
        }
        
        public String getWorkflowModuleId() {
            return workflowModuleId;
        }
        
        public void setWorkflowModuleId(String workflowModuleId) {
            this.workflowModuleId = workflowModuleId;
        }
        
    }

    public boolean isUseBpmnAsyncDefinitions() {
        return useBpmnAsyncDefinitions;
    }

    public void setUseBpmnAsyncDefinitions(boolean useBpmnAsyncDefinitions) {
        this.useBpmnAsyncDefinitions = useBpmnAsyncDefinitions;
    }

    public List<BpmnAsyncDefinition> getBpmnAsyncDefinitions() {
        return bpmnAsyncDefinitions;
    }

    public void setBpmnAsyncDefinitions(List<BpmnAsyncDefinition> bpmnAsyncDefinitions) {
        this.bpmnAsyncDefinitions = bpmnAsyncDefinitions;
    };
    
}
