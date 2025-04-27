package mods.battlegear2.mixins.early;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import mods.battlegear2.Offhand;
import mods.battlegear2.api.core.BattlegearUtils;
import mods.battlegear2.api.core.IBattlePlayer;

@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer extends EntityLivingBase implements IBattlePlayer {

    @Shadow
    private ItemStack itemInUse;
    @Shadow
    public InventoryPlayer inventory;
    @Unique
    private float battlegear2$offHandSwingProgress = 0F;
    @Unique
    private float battlegear2$prevOffHandSwingProgress = 0F;
    @Unique
    private int battlegear2$offHandSwingProgressInt = 0;
    @Unique
    private boolean battlegear2$isOffHandSwingInProgress = false;
    @Unique
    private int battlegear2$specialActionTimer = 0;
    @Unique
    private boolean battlegear2$isShielding = false;

    private MixinEntityPlayer(World p_i1594_1_) {
        super(p_i1594_1_);
    }

    @Unique
    private int battlegear2$local$onItemFinish$i;

    @ModifyExpressionValue(
            method = "onItemUseFinish",
            at = @At(value = "FIELD", target = "Lnet/minecraft/item/ItemStack;stackSize:I", ordinal = 0))
    private int battlegear2$captureLocal$onItemFinish(int i) {
        this.battlegear2$local$onItemFinish$i = i;
        return i;
    }

    @ModifyExpressionValue(
            method = "onItemUseFinish",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/event/ForgeEventFactory;onItemUseFinish(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/item/ItemStack;ILnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;",
                    remap = false))
    private ItemStack battlegear2$onItemUseFinish$beforeFinishUse(ItemStack itemStack) {
        return BattlegearUtils.beforeFinishUseEvent(
                (EntityPlayer) (Object) this,
                this.itemInUse,
                itemStack,
                this.battlegear2$local$onItemFinish$i);
    }

    @ModifyExpressionValue(
            method = "onUpdate",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/InventoryPlayer;getCurrentItem()Lnet/minecraft/item/ItemStack;"))
    private ItemStack battlegear2$onUpdate$getCurrentItem(ItemStack currentItemStack) {
        EntityPlayer player = (EntityPlayer) (Object) this;
        if (BattlegearUtils.isPlayerInBattlemode(player)) {
            ItemStack itemStack = Offhand.getOffhandStack(player);
            if (itemInUse == itemStack) {
                return itemStack;
            }
        }
        return currentItemStack;
    }

    /**
     * @author Alexdoru
     * @reason IDK it's the original mod that does that
     */
    @Overwrite
    public boolean interactWith(Entity var1) {
        return BattlegearUtils.interactWith((EntityPlayer) (Object) this, var1);
    }

    @Override
    protected void updateArmSwingProgress() {
        super.updateArmSwingProgress();
        this.battlegear2$prevOffHandSwingProgress = this.battlegear2$offHandSwingProgress;
        int var1 = this.getArmSwingAnimationEnd();
        if (this.battlegear2$isOffHandSwingInProgress) {
            ++this.battlegear2$offHandSwingProgressInt;
            if (this.battlegear2$offHandSwingProgressInt >= var1) {
                this.battlegear2$offHandSwingProgressInt = 0;
                this.battlegear2$isOffHandSwingInProgress = false;
            }
        } else {
            this.battlegear2$offHandSwingProgressInt = 0;
        }

        this.battlegear2$offHandSwingProgress = (float) this.battlegear2$offHandSwingProgressInt / (float) var1;
        if (this.battlegear2$specialActionTimer > 0) {
            this.battlegear2$isOffHandSwingInProgress = false;
            this.isSwingInProgress = false;
            this.battlegear2$offHandSwingProgress = 0.0F;
            this.battlegear2$offHandSwingProgressInt = 0;
            this.swingProgress = 0.0F;
            this.swingProgressInt = 0;
        }

    }

    @Override
    public void battlegear2$swingOffItem() {
        if (!this.battlegear2$isOffHandSwingInProgress
                || this.battlegear2$offHandSwingProgressInt >= this.getArmSwingAnimationEnd() / 2
                || this.battlegear2$offHandSwingProgressInt < 0) {
            this.battlegear2$offHandSwingProgressInt = -1;
            this.battlegear2$isOffHandSwingInProgress = true;
        }
    }

    @Override
    public float battlegear2$getOffSwingProgress(float frame) {
        float diff = this.battlegear2$offHandSwingProgress - this.battlegear2$prevOffHandSwingProgress;
        if (diff < 0.0F) {
            ++diff;
        }
        return this.battlegear2$prevOffHandSwingProgress + diff * frame;
    }

    public void battlegear2$attackTargetEntityWithCurrentOffItem(Entity target) {
        BattlegearUtils.attackTargetEntityWithCurrentOffItem((EntityPlayer) (Object) this, target);
    }

    @Override
    public boolean battlegear2$isBattlemode() {
        return BattlegearUtils.isPlayerInBattlemode((EntityPlayer) (Object) this);
    }

    @Override
    public boolean battlegear2$isBlockingWithShield() {
        return BattlegearUtils.canBlockWithShield((EntityPlayer) (Object) this) && this.battlegear2$isShielding;
    }

    @Override
    public void battlegear2$setBlockingWithShield(boolean block) {
        this.battlegear2$isShielding = block && BattlegearUtils.canBlockWithShield((EntityPlayer) (Object) this);
    }

    @Override
    public int battlegear2$getSpecialActionTimer() {
        return this.battlegear2$specialActionTimer;
    }

    @Override
    public void battlegear2$setSpecialActionTimer(int time) {
        this.battlegear2$specialActionTimer = time;
    }

}
