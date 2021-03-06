public class Spike {

    public int latency;
    public int ttl;

    public Spike(int latency) {
        this.latency = latency;
        this.ttl = TsuICMP.msListSize;
    }

}
