package mods.battlegear2;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.Loader;
import mods.battlegear2.api.core.IBattlePlayer;
import mods.battlegear2.api.core.IInventoryPlayerBattle;
import xonin.backhand.api.core.BackhandUtils;

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

    public static boolean isOffhandVisible(EntityPlayer player) {
        return ((IBattlePlayer) player).battlegear2$isBattlemode() || Loader.isModLoaded("backhand");
    }

    public static boolean isOffhandSlot(int slot, EntityPlayer player) {
        return slot == BackhandUtils.getOffhandSlot(player) && Loader.isModLoaded("backhand");
    }
}
