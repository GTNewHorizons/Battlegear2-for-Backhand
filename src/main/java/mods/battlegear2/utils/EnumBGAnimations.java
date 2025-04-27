package mods.battlegear2.utils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import mods.battlegear2.Offhand;
import mods.battlegear2.api.core.IBattlePlayer;
import mods.battlegear2.api.shield.IShield;

public enum EnumBGAnimations {

    OffHandSwing {

        @Override
        public void processAnimation(IBattlePlayer entity) {
            entity.battlegear2$swingOffItem();
        }
    },
    SpecialAction {

        @Override
        public void processAnimation(IBattlePlayer entity) {
            ItemStack offhand = Offhand.getOffhandStack((EntityPlayer) entity);
            if (offhand != null && offhand.getItem() instanceof IShield) {
                entity.battlegear2$setSpecialActionTimer(((IShield) offhand.getItem()).getBashTimer(offhand));
            }
        }
    };

    public abstract void processAnimation(IBattlePlayer entity);
}
