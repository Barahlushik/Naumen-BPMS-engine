package ru.naumen.bpms.repository.impl;

import org.springframework.stereotype.Repository;
import ru.naumen.bpms.model.ProcessDefinition;
import ru.naumen.bpms.repository.CrudRepository;

import java.util.List;

@Repository
public class ProcessDefinitionRepository implements CrudRepository<ProcessDefinition, Long> {

    private final List<ProcessDefinition> store;

    public ProcessDefinitionRepository(List<ProcessDefinition> store) {
        this.store = store;
    }


    @Override
    public void create(ProcessDefinition entity) {
        store.add(entity);
    }

    @Override
    public ProcessDefinition read(Long id) {
        return store.stream()
                .filter(pd -> pd.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void update(ProcessDefinition entity) {

        for (int i = 0; i < store.size(); i++) {
            if (store.get(i).getId().equals(entity.getId())) {
                store.set(i, entity);
                return;
            }
        }

        throw new IllegalArgumentException(
                "ProcessDefinition с id=" + entity.getId() + " не найден."
        );
    }

    @Override
    public void delete(Long id) {
        ProcessDefinition toRemove = store.stream()
                .filter(pd -> pd.getId().equals(id))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("ProcessDefinition с id=" + id + " не найден."));
        store.remove(toRemove);
    }
}
