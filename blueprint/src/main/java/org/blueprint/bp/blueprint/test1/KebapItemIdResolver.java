package org.blueprint.bp.blueprint.test1;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import at.phactum.bp.blueprint.service.MultiInstanceElementResolver;
import at.phactum.bp.blueprint.utilities.CaseUtils;

@Component
public class KebapItemIdResolver implements MultiInstanceElementResolver<Test1DomainEntity, String>  {

    @Override
    public Collection<String> getNames() {

        return List.of("EmbeddedSubprocess");

    }

    @Override
    public String resolve(
            final Test1DomainEntity domainEntity,
            final Map<String, MultiInstance<Object>> multiInstances) {
        
        final var multiInstance = multiInstances.get("EmbeddedSubprocess");
        
        return CaseUtils.camelToKebap(
                    multiInstance.getElement().toString());
        
    }

}
