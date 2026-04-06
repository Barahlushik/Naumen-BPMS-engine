package ru.naumen.bpms.model;

import jakarta.persistence.*;

import java.util.LinkedHashSet;
import java.util.Set;


@Entity
@Table(name = "process_definitions")
public class ProcessDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 60)
    private String category;

    @Column(length = 500)
    private String description;

    @OneToMany(
            mappedBy = "processDefinition",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<StepDefinition> steps = new LinkedHashSet<>();

    @OneToMany(
            mappedBy = "processDefinition",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<Transition> transitions = new LinkedHashSet<>();

    protected ProcessDefinition() {
    }

    public ProcessDefinition(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public void addStep(StepDefinition step) {
        step.setProcessDefinition(this);
        steps.add(step);
    }

    public void addTransition(Transition transition) {
        transition.setProcessDefinition(this);
        transitions.add(transition);
    }

    public Long getId() { return id; }

    public String getTitle() { return title; }
    public void setTitle(String title) {this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Set<StepDefinition> getSteps() { return steps; }
    public void setSteps(Set<StepDefinition> steps) { this.steps = steps; }

    public Set<Transition> getTransitions() { return transitions; }
    public void setTransitions(Set<Transition> transitions) { this.transitions = transitions; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
