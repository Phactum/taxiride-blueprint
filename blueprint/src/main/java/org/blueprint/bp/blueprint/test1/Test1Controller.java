package org.blueprint.bp.blueprint.test1;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import at.phactum.bp.blueprint.process.ProcessService;

@RestController
@RequestMapping("/test1")
public class Test1Controller {

    @Autowired
    private ProcessService<Test1DomainEntity> test1ProcessService;

    @GetMapping(path = "/start/{id}")
    public void startTest1(
            @PathVariable final String id) throws Exception {

        final var domainEntity = new Test1DomainEntity();
        domainEntity.setId(id);
        domainEntity.setItemIds(List.of("itemNamedA", "itemNamedB"));

        test1ProcessService.startWorkflow(domainEntity);

    }

}
