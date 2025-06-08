package mods.battlegear2.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.RenderSkeleton;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import mods.battlegear2.api.EnchantmentHelper;
import mods.battlegear2.api.IDyable;
import mods.battlegear2.api.RenderItemBarEvent;
import mods.battlegear2.api.core.IBattlePlayer;
import mods.battlegear2.api.quiver.IArrowContainer2;
import mods.battlegear2.api.quiver.QuiverArrowRegistry;
import mods.battlegear2.api.weapons.IBackStabbable;
import mods.battlegear2.api.weapons.IExtendedReachWeapon;
import mods.battlegear2.api.weapons.IHitTimeModifier;
import mods.battlegear2.api.weapons.IPenetrateWeapon;
import mods.battlegear2.client.gui.BattlegearInGameGUI;
import mods.battlegear2.client.model.QuiverModel;
import mods.battlegear2.client.utils.BattlegearRenderHelper;
import mods.battlegear2.enchantments.BaseEnchantment;
import mods.battlegear2.items.ItemWeapon;
import mods.battlegear2.utils.BattlegearConfig;
import xonin.backhand.api.core.BackhandUtils;

public final class BattlegearClientEvents {

    private final BattlegearInGameGUI inGameGUI;
    private final QuiverModel quiverModel;
    private final ResourceLocation quiverDetails;
    private final ResourceLocation quiverBase;
    // public static final ResourceLocation patterns = new ResourceLocation("battlegear2",
    // "textures/heraldry/Patterns-small.png");
    // public static int storageIndex;

    public static final BattlegearClientEvents INSTANCE = new BattlegearClientEvents();

    private BattlegearClientEvents() {
        inGameGUI = new BattlegearInGameGUI();
        quiverModel = new QuiverModel();
        quiverDetails = new ResourceLocation("battlegear2", "textures/armours/quiver/QuiverDetails.png");
        quiverBase = new ResourceLocation("battlegear2", "textures/armours/quiver/QuiverBase.png");
    }

    /**
     * Offset quiver slots rendering according to config values
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void postRenderQuiver(RenderItemBarEvent.QuiverSlots event) {
        event.xOffset += BattlegearConfig.quiverBarOffset[0];
        event.yOffset += BattlegearConfig.quiverBarOffset[1];
    }

    /**
     * Offset shield stamina rendering according to config values
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void postRenderShield(RenderItemBarEvent.ShieldBar event) {
        event.xOffset += BattlegearConfig.shieldBarOffset[0];
        event.yOffset += BattlegearConfig.shieldBarOffset[1];
    }

    /**
     * Render all the Battlegear HUD elements
     */
    @SubscribeEvent(receiveCanceled = true)
    public void postRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type == RenderGameOverlayEvent.ElementType.HOTBAR
                && (BattlegearConfig.forceHUD || !event.isCanceled())) {
            inGameGUI.renderGameOverlay(event.partialTicks, event.mouseX, event.mouseY);
        }
    }

    /**
     * Bend the models when the item in left hand is used And stop the right hand inappropriate bending
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public void renderPlayerLeftItemUsage(RenderLivingEvent.Pre event) {
        if (event.entity instanceof EntityPlayer entityPlayer) {
            ItemStack offhand = BackhandUtils.getOffhandItem(entityPlayer);
            if (offhand != null && event.renderer instanceof RenderPlayer renderer) {
                if (((IBattlePlayer) entityPlayer).battlegear2$isBlockingWithShield()) {
                    renderer.modelArmorChestplate.heldItemLeft = renderer.modelArmor.heldItemLeft = renderer.modelBipedMain.heldItemLeft = 3;
                }
            }
        }
    }

    /**
     * Reset models to default values
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public void resetPlayerLeftHand(RenderPlayerEvent.Post event) {
        event.renderer.modelArmorChestplate.heldItemLeft = event.renderer.modelArmor.heldItemLeft = event.renderer.modelBipedMain.heldItemLeft = 0;
    }

    /**
     * Render a player left hand item, or sheathed items, and quiver on player back
     */
    @SubscribeEvent
    public void render3rdPersonBattlemode(RenderPlayerEvent.Specials.Post event) {

        ModelBiped biped = (ModelBiped) event.renderer.mainModel;
        BattlegearRenderHelper.renderItemIn3rdPerson(event.entityPlayer, biped, event.partialRenderTick);

        ItemStack quiverStack = QuiverArrowRegistry.getArrowContainer(event.entityPlayer);
        if (quiverStack != null && ((IArrowContainer2) quiverStack.getItem()).renderDefaultQuiverModel(quiverStack)) {

            IArrowContainer2 quiver = (IArrowContainer2) quiverStack.getItem();
            int maxStack = quiver.getSlotCount(quiverStack);
            int arrowCount = 0;
            for (int i = 0; i < maxStack; i++) {
                arrowCount += quiver.getStackInSlot(quiverStack, i) == null ? 0 : 1;
            }
            GL11.glColor3f(1, 1, 1);
            Minecraft.getMinecraft().getTextureManager().bindTexture(quiverDetails);
            GL11.glPushMatrix();
            if (event.entityPlayer.getEquipmentInSlot(3) != null) { // chest armor
                GL11.glTranslatef(0, 0, BattlegearRenderHelper.RENDER_UNIT);
            }
            biped.bipedBody.postRender(BattlegearRenderHelper.RENDER_UNIT);
            GL11.glScalef(1.05F, 1.05F, 1.05F);
            quiverModel.render(arrowCount, BattlegearRenderHelper.RENDER_UNIT);

            Minecraft.getMinecraft().getTextureManager().bindTexture(quiverBase);
            if (quiverStack.getItem() instanceof IDyable) {
                int col = ((IDyable) quiver).getColor(quiverStack);
                float red = (float) (col >> 16 & 255) / 255.0F;
                float green = (float) (col >> 8 & 255) / 255.0F;
                float blue = (float) (col & 255) / 255.0F;
                GL11.glColor3f(red, green, blue);
            }
            quiverModel.render(0, BattlegearRenderHelper.RENDER_UNIT);
            GL11.glColor3f(1, 1, 1);

            GL11.glPopMatrix();
        }
    }

    private static final int SKELETON_ARROW = 5;

    /**
     * Render quiver on skeletons if possible
     */
    @SubscribeEvent
    public void renderLiving(RenderLivingEvent.Post event) {

        if (BattlegearConfig.enableSkeletonQuiver && event.entity instanceof EntitySkeleton
                && event.renderer instanceof RenderSkeleton) {

            GL11.glPushMatrix();
            GL11.glDisable(GL11.GL_CULL_FACE);

            GL11.glColor3f(1, 1, 1);
            Minecraft.getMinecraft().getTextureManager().bindTexture(quiverDetails);

            double d0 = (((EntitySkeleton) event.entity).lastTickPosX
                    + ((((EntitySkeleton) event.entity).posX - ((EntitySkeleton) event.entity).lastTickPosX)
                            * BattlegearClientTickHandeler.getPartialTick()));
            double d1 = (((EntitySkeleton) event.entity).lastTickPosY
                    + ((((EntitySkeleton) event.entity).posY - ((EntitySkeleton) event.entity).lastTickPosY)
                            * BattlegearClientTickHandeler.getPartialTick()));
            double d2 = (((EntitySkeleton) event.entity).lastTickPosZ
                    + (((EntitySkeleton) event.entity).posZ - ((EntitySkeleton) event.entity).lastTickPosZ)
                            * BattlegearClientTickHandeler.getPartialTick());

            GL11.glTranslatef(
                    (float) (d0 - RenderManager.renderPosX),
                    (float) (d1 - RenderManager.renderPosY),
                    (float) (d2 - RenderManager.renderPosZ));

            GL11.glScalef(1, -1, 1);

            float f2 = interpolateRotation(event.entity.prevRenderYawOffset, event.entity.renderYawOffset, 0);

            GL11.glRotatef(180.0F - f2, 0.0F, 1.0F, 0.0F);

            if (event.entity.deathTime > 0) {
                float f3 = ((float) event.entity.deathTime + BattlegearClientTickHandeler.getPartialTick() - 1.0F)
                        / 20.0F
                        * 1.6F;
                f3 = MathHelper.sqrt_float(f3);

                if (f3 > 1.0F) {
                    f3 = 1.0F;
                }

                GL11.glRotatef(-f3 * 90, 0.0F, 0.0F, 1.0F);
            }

            GL11.glTranslatef(0, -1.5F, 0);

            GL11.glRotatef(event.entity.rotationPitch, 0, 1, 0);

            if (event.entity.getEquipmentInSlot(3) != null) { // chest armor
                GL11.glTranslatef(0, 0, BattlegearRenderHelper.RENDER_UNIT);
            }
            ((ModelBiped) event.renderer.mainModel).bipedBody.postRender(BattlegearRenderHelper.RENDER_UNIT);
            GL11.glScalef(1.05F, 1.05F, 1.05F);
            quiverModel.render(SKELETON_ARROW, BattlegearRenderHelper.RENDER_UNIT);

            Minecraft.getMinecraft().getTextureManager().bindTexture(quiverBase);
            GL11.glColor3f(0.10F, 0.10F, 0.10F);
            quiverModel.render(0, BattlegearRenderHelper.RENDER_UNIT);
            GL11.glColor3f(1, 1, 1);

            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glPopMatrix();
        }
    }

    /**
     * Counter the bow use fov jerkyness with the draw enchantment
     */
    @SubscribeEvent
    public void onBowFOV(FOVUpdateEvent event) {
        ItemStack stack = event.entity.getItemInUse();
        if (EnchantmentHelper.getEnchantmentLevel(BaseEnchantment.bowCharge, stack) > 0) {
            int i = event.entity.getItemInUseDuration();
            float f1 = (float) i / 20.0F;
            if (f1 > 1.0F) {
                f1 = 1.0F;
            } else {
                f1 *= f1;
            }
            event.newfov /= 1.0F - f1 * 0.15F;
        }
    }

    /**
     * Returns a rotation angle that is inbetween two other rotation angles. par1 and par2 are the angles between which
     * to interpolate, par3 is probably a float between 0.0 and 1.0 that tells us where "between" the two angles we are.
     * Example: par1 = 30, par2 = 50, par3 = 0.5, then return = 40
     */
    public float interpolateRotation(float par1, float par2, float par3) {
        float f3 = par2 - par1;

        while (f3 < -180.0F) {
            f3 += 360.0F;
        }

        while (f3 >= 180.0F) {
            f3 -= 360.0F;
        }

        return par1 + par3 * f3;
    }

    /**
     * Register a few "item" icons
     */
    @SubscribeEvent
    public void preStitch(TextureStitchEvent.Pre event) {
        if (event.map.getTextureType() == 1) {
            ClientProxy.backgroundIcon = new IIcon[] { event.map.registerIcon("battlegear2:slots/mainhand"),
                    event.map.registerIcon("battlegear2:slots/offhand") };

            ClientProxy.bowIcons = new IIcon[3];
            for (int i = 0; i < ClientProxy.bowIcons.length; i++) {
                ClientProxy.bowIcons[i] = event.map.registerIcon("battlegear2:bow_pulling_" + i);
            }
            ClientProxy.bowIronIcons = new IIcon[3];
            for (int i = 0; i < ClientProxy.bowIronIcons.length; i++) {
                ClientProxy.bowIronIcons[i] = event.map.registerIcon("battlegear2:bow.iron_pulling_" + i);
            }
            ClientProxy.bowDiamondIcons = new IIcon[3];
            for (int i = 0; i < ClientProxy.bowDiamondIcons.length; i++) {
                ClientProxy.bowDiamondIcons[i] = event.map.registerIcon("battlegear2:bow.diamond_pulling_" + i);
            }
            // ClientProxy.bowGregIcons = new IIcon[3];
            // for(int i = 0; i < ClientProxy.bowGregIcons.length; i++) {
            // ClientProxy.bowGregIcons[i] = event.map.registerIcon("battlegear2:bow.greg_pulling_"+i);
            // }

            // ClientProxy.bowGoldIcons = new IIcon[3];
            // for(int i = 0; i < ClientProxy.bowGoldIcons.length; i++) {
            // ClientProxy.bowGoldIcons[i] = event.map.registerIcon("battlegear2:bow.gold_pulling_"+i);
            // }

            // storageIndex = PatternStore.DEFAULT.buildPatternAndStore(patterns);
            /*
             * CrestImages.initialise(Minecraft.getMinecraft().getResourceManager()); for (HeraldryPattern pattern :
             * HeraldryPattern.patterns) { pattern.registerIcon(event.map); }
             */
        }
    }

    @SubscribeEvent
    public void onUpdateFOV(FOVUpdateEvent event) {
        float fov = event.fov;

        if (event.entity.isUsingItem() && ((event.entity.getItemInUse().getItem() == BattlegearConfig.mobBowIron)
                || (event.entity.getItemInUse().getItem() == BattlegearConfig.modBowDiamond))) {
            int duration = event.entity.getItemInUseDuration();
            float multiplier = duration / 10.0F;

            if (multiplier > 1.0F) {
                multiplier = 1.0F;
            } else {
                multiplier *= multiplier;
            }

            fov *= 1.0F - multiplier * 0.3F;
        }

        event.newfov = fov;
    }

    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
        if (event.itemStack.getItem() instanceof IPenetrateWeapon
                || event.itemStack.getItem() instanceof IHitTimeModifier
                || event.itemStack.getItem() instanceof IExtendedReachWeapon) {
            for (String txt : event.toolTip) {
                if (txt.startsWith(EnumChatFormatting.BLUE.toString())) {
                    if (txt.contains(
                            StatCollector.translateToLocal(
                                    "attribute.name." + ItemWeapon.armourPenetrate.getAttributeUnlocalizedName()))
                            || txt.contains(
                                    StatCollector.translateToLocal(
                                            "attribute.name." + ItemWeapon.attackSpeed.getAttributeUnlocalizedName()))
                            || txt.contains(
                                    StatCollector.translateToLocal(
                                            "attribute.name."
                                                    + ItemWeapon.extendedReach.getAttributeUnlocalizedName())))
                        event.toolTip.set(
                                event.toolTip.indexOf(txt),
                                EnumChatFormatting.DARK_GREEN + EnumChatFormatting.getTextWithoutFormattingCodes(txt));
                }
            }
        }
        if (event.itemStack.getItem() instanceof IBackStabbable) {
            event.toolTip
                    .add(EnumChatFormatting.GOLD + StatCollector.translateToLocal("attribute.name.weapon.backstab"));
        }
    }
}
