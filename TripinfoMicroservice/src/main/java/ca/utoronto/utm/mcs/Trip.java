package ca.utoronto.utm.mcs;

public class Trip {

    private String _id = null;
    private double distance;
    private double totalCost;
    private long startTime;
    private long endTime;
    private String timeElapsed;
    private String driver = null;
    private double driverPayout;
    private String passenger = null;
    private int discount;

    public String getTimeElapsed() {
        return timeElapsed;
    }

    public void setTimeElapsed(String timeElapsed) {
        this.timeElapsed = timeElapsed;
    }

    public void setId(String id) {
        this._id = id;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }


    public void setDriver(String driver) {
        this.driver = driver;
    }

    public void setDriverPayout(double driverPayout) {
        this.driverPayout = driverPayout;
    }

    public void setPassenger(String passenger) {
        this.passenger = passenger;
    }

    public void setDiscount(int discount) {
        this.discount = discount;
    }

    public String getId() {
        return _id;
    }

    public double getDistance() {
        return distance;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }


    public String getDriver() {
        return driver;
    }

    public double getDriverPayout() {
        return driverPayout;
    }

    public String getPassenger() {
        return passenger;
    }

    public int getDiscount() {
        return discount;
    }
}
