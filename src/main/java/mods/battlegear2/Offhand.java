package mods.battlegear2;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.Loader;
import mods.battlegear2.api.core.IInventoryPlayerBattle;

public class Offhand {

    public static ItemStack getOffhandStack(EntityPlayer entityPlayer) {
        ItemStack offhandItem = null;
        if (Loader.isModLoaded("backhand")) {
            offhandItem = BackhandProxy.getOffhandItem(entityPlayer);
        }
        if (offhandItem == null) {
            offhandItem = ((IInventoryPlayerBattle) entityPlayer.inventory).battlegear2$getCurrentOffhandWeapon();
        }
        return offhandItem;
    }

}
