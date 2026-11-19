package ua.edu.nupp.kpp.server.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Observation {

    private long id;
    private long plotId;
    private LocalDate observationDate;
    private BigDecimal areaAffected;
    private String comment;

    public Observation() {
    }

    public Observation(long id, long plotId, LocalDate observationDate,
                       BigDecimal areaAffected, String comment) {
        this.id = id;
        this.plotId = plotId;
        this.observationDate = observationDate;
        this.areaAffected = areaAffected;
        this.comment = comment;
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

    public LocalDate getObservationDate() {
        return observationDate;
    }

    public void setObservationDate(LocalDate observationDate) {
        this.observationDate = observationDate;
    }

    public BigDecimal getAreaAffected() {
        return areaAffected;
    }

    public void setAreaAffected(BigDecimal areaAffected) {
        this.areaAffected = areaAffected;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}