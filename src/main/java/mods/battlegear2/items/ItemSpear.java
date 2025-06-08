package mods.battlegear2.items;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import com.google.common.collect.Multimap;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.battlegear2.api.shield.IShield;
import mods.battlegear2.api.weapons.IExtendedReachWeapon;

public class ItemSpear extends TwoHandedWeapon implements IExtendedReachWeapon {

    // Will make it one more than a sword
    private final int mounted_extra_damage;
    public IIcon bigIcon;

    public ItemSpear(ToolMaterial material, String name, int mount, float reach) {
        super(material, name);
        this.mounted_extra_damage = mount;
        // set the base damage to that of lower than usual (balance)
        this.baseDamage -= 2;
        GameRegistry.registerItem(this, this.name);
    }

    @Override
    public float getReachModifierInBlocks(ItemStack stack) {
        return getModifiedAmount(stack, extendedReach.getAttributeUnlocalizedName());
    }

    @Override
    public boolean allowOffhand(ItemStack mainhand, ItemStack offhand) {
        return super.allowOffhand(mainhand, offhand) || offhand.getItem() instanceof IShield;
    }

    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(ItemStack stack) {
        Multimap<String, AttributeModifier> map = super.getAttributeModifiers(stack);
        map.put(
                extendedReach.getAttributeUnlocalizedName(),
                new AttributeModifier(extendReachUUID, "Reach Modifier", this.reach, 0));
        map.put(
                mountedBonus.getAttributeUnlocalizedName(),
                new AttributeModifier(mountedBonusUUID, "Attack Modifier", this.mounted_extra_damage, 0));
        return map;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
        return par1ItemStack;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack par1ItemStack) {
        return EnumAction.none;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack itemStack) {
        return 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister par1IconRegister) {
        super.registerIcons(par1IconRegister);
        bigIcon = par1IconRegister.registerIcon(this.getIconString() + ".big");
    }
}
