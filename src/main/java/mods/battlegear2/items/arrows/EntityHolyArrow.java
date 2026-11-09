package mods.battlegear2.items.arrows;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.world.World;

public class EntityHolyArrow extends AbstractMBArrow {

    public EntityHolyArrow(World par1World) {
        super(par1World, 0);
    }

    public EntityHolyArrow(World par1World, EntityLivingBase par2EntityLivingBase, float par3) {
        super(par1World, par2EntityLivingBase, par3, 0);
    }

    public EntityHolyArrow(World par1World, EntityLivingBase par2EntityLivingBase,
            EntityLivingBase par3EntityLivingBase, float par4, float par5) {
        super(par1World, par2EntityLivingBase, par3EntityLivingBase, par4, par5, 0);
    }

    @Override
    public boolean onHitEntity(Entity entityHit, DamageSource source, float amount) {
        boolean flag = false;
        if (entityHit.isCreatureType(EnumCreatureType.monster, false)) {
            entityHit.attackEntityFrom(
                    new EntityDamageSourceIndirect("holy", null, shootingEntity).setProjectile()
                            .setDamageBypassesArmor(),
                    amount + 5);
        }
        if (entityHit instanceof EntityLivingBase) {
            entityHit.attackEntityFrom(
                    new EntityDamageSourceIndirect("default", null, shootingEntity).setProjectile(),
                    amount - 2);
            flag = true;
        }
        setDead();
        return flag;
    }

    @Override
    public void onHitGround(int x, int y, int z) {
        if (worldObj.isAirBlock(x, y + 1, z) && Blocks.torch.canPlaceBlockAt(worldObj, x, y + 1, z)) {
            worldObj.setBlock(x, y + 1, z, Blocks.torch);
        }
        setDead();
    }
}
