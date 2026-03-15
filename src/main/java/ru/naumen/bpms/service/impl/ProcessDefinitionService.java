package ru.naumen.bpms.service.impl;

import org.springframework.stereotype.Service;
import ru.naumen.bpms.model.ProcessDefinition;
import ru.naumen.bpms.repository.CrudRepository;
import ru.naumen.bpms.service.ProcessService;

@Service
public class ProcessDefinitionService implements ProcessService {

    private final CrudRepository<ProcessDefinition, Long> repository;

    public ProcessDefinitionService(CrudRepository<ProcessDefinition, Long> repository) {
        this.repository = repository;
    }

    @Override
    public void createProcess(ProcessDefinition processDefinition) {
        if (processDefinition == null) {
            throw new IllegalArgumentException("ProcessDefinition не может быть null");
        }
        if (processDefinition.getTitle().isEmpty()) {
            throw new IllegalArgumentException("title не может быть пустым");
        }
        repository.create(processDefinition);
    }

    @Override
    public ProcessDefinition getProcess(Long id) {
        if (id == null || id < 1) {
            throw new IllegalArgumentException("id не может быть меньше единицы");
        }
        ProcessDefinition process = repository.read(id);

        if (process == null) {
            throw new IllegalArgumentException("ProcessDefinition c id=" + id + " не найден");
        }

        return process;
    }

    @Override
    public void updateProcess(ProcessDefinition processDefinition) {

        if (processDefinition == null || processDefinition.getId() == null) {
            throw new IllegalArgumentException("Невозможно обновить. processDefinition или его id равен null ");
        }

        repository.update(processDefinition);
    }

    @Override
    public void deleteProcess(Long id) {
        if (id == null || id < 1) {
            throw new IllegalArgumentException("id не может быть меньше единицы");
        }
        repository.delete(id);
    }

}