package org.bukkit.craftbukkit.entity;

import net.minecraftforge.common.util.FakePlayer;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

// FIXME: THIS CLASS IS BY CatServer
public class CraftFuckPlayer extends CraftPlayer {
    private Player realPlayer = null;

    public CraftFuckPlayer(CraftServer server, FakePlayer entity) {
        super(server, entity);
        realPlayer = getRealPlayer();
    }

    @Override
    public boolean hasPermission(String name) {
        final Player realPlayer = getRealPlayer();
        if (realPlayer == null)
            return super.hasPermission(name);
        return realPlayer.hasPermission(name);
    }

    @Override
    public boolean isPermissionSet(String name) {
        final Player realPlayer = getRealPlayer();
        if (realPlayer == null)
            return super.isPermissionSet(name);
        return realPlayer.isPermissionSet(name);
    }

    @Override
    public boolean isPermissionSet(Permission perm) {
        final Player realPlayer = getRealPlayer();
        if (realPlayer == null)
            return super.isPermissionSet(perm);
        return realPlayer.isPermissionSet(perm);
    }

    @Override
    public boolean hasPermission(Permission perm) {
        final Player realPlayer = getRealPlayer();
        if (realPlayer == null)
            return super.hasPermission(perm);
        return realPlayer.hasPermission(perm);
    }

    private Player getRealPlayer() {
        if (this.realPlayer != null && this.realPlayer.isOnline())
            return this.realPlayer;
        if (realPlayer != null)
            realPlayer = null;
        final String myName = getHandle().getName();
        final Player getRealPlayer = server.getPlayer(myName);
        if (getRealPlayer instanceof CraftFuckPlayer)
            return null;
        if (getRealPlayer != null)
            realPlayer = getRealPlayer;
        return getRealPlayer;
    }
}
