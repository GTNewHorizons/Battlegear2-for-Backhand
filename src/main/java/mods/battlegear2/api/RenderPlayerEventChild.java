package mods.battlegear2.api;

import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderPlayerEvent;

import cpw.mods.fml.common.eventhandler.Cancelable;
import mods.battlegear2.api.core.BattlegearUtils;
import mods.battlegear2.client.utils.BattlegearRenderHelper;

/**
 * Those events are posted to {@link BattlegearUtils.RENDER_BUS} from {@link BattlegearRenderHelper}
 */
public abstract class RenderPlayerEventChild extends RenderPlayerEvent {

    public static enum PlayerElementType {
        Offhand,
        ItemOffhand,
    }

    /**
     * Describe what element is rendered, either the player arm or the item hold/sheathed
     */
    public final PlayerElementType type;
    /**
     * True in first person rendering, false in third person rendering
     */
    public final boolean isFirstPerson;
    /**
     * The element to be rendered, or null if a player arm
     */
    public final ItemStack element;

    public RenderPlayerEventChild(RenderPlayerEvent parent, PlayerElementType type, boolean firstPerson,
            ItemStack item) {
        super(parent.entityPlayer, parent.renderer, parent.partialRenderTick);
        this.type = type;
        this.isFirstPerson = firstPerson;
        this.element = item;
    }

    @Cancelable
    public static class PreRenderPlayerElement extends RenderPlayerEventChild {

        public PreRenderPlayerElement(RenderPlayerEvent parent, boolean isFirstPerson, PlayerElementType type,
                ItemStack item) {
            super(parent, type, isFirstPerson, item);
        }
    }

    public static class PostRenderPlayerElement extends RenderPlayerEventChild {

        public PostRenderPlayerElement(RenderPlayerEvent parent, boolean isFirstPerson, PlayerElementType type,
                ItemStack item) {
            super(parent, type, isFirstPerson, item);
        }
    }
}
