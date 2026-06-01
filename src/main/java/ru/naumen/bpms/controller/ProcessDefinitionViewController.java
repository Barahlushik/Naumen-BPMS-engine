package ru.naumen.bpms.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.naumen.bpms.service.ProcessDefinitionService;

@Controller
@Slf4j
public class ProcessDefinitionViewController {

    private final ProcessDefinitionService processDefinitionService;

    public ProcessDefinitionViewController(ProcessDefinitionService processDefinitionService) {
        this.processDefinitionService = processDefinitionService;
    }

    @GetMapping("/process-definitions/view")
    public String showProcessDefinitions(Model model) {
        log.info("Process definitions view requested.");
        var processDefinitions = processDefinitionService.getAllProcessesWithStepsAndTransitions();
        model.addAttribute("processDefinitions", processDefinitions);
        log.info("Process definitions view prepared. processDefinitionsCount={}", processDefinitions.size());
        return "process-definitions";
    }
}
