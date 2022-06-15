package at.phactum.bp.blueprint.bpm.deployment.parameters;

import java.util.HashSet;
import java.util.Set;

import at.phactum.bp.blueprint.service.TaskEvent;
import at.phactum.bp.blueprint.service.TaskEvent.Event;

public class TaskEventMethodParameter extends MethodParameter {

    private final Set<TaskEvent.Event> events;

    public TaskEventMethodParameter(
            final Event[] annotationParameter) {

        events = new HashSet<Event>();
        
        for (final var event : annotationParameter) {

            if (event == Event.ALL) {
                events.add(Event.CREATED);
                events.add(Event.CANCELED);
                events.add(Event.COMPLETED);
            } else if (event == Event.BPMS) {
                events.add(Event.CREATED);
                events.add(Event.CANCELED);
            } else {
                events.add(event);
            }

        }
        
    }

    public Set<TaskEvent.Event> getEvents() {

        return events;

    }
    
}
