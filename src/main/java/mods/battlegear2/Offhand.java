package mods.battlegear2;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.Loader;
import xonin.backhand.api.core.BackhandUtils;

public class Offhand {

    public static ItemStack getOffhandStack(EntityPlayer entityPlayer) {
        ItemStack offhandItem = null;
        if (Loader.isModLoaded("backhand")) {
            offhandItem = BackhandProxy.getOffhandItem(entityPlayer);
        }
        return offhandItem;
    }

    public static boolean isOffhandSlot(int slot, EntityPlayer player) {
        return slot == BackhandUtils.getOffhandSlot(player);
    }
}
