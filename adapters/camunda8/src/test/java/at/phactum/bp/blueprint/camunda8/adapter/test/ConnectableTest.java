package at.phactum.bp.blueprint.camunda8.adapter.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import at.phactum.bp.blueprint.camunda8.adapter.deployment.Camunda8DeploymentAdapter;
import at.phactum.bp.blueprint.camunda8.adapter.test.springboot.TestConfiguration;
import at.phactum.bp.blueprint.camunda8.adapter.test.testcase.TestModuleProperties;
import at.phactum.bp.blueprint.camunda8.adapter.test.testcase.TestWorkflowService1;
import at.phactum.bp.blueprint.camunda8.adapter.test.testcase.TestWorkflowService2;
import at.phactum.bp.blueprint.camunda8.adapter.wiring.Camunda8Connectable;
import at.phactum.bp.blueprint.camunda8.adapter.wiring.Camunda8TaskWiring;
import io.camunda.zeebe.client.ZeebeClient;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { TestConfiguration.class, TestModuleProperties.class,
        TestWorkflowService1.class, TestWorkflowService2.class })
@SpringBootTest()
public class ConnectableTest {

    @Autowired
    private ZeebeClient client;

    @SpyBean
    private Camunda8DeploymentAdapter adapter;

    @SpyBean
    private Camunda8TaskWiring taskWiring;
    
    @Captor
    private ArgumentCaptor<Camunda8Connectable> connectablesCaptor;

    @Test
    public void testConnectables() {

        // connect and deploy
        adapter.accept(client);

        verify(taskWiring, times(9))
                .wireTask(connectablesCaptor.capture());
        
        final var connectables = connectablesCaptor.getAllValues();
        assertEquals(9, connectables.size());
        assertTrue(connectables.stream().anyMatch(c -> c.getTaskDefinition().equals("SEND")));
        assertTrue(connectables.stream().anyMatch(c -> c.getTaskDefinition().equals("BUSINESS_RULE")));
        assertTrue(connectables.stream().anyMatch(c -> c.getTaskDefinition().equals("SERVICE")));
        assertTrue(connectables.stream().anyMatch(c -> c.getTaskDefinition().equals("SEND_EMBEDDED")));
        assertTrue(connectables.stream().anyMatch(c -> c.getTaskDefinition().equals("SEND_SECOND")));
        assertTrue(connectables.stream().anyMatch(c -> c.getTaskDefinition().equals("INTERMEDIATE")));
        assertTrue(connectables.stream().anyMatch(c -> c.getTaskDefinition().equals("END")));
        assertTrue(connectables.stream().anyMatch(c -> c.getTaskDefinition().equals("INTERMEDIATE_EVENTBASED")));
        assertTrue(connectables.stream().anyMatch(c -> c.getTaskDefinition().equals("END_EVENTBASED")));

    }

}
