package eu.metacloudservice.async.pool.service;

import eu.metacloudservice.async.pool.service.entrys.CloudService;
import eu.metacloudservice.process.ServiceState;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ServicePool {


    private final ArrayList<CloudService> connectedServices;

    public ServicePool() {
        connectedServices = new ArrayList<>();
    }

    public CompletableFuture<List<CloudService>> getServices(){
        return CompletableFuture.supplyAsync(() -> connectedServices);
    }

    public CompletableFuture<CloudService> getService(@NonNull String name){
        return CompletableFuture.supplyAsync(() -> connectedServices.stream().filter(cloudService -> cloudService.getName().equalsIgnoreCase(name)).findFirst().orElse(null));
    }

    public CompletableFuture<List<CloudService>> getServicesByGroup(@NonNull String group){
        return CompletableFuture.supplyAsync(() -> connectedServices.stream().filter(cloudService -> cloudService.getGroup()== null ? false : cloudService.getGroup().getGroup().equals(group)).collect(Collectors.toList()));
    }

    public CompletableFuture<List<CloudService>> getServicesByState(@NonNull  ServiceState state){
        return CompletableFuture.supplyAsync( () ->connectedServices.stream().filter(cloudService -> cloudService.getState() == state).collect(Collectors.toList()));
    }

    public boolean serviceNotNull(@NonNull String name){
        return connectedServices.stream().anyMatch(service -> service.getName().equalsIgnoreCase(name));
    }

    public boolean registerService(CloudService service){
        if (connectedServices.stream().noneMatch(service1 -> service1.getName().equals(service.getName()))){
            connectedServices.add(service);
            return true;
        }else {
            return false;
        }
    }

    public boolean unregisterService(String service){
        if (connectedServices.stream().anyMatch(service1 -> service1.getName().equals(service))){
            connectedServices.removeIf(service1 -> service1.getName().equals(service));
            return true;
        }else {
            return false;
        }
    }
}
