package ru.naumen.bpms.model.report;

import jakarta.persistence.*;

@Entity
@Table(name = "reports")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReportStatus status = ReportStatus.CREATED;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    protected Report() {
    }

    public Report(ReportStatus status, String content) {
        this.status = status;
        this.content = content;
    }

    public static Report created() {
        return new Report(ReportStatus.CREATED, null);
    }

    public Long getId() {
        return id;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
