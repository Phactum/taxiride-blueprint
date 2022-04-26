package at.phactum.bp.blueprint.bpm.deployment.parameters;

import java.util.HashSet;
import java.util.Set;

import at.phactum.bp.blueprint.service.UserTaskEvent;
import at.phactum.bp.blueprint.service.UserTaskEvent.TaskEvent;

public class UserTaskEventMethodParameter extends MethodParameter {

    private final Set<UserTaskEvent.TaskEvent> events;

    public UserTaskEventMethodParameter(
            final TaskEvent[] annotationParameter) {

        events = new HashSet<TaskEvent>();
        
        for (final var event : annotationParameter) {

            if (event == TaskEvent.ALL) {
                events.add(TaskEvent.CREATED);
                events.add(TaskEvent.CANCELED);
                events.add(TaskEvent.COMPLETED);
            } else if (event == TaskEvent.BPMS) {
                events.add(TaskEvent.CREATED);
                events.add(TaskEvent.CANCELED);
            } else {
                events.add(event);
            }

        }
        
    }

    public Set<UserTaskEvent.TaskEvent> getEvents() {

        return events;

    }
    
}
