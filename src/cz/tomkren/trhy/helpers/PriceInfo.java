package cz.tomkren.trhy.helpers;


public class PriceInfo {
    private Double maxSupply;
    private Double bestSupply;
    private Double bestDemand;
    private Double minDemand;

    public PriceInfo(Double maxSupply, Double bestSupply, Double bestDemand, Double minDemand) {
        this.maxSupply = maxSupply;
        this.bestSupply = bestSupply;
        this.bestDemand = bestDemand;
        this.minDemand = minDemand;
    }

    public Double getMaxSupply () {
        return maxSupply;
    }
    public Double getBestSupply() {
        return bestSupply;
    }
    public Double getBestDemand() {
        return bestDemand;
    }
    public Double getMinDemand () {
        return minDemand;
    }

    public Double getHigh() {
        return maxSupply != null ? maxSupply : bestDemand;
    }
    public Double getLow () {
        return minDemand != null ? minDemand : bestSupply;
    }

    public boolean isEmpty () {
        return isNothingToBuy() && isNothingToSell();
    }
    public boolean isNothingToBuy () {
        return bestSupply == null;
    }
    public boolean isNothingToSell() {
        return bestDemand == null;
    }



    @Override
    public String toString() {
        return "[" + maxSupply +", " + bestSupply +", " + bestDemand +", " + minDemand +" ]";
    }
}
