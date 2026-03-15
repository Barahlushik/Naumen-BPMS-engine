package ru.naumen.bpms.repository.impl;

import org.springframework.stereotype.Repository;
import ru.naumen.bpms.model.ProcessDefinition;
import ru.naumen.bpms.repository.CrudRepository;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class ProcessDefinitionRepository implements CrudRepository<ProcessDefinition, Long> {

    private final List<ProcessDefinition> store;
    private final AtomicLong idSeq = new AtomicLong(1);

    public ProcessDefinitionRepository(List<ProcessDefinition> store) {
        this.store = store;
    }


    @Override
    public void create(ProcessDefinition entity) {
        entity.setId(idSeq.getAndIncrement());
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
        boolean removed = store.removeIf(pd -> pd.getId().equals(id));

        if (!removed) {
            throw new IllegalArgumentException("ProcessDefinition с id=" + id + " не найден.");
        }

    }
}
