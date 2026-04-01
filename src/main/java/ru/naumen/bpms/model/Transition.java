package ru.naumen.bpms.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Objects;

@Entity
@Table(
        name = "transitions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_transitions_process_from_to_name",
                        columnNames = {"process_definition_id", "from_step_id", "to_step_id", "name"}
                )
        }
)
public class Transition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "from_step_id", nullable = false)
    private StepDefinition fromStep;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "to_step_id", nullable = false)
    private StepDefinition toStep;

    @Size(max = 100)
    @Column(length = 100)
    private String condition;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "process_definition_id", nullable = false)
    private ProcessDefinition processDefinition;

    protected Transition() {
    }

    public Transition(StepDefinition toStep, String condition) {
        this.toStep = toStep;
        this.condition = condition;
    }

    public Long getId() {
        return id;
    }

    public StepDefinition getFromStep() {
        return fromStep;
    }

    public void setFromStep(StepDefinition fromStep) {
        this.fromStep = fromStep;
    }

    public StepDefinition getToStep() {
        return toStep;
    }

    public void setToStep(StepDefinition toStep) {
        this.toStep = toStep;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public ProcessDefinition getProcessDefinition() {
        return processDefinition;
    }

    public void setProcessDefinition(ProcessDefinition processDefinition) {
        this.processDefinition = processDefinition;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Transition that = (Transition) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(fromStep, that.fromStep) && Objects.equals(toStep, that.toStep) && Objects.equals(condition, that.condition) && Objects.equals(processDefinition, that.processDefinition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, fromStep, toStep, condition, processDefinition);
    }
}