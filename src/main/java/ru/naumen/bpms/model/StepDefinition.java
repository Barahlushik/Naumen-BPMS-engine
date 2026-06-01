package ru.naumen.bpms.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(
        name = "step_definitions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_step_definitions_process_definition_name",
                        columnNames = {"process_definition_id", "name"}
                )
        }
)
public class StepDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private StepType type;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "process_definition_id", nullable = false)
    private ProcessDefinition processDefinition;

    @OneToMany(
            mappedBy = "fromStep",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<Transition> outgoingTransitions = new LinkedHashSet<>();

    protected StepDefinition() {
    }

    public StepDefinition(String name, StepType type) {
        this.name = name;
        this.type = type;
    }

    public void addOutgoingTransition(Transition transition) {
        transition.setFromStep(this);
        outgoingTransitions.add(transition);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public StepType getType() {
        return type;
    }

    public ProcessDefinition getProcessDefinition() {
        return processDefinition;
    }

    public void setProcessDefinition(ProcessDefinition processDefinition) {
        this.processDefinition = processDefinition;
    }

    public Set<Transition> getOutgoingTransitions() {
        return outgoingTransitions;
    }

    public void setOutgoingTransitions(Set<Transition> outgoingTransitions) {
        this.outgoingTransitions = outgoingTransitions;
    }
}