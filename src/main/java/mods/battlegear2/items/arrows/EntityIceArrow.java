package mods.battlegear2.items.arrows;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.world.World;

/**
 * An arrow which is effective against fire immune creatures
 *
 * @author Seitenca
 */
public class EntityIceArrow extends AbstractMBArrow {

    public EntityIceArrow(World par1World) {
        super(par1World, 1);
    }

    public EntityIceArrow(World par1World, EntityLivingBase par2EntityLivingBase, float par3) {
        super(par1World, par2EntityLivingBase, par3, 1);
    }

    public EntityIceArrow(World par1World, EntityLivingBase par2EntityLivingBase, EntityLivingBase par3EntityLivingBase,
            float par4, float par5) {
        super(par1World, par2EntityLivingBase, par3EntityLivingBase, par4, par5, 1);
    }

    @Override
    public boolean onHitEntity(Entity entityHit, DamageSource source, float amount) {
        boolean flag = false;
        if (entityHit.isImmuneToFire()) {
            entityHit.attackEntityFrom(
                    new EntityDamageSourceIndirect("frost", null, shootingEntity).setProjectile()
                            .setDamageBypassesArmor(),
                    amount + 10);
        }
        if (entityHit instanceof EntityLivingBase) {
            entityHit.attackEntityFrom(
                    new EntityDamageSourceIndirect("default", null, shootingEntity).setProjectile(),
                    amount - 2);
            ((EntityLivingBase) entityHit).addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 200, 1));
            flag = true;
        }
        setDead();
        return flag;
    }

    @Override
    public void onHitGround(int x, int y, int z) {}
}
