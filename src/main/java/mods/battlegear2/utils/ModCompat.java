package mods.battlegear2.utils;

import static thaumcraft.common.config.ConfigItems.itemPrimalArrow;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import mods.battlegear2.api.quiver.IArrowFireHandler;
import mods.battlegear2.api.quiver.QuiverArrowRegistry;
import thaumcraft.common.entities.projectile.EntityPrimalArrow;
import thaumcraft.common.items.equipment.ItemPrimalArrow;

public class ModCompat {

    /*
     * Note when registering arrows for the quiver: Other mods should prefer registering using IMC, through
     * FMLInterModComms.sendMessage("battlegear2", "Arrow", itemStack) if possible. Registration here should be focused
     * on mods no modifiable source code.
     */

    public static void init(FMLInitializationEvent event) {
        registerThaumcraftCompat();
    }

    private static void registerThaumcraftCompat() {
        if (!Loader.isModLoaded("Thaumcraft")) return;

        // Allow TC arrows in Quiver
        boolean thaumcraftHandlerRegistered = QuiverArrowRegistry.addArrowFireHandler(new IArrowFireHandler() {

            @Override
            public boolean canFireArrow(ItemStack arrow, World world, EntityPlayer player, float charge) {
                return arrow.getItem() instanceof ItemPrimalArrow;
            }

            @Override
            public EntityArrow getFiredArrow(ItemStack arrow, World world, EntityPlayer player, float charge) {
                if (!(arrow.getItem() instanceof ItemPrimalArrow)) return null;
                return new EntityPrimalArrow(world, player, charge, arrow.getItemDamage());
            }
        });

        if (thaumcraftHandlerRegistered) {
            QuiverArrowRegistry.addArrowToRegistry(itemPrimalArrow, null);
        }
    }
}
