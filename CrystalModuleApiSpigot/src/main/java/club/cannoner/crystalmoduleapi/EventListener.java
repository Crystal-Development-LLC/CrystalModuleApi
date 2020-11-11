package club.cannoner.crystalmoduleapi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;

// MIT License
//
// Copyright (c) 2018 Turtle Entertainment Online, Inc
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

public class EventListener implements Listener {
    private CrystalModuleApi plugin;
    private Gson gson;

    private String versionSuffix;
    private Class<?> craftPlayerClass;
    private Method getHandleMethod;

    private Class<?> nmsPlayerClass;
    private Field playerConnectionField;

    private Class<?> playerConnectionClass;
    private Method sendPacketMethod;

    private Class<?> packetPlayOutCustomPayloadClass;
    private Constructor<?> packetPlayOutCustomPayloadConstructor;

    // Bukkit 1.8+ support
    private Class<?> packetDataSerializerClass;
    private Constructor<?> packetDataSerializerConstructor;

    // Netty classes used by newer 1.8 and newer
    private Class<?> byteBufClass;
    private Class<?> unpooledClass;
    private Method wrappedBufferMethod;

    public EventListener(CrystalModuleApi plugin) {
        this.plugin = plugin;
        this.gson = new GsonBuilder().disableHtmlEscaping().create();

        // Get the v1_X_Y from the end of the package name, e.g. v_1_7_R4 or v_1_12_R1
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        String[] parts = packageName.split("\\.");

        if (parts.length > 0) {
            String suffix = parts[parts.length - 1];
            if (!suffix.startsWith("v")) {
                throw new RuntimeException("Failed to find version for running Minecraft server, got suffix " + suffix);
            }

            this.versionSuffix = suffix;

            this.plugin.getLogger().info("Found version " + this.versionSuffix);
        }

        // We need to use reflection because Bukkit by default handles plugin messages in a really silly way
        this.craftPlayerClass = ReflectionHelper.getClass("org.bukkit.craftbukkit." + this.versionSuffix + ".entity.CraftPlayer");
        if (this.craftPlayerClass == null) {
            throw new RuntimeException("Failed to find CraftPlayer class");
        }

        this.nmsPlayerClass = ReflectionHelper.getClass("net.minecraft.server." + this.versionSuffix + ".EntityPlayer");
        if (this.nmsPlayerClass == null) {
            throw new RuntimeException("Failed to find EntityPlayer class");
        }

        this.playerConnectionClass = ReflectionHelper.getClass("net.minecraft.server." + this.versionSuffix + ".PlayerConnection");
        if (this.playerConnectionClass == null) {
            throw new RuntimeException("Failed to find PlayerConnection class");
        }

        this.packetPlayOutCustomPayloadClass = ReflectionHelper.getClass("net.minecraft.server." + this.versionSuffix + ".PacketPlayOutCustomPayload");
        if (this.packetPlayOutCustomPayloadClass == null) {
            throw new RuntimeException("Failed to find PacketPlayOutCustomPayload class");
        }

        this.packetPlayOutCustomPayloadConstructor = ReflectionHelper.getConstructor(this.packetPlayOutCustomPayloadClass, String.class, byte[].class);
        if (this.packetPlayOutCustomPayloadConstructor == null) {
            // Newer versions of Minecraft use a different custom packet system
            this.packetDataSerializerClass = ReflectionHelper.getClass("net.minecraft.server." + this.versionSuffix + ".PacketDataSerializer");
            if (this.packetDataSerializerClass == null) {
                throw new RuntimeException("Failed to find PacketPlayOutCustomPayload constructor or PacketDataSerializer class");
            }

            this.byteBufClass = ReflectionHelper.getClass("io.netty.buffer.ByteBuf");
            if (this.byteBufClass == null) {
                throw new RuntimeException("Failed to find PacketPlayOutCustomPayload constructor or ByteBuf class");
            }

            this.packetDataSerializerConstructor = ReflectionHelper.getConstructor(this.packetDataSerializerClass, this.byteBufClass);
            if (this.packetDataSerializerConstructor == null) {
                throw new RuntimeException("Failed to find PacketPlayOutCustomPayload constructor or PacketDataSerializer constructor");
            }

            this.unpooledClass = ReflectionHelper.getClass("io.netty.buffer.Unpooled");
            if (this.unpooledClass == null) {
                throw new RuntimeException("Failed to find PacketPlayOutCustomPayload constructor or Unpooled class");
            }

            this.wrappedBufferMethod = ReflectionHelper.getMethod(this.unpooledClass, "wrappedBuffer", byte[].class);
            if (this.wrappedBufferMethod == null) {
                throw new RuntimeException("Failed to find PacketPlayOutCustomPayload constructor or wrappedBuffer()");
            }

            // If we made it this far in theory we are on at least 1.8
            this.packetPlayOutCustomPayloadConstructor = ReflectionHelper.getConstructor(this.packetPlayOutCustomPayloadClass, String.class, this.packetDataSerializerClass);
            if (this.packetPlayOutCustomPayloadConstructor == null) {
                throw new RuntimeException("Failed to find PacketPlayOutCustomPayload constructor 2x");
            }
        }

        this.getHandleMethod = ReflectionHelper.getMethod(this.craftPlayerClass, "getHandle");
        if (this.getHandleMethod == null) {
            throw new RuntimeException("Failed to find CraftPlayer.getHandle()");
        }

        this.playerConnectionField = ReflectionHelper.getField(this.nmsPlayerClass, "playerConnection");
        if (this.playerConnectionField == null) {
            throw new RuntimeException("Failed to find EntityPlayer.playerConnection");
        }

        this.sendPacketMethod = ReflectionHelper.getMethod(this.playerConnectionClass, "sendPacket");
        if (this.sendPacketMethod == null) {
            throw new RuntimeException("Failed to find PlayerConnection.sendPacket()");
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) throws IOException {
        String json = gson.toJson(this.plugin.getPluginConfig());
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);
        out.writeUTF(json);
        String channel = "crystal:modules";
        byte[] message = b.toByteArray();
        try {
            Object packet;

            // Newer MC version, setup ByteBuf object
            if (this.packetDataSerializerClass != null) {
                Object byteBuf = this.wrappedBufferMethod.invoke(null, message);
                Object packetDataSerializer = this.packetDataSerializerConstructor.newInstance(byteBuf);

                packet = this.packetPlayOutCustomPayloadConstructor.newInstance(channel, packetDataSerializer);
            }
            else {
                // Work our magic to make the packet
                packet = this.packetPlayOutCustomPayloadConstructor.newInstance(channel, message);
            }

            // Work our magic to send the packet
            Object nmsPlayer = this.getHandleMethod.invoke(e.getPlayer());
            Object playerConnection = this.playerConnectionField.get(nmsPlayer);
            this.sendPacketMethod.invoke(playerConnection, packet);
        }
        catch (IllegalAccessException | InstantiationException | InvocationTargetException ex) {
            this.plugin.getLogger().log(Level.SEVERE, "Error sending packet");
            ex.printStackTrace();
        }
        out.close();
        b.close();
    }
}
