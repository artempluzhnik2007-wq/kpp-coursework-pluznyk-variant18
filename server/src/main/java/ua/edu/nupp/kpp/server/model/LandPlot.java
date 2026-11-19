
package ua.edu.nupp.kpp.server.model;

public class LandPlot {

    private long id;
    private String address;
    private double area;
    private int ecologicalScore;

    public LandPlot() {
    }

    public LandPlot(long id, String address, double area, int ecologicalScore) {
        this.id = id;
        this.address = address;
        this.area = area;
        this.ecologicalScore = ecologicalScore;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getArea() {
        return area;
    }

    public void setArea(double area) {
        this.area = area;
    }

    public int getEcologicalScore() {
        return ecologicalScore;
    }

    public void setEcologicalScore(int ecologicalScore) {
        this.ecologicalScore = ecologicalScore;
    }
}


