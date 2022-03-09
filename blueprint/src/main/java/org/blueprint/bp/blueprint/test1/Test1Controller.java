package org.blueprint.bp.blueprint.test1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import at.phactum.bp.blueprint.process.ProcessService;

@RestController
@RequestMapping("/test1")
public class Test1Controller {

    @Autowired
    private ProcessService<Test1DomainEntity> test1ProcessService;

    @GetMapping(path = "/start")
    public void startTest1() throws Exception {

        final var domainEntity = new Test1DomainEntity();

        test1ProcessService.startWorkflow(domainEntity);

    }

}
