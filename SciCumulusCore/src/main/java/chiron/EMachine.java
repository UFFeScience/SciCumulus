package chiron;

import chiron.adaptive.ALinpack;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 *
 * @author vitor
 */
public class EMachine {
    
    public Integer ID = null;
    public double MflopsPerSecond;
//    reliability
    public int processedActivations = 0;
    public int failures = 0;
//    properties for cloud environment
    public String publicDNS = null;
    public String publicIP = null;
    public String privateIP = new String();
    public int rank;
    public String name = new String();
    public String ami = new String();
    public String type = new String();
    public int cores = 1;
    
    public EMachine() throws UnknownHostException{
        initInstance();
    }
    
    public EMachine(String publicDNS, int rank, int cores){
        this.publicDNS = publicDNS;
        this.rank = rank;
        this.cores = cores;
    }
    
    public EMachine(int rank, String publicDNS, String publicIP, String privateIP, String type, int cores){
        ALinpack l = new ALinpack();
        l.run_benchmark();
        this.MflopsPerSecond = l.MflopsPerSecond;
            
        this.rank = rank;
        this.publicDNS = publicDNS;
        this.publicIP = publicIP;
        this.privateIP = privateIP;
        this.type = type;
        this.cores = cores;
    }
    
    public synchronized int incrementProcessedActivations(){
        return ++processedActivations;
    }
    
    public synchronized int incrementFailuresInActivations(){
        processedActivations++;
        return ++failures;
    }
    
    public double computeReliability(){
        double reliability = ((double) (processedActivations - failures)) / ((double) processedActivations);
        return reliability;
    }

    private void initInstance() throws UnknownHostException {
//        long starttime = System.currentTimeMillis();
        int numberOfIterations = 1000;
        ALinpack l;
        double sum = 0.0;
        for(int i=0; i<numberOfIterations; i++){
            l = new ALinpack();
            l.run_benchmark();
            sum += l.MflopsPerSecond;
        }
        this.MflopsPerSecond = sum / (double) numberOfIterations;
//        long endtime = System.currentTimeMillis();
//        System.out.println(endtime - starttime);
        
        InetAddress inetAddress = Inet4Address.getLocalHost();
        this.publicDNS = inetAddress.getHostName();
        this.publicIP = inetAddress.getHostAddress();
    }
    
}
