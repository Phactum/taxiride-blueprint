package at.phactum.bp.blueprint.camunda8.adapter.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import at.phactum.bp.blueprint.camunda8.adapter.Camunda8DeploymentAdapter;
import at.phactum.bp.blueprint.camunda8.adapter.Camunda8TaskWiring;
import at.phactum.bp.blueprint.camunda8.adapter.Connectable;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.ZeebeFuture;
import io.camunda.zeebe.client.api.command.DeployProcessCommandStep1;
import io.camunda.zeebe.client.api.command.DeployProcessCommandStep1.DeployProcessCommandBuilderStep2;
import io.camunda.zeebe.client.api.response.DeploymentEvent;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfiguration.class)
@SpringBootTest()
public class ConnectableTest {

    private ZeebeClient client;

    @SpyBean
    private Camunda8DeploymentAdapter adapter;

    @SpyBean
    private Camunda8TaskWiring taskWiring;
    
    @Captor
    private ArgumentCaptor<Connectable> connectablesCaptor;

    @BeforeEach
    public void initZeebeClient() {

        client = mock(ZeebeClient.class);

        final var deploymentEvent = mock(DeploymentEvent.class);

        @SuppressWarnings("unchecked")
        final var deploymentFuture = (ZeebeFuture<DeploymentEvent>) mock(ZeebeFuture.class);
        when(deploymentFuture.join()).thenReturn(deploymentEvent);
        
        final var command2 = mock(DeployProcessCommandBuilderStep2.class);
        when(command2.send()).thenReturn(deploymentFuture);
        
        final var command1 = mock(DeployProcessCommandStep1.class);
        when(command1.addProcessModel(any(), any())).thenReturn(command2);
        
        when(client.newDeployCommand()).thenReturn(command1);

    }

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
        assertTrue(connectables.stream().anyMatch(c -> c.getTaskDefinition().equals("END_EVENDBASED")));

    }

}
