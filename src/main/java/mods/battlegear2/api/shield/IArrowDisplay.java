package mods.battlegear2.api.shield;

import net.minecraft.item.ItemStack;

import mods.battlegear2.items.ItemShield;

/**
 * Defines an {@link Item} that can hold arrows as an internal variable Used by {@link ItemShield} to display blocked
 * arrows
 *
 * @author GotoLink
 */
public interface IArrowDisplay {

    String NBT_KEY_DISABLED = "disabled";

    public void setArrowCount(ItemStack stack, int count);

    public int getArrowCount(ItemStack stack);

    public boolean isDisabled(ItemStack stack);
}
