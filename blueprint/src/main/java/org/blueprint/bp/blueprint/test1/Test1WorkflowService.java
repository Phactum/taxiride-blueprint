package org.blueprint.bp.blueprint.test1;

import java.util.List;

import org.springframework.stereotype.Service;

import at.phactum.bp.blueprint.service.BpmnProcess;
import at.phactum.bp.blueprint.service.MultiInstanceElement;
import at.phactum.bp.blueprint.service.MultiInstanceIndex;
import at.phactum.bp.blueprint.service.MultiInstanceTotal;
import at.phactum.bp.blueprint.service.TaskParam;
import at.phactum.bp.blueprint.service.WorkflowService;
import at.phactum.bp.blueprint.service.WorkflowTask;

@Service
@WorkflowService(
        workflowAggregateClass = Test1DomainEntity.class,
        bpmnProcess = {
                @BpmnProcess(bpmnProcessId = "Process_Test1"),
                @BpmnProcess(bpmnProcessId = "Process_Test2")
            }
    )
public class Test1WorkflowService {

    @WorkflowTask(taskDefinition = "TEST1")
    public void doTest1Task(
            final Test1DomainEntity rootEntity) {
        
        System.err.println("YEAH 1: " + rootEntity.getId());
        
    }

    @WorkflowTask
    public void TEST2(@TaskParam("listOfAAndB") final List<String> listOfAAndB) {
        
        System.err.println("YEAH 2: " + listOfAAndB);
        
    }

    @WorkflowTask(taskDefinition = "TEST3")
    public void doTest3Task(
            final Test1DomainEntity rootEntity,
            final @MultiInstanceTotal("MiTask") int total,
            final @MultiInstanceIndex("MiTask") int index) {
        
        System.err.println("YEAH 3: " + index + "/" + total);
        
    }

    @WorkflowTask(taskDefinition = "TEST4")
    public void doTest4Task(
            final Test1DomainEntity rootEntity,
            final @MultiInstanceElement("EmbeddedSubprocess") String itemId,
            final @MultiInstanceTotal("EmbeddedSubprocess") int total,
            final @MultiInstanceIndex("EmbeddedSubprocess") int index) {
        
        System.err.println("YEAH 4: " + itemId + " (" + index + "/" + total + ")");
        
    }
    
    @WorkflowTask(taskDefinition = "TEST5")
    public void doTest5Task(
            final Test1DomainEntity rootEntity,
            final @MultiInstanceElement(resolverBean = KebapItemIdResolver.class) String kebapItemId) {

        System.err.println("YEAH 5: " + kebapItemId);
        
    }
    
}
