package mods.battlegear2.api.core;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import mods.battlegear2.api.shield.IShield;

/**
 * Interface added to {@link EntityPlayer} to support offhand management
 *
 * @author GotoLink
 */
public interface IBattlePlayer {

    /**
     * A copied animation for the offhand, similar to {@link EntityPlayer#swingItem()}
     */
    void battlegear2$swingOffItem();

    /**
     * The partial render progress for the offhand swing animation
     */
    float battlegear2$getOffSwingProgress(float frame);

    /**
     * Hotswap the {@link EntityPlayer} current item to offhand, behaves like
     * {@link EntityPlayer#attackTargetEntityWithCurrentItem(Entity)}
     *
     * @param target to attack
     */
    void battlegear2$attackTargetEntityWithCurrentOffItem(Entity target);

    /**
     * Since battle mode is removed in this version, the implementation always returns false. <br>
     * Kept for compat with Angelica
     *
     * @return true if player has pressed the bound key to activate dual-wielding, resulting in current item offset
     */
    boolean battlegear2$isBattlemode();

    /**
     * Helper for {@link IShield} usage
     *
     * @return true if a {@link IShield} is being used in offhand
     */
    boolean battlegear2$isBlockingWithShield();

    /**
     * Helper for {@link IShield} usage, sets the flag according to argument if {@link IShield} is being held in offhand
     *
     * @param block new value for the shield block flag
     */
    void battlegear2$setBlockingWithShield(boolean block);

    /**
     * Getter for the special timer field
     *
     * @return the field value
     */
    int battlegear2$getSpecialActionTimer();

    /**
     * Setter for the special timer field
     *
     * @param time new value to set
     */
    void battlegear2$setSpecialActionTimer(int time);
}
