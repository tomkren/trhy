package cz.tomkren.trhy;


public class PriceInfo {
    private double high;
    private double bestSupply;
    private double bestDemand;
    private double low;

    public PriceInfo(double high, double bestSupply, double bestDemand, double low) {
        this.high = high;
        this.bestSupply = bestSupply;
        this.bestDemand = bestDemand;
        this.low = low;
    }

    public double getHigh() {
        return high;
    }

    public double getBestSupply() {
        return bestSupply;
    }

    public double getBestDemand() {
        return bestDemand;
    }

    public double getLow() {
        return low;
    }
}
