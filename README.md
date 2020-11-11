# Crystal Module API
Two plugin implementations of Crystal Mod's Module API - one for BungeeCord and one for Spigot.

Generates a `.yml` configuration file on first launch, setting any of the modules to `true` will disable it on your server.

### Installation
Installed the same way as any other plugin.

Download the `.jar` file from the releases section of this repository and drag it into your server's plugins folder.

It is recommended that you use the BungeeCord plugin as it is more stable. If you don't have to use the Spigot version then do not. One reason you may need to use the Spigot version is if you want to disable different modules for different realms as the BungeeCord version would apply those settings to all servers.

May have issues running if your server is (for some reason) running on a Java language level lower than `1.8`. If this ends up becoming an issue I'll fix it, however you should be running `1.8` anyway.

### Technical Information
Utilizes the plugin channel system implemented by Minecraft, Forge, Bukkit and Spigot to send a custom packet from the server to the client upon login on the channel `crystal:modules`. This message structure is as follows:
- Discriminator (1 byte - should always be `00`)
- Length (1 byte - the length of the following text)
- Message (`Length` bytes - contains UTF-8 encoded JSON with relevant module data)

### Credits
Huge thanks to [Badlion Client](https://github.com/BadlionClient/BadlionClientModAPI/blob/master/blcmodapibukkit/src/main/java/net/badlion/blcmodapibukkit/listener/PlayerListener.java) - without them the Spigot implementation would have involved significantly more banging my head against the desk, among other things.
[BungeeCord](https://github.com/SpigotMC/BungeeCord/)
[Spigot](https://hub.spigotmc.org/stash/projects/SPIGOT)
[Bukkit](https://github.com/Bukkit/Bukkit)
[SnakeYAML](https://github.com/asomov/snakeyaml)