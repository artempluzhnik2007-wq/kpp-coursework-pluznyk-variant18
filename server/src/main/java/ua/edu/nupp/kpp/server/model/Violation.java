package ua.edu.nupp.kpp.server.model;

import java.time.LocalDate;

public class Violation {


        private long id;
        private long plotId;
        private String description;
        private int severity;
        private LocalDate detectedDate;
        private String status;

        public Violation() {
        }

        public Violation(long id, long plotId, String description,
                         int severity, LocalDate detectedDate, String status) {
            this.id = id;
            this.plotId = plotId;
            this.description = description;
            this.severity = severity;
            this.detectedDate = detectedDate;
            this.status = status;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public long getPlotId() {
            return plotId;
        }

        public void setPlotId(long plotId) {
            this.plotId = plotId;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public int getSeverity() {
            return severity;
        }

        public void setSeverity(int severity) {
            this.severity = severity;
        }

        public LocalDate getDetectedDate() {
            return detectedDate;
        }

        public void setDetectedDate(LocalDate detectedDate) {
            this.detectedDate = detectedDate;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

