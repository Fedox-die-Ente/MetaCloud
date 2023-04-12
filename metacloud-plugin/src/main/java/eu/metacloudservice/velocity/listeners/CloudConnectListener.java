package eu.metacloudservice.velocity.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import eu.metacloudservice.CloudAPI;
import eu.metacloudservice.Driver;
import eu.metacloudservice.async.AsyncCloudAPI;
import eu.metacloudservice.configuration.ConfigDriver;
import eu.metacloudservice.configuration.dummys.serviceconfig.LiveService;
import eu.metacloudservice.groups.dummy.Group;
import eu.metacloudservice.networking.in.service.playerbased.PacketInPlayerConnect;
import eu.metacloudservice.networking.in.service.playerbased.PacketInPlayerDisconnect;
import eu.metacloudservice.networking.in.service.playerbased.PacketInPlayerSwitchService;
import eu.metacloudservice.pool.service.entrys.CloudService;
import eu.metacloudservice.velocity.VelocityBootstrap;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.UUID;

public class CloudConnectListener {


    private final ArrayList<UUID> connected = new ArrayList<>();
    private final ProxyServer server;
    public ServerInfo target;

    public CloudConnectListener(ProxyServer server) {
        this.server = server;
    }


    @Subscribe
    public void handel(ServerPreConnectEvent event){
        if (event.getOriginalServer() == null){
            target = server.getServer(VelocityBootstrap.getLobby(event.getPlayer()).getName()).get().getServerInfo();
            if (target != null){
                event.setResult(ServerPreConnectEvent.ServerResult.allowed(server.getServer(target.getName()).get()));
            }else event.setResult(ServerPreConnectEvent.ServerResult.denied());
        }
    }


    @Subscribe
    public void handle(PostLoginEvent event){
        LiveService service = (LiveService)(new ConfigDriver("./CLOUDSERVICE.json")).read(LiveService.class);
        Group group = CloudAPI.getInstance().getGroups().parallelStream().filter(group1 -> group1.getGroup().equalsIgnoreCase(service.getGroup())).findFirst().get();

        this.connected.add(event.getPlayer().getUniqueId());
        CloudAPI.getInstance().sendPacketSynchronized(new PacketInPlayerConnect(event.getPlayer().getUsername(), service.getService()));

        if (group.isMaintenance()
                && !server.getPlayer(event.getPlayer().getUniqueId()).get().hasPermission("metacloud.connection.maintenance")
                && !CloudAPI.getInstance().getWhitelist().contains(server.getPlayer(event.getPlayer().getUniqueId()).get().getUsername())){
            event.getPlayer().disconnect(Component.text(Driver.getInstance().getMessageStorage().base64ToUTF8(CloudAPI.getInstance().getMessages().getKickNetworkIsMaintenance()).replace("&", "§")));

        }else if (CloudAPI.getInstance().getPlayerPool().getPlayers().size() >= group.getMaxPlayers().intValue()

                && !server.getPlayer(event.getPlayer().getUniqueId()).get().hasPermission("metacloud.connection.full")
                && !CloudAPI.getInstance().getWhitelist().contains(server.getPlayer(event.getPlayer().getUniqueId()).get().getUsername())){
            event.getPlayer().disconnect(Component.text(Driver.getInstance().getMessageStorage().base64ToUTF8(CloudAPI.getInstance().getMessages().getKickNetworkIsFull()).replace("&", "§")));

        }else if (server.getPlayer(event.getPlayer().getUniqueId()).isPresent()
                && VelocityBootstrap.getLobby( server.getPlayer(event.getPlayer().getUniqueId()).get()) == null){
            event.getPlayer().disconnect(Component.text(Driver.getInstance().getMessageStorage().base64ToUTF8(CloudAPI.getInstance().getMessages().getKickNoFallback()).replace("&", "§")));

        }
    }

    @Subscribe
    public void handle(DisconnectEvent event){
        if (this.connected.contains(event.getPlayer().getUniqueId())) {
            CloudAPI.getInstance().sendPacketSynchronized(new PacketInPlayerDisconnect(event.getPlayer().getUsername()));
        }
    }

    @Subscribe
    public void handle(ServerConnectedEvent event){
        CloudAPI.getInstance().sendPacketSynchronized(new PacketInPlayerSwitchService(event.getPlayer().getUsername(), event.getServer().getServerInfo().getName()));
    }

    @Subscribe
    public void handle(KickedFromServerEvent event){
        if (event.getPlayer().isActive()) {
            CloudService target = VelocityBootstrap.getLobby(event.getPlayer(), event.getServer().getServerInfo().getName());
            if (target != null) {
                event.setResult(KickedFromServerEvent.RedirectPlayer.create(server.getServer(target.getName()).get()));
            } else {
                event.setResult(KickedFromServerEvent.DisconnectPlayer.create(Component.text(Driver.getInstance().getMessageStorage().base64ToUTF8(CloudAPI.getInstance().getMessages().getKickNoFallback()).replace("&", "§"))));

            }
        }
    }


}
