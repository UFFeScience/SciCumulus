package environment;

/**
 *
 * @author pedro, Vítor
 */
public class AmazonMachine {
    
    int rank;
    public String name = new String();
    public String ami = new String();
    public String type = new String();
    public int cores = 1;
    public String publicDNS = new String();
    public String privateIP = new String();
    public String publicIP = new String();

    public int getRank() {
        return rank;
    }

    public String getName() {
        return name;
    }

    public String getAmi() {
        return ami;
    }

    public String getPublicDNS() {
        return publicDNS;
    }

    private void setRank(int rank) {
        this.rank = rank;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAmi(String ami) {
        this.ami = ami;
    }

    private void setPublicDNS(String publicDNS) {
        this.publicDNS = publicDNS;
    }

    public String getPrivateIP() {
        return privateIP;
    }

    private void setPrivateIP(String privateIP) {
        this.privateIP = privateIP;
    }

    private void setPublicIP(String publicIP) {
        this.publicIP = publicIP;
    }

    public String getPublicIP() {
        return publicIP;
    }

    public String getType() {
        return type;
    }

    private void setType(String type) {
        this.type = type;
    }
    
    public int getCores() {
        return cores;
    }

    private void setCores(int cores) {
        this.cores = cores;
    }

    public enum Status {
        PENDING, RUNNING, TERMINATED
    }
    
    @Override
    public String toString() {
        return "Machine{" + "rank=" + rank + ", type=" + type + ", publicDNS=" + publicDNS + ", privateIP=" + privateIP + ", publicIP=" + publicIP + '}';
    }
  
    public AmazonMachine(int Rank, String publicDNS, String publicIP, String privateIP, String type, int cores) {
        setRank(Rank);
        setPublicDNS(publicDNS);
        setPublicIP(publicIP);
        setPrivateIP(privateIP);
        setType(type);
        setCores(cores);
    }
    
    public AmazonMachine(String publicDNS, String publicIP, String privateIP) {
        setPublicDNS(publicDNS);
        setPublicIP(publicIP);
        setPrivateIP(privateIP);
    }
}
