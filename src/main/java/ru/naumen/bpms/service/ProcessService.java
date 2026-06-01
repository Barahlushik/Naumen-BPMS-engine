package ru.naumen.bpms.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public interface ProcessService<Proc, ID> {
    Proc createProcess(@NotNull @Valid Proc process);
    Proc getProcess(@NotNull ID id);
    List<Proc> getAllProcesses();
    Proc updateProcess(@NotNull Proc process);
    void deleteProcess(@NotNull ID id);
}