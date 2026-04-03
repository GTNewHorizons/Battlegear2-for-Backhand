package mods.battlegear2.items.arrows;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;

/**
 * An arrow which teleports living entities or blocks on contact
 *
 * @author GotoLink
 */
public class EntityEnderArrow extends AbstractMBArrow {

    public static final Set<String> BLOCK_BLACKLIST = new HashSet<>();

    public static float tpRange = 32.0F;
    public static String PARTICLES = "portal", SOUND = "mob.endermen.portal";

    public EntityEnderArrow(World par1World) {
        super(par1World);
    }

    public EntityEnderArrow(World par1World, EntityLivingBase par2EntityLivingBase, float par3) {
        super(par1World, par2EntityLivingBase, par3);
    }

    public EntityEnderArrow(World par1World, EntityLivingBase par2EntityLivingBase,
            EntityLivingBase par3EntityLivingBase, float par4, float par5) {
        super(par1World, par2EntityLivingBase, par3EntityLivingBase, par4, par5);
    }

    @Override
    public boolean onHitEntity(Entity entityHit, DamageSource source, float ammount) {
        this.setDead();
        if (entityHit instanceof EntityLivingBase) {
            if (!this.worldObj.isRemote) {
                if (shootingEntity == null) {
                    tryTeleport((EntityLivingBase) entityHit);
                } else if (shootingEntity instanceof EntityLivingBase) {
                    if (shootingEntity instanceof EntityPlayerMP
                            && !(((EntityPlayerMP) this.shootingEntity).playerNetServerHandler.func_147362_b()
                                    .isChannelOpen() && shootingEntity.worldObj == this.worldObj))
                        return false;
                    double x = shootingEntity.posX;
                    double y = shootingEntity.posY;
                    double z = shootingEntity.posZ;
                    EnderTeleportEvent event = new EnderTeleportEvent(
                            (EntityLivingBase) shootingEntity,
                            entityHit.posX + 0.5F,
                            entityHit.posY,
                            entityHit.posZ + 0.5F,
                            getDamageAgainst((EntityLivingBase) shootingEntity));
                    if (handleTeleportEvent(event)) {
                        event = new EnderTeleportEvent(
                                (EntityLivingBase) entityHit,
                                x + 0.5F,
                                y,
                                z + 0.5F,
                                getDamageAgainst((EntityLivingBase) entityHit));
                        handleTeleportEvent(event);
                    }
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Handle calculating teleport damage. Takes into account the feather falling enchanted boots on a player, no matter
     * the type of damage done
     *
     * @param entityHit that will be damaged
     * @return the value of the damage posted into EnderTeleportEvent
     */
    public float getDamageAgainst(EntityLivingBase entityHit) {
        if (entityHit instanceof EntityPlayer) {
            int fall = EnchantmentHelper
                    .getEnchantmentLevel(Enchantment.featherFalling.effectId, entityHit.getEquipmentInSlot(1));
            return (float) getDamage() * 2.5F - 0.5F * fall;
        }
        return entityHit.getMaxHealth() / 10;
    }

    /**
     * Teleport at random, for automated systems
     *
     * @param entity the entity hit by the arrow
     */
    protected void tryTeleport(EntityLivingBase entity) {
        double x = entity.posX + (this.rand.nextDouble() - 0.5D) * tpRange * 2;
        double y = entity.posY + (double) (this.rand.nextInt(4) - 2);
        double z = entity.posZ + (this.rand.nextDouble() - 0.5D) * tpRange * 2;
        EnderTeleportEvent event = new EnderTeleportEvent(entity, x, y, z, 0);
        if (MinecraftForge.EVENT_BUS.post(event)) {
            return;
        }
        x = entity.posX;
        y = entity.posY;
        z = entity.posZ;
        entity.posX = event.targetX;
        entity.posY = event.targetY;
        entity.posZ = event.targetZ;
        boolean success = false;
        int i = MathHelper.floor_double(entity.posX);
        int j = MathHelper.floor_double(entity.posY);
        int k = MathHelper.floor_double(entity.posZ);

        if (this.worldObj.blockExists(i, j, k)) {
            while (j > 0) {
                Block block = this.worldObj.getBlock(i, j - 1, k);
                if (block.getMaterial().blocksMovement()) {
                    entity.setPosition(entity.posX, entity.posY, entity.posZ);
                    if (this.worldObj.getCollidingBoundingBoxes(entity, entity.boundingBox).isEmpty()) {
                        success = true;
                        for (int l = 0; l < 128; ++l) {
                            double d6 = (double) l / 127.0D;
                            float f = (this.rand.nextFloat() - 0.5F) * 0.2F;
                            float f1 = (this.rand.nextFloat() - 0.5F) * 0.2F;
                            float f2 = (this.rand.nextFloat() - 0.5F) * 0.2F;
                            double d7 = x + (entity.posX - x) * d6
                                    + (this.rand.nextDouble() - 0.5D) * (double) entity.width * 2.0D;
                            double d8 = y + (entity.posY - y) * d6 + this.rand.nextDouble() * (double) entity.height;
                            double d9 = z + (entity.posZ - z) * d6
                                    + (this.rand.nextDouble() - 0.5D) * (double) entity.width * 2.0D;
                            this.worldObj.spawnParticle(PARTICLES, d7, d8, d9, (double) f, (double) f1, (double) f2);
                        }
                        this.worldObj.playSoundEffect(x, y, z, SOUND, 1.0F, 1.0F);
                        entity.playSound(SOUND, 1.0F, 1.0F);
                    }
                    break;
                } else {
                    --entity.posY;
                    --j;
                }
            }
        }

        if (!success) {
            entity.setPosition(x, y, z);
        }
    }

    @Override
    public void onHitGround(int x, int y, int z) {
        this.setDead();
        if (shootingEntity instanceof EntityPlayer player && shootingEntity.isSneaking()) {
            if (!worldObj.getGameRules().getGameRuleBooleanValue("doTileDrops")) return;

            Block block = worldObj.getBlock(x, y, z);
            int meta = worldObj.getBlockMetadata(x, y, z);

            if (!canHarvestBlock(worldObj, x, y, z, block, meta)) return;

            ArrayList<ItemStack> drops = new ArrayList<>();

            if (block.canSilkHarvest(worldObj, player, x, y, z, meta)) {
                ItemStack silkDrop = createSilkTouchDrop(block, meta);
                if (silkDrop != null) {
                    drops.add(silkDrop);
                }
            } else {
                drops.addAll(block.getDrops(worldObj, x, y, z, meta, 0));
            }

            if (drops.isEmpty()) return;

            block.onBlockHarvested(worldObj, x, y, z, meta, player);

            if (!block.removedByPlayer(worldObj, player, x, y, z, true)) return;

            block.onBlockDestroyedByPlayer(worldObj, x, y, z, meta);
            worldObj.setBlockToAir(x, y, z);

            for (ItemStack drop : drops) {
                if (drop == null || drop.getItem() == null) continue;

                if (player.inventory.addItemStackToInventory(drop)) continue;

                EntityItem entityitem = ForgeHooks.onPlayerTossEvent(player, drop, true);
                if (entityitem == null) continue;

                entityitem.delayBeforeCanPickup = 0;
                entityitem.func_145797_a(player.getCommandSenderName());
            }

            return;
        }

        if (!(shootingEntity instanceof EntityLivingBase)) return;

        while (y < 255 && !(worldObj.isAirBlock(x, y, z) && worldObj.isAirBlock(x, y + 1, z))) {
            if (worldObj.getBlock(x, y, z) == Blocks.bedrock) return;
            y++;
        }

        if (!worldObj.isAirBlock(x, y, z)) {
            while (y > 0 && !(worldObj.isAirBlock(x, y, z) && worldObj.isAirBlock(x, y - 1, z))) {
                if (worldObj.getBlock(x, y, z) == Blocks.bedrock) return;
                y--;
            }
        }

        if (!worldObj.isAirBlock(x, y, z)) return;

        if (shootingEntity instanceof EntityPlayerMP playerMP) {
            if (!(playerMP.playerNetServerHandler.func_147362_b().isChannelOpen()
                    && shootingEntity.worldObj == this.worldObj)) {
                return;
            }
        }

        handleTeleportEvent(
                new EnderTeleportEvent(
                        (EntityLivingBase) shootingEntity,
                        x + 0.5F,
                        y,
                        z + 0.5F,
                        getDamageAgainst((EntityLivingBase) shootingEntity)));
    }

    private ItemStack createSilkTouchDrop(Block block, int meta) {
        Item item = Item.getItemFromBlock(block);
        if (item == null) return null;

        return new ItemStack(item, 1, block.damageDropped(meta));
    }

    private boolean canHarvestBlock(World world, int x, int y, int z, Block block, int meta) {
        if (block == null || block.isAir(world, x, y, z)) {
            return false;
        }

        if (block.getBlockHardness(world, x, y, z) < 0) {
            return false;
        }

        if (isBlacklisted(block, meta)) {
            return false;
        }

        String key = Block.blockRegistry.getNameForObject(block) + ":" + meta;
        return !BLOCK_BLACKLIST.contains(key);
    }

    private boolean isBlacklisted(Block block, int meta) {
        String name = Block.blockRegistry.getNameForObject(block);

        if (name == null) {
            return false;
        }

        int idx = name.indexOf(':');
        if (idx < 0) return false;
        String modid = name.substring(0, idx);

        String fullKey = name + ":" + meta;
        String blockKey = name + ":*";
        String modKey = modid + ":*";

        return BLOCK_BLACKLIST.contains(fullKey) || BLOCK_BLACKLIST.contains(blockKey)
                || BLOCK_BLACKLIST.contains(modKey);
    }

    /**
     * The type of damage done when teleporting entities, almost identical to EnderPearl damage
     */
    public DamageSource getEnderDamage() {
        return new DamageSource("fall").setDamageBypassesArmor().setProjectile();
    }

    /**
     * Most generic handling of EnderTeleportEvent
     *
     * @param event to handle
     * @return true only if the event is not cancelled
     */
    private boolean handleTeleportEvent(EnderTeleportEvent event) {
        if (!MinecraftForge.EVENT_BUS.post(event)) {
            for (int i = 0; i < 32; ++i) {
                this.worldObj.spawnParticle(
                        PARTICLES,
                        event.targetX,
                        event.targetY + this.rand.nextDouble() * 2.0D,
                        event.targetZ,
                        this.rand.nextGaussian(),
                        0.0D,
                        this.rand.nextGaussian());
            }
            if (event.entity.isRiding()) {
                event.entity.mountEntity(null);
            }
            event.entityLiving.setPositionAndUpdate(event.targetX, event.targetY, event.targetZ);
            event.entity.fallDistance = 0.0F;
            event.entity.attackEntityFrom(getEnderDamage(), event.attackDamage);
            return true;
        }
        return false;
    }
}
