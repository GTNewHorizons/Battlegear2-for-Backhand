package mods.battlegear2.items;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;

import baubles.api.BaubleType;
import baubles.api.expanded.BaubleExpandedSlots;
import baubles.api.expanded.BaubleItemHelper;
import baubles.api.expanded.IBaubleExpanded;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.battlegear2.api.IDyable;
import mods.battlegear2.api.PlayerEventChild;
import mods.battlegear2.api.core.BattlegearUtils;
import mods.battlegear2.api.quiver.IArrowContainer2;
import mods.battlegear2.api.quiver.QuiverArrowRegistry;
import xonin.backhand.api.core.BackhandUtils;

public class ItemQuiver extends Item implements IArrowContainer2, IDyable, IBaubleExpanded {

    private static final String[] baubleTypes = { BaubleExpandedSlots.quiverType };

    public IIcon quiverDetails;
    public IIcon quiverArrows;

    public ItemQuiver() {
        super();
        this.setMaxStackSize(1);
    }

    private NBTTagCompound getNBTTagComound(ItemStack stack) {
        if (!stack.hasTagCompound()) {
            NBTTagCompound compound = new NBTTagCompound();
            compound.setByte("current", (byte) 0);
            stack.setTagCompound(compound);
        }
        return stack.getTagCompound();
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {

        if (!player.isSneaking()) {
            BaubleItemHelper.onBaubleRightClick(stack, world, player);
        } else if (BackhandUtils.getOffhandItem(player) != stack) {
            for (int i = 0; i < getSlotCount(stack); i++) {
                ItemStack arrowStack = getStackInSlot(stack, i);
                if (arrowStack != null) {
                    EntityItem entityitem = ForgeHooks.onPlayerTossEvent(player, arrowStack, true);
                    if (entityitem != null) {
                        entityitem.delayBeforeCanPickup = 0;
                        entityitem.func_145797_a(player.getCommandSenderName());
                    }
                    setStackInSlot(stack, i, null);
                }
            }
        }

        return super.onItemRightClick(stack, world, player);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister par1IconRegister) {
        super.registerIcons(par1IconRegister);
        quiverDetails = par1IconRegister.registerIcon("battlegear2:quiver/quiver-details");
        quiverArrows = par1IconRegister.registerIcon("battlegear2:quiver/quiver-arrows");
    }

    @Override
    public int getSlotCount(ItemStack container) {
        return 4;
    }

    @Override
    public int getSelectedSlot(ItemStack container) {
        return getNBTTagComound(container).getByte("current");
    }

    @Override
    public void setSelectedSlot(ItemStack container, int newSlot) {
        getNBTTagComound(container).setByte("current", (byte) newSlot);
    }

    @Override
    public ItemStack getStackInSlot(ItemStack container, int slot) {
        NBTTagCompound compound = getNBTTagComound(container);
        if (compound.hasKey("Slot" + slot)) {
            return ItemStack.loadItemStackFromNBT(compound.getCompoundTag("Slot" + slot));
        } else {
            return null;
        }
    }

    @Override
    public void setStackInSlot(ItemStack container, int slot, ItemStack stack) {
        NBTTagCompound compound = getNBTTagComound(container);
        if (stack == null) {
            compound.removeTag("Slot" + slot);
        } else {
            NBTTagCompound newSlotCompound = new NBTTagCompound();

            stack.writeToNBT(newSlotCompound);
            compound.setTag("Slot" + slot, newSlotCompound);
        }
    }

    @Override
    public boolean hasArrowFor(ItemStack stack, ItemStack bow, EntityPlayer player, int slot) {
        return bow != null && BattlegearUtils.isBow(bow.getItem())
                && ((IArrowContainer2) stack.getItem()).getStackInSlot(stack, slot) != null;
    }

    @Override
    public EntityArrow getArrowType(ItemStack stack, World world, EntityPlayer player, float charge) {
        ItemStack selected = getStackInSlot(stack, getSelectedSlot(stack));
        if (selected == null) return null;
        else return QuiverArrowRegistry.getArrowType(selected, world, player, charge);
    }

    @Override
    public void onArrowFired(World world, EntityPlayer player, ItemStack stack, ItemStack bow, EntityArrow arrow) {
        if (!player.capabilities.isCreativeMode
                && EnchantmentHelper.getEnchantmentLevel(Enchantment.infinity.effectId, bow) == 0) {
            int selectedSlot = getSelectedSlot(stack);
            ItemStack arrowStack = getStackInSlot(stack, selectedSlot);
            arrowStack.stackSize--;
            if (arrowStack.stackSize <= 0) {
                ForgeEventFactory.onPlayerDestroyItem(player, arrowStack);
                arrowStack = null;
            }
            setStackInSlot(stack, selectedSlot, arrowStack);
        }
    }

    @Override
    public void onPreArrowFired(PlayerEventChild.QuiverArrowEvent.Firing arrowEvent) {
        if (arrowEvent.getArcher().capabilities.isCreativeMode
                || EnchantmentHelper.getEnchantmentLevel(Enchantment.infinity.effectId, arrowEvent.getBow()) > 0) {
            arrowEvent.arrow.canBePickedUp = 2;
        }
        writeBowNBT(arrowEvent.getBow(), getStackInSlot(arrowEvent.quiver, getSelectedSlot(arrowEvent.quiver)));
    }

    /**
     * Convenience feature for "AnonymousProductions" dude
     *
     * @param bow
     * @param loadedArrow
     */
    public static void writeBowNBT(ItemStack bow, ItemStack loadedArrow) {
        NBTTagCompound tags = new NBTTagCompound();
        loadedArrow.writeToNBT(tags);
        if (!bow.hasTagCompound()) {
            bow.stackTagCompound = new NBTTagCompound();
        }
        bow.stackTagCompound.setTag("Battlegear2-LoadedArrow", tags);
    }

    @Override
    public boolean isCraftableWithArrows(ItemStack stack, ItemStack arrows) {
        return QuiverArrowRegistry.isKnownArrow(arrows);
    }

    @Override
    public ItemStack addArrows(ItemStack container, ItemStack newStack) {
        if (newStack != null) {
            int left_over = newStack.stackSize;
            int slotCount = getSlotCount(container);
            for (int i = 0; i < slotCount && left_over > 0; i++) {
                ItemStack slotStack = getStackInSlot(container, i);
                if (slotStack == null) {
                    newStack.stackSize = left_over;
                    setStackInSlot(container, i, newStack);
                    left_over = 0;
                } else {
                    if (newStack.getItem() == slotStack.getItem()
                            && newStack.getItemDamage() == slotStack.getItemDamage()) {
                        int newSize = Math.min(64, slotStack.stackSize + left_over);
                        left_over = left_over - (newSize - slotStack.stackSize);
                        slotStack.stackSize = newSize;
                        setStackInSlot(container, i, slotStack);
                    }
                }
            }
            if (left_over > 0) {
                newStack.stackSize = left_over;
                return newStack;
            }
        }
        return null;
    }

    @Override
    public boolean renderDefaultQuiverModel(ItemStack container) {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer par2EntityPlayer, List<String> list, boolean par4) {
        super.addInformation(stack, par2EntityPlayer, list, par4);

        list.add(StatCollector.translateToLocal("attribute.quiver.arrow.count"));

        int slotCount = getSlotCount(stack);
        int selected = getSelectedSlot(stack);
        boolean containsArrows = false;
        for (int i = 0; i < slotCount; i++) {
            ItemStack slotStack = getStackInSlot(stack, i);
            if (slotStack != null) {
                containsArrows = true;
                list.add(
                        String.format(
                                " %s%s: %s x %s",
                                i,
                                i == selected ? EnumChatFormatting.DARK_GREEN : EnumChatFormatting.GOLD,
                                slotStack.stackSize,
                                slotStack.getDisplayName()));
            } else {
                list.add(
                        String.format(
                                " %s%s: %s",
                                i,
                                i == selected ? EnumChatFormatting.DARK_GREEN : EnumChatFormatting.GOLD,
                                StatCollector.translateToLocal("attribute.quiver.arrow.empty")));
            }
        }
        list.add("");
        if (containsArrows) {
            list.add(StatCollector.translateToLocal("attribute.quiver.tooltip.retrieve"));
        }
        BaubleItemHelper.addSlotInformation(list, baubleTypes);
    }

    @Override
    public boolean hasColor(ItemStack par1ItemStack) {
        return par1ItemStack.hasTagCompound() && par1ItemStack.getTagCompound().hasKey("display")
                && par1ItemStack.getTagCompound().getCompoundTag("display").hasKey("color");
    }

    @Override
    public int getColor(ItemStack par1ItemStack) {
        {
            NBTTagCompound nbttagcompound = par1ItemStack.getTagCompound();

            if (nbttagcompound == null) {
                return getDefaultColor(par1ItemStack);
            } else {
                NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("display");
                return nbttagcompound1 == null ? getDefaultColor(par1ItemStack)
                        : (nbttagcompound1.hasKey("color") ? nbttagcompound1.getInteger("color")
                                : getDefaultColor(par1ItemStack));
            }
        }
    }

    @Override
    public void removeColor(ItemStack par1ItemStack) {
        NBTTagCompound nbttagcompound = par1ItemStack.getTagCompound();

        if (nbttagcompound != null) {
            NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("display");

            if (nbttagcompound1.hasKey("color")) {
                nbttagcompound1.removeTag("color");
            }
        }
    }

    @Override
    public int getDefaultColor(ItemStack par1ItemStack) {
        return 0xFFC65C35;
    }

    @Override
    public void setColor(ItemStack par1ItemStack, int par2) {
        NBTTagCompound nbttagcompound = par1ItemStack.getTagCompound();
        if (nbttagcompound == null) {
            nbttagcompound = new NBTTagCompound();
            par1ItemStack.setTagCompound(nbttagcompound);
        }
        NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("display");
        if (!nbttagcompound.hasKey("display")) {
            nbttagcompound.setTag("display", nbttagcompound1);
        }
        nbttagcompound1.setInteger("color", par2);
    }

    // Extended Baubles interface methods

    @Override
    public String[] getBaubleTypes(ItemStack itemstack) {
        return baubleTypes;
    }

    @Override
    public BaubleType getBaubleType(ItemStack itemstack) {
        return null;
    }

    @Override
    public void onWornTick(ItemStack itemstack, EntityLivingBase player) {}

    @Override
    public void onEquipped(ItemStack itemstack, EntityLivingBase player) {}

    @Override
    public void onUnequipped(ItemStack itemstack, EntityLivingBase player) {}

    @Override
    public boolean canEquip(ItemStack itemstack, EntityLivingBase player) {
        return true;
    }

    @Override
    public boolean canUnequip(ItemStack itemstack, EntityLivingBase player) {
        return true;
    }
}
