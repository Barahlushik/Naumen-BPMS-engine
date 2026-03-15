package ru.naumen.bpms.service;

import ru.naumen.bpms.model.ProcessDefinition;

public interface ProcessService {
    void createProcess(ProcessDefinition processDefinition);
    ProcessDefinition getProcess(Long id);
    void updateProcess(ProcessDefinition processDefinition);
    void deleteProcess(Long id);
}
