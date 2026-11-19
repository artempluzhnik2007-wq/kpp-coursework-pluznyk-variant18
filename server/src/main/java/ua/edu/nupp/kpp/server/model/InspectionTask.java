package ua.edu.nupp.kpp.server.model;

import java.time.LocalDate;

public class InspectionTask {

    private long id;
    private long violationId;
    private LocalDate plannedDate;
    private LocalDate completionDate;
    private String taskStatus;

    public InspectionTask() {
    }

    public InspectionTask(long id, long violationId,
                          LocalDate plannedDate, LocalDate completionDate,
                          String taskStatus) {
        this.id = id;
        this.violationId = violationId;
        this.plannedDate = plannedDate;
        this.completionDate = completionDate;
        this.taskStatus = taskStatus;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getViolationId() {
        return violationId;
    }

    public void setViolationId(long violationId) {
        this.violationId = violationId;
    }

    public LocalDate getPlannedDate() {
        return plannedDate;
    }

    public void setPlannedDate(LocalDate plannedDate) {
        this.plannedDate = plannedDate;
    }

    public LocalDate getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(LocalDate completionDate) {
        this.completionDate = completionDate;
    }

    public String getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(String taskStatus) {
        this.taskStatus = taskStatus;
    }
}
