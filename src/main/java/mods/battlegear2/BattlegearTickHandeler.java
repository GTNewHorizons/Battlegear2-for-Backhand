package mods.battlegear2;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import mods.battlegear2.api.core.IBattlePlayer;
import mods.battlegear2.api.quiver.QuiverArrowRegistry;
import mods.battlegear2.api.shield.IShield;
import xonin.backhand.api.core.BackhandUtils;

public final class BattlegearTickHandeler {

    public static final BattlegearTickHandeler INSTANCE = new BattlegearTickHandeler();

    private BattlegearTickHandeler() {}

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            tickStart(event.player);
        } else {
            tickEnd(event.player);
        }
    }

    public void tickStart(EntityPlayer entityPlayer) {

        // if (!entityPlayer.worldObj.isRemote && entityPlayer.worldObj instanceof WorldServer) {
        //
        // if (((IInventoryPlayerBattle) entityPlayer.inventory).battlegear2$isDirty()) {
        // ((WorldServer) entityPlayer.worldObj).getEntityTracker()
        // .func_151248_b(entityPlayer, new BattlegearSyncItemPacket(entityPlayer).generatePacket());
        // ((IBattlePlayer) entityPlayer).battlegear2$setSpecialActionTimer(0);
        // ((IInventoryPlayerBattle) entityPlayer.inventory).battlegear2$setDirty(entityPlayer.ticksExisted < 10);
        // }
        // // Force update every 3 seconds
        // else if (((IBattlePlayer) entityPlayer).battlegear2$isBattlemode()
        // && entityPlayer.ticksExisted % BattlegearConfig.updateRate == 0
        // && !entityPlayer.isUsingItem()) {
        // ((WorldServer) entityPlayer.worldObj).getEntityTracker().func_151248_b(
        // entityPlayer,
        // new BattlegearSyncItemPacket(entityPlayer).generatePacket());
        // }
        // }
    }

    public void tickEnd(EntityPlayer entityPlayer) {
        int timer = ((IBattlePlayer) entityPlayer).battlegear2$getSpecialActionTimer();
        if (timer > 0) {
            ((IBattlePlayer) entityPlayer).battlegear2$setSpecialActionTimer(timer - 1);
            int targetTime = -1;
            ItemStack offhand = BackhandUtils.getOffhandItem(entityPlayer);
            if (offhand != null && offhand.getItem() instanceof IShield) {
                targetTime = ((IShield) offhand.getItem()).getBashTimer(offhand) / 2;
            } else {
                offhand = QuiverArrowRegistry.getArrowContainer(entityPlayer);
                if (offhand != null) {
                    targetTime = 0;
                }
            }
            if (timer - 1 == targetTime) {
                Battlegear.proxy.doSpecialAction(entityPlayer, offhand);
            }
        }
    }
}
