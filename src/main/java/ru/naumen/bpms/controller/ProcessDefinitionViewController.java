package ru.naumen.bpms.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.naumen.bpms.repository.ProcessDefinitionRepository;

@Controller
public class ProcessDefinitionViewController {

    private final ProcessDefinitionRepository processDefinitionRepository;

    public ProcessDefinitionViewController(ProcessDefinitionRepository processDefinitionRepository) {
        this.processDefinitionRepository = processDefinitionRepository;
    }

    @GetMapping("/process-definitions/view")
    public String showProcessDefinitions(Model model) {
        model.addAttribute("processDefinitions", processDefinitionRepository.findAllWithStepsAndTransitions());
        return "process-definitions";
    }
}