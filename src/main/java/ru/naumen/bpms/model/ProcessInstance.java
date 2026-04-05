package ru.naumen.bpms.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Table(
        name = "process_instances",
        indexes = {
                @Index(name = "idx_process_instances_owner_id", columnList = "owner_id"),
                @Index(name = "idx_process_instances_status", columnList = "status"),
                @Index(name = "idx_process_instances_process_definition_id", columnList = "process_definition_id"),
                @Index(name = "idx_process_instances_current_step_id", columnList = "current_step_id")
        }
)
public class ProcessInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "process_definition_id", nullable = false)
    private ProcessDefinition processDefinition;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_step_id")
    private StepDefinition currentStep;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private ProcessStatus status;

    @NotNull
    @Column(name = "started_at", nullable = false, updatable = false)
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @ManyToMany
    @JoinTable(
            name = "process_instance_participants",
            joinColumns = @JoinColumn(name = "process_instance_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> participants = new LinkedHashSet<>();

    protected ProcessInstance() {
    }

    public ProcessInstance(
            ProcessDefinition processDefinition,
            User owner,
            StepDefinition startStep
    ) {
        setProcessDefinition(processDefinition);
        setOwner(owner);
        setCurrentStep(startStep);
        this.status = ProcessStatus.RUNNING;
        this.startedAt = LocalDateTime.now();
        addParticipant(owner);
    }

    public Long getId() {
        return id;
    }

    public ProcessDefinition getProcessDefinition() {
        return processDefinition;
    }

    public void setProcessDefinition(ProcessDefinition processDefinition) {
        this.processDefinition = processDefinition;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public StepDefinition getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(StepDefinition currentStep) {
        this.currentStep = currentStep;
    }

    public ProcessStatus getStatus() {
        return status;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public Set<User> getParticipants() {
        return participants;
    }

    public void addParticipant(User user) {
        participants.add(user);
    }

    public void removeParticipant(User user) {
        participants.remove(user);
    }

    public void changeOwner(User newOwner) {
        this.owner = newOwner;
        addParticipant(newOwner);
    }

    public void moveToStep(StepDefinition nextStep) {
        this.currentStep = nextStep;
    }

    public void complete() {
        this.status = ProcessStatus.COMPLETED;
        this.finishedAt = LocalDateTime.now();
        this.currentStep = null;
    }

    public void cancel() {
        this.status = ProcessStatus.CANCELLED;
        this.finishedAt = LocalDateTime.now();
        this.currentStep = null;
    }

    @PrePersist
    private void prePersist() {
        if (status == null) {
            status = ProcessStatus.RUNNING;
        }

        if (startedAt == null) {
            startedAt = LocalDateTime.now();
        }
    }

    @Override
    public String toString() {
        return "ProcessInstance{" +
                "id=" + id +
                ", processDefinitionId=" + (processDefinition != null ? processDefinition.getId() : null) +
                ", ownerId=" + (owner != null ? owner.getId() : null) +
                ", currentStepId=" + (currentStep != null ? currentStep.getId() : null) +
                ", status=" + status +
                ", startedAt=" + startedAt +
                ", finishedAt=" + finishedAt +
                ", participantsCount=" + (participants != null ? participants.size() : 0) +
                '}';
    }
}