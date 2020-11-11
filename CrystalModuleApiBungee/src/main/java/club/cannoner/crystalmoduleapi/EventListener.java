package club.cannoner.crystalmoduleapi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.protocol.packet.PluginMessage;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class EventListener implements Listener {
    private CrystalModuleApi plugin;
    private Gson gson;

    public EventListener(CrystalModuleApi plugin) {
        this.plugin = plugin;
        this.gson = new GsonBuilder().disableHtmlEscaping().create();
    }

    @EventHandler
    public void onPlayerJoin(PostLoginEvent e) throws IOException {
        String json = gson.toJson(this.plugin.getConfig());
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);
        out.writeUTF(json);
        e.getPlayer().unsafe().sendPacket(new PluginMessage("crystal:modules", b.toByteArray(), true));
        out.close();
        b.close();
    }
}