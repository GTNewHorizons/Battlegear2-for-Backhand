package mods.battlegear2;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import xonin.backhand.api.core.BackhandUtils;

public class BackhandProxy {

    public static ItemStack getOffhandItem(EntityPlayer entityPlayer) {
        return BackhandUtils.getOffhandItem(entityPlayer);
    }
}
