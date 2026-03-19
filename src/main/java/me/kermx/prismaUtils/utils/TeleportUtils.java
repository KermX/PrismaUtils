package me.kermx.prismaUtils.utils;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class TeleportUtils {

    private TeleportUtils() {
        throw new UnsupportedOperationException("Utility class (TextUtils) - cannot be instantiated");
    }

    private static final Set<UUID> IN_FLIGHT = ConcurrentHashMap.newKeySet();

    public static boolean tryBeginTeleport(Player player) {
        return IN_FLIGHT.add(player.getUniqueId());
    }

    public static void endTeleport(Player player) {
        IN_FLIGHT.remove(player.getUniqueId());
    }

    public static CompletableFuture<Void> ensureChunkReady(JavaPlugin plugin, Location location) {
        if (location == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("location is null"));
        }
        World world = location.getWorld();
        if (world == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("location world is null"));
        }

        int chunkX = location.getBlockX() >> 4;
        int chunkZ = location.getBlockZ() >> 4;
        try {
            Method m = world.getClass().getMethod("getChunkAtAsync", int.class, int.class, boolean.class);
            @SuppressWarnings("unchecked")
            CompletableFuture<Chunk> chunkFuture = (CompletableFuture<Chunk>) m.invoke(world, chunkX, chunkZ, true);
            return chunkFuture.thenApply(chunk -> null);
        } catch (NoSuchMethodException ignored) {
            // continue to next attempt
        } catch (Throwable t) {
            // If reflection fails for any reason, fall back to sync load below
        }

        try {
            Method m = world.getClass().getMethod("getChunkAtAsync", int.class, int.class);
            @SuppressWarnings("unchecked")
            CompletableFuture<Chunk> chunkFuture = (CompletableFuture<Chunk>) m.invoke(world, chunkX, chunkZ);
            return chunkFuture.thenApply(chunk -> null);
        } catch (NoSuchMethodException ignored) {
            // continue to fallback
        } catch (Throwable t) {
            // fall back to sync load
        }

        // Fallback: sync load on main thread
        CompletableFuture<Void> future = new CompletableFuture<>();
        Runnable loadTask = () -> {
            try {
                Chunk chunk = world.getChunkAt(chunkX, chunkZ);
                if (!chunk.isLoaded()) {
                    chunk.load(true);
                }
                future.complete(null);
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        };

        if (Bukkit.isPrimaryThread()) {
            loadTask.run();
        } else {
            Bukkit.getScheduler().runTask(plugin, loadTask);
        }

        return future;
    }

    public static CompletableFuture<Boolean> teleportAsyncWithChunkReady(
            JavaPlugin plugin,
            Player player,
            Location location,
            PlayerTeleportEvent.TeleportCause cause
    ) {
        return ensureChunkReady(plugin, location)
                .thenCompose(ignored -> player.teleportAsync(location, cause));
    }
}
