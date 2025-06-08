package mods.battlegear2;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import mods.battlegear2.recipies.CraftingHandeler;

public final class BgPlayerTracker {

    public static final BgPlayerTracker INSTANCE = new BgPlayerTracker();

    private BgPlayerTracker() {}

    @SubscribeEvent
    public void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        CraftingHandeler.onCrafting(event.player, event.crafting, event.craftMatrix);
    }
}
