package mods.battlegear2.mixins.early;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.sugar.Local;

import mods.battlegear2.enchantments.BaseEnchantment;

// Janky way to map Bow Loot to Looting enchantment
@Mixin(EnchantmentHelper.class)
public class MixinEnchantmentHelper {

    @Inject(
            method = "getEnchantmentLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/nbt/NBTTagCompound;getShort(Ljava/lang/String;)S",
                    ordinal = 1,
                    shift = At.Shift.BY,
                    by = 2),
            cancellable = true)
    private static void battlegear2$getEnchantmentLevel(int p_77506_0_, ItemStack p_77506_1_,
            CallbackInfoReturnable<Integer> cir, @Local(ordinal = 0) short short1, @Local(ordinal = 1) short short2) {
        if (p_77506_0_ == Enchantment.looting.effectId && BaseEnchantment.bowLoot.isPresent()
                && short1 == BaseEnchantment.bowLoot.get().effectId) {
            cir.setReturnValue((int) short2);
        }
    }
}
