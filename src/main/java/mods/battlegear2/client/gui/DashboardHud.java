package mods.battlegear2.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeHooks;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.gtnewhorizons.dashboard.api.HudBounds;
import com.gtnewhorizons.dashboard.api.HudElement;
import com.gtnewhorizons.dashboard.api.HudRegistry;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.battlegear2.utils.BattlegearConfig;

/**
 * Lets players move, resize and hide the shield block bar and the quiver slots in Dashboard's HUD editor. The only
 * class using Dashboard, so only call it while Dashboard is loaded.
 */
@SideOnly(Side.CLIENT)
public final class DashboardHud {

    private static final int SHIELD_BAR_WIDTH = 182;
    private static final int SHIELD_BAR_HEIGHT = 9;
    private static final int QUIVER_SLOTS = 4;
    private static final int QUIVER_HALF_WIDTH = 1 + QUIVER_SLOTS * 10;

    private static final HudElement SHIELD_BAR = new HudElement(
            "battlegear2:shield_bar",
            DashboardHud::getShieldBarBounds);

    /** One pixel wider and taller than the bar, for the selected slot's frame. */
    private static final HudElement QUIVER = new HudElement(
            "battlegear2:quiver",
            (screenWidth, screenHeight) -> new HudBounds(
                    screenWidth / 2 + BattlegearConfig.quiverBarOffset[0] - QUIVER_HALF_WIDTH - 1,
                    BattlegearConfig.quiverBarOffset[1] - 1,
                    QUIVER_HALF_WIDTH * 2 + 2,
                    BattlegearInGameGUI.SLOT_H + 1));

    private static final Gui GUI = new Gui();

    private DashboardHud() {}

    public static void register() {
        SHIELD_BAR.setPreview(
                (screenWidth, screenHeight) -> HudRegistry
                        .render(SHIELD_BAR, () -> drawShieldBarPreview(screenWidth, screenHeight)));
        QUIVER.setPreview(
                (screenWidth, screenHeight) -> HudRegistry
                        .render(QUIVER, () -> drawQuiverPreview(QUIVER.getDefaultBounds(screenWidth, screenHeight))));
        HudRegistry.register(SHIELD_BAR);
        HudRegistry.register(QUIVER);
    }

    // False means the player hid the element, so skip drawing it and do not call endRender()

    static boolean beginShieldBar() {
        return HudRegistry.beginRender(SHIELD_BAR);
    }

    static boolean beginQuiver() {
        return HudRegistry.beginRender(QUIVER);
    }

    static void endRender() {
        HudRegistry.endRender();
    }

    /**
     * Above the hotbar, raised like the bar itself: over the health bar in survival, and over the armor or air bar too
     * when either shows or the player rides a horse.
     */
    private static HudBounds getShieldBarBounds(int screenWidth, int screenHeight) {
        int y = screenHeight - 35 + BattlegearConfig.shieldBarOffset[1];
        EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
        if (player != null) {
            boolean ridingHorse = player.isRidingHorse();
            if (player.capabilities.isCreativeMode) {
                y -= ridingHorse ? 5 : 0;
            } else {
                y -= 16;
                if (ridingHorse || player.getAir() < 300 || ForgeHooks.getTotalArmorValue(player) > 0) {
                    y -= 10;
                }
            }
        }
        return new HudBounds(
                screenWidth / 2 - 91 + BattlegearConfig.shieldBarOffset[0],
                y,
                SHIELD_BAR_WIDTH,
                SHIELD_BAR_HEIGHT);
    }

    /** The bar two thirds full, in its normal color. */
    private static void drawShieldBarPreview(int screenWidth, int screenHeight) {
        HudBounds bounds = getShieldBarBounds(screenWidth, screenHeight);
        bindTexture(BattlegearInGameGUI.resourceLocationShield);
        GUI.drawTexturedModalRect(bounds.x, bounds.y, 0, 0, SHIELD_BAR_WIDTH, SHIELD_BAR_HEIGHT);
        float[] colour = BattlegearInGameGUI.COLOUR_DEFAULT;
        GL11.glColor3f(colour[0], colour[1], colour[2]);
        GUI.drawTexturedModalRect(
                bounds.x,
                bounds.y,
                0,
                SHIELD_BAR_HEIGHT,
                SHIELD_BAR_WIDTH * 2 / 3,
                SHIELD_BAR_HEIGHT);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(GL11.GL_BLEND);
    }

    /** The two ends of the hotbar texture with the first slot selected, holding arrows. */
    private static void drawQuiverPreview(HudBounds bounds) {
        Minecraft mc = Minecraft.getMinecraft();
        int centerX = bounds.x + 1 + QUIVER_HALF_WIDTH;
        int y = bounds.y + 1;
        bindTexture(BattlegearInGameGUI.resourceLocation);
        GUI.drawTexturedModalRect(centerX - QUIVER_HALF_WIDTH, y, 0, 0, QUIVER_HALF_WIDTH, BattlegearInGameGUI.SLOT_H);
        GUI.drawTexturedModalRect(
                centerX,
                y,
                182 - QUIVER_HALF_WIDTH,
                0,
                QUIVER_HALF_WIDTH,
                BattlegearInGameGUI.SLOT_H);
        GUI.drawTexturedModalRect(bounds.x, bounds.y, 0, 22, 24, BattlegearInGameGUI.SLOT_H);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.enableGUIStandardItemLighting();
        ItemStack arrows = new ItemStack(Items.arrow, 64);
        int itemX = centerX - QUIVER_HALF_WIDTH + 3;
        BattlegearInGameGUI.itemRenderer
                .renderItemAndEffectIntoGUI(mc.fontRenderer, mc.renderEngine, arrows, itemX, y + 3);
        BattlegearInGameGUI.itemRenderer
                .renderItemOverlayIntoGUI(mc.fontRenderer, mc.renderEngine, arrows, itemX, y + 3);
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glDisable(GL11.GL_BLEND);
    }

    private static void bindTexture(ResourceLocation texture) {
        Minecraft.getMinecraft().renderEngine.bindTexture(texture);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
