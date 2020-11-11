package club.cannoner.crystalmoduleapi;

import lombok.Getter;
import net.md_5.bungee.api.plugin.Plugin;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class CrystalModuleApi extends Plugin {

    @Getter
    private Config config;
    private EventListener listener;

    @Override
    public void onEnable() {
        if (!this.getDataFolder().exists()) {
            if (!this.getDataFolder().mkdir()) {
                this.getLogger().log(Level.SEVERE, "Error creating plugin directory.");
            }
            else {
                try  {
                    File configFile = new File(this.getDataFolder(), "config.yml");
                    if (configFile.createNewFile()) {
                        FileWriter writer = new FileWriter(configFile);
                        InputStream is = this.getClass().getClassLoader().getResourceAsStream("config.yml");
                        InputStreamReader ir = new InputStreamReader(is);
                        BufferedReader reader = new BufferedReader(ir);
                        writer.write(reader.lines().collect(Collectors.joining("\n")));
                        writer.close();
                        is.close();
                        reader.close();
                        ir.close();
                    }
                }
                catch (IOException e) {
                    this.getLogger().log(Level.INFO, "Error creating config file.");
                    e.printStackTrace();
                }
            }
        }

        try {
            this.config = loadConfig(new File(this.getDataFolder(), "config.yml"));
            if (listener == null) {
                listener = new EventListener(this);
            }
            this.getProxy().getPluginManager().registerListener(this, listener);
        }
        catch (IOException e) {
            this.getLogger().log(Level.SEVERE, "Something went wrong loading the config, the plugin will not work.");
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        if (listener != null) {
            this.getProxy().getPluginManager().unregisterListener(listener);
            listener = null;
        }
        if (config != null) {
            config = null;
        }
    }

    private Config loadConfig(File file) throws IOException {
        FileReader reader = new FileReader(file);
        HashMap<String, Boolean> obj = (HashMap<String, Boolean>) new Yaml().load(reader);
        reader.close();
        return new Config(obj);
    }
}
