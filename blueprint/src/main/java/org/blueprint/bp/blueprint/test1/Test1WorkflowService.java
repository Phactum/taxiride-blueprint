package org.blueprint.bp.blueprint.test1;

import org.springframework.stereotype.Service;

import at.phactum.bp.blueprint.service.MultiInstanceElement;
import at.phactum.bp.blueprint.service.MultiInstanceIndex;
import at.phactum.bp.blueprint.service.MultiInstanceTotal;
import at.phactum.bp.blueprint.service.WorkflowService;
import at.phactum.bp.blueprint.service.WorkflowServicePort;
import at.phactum.bp.blueprint.service.WorkflowTask;

@Service
@WorkflowService(bpmnProcessId = "Process_Test1")
public class Test1WorkflowService implements WorkflowServicePort<Test1DomainEntity> {

    @WorkflowTask(taskDefinition = "TEST1")
    public void doTest1Task(
            final Test1DomainEntity rootEntity) {
        
        System.err.println("YEAH 1: " + rootEntity.getId());
        
    }

    @WorkflowTask(taskDefinition = "TEST2")
    public void doTest2Task() {
        
        System.err.println("YEAH 2");
        
    }

    @WorkflowTask(taskDefinition = "TEST3")
    public void doTest3Task(
            final Test1DomainEntity rootEntity,
            final @MultiInstanceElement String element,
            final @MultiInstanceTotal int total,
            final @MultiInstanceIndex int index) {
        
        System.err.println("YEAH 3: " + element + " (" + index + "/" + total + ")");
        
    }

    @WorkflowTask(taskDefinition = "TEST4")
    public void doTest4Task(
            final Test1DomainEntity rootEntity,
            final @MultiInstanceElement String itemId,
            final @MultiInstanceTotal int total,
            final @MultiInstanceIndex int index) {
        
        System.err.println("YEAH 4: " + itemId + " (" + index + "/" + total + ")");
        
    }
    
    @WorkflowTask(taskDefinition = "TEST5")
    public void doTest4Task(
            final Test1DomainEntity rootEntity,
            final @MultiInstanceElement(resolverBean = KebapItemIdResolver.class) String kebapItemId) {

        System.err.println("YEAH 5: " + kebapItemId);
        
    }
    
}
