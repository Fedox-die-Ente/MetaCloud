package eu.metacloudservice.webserver.dummys.liveservice;

import eu.metacloudservice.configuration.interfaces.IConfigAdapter;

import java.util.ArrayDeque;
import java.util.ArrayList;

public class LiveServiceList implements IConfigAdapter {


    private String cloudServiceSplitter;
    private ArrayDeque<String> cloudServices;

    public LiveServiceList() {}

    public String getCloudServiceSplitter() {
        return cloudServiceSplitter;
    }

    public ArrayDeque<String> getCloudServices() {
        return cloudServices;
    }

    public void  remove(String service){
        cloudServices.removeIf(s -> s.equalsIgnoreCase(service));
    }

    public void setCloudServiceSplitter(String cloudServiceSplitter) {
        this.cloudServiceSplitter = cloudServiceSplitter;
    }

    public void setCloudServices(ArrayDeque<String> cloudServices) {
        this.cloudServices = cloudServices;
    }
}
