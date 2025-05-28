package mods.battlegear2.mixins.early;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import mods.battlegear2.api.core.BattlegearUtils;
import mods.battlegear2.api.core.IBattlePlayer;

@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer extends EntityLivingBase implements IBattlePlayer {

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

    @Override
    public boolean battlegear2$isBattlemode() {
        // Always false in this version.
        return false;
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
