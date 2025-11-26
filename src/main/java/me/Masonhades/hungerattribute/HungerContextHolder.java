package me.Masonhades.hungerattribute;

import net.minecraft.world.entity.player.Player;

public class HungerContextHolder {
    public static final ThreadLocal<Player> PLAYER = new ThreadLocal<>();
}

