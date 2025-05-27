package mods.battlegear2.packet;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.MinecraftForge;

import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import mods.battlegear2.Battlegear;
import mods.battlegear2.Offhand;
import mods.battlegear2.api.EnchantmentHelper;
import mods.battlegear2.api.quiver.IArrowContainer2;
import mods.battlegear2.api.quiver.QuiverArrowRegistry;
import mods.battlegear2.api.quiver.SwapArrowEvent;
import mods.battlegear2.api.shield.IShield;
import mods.battlegear2.enchantments.BaseEnchantment;

public final class SpecialActionPacket extends AbstractMBPacket {

    public static final String packetName = "MB2|Special";
    private EntityPlayer player;
    private Entity entityHit;

    @Override
    public void process(ByteBuf inputStream, EntityPlayer player) {
        try {
            this.player = player.worldObj.getPlayerEntityByName(ByteBufUtils.readUTF8String(inputStream));
            if (inputStream.readBoolean()) {
                entityHit = player.worldObj.getPlayerEntityByName(ByteBufUtils.readUTF8String(inputStream));
            } else {
                entityHit = player.worldObj.getEntityByID(inputStream.readInt());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        if (this.player != null) {
            if (entityHit instanceof EntityLivingBase) {
                ItemStack offhand = Offhand.getOffhandStack(this.player);
                if (offhand != null && offhand.getItem() instanceof IShield) {
                    if (entityHit.canBePushed()) {
                        double d0 = entityHit.posX - this.player.posX;
                        double d1;

                        for (d1 = entityHit.posZ - this.player.posZ; d0 * d0 + d1 * d1
                                < 1.0E-4D; d1 = (Math.random() - Math.random()) * 0.01D) {
                            d0 = (Math.random() - Math.random()) * 0.01D;
                        }
                        double pow = EnchantmentHelper.getEnchantmentLevel(BaseEnchantment.bashPower, offhand) * 0.1D;

                        ((EntityLivingBase) entityHit).knockBack(this.player, 0, -d0 * (1 + pow), -d1 * (1 + pow));
                    }
                    if (entityHit.getDistanceToEntity(this.player) < 2) {
                        float dam = EnchantmentHelper.getEnchantmentLevel(BaseEnchantment.bashDamage, offhand) * 2F;
                        if (dam > 0) {
                            entityHit.attackEntityFrom(DamageSource.causeThornsDamage(this.player), dam);
                            entityHit.playSound("damage.thorns", 0.5F, 1.0F);
                        }
                    }
                    if (!this.player.worldObj.isRemote && entityHit instanceof EntityPlayerMP) {
                        Battlegear.packetHandler.sendPacketToPlayer(this.generatePacket(), (EntityPlayerMP) entityHit);
                    }
                }
            } else {
                ItemStack quiver = QuiverArrowRegistry.getArrowContainer(this.player);
                if (quiver != null) {
                    SwapArrowEvent swapEvent = new SwapArrowEvent(this.player, quiver);
                    if (!MinecraftForge.EVENT_BUS.post(swapEvent) && swapEvent.slotStep != 0) {
                        ((IArrowContainer2) quiver.getItem()).setSelectedSlot(quiver, swapEvent.getNextSlot());
                        if (this.player instanceof EntityPlayerMP) {
                            Battlegear.packetHandler
                                    .sendPacketToPlayer(this.generatePacket(), (EntityPlayerMP) this.player);
                        }
                    }
                }
            }
        }
    }

    public SpecialActionPacket(EntityPlayer player, Entity entityHit) {
        this.player = player;
        this.entityHit = entityHit;
    }

    public SpecialActionPacket() {}

    @Override
    public String getChannel() {
        return packetName;
    }

    @Override
    public void write(ByteBuf out) {
        boolean isPlayer = entityHit instanceof EntityPlayer;

        ByteBufUtils.writeUTF8String(out, player.getCommandSenderName());

        out.writeBoolean(isPlayer);
        if (isPlayer) {
            ByteBufUtils.writeUTF8String(out, entityHit.getCommandSenderName());
        } else {
            out.writeInt(entityHit != null ? entityHit.getEntityId() : -1);
        }
    }
}
