package mods.battlegear2.api.core;

import java.io.Closeable;
import java.io.IOException;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.BaseAttributeMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.item.ItemFireball;
import net.minecraft.item.ItemFlintAndSteel;
import net.minecraft.item.ItemSnowball;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.oredict.OreDictionary;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.common.eventhandler.EventBus;
import mods.battlegear2.api.IAllowItem;
import mods.battlegear2.api.IOffhandDual;
import mods.battlegear2.api.IOffhandWield;
import mods.battlegear2.api.IUsableItem;
import mods.battlegear2.api.PlayerEventChild;
import mods.battlegear2.api.quiver.IArrowContainer2;
import mods.battlegear2.api.quiver.ISpecialBow;
import mods.battlegear2.api.shield.IShield;
import mods.battlegear2.api.weapons.IBattlegearWeapon;
import mods.battlegear2.api.weapons.WeaponRegistry;
import mods.battlegear2.asm.loader.BattlegearLoadingPlugin;
import xonin.backhand.api.core.BackhandUtils;

/**
 * Store commonly used method, mostly for the {@link EntityPlayer} {@link ItemStack}s management
 */
public class BattlegearUtils {

    /**
     * Event bus to which {@link mods.battlegear2.api.RenderPlayerEventChild} events are post to
     */
    public static final EventBus RENDER_BUS = new EventBus();
    /**
     * Method names that are not allowed in {@link Item} subclasses for common wielding
     */
    private static final String[] itemBlackListMethodNames = {
            BattlegearLoadingPlugin.isObf() ? "func_77648_a" : "onItemUse", "onItemUseFirst", // Added by Forge
            BattlegearLoadingPlugin.isObf() ? "func_77659_a" : "onItemRightClick" };
    /**
     * Method arguments classes that are not allowed in {@link Item} subclasses for common wielding
     */
    private static final Class<?>[][] itemBlackListMethodParams = {
            new Class[] { ItemStack.class, EntityPlayer.class, World.class, int.class, int.class, int.class, int.class,
                    float.class, float.class, float.class },
            new Class[] { ItemStack.class, EntityPlayer.class, World.class, int.class, int.class, int.class, int.class,
                    float.class, float.class, float.class },
            new Class[] { ItemStack.class, World.class, EntityPlayer.class } };

    private static ItemStack prevNotWieldable;
    /**
     * The generic attack damage key for {@link ItemStack#getAttributeModifiers()}
     */
    private static final String genericAttack = SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName();

    /**
     * Helper method to check if player can use {@link IShield}
     */
    public static boolean canBlockWithShield(EntityPlayer player) {
        ItemStack offhand = BackhandUtils.getOffhandItem(player);
        return offhand != null && offhand.getItem() instanceof IShield;
    }

    /**
     * Helper method to check if player is using {@link IShield}
     */
    public static boolean isBlockingWithShield(EntityPlayer player) {
        return ((IBattlePlayer) player).battlegear2$isBlockingWithShield();
    }

    /*
     * Helper method to set the mainhand item
     */
    public static void setPlayerCurrentItem(EntityPlayer player, ItemStack stack) {
        player.inventory.mainInventory[player.inventory.currentItem] = stack;
    }

    /**
     * Helper method to set the offhand item, if battlemode is activated
     */
    public static void setPlayerOffhandItem(EntityPlayer player, ItemStack stack) {
        BackhandUtils.setPlayerOffhandItem(player, stack);
    }

    /**
     * Defines a generic weapon
     *
     * @param main the item to check
     * @return true if the item is a generic weapon
     */
    public static boolean isWeapon(ItemStack main) {
        if (main.getItem() instanceof IBattlegearWeapon) // Our generic weapon flag
            return true;
        else if (main.getMaxStackSize() == 1 && main.getMaxDamage() > 0 && !main.getHasSubtypes()) // Usual values for
            // tools, sword, and
            // bow
            return true;
        else if (main == prevNotWieldable) // Prevent lag from check spam
            return false;
        else if (WeaponRegistry.isWeapon(main)) // Registered as such
            return true;
        else if (checkWeaponOreDictEntries(main)) return true;
        else if (!checkForRightClickFunction(main)) { // Make sure there are no special functions for offhand/mainhand
            // weapons
            WeaponRegistry.addDualWeapon(main); // register so as not to make that costly check again
            return true;
        }
        prevNotWieldable = main;
        return false;
    }

    /**
     * Checks if an item is a GT weapon based on OreDict entries
     *
     * @param main the item to check
     * @return true if the item is a GT weapon
     */
    public static boolean checkWeaponOreDictEntries(ItemStack main) {
        int[] oreDictEntries = OreDictionary.getOreIDs(main);

        for (int i = 0; i < oreDictEntries.length; i++) {
            int ore = oreDictEntries[i];
            String name = OreDictionary.getOreName(ore);

            if (name.equals("craftingToolBlade") || name.equals("craftingToolAxe")) // craftingToolPickaxe?
            {
                return true;
            }
        }

        return false;
    }

    /**
     * @deprecated see below
     */
    public static boolean isMainHand(ItemStack main, ItemStack off) {
        if (main == null) return true;
        else if (main.getItem() instanceof IAllowItem) // An item using the API
            return ((IAllowItem) main.getItem()).allowOffhand(main, off); // defined by the item
        else if (main.getItem() instanceof IArrowContainer2) // A quiver
            return true; // anything ?
        else if (usagePriorAttack(main)) // "Usable" item
            return off == null || !usagePriorAttack(off); // With empty hand or non "usable item"
        else if (isWeapon(main)) // A generic weapon
            return checkWeaponOreDictEntries(main) || main.getAttributeModifiers().containsKey(genericAttack)
                    || WeaponRegistry.isMainHand(main); // With either generic attack, or registered
        return false;
    }

    /**
     * Defines a combination of left hand/right hand items that is valid to wield
     *
     * @param main    Item to be wield in the right hand
     * @param off     Item to be wield in the left hand
     * @param wielder The player trying to wield this combination of items
     * @return true if the right hand item allows left hand item
     */
    public static boolean isMainHand(ItemStack main, ItemStack off, EntityPlayer wielder) {
        if (main == null) return true;
        else if (main.getItem() instanceof IAllowItem) // An item using the API
            return ((IAllowItem) main.getItem()).allowOffhand(main, off); // defined by the item TODO pass through third
        // parameter
        else if (main.getItem() instanceof IArrowContainer2) // A quiver
            return true; // anything ?
        else if (usagePriorAttack(main, wielder, false)) // "Usable" item
            return off == null || !usagePriorAttack(off, wielder, true); // With empty hand or non "usable item"
        else if (isWeapon(main)) // A generic weapon
            return checkWeaponOreDictEntries(main) || main.getAttributeModifiers().containsKey(genericAttack)
                    || WeaponRegistry.isMainHand(main); // With either generic attack, or registered
        return false;
    }

    /**
     * @deprecated see below
     */
    public static boolean isOffHand(ItemStack off) {
        if (off == null) return true;
        else if (off.getItem() instanceof IOffhandDual) // An item using the API
            return ((IOffhandDual) off.getItem()).isOffhandHandDual(off); // defined by the item
        else if (off.getItem() instanceof IShield || off.getItem() instanceof IArrowContainer2 || usagePriorAttack(off)) // Shield,
            // Quiver,
            // or
            // "usable"
            return true; // always
        else if (isWeapon(off)) // A generic weapon
            return checkWeaponOreDictEntries(off) || off.getAttributeModifiers().containsKey(genericAttack)
                    || WeaponRegistry.isOffHand(off); // with a generic attack or registered
        return false;
    }

    /**
     * Defines a item which can be wield in the left hand
     *
     * @param off     The item to be wield in left hand
     * @param wielder The player trying to wield this item
     * @return true if the item is allowed in left hand
     */
    @SuppressWarnings("deprecation")
    public static boolean isOffHand(ItemStack off, EntityPlayer wielder) {
        if (off == null) return true;
        else if (off.getItem() instanceof IOffhandDual) // An item using the API
            return ((IOffhandDual) off.getItem()).isOffhandHandDual(off); // defined by the item
        else if (off.getItem() instanceof IOffhandWield) // An item using the API
            return ((IOffhandWield) off.getItem()).isOffhandWieldable(off, wielder); // defined by the item
        else if (off.getItem() instanceof IShield || off.getItem() instanceof IArrowContainer2
                || usagePriorAttack(off, wielder, true)) // Shield, Quiver, or "usable"
            return true; // always
        else if (isWeapon(off)) // A generic weapon
            return checkWeaponOreDictEntries(off) || off.getAttributeModifiers().containsKey(genericAttack)
                    || WeaponRegistry.isOffHand(off); // with a generic attack or registered
        return false;
    }

    /**
     * @deprecated see below
     */
    public static boolean usagePriorAttack(ItemStack itemStack) {
        if (itemStack.getItem() instanceof IUsableItem)
            return ((IUsableItem) itemStack.getItem()).isUsedOverAttack(itemStack);
        else {
            EnumAction useAction = itemStack.getItemUseAction();
            return useAction == EnumAction.bow || useAction == EnumAction.drink
                    || useAction == EnumAction.eat
                    || isCommonlyUsable(itemStack.getItem());
        }
    }

    /**
     * Defines a item which "use" (effect on right click) should have priority over its "attack" (effect on left click)
     *
     * @param itemStack the item which will be "used", instead of attacking
     * @param wielder   The player trying to use or attack with this item
     * @return true if such item prefer being "used"
     */
    public static boolean usagePriorAttack(ItemStack itemStack, EntityPlayer wielder, boolean off) {
        if (itemStack.getItem() instanceof IUsableItem) // TODO pass through wielding player
            return ((IUsableItem) itemStack.getItem()).isUsedOverAttack(itemStack);
        else {
            EnumAction useAction = itemStack.getItemUseAction();
            return useAction == EnumAction.bow || useAction == EnumAction.drink
                    || useAction == EnumAction.eat
                    || isCommonlyUsable(itemStack.getItem())
                    || WeaponRegistry.useOverAttack(itemStack, off);
        }
    }

    /**
     * Defines items that are usually usable (the vanilla instances do, at least), and that battlemode can support
     *
     * @param item the instance to consider for usability
     * @return true if it is commonly usable
     */
    public static boolean isCommonlyUsable(Item item) {
        return isBow(item) || item instanceof ItemBlock
                || item instanceof ItemFlintAndSteel
                || item instanceof ItemFireball
                || item instanceof ItemBucket
                || item instanceof ItemSnowball
                || item instanceof ItemEnderPearl;
    }

    /**
     * Defines a bow
     *
     * @param item the instance
     * @return true if it is considered a generic enough bow
     */
    public static boolean isBow(Item item) {
        return item instanceof ItemBow || item instanceof ISpecialBow;
    }

    @Deprecated // See method below
    public static boolean checkForRightClickFunction(Item item, ItemStack stack) {
        return checkForRightClickFunction(stack);
    }

    public static boolean checkForRightClickFunction(ItemStack stack) {
        if (stack.getItemUseAction() == EnumAction.block || stack.getItemUseAction() == EnumAction.none) {
            Class<?> c = stack.getItem().getClass();
            while (!(c.equals(Item.class) || c.equals(ItemTool.class) || c.equals(ItemSword.class))) {
                if (getBlackListedMethodIn(c)) {
                    return true;
                }

                c = c.getSuperclass();
            }

            return false;
        }
        return true;
    }

    private static boolean getBlackListedMethodIn(Class<?> c) {
        for (int i = 0; i < itemBlackListMethodNames.length; i++) {
            try {
                c.getDeclaredMethod(itemBlackListMethodNames[i], itemBlackListMethodParams[i]);
                return true;
            } catch (Throwable ignored) {}
        }
        return false;
    }

    /**
     * Reads a {@link ItemStack} from the InputStream
     */
    public static ItemStack readItemStack(ByteArrayDataInput par0DataInputStream) throws IOException {
        ItemStack itemstack = null;
        int short1 = par0DataInputStream.readInt();

        if (short1 >= 0) {
            byte b0 = par0DataInputStream.readByte();
            short short2 = par0DataInputStream.readShort();
            itemstack = new ItemStack(Item.getItemById(short1), b0, short2);
            itemstack.stackTagCompound = readNBTTagCompound(par0DataInputStream);
        }

        return itemstack;
    }

    /**
     * Reads a compressed {@link NBTTagCompound} from the InputStream
     */
    public static NBTTagCompound readNBTTagCompound(ByteArrayDataInput par0DataInputStream) throws IOException {
        short short1 = par0DataInputStream.readShort();

        if (short1 < 0) {
            return null;
        } else {
            byte[] abyte = new byte[short1];
            par0DataInputStream.readFully(abyte);

            return CompressedStreamTools.func_152457_a(abyte, NBTSizeTracker.field_152451_a);
        }
    }

    /**
     * Writes a {@link ItemStack} to the OutputStream
     *
     * @param par1DataOutputStream the output stream
     * @param par0ItemStack        to write
     * @throws IOException
     */
    public static void writeItemStack(ByteArrayDataOutput par1DataOutputStream, ItemStack par0ItemStack)
            throws IOException {

        if (par0ItemStack == null) {
            par1DataOutputStream.writeShort(-1);
        } else {
            par1DataOutputStream.writeInt(Item.getIdFromItem(par0ItemStack.getItem()));
            par1DataOutputStream.writeByte(par0ItemStack.stackSize);
            par1DataOutputStream.writeShort(par0ItemStack.getItemDamage());
            NBTTagCompound nbttagcompound = null;

            if (par0ItemStack.getItem().isDamageable() || par0ItemStack.getItem().getShareTag()) {
                nbttagcompound = par0ItemStack.stackTagCompound;
            }

            writeNBTTagCompound(nbttagcompound, par1DataOutputStream);
        }
    }

    /**
     * Writes a compressed {@link NBTTagCompound} to the output
     *
     * @param par0NBTTagCompound
     * @param par1DataOutputStream
     * @throws IOException
     */
    protected static void writeNBTTagCompound(NBTTagCompound par0NBTTagCompound,
            ByteArrayDataOutput par1DataOutputStream) throws IOException {
        if (par0NBTTagCompound == null) {
            par1DataOutputStream.writeShort(-1);
        } else {
            byte[] abyte = CompressedStreamTools.compress(par0NBTTagCompound);
            par1DataOutputStream.writeShort((short) abyte.length);
            par1DataOutputStream.write(abyte);
        }
    }

    /**
     * Helper to send {@link PlayerEventChild.OffhandSwingEvent}
     *
     * @param event       the "parent" event
     * @param offhandItem the item stack held in offhand
     */
    public static void sendOffSwingEvent(PlayerEvent event, ItemStack offhandItem) {
        if (!MinecraftForge.EVENT_BUS.post(new PlayerEventChild.OffhandSwingEvent(event, offhandItem))) {
            ((IBattlePlayer) event.entityPlayer).battlegear2$swingOffItem();
        }
    }

    /**
     * Refresh the attribute map by removing from the old item and applying the current item
     *
     * @param attributeMap the map to refresh
     * @param oldItem      the old item whose attributes will be removed
     * @param currentItem  the current item whose attributes will be applied
     */
    public static void refreshAttributes(BaseAttributeMap attributeMap, ItemStack oldItem, ItemStack currentItem) {
        if (oldItem != null) attributeMap.removeAttributeModifiers(oldItem.getAttributeModifiers());
        if (currentItem != null) attributeMap.applyAttributeModifiers(currentItem.getAttributeModifiers());
    }

    /**
     * Helper to close a stream fail-safely by printing the error stack trace
     *
     * @param c the stream to close
     */
    public static void closeStream(Closeable c) {
        try {
            if (c != null) {
                c.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Patch in ItemStack#damageItem() to fix bow stack weird depletion
     *
     * @param itemStack    which item is instance of ItemBow, and size is 0
     * @param entityPlayer who has damaged and depleted the stack
     */
    public static void onBowStackDepleted(EntityPlayer entityPlayer, ItemStack itemStack) {
        if (itemStack == entityPlayer.getCurrentEquippedItem()) {
            entityPlayer.destroyCurrentEquippedItem();
        } else {
            ItemStack orig = BackhandUtils.getOffhandItem(entityPlayer);
            if (orig == itemStack) {
                setPlayerOffhandItem(entityPlayer, null);
                ForgeEventFactory.onPlayerDestroyItem(entityPlayer, orig);
            }
        }
    }
}
