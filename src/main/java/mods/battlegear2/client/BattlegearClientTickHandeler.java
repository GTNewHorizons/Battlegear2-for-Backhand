package mods.battlegear2.client;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import cpw.mods.fml.relauncher.Side;
import mods.battlegear2.Battlegear;
import mods.battlegear2.BattlemodeHookContainerClass;
import mods.battlegear2.Offhand;
import mods.battlegear2.api.EnchantmentHelper;
import mods.battlegear2.api.PlayerEventChild;
import mods.battlegear2.api.core.BattlegearUtils;
import mods.battlegear2.api.core.IBattlePlayer;
import mods.battlegear2.api.core.IInventoryPlayerBattle;
import mods.battlegear2.api.core.IOffhandRender;
import mods.battlegear2.api.quiver.QuiverArrowRegistry;
import mods.battlegear2.api.shield.IShield;
import mods.battlegear2.api.weapons.IExtendedReachWeapon;
import mods.battlegear2.enchantments.BaseEnchantment;
import mods.battlegear2.packet.BattlegearAnimationPacket;
import mods.battlegear2.packet.BattlegearShieldBlockPacket;
import mods.battlegear2.packet.OffhandPlaceBlockPacket;
import mods.battlegear2.utils.EnumBGAnimations;

public final class BattlegearClientTickHandeler {

    private static final int FLASH_MAX = 30;
    private final KeyBinding drawWeapons, special;
    private final Minecraft mc;

    private float blockBar = 1;
    private float partialTick;
    private boolean wasBlocking = false;
    private int previousBattlemode = IInventoryPlayerBattle.OFFSET;
    private int previousNormal = 0;
    private int flashTimer;
    private boolean specialDone = false, drawDone = false, inBattle = false;
    public static final BattlegearClientTickHandeler INSTANCE = new BattlegearClientTickHandeler();

    private BattlegearClientTickHandeler() {
        drawWeapons = new KeyBinding(I18n.format("key.drawWeapons"), Keyboard.KEY_G, "key.categories.battlegear");
        special = new KeyBinding(I18n.format("key.special"), Keyboard.KEY_NONE, "key.categories.battlegear");
        ClientRegistry.registerKeyBinding(drawWeapons);
        ClientRegistry.registerKeyBinding(special);
        mc = FMLClientHandler.instance().getClient();
    }

    @SubscribeEvent
    public void keyDown(TickEvent.ClientTickEvent event) {
        if (Battlegear.battlegearEnabled) {
            // null checks to prevent any crash outside the world (and to make sure we have no screen open)
            if (mc.thePlayer != null && mc.theWorld != null && mc.currentScreen == null) {
                EntityClientPlayerMP player = mc.thePlayer;
                if (event.phase == TickEvent.Phase.START) {
                    if (!specialDone && special.getIsKeyPressed()
                            && ((IBattlePlayer) player).battlegear2$getSpecialActionTimer() == 0) {
                        ItemStack quiver = QuiverArrowRegistry.getArrowContainer(player);

                        if (quiver != null) {
                            FMLProxyPacket p = new BattlegearAnimationPacket(EnumBGAnimations.SpecialAction, player)
                                    .generatePacket();
                            Battlegear.packetHandler.sendPacketToServer(p);
                            ((IBattlePlayer) player).battlegear2$setSpecialActionTimer(2);
                        } else if (((IBattlePlayer) player).battlegear2$isBattlemode()) {
                            ItemStack offhand = Offhand.getOffhandStack(player);

                            if (offhand != null && offhand.getItem() instanceof IShield) {
                                float shieldBashPenalty = 0.33F - 0.06F
                                        * EnchantmentHelper.getEnchantmentLevel(BaseEnchantment.bashWeight, offhand);

                                if (blockBar >= shieldBashPenalty) {
                                    FMLProxyPacket p = new BattlegearAnimationPacket(
                                            EnumBGAnimations.SpecialAction,
                                            player).generatePacket();
                                    Battlegear.packetHandler.sendPacketToServer(p);
                                    ((IBattlePlayer) player).battlegear2$setSpecialActionTimer(
                                            ((IShield) offhand.getItem()).getBashTimer(offhand));

                                    blockBar -= shieldBashPenalty;
                                }
                            }
                        }
                        specialDone = true;
                    } else if (specialDone && !special.getIsKeyPressed()) {
                        specialDone = false;
                    }
                    if (!drawDone && drawWeapons.getIsKeyPressed()) {
                        if (((IBattlePlayer) player).battlegear2$isBattlemode()) {
                            previousBattlemode = player.inventory.currentItem;
                            player.inventory.currentItem = previousNormal;
                        } else {
                            previousNormal = player.inventory.currentItem;
                            player.inventory.currentItem = previousBattlemode;
                        }
                        mc.playerController.syncCurrentPlayItem();
                        drawDone = true;
                    } else if (drawDone && !drawWeapons.getIsKeyPressed()) {
                        drawDone = false;
                    }
                    inBattle = ((IBattlePlayer) player).battlegear2$isBattlemode();
                } else {
                    if (inBattle && !((IBattlePlayer) player).battlegear2$isBattlemode()) {
                        for (int i = 0; i < IInventoryPlayerBattle.WEAPON_SETS; ++i) {
                            if (mc.gameSettings.keyBindsHotbar[i].getIsKeyPressed()) {
                                previousBattlemode = IInventoryPlayerBattle.OFFSET + i;
                            }
                        }
                        player.inventory.currentItem = previousBattlemode;
                        mc.playerController.syncCurrentPlayItem();
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.player == mc.thePlayer) {
            if (event.phase == TickEvent.Phase.START) {
                tickStart(mc.thePlayer);
            } else {
                tickEnd(mc.thePlayer);
            }
        }
    }

    private void tickStart(EntityPlayer player) {
        if (Offhand.isOffhandVisible((IBattlePlayer) player)) {
            ItemStack offhand = Offhand.getOffhandStack(player);
            if (offhand != null) {
                if (offhand.getItem() instanceof IShield) {
                    if (flashTimer == FLASH_MAX) {
                        player.motionY = player.motionY / 2;
                    }
                    if (flashTimer > 0) {
                        flashTimer--;
                    }
                    if (mc.gameSettings.keyBindUseItem.getIsKeyPressed() && !player.isSwingInProgress) {
                        blockBar -= ((IShield) offhand.getItem()).getDecayRate(offhand);
                        if (blockBar > 0) {
                            if (!wasBlocking) {
                                Battlegear.packetHandler.sendPacketToServer(
                                        new BattlegearShieldBlockPacket(true, player).generatePacket());
                            }
                            wasBlocking = true;
                        } else {
                            if (wasBlocking) {
                                // Send packet
                                Battlegear.packetHandler.sendPacketToServer(
                                        new BattlegearShieldBlockPacket(false, player).generatePacket());
                            }
                            wasBlocking = false;
                            blockBar = 0;
                        }
                    } else {
                        if (wasBlocking) {
                            // send packet
                            Battlegear.packetHandler.sendPacketToServer(
                                    new BattlegearShieldBlockPacket(false, player).generatePacket());
                        }
                        wasBlocking = false;
                        blockBar += ((IShield) offhand.getItem()).getRecoveryRate(offhand);
                        if (blockBar > 1) {
                            blockBar = 1;
                        }
                    }
                } else if (mc.gameSettings.keyBindUseItem.getIsKeyPressed() && mc.rightClickDelayTimer == 4
                        && !player.isUsingItem()) {
                            tryCheckUseItem(offhand, player);
                        }
            }
        }
    }

    public void tryCheckUseItem(ItemStack offhand, EntityPlayer player) {
        MovingObjectPosition mouseOver = mc.objectMouseOver;
        boolean flag = true;
        if (mouseOver != null && mouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            int j = mouseOver.blockX;
            int k = mouseOver.blockY;
            int l = mouseOver.blockZ;
            if (!player.worldObj.getBlock(j, k, l).isAir(player.worldObj, j, k, l)) {
                final int size = offhand.stackSize;
                int i1 = mouseOver.sideHit;
                PlayerEventChild.UseOffhandItemEvent useItemEvent = new PlayerEventChild.UseOffhandItemEvent(
                        new PlayerInteractEvent(
                                player,
                                PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK,
                                j,
                                k,
                                l,
                                i1,
                                player.worldObj),
                        offhand);
                if (!MinecraftForge.EVENT_BUS.post(useItemEvent)) {
                    BattlegearUtils.refreshAttributes(player, false);
                    boolean result = onPlayerPlaceBlock(
                            mc.playerController,
                            player,
                            useItemEvent.offhand,
                            j,
                            k,
                            l,
                            i1,
                            mouseOver.hitVec);
                    BattlegearUtils.refreshAttributes(player, true);
                    if (result) {
                        if (useItemEvent.swingOffhand)
                            BattlegearUtils.sendOffSwingEvent(useItemEvent.event, useItemEvent.offhand);
                        flag = false;
                    }
                }
                if (useItemEvent.offhand.stackSize == 0) {
                    BattlegearUtils.setPlayerOffhandItem(player, null);
                } else if (useItemEvent.offhand.stackSize != size || mc.playerController.isInCreativeMode()) {
                    ((IOffhandRender) mc.entityRenderer.itemRenderer).battlegear2$setEquippedOffHandProgress(0.0F);
                }
            }
        }
        if (flag) {
            offhand = Offhand.getOffhandStack(player);
            PlayerEventChild.UseOffhandItemEvent useItemEvent = new PlayerEventChild.UseOffhandItemEvent(
                    new PlayerInteractEvent(
                            player,
                            PlayerInteractEvent.Action.RIGHT_CLICK_AIR,
                            0,
                            0,
                            0,
                            -1,
                            player.worldObj),
                    offhand);
            if (offhand != null && !MinecraftForge.EVENT_BUS.post(useItemEvent)) {
                Battlegear.packetHandler.sendPacketToServer(
                        new OffhandPlaceBlockPacket(-1, -1, -1, 255, useItemEvent.offhand, 0.0F, 0.0F, 0.0F)
                                .generatePacket());
                if (useItemEvent.event.useItem != Event.Result.DENY) {
                    BattlegearUtils.refreshAttributes(player, false);
                    flag = BattlemodeHookContainerClass.tryUseItem(player, useItemEvent.offhand, Side.CLIENT);
                    BattlegearUtils.refreshAttributes(player, true);
                }
                if (flag) {
                    if (useItemEvent.swingOffhand)
                        BattlegearUtils.sendOffSwingEvent(useItemEvent.event, useItemEvent.offhand);
                    ((IOffhandRender) mc.entityRenderer.itemRenderer).battlegear2$setEquippedOffHandProgress(0.0F);
                }
            }
        }
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            partialTick = event.renderTickTime;
            if (mc.currentScreen instanceof GuiMainMenu) {
                Battlegear.battlegearEnabled = false;
            }
        }
    }

    private boolean onPlayerPlaceBlock(PlayerControllerMP controller, EntityPlayer player, ItemStack offhand, int i,
            int j, int k, int l, Vec3 hitVec) {
        float f = (float) hitVec.xCoord - (float) i;
        float f1 = (float) hitVec.yCoord - (float) j;
        float f2 = (float) hitVec.zCoord - (float) k;
        boolean flag = false;
        int i1;
        final World worldObj = player.worldObj;
        if (offhand.getItem().onItemUseFirst(offhand, player, worldObj, i, j, k, l, f, f1, f2)) {
            return true;
        }
        if (!player.isSneaking() || player.getCurrentEquippedItem() == null
                || player.getCurrentEquippedItem().getItem().doesSneakBypassUse(worldObj, i, j, k, player)) {
            Block b = worldObj.getBlock(i, j, k);
            if (!b.isAir(worldObj, i, j, k) && b.onBlockActivated(worldObj, i, j, k, player, l, f, f1, f2)) {
                flag = true;
            }
        }
        if (!flag && offhand.getItem() instanceof ItemBlock) {
            ItemBlock itemblock = (ItemBlock) offhand.getItem();
            if (!itemblock.func_150936_a(worldObj, i, j, k, l, player, offhand)) {
                return false;
            }
        }
        Battlegear.packetHandler
                .sendPacketToServer(new OffhandPlaceBlockPacket(i, j, k, l, offhand, f, f1, f2).generatePacket());
        if (flag) {
            return true;
        } else {
            if (controller.isInCreativeMode()) {
                i1 = offhand.getItemDamage();
                int j1 = offhand.stackSize;
                boolean flag1 = offhand.tryPlaceItemIntoWorld(player, worldObj, i, j, k, l, f, f1, f2);
                offhand.setItemDamage(i1);
                offhand.stackSize = j1;
                return flag1;
            } else {
                if (!offhand.tryPlaceItemIntoWorld(player, worldObj, i, j, k, l, f, f1, f2)) {
                    return false;
                }
                if (offhand.stackSize <= 0) {
                    ForgeEventFactory.onPlayerDestroyItem(player, offhand);
                }
                return true;
            }
        }
    }

    private void tickEnd(EntityPlayer player) {
        ItemStack offhand = Offhand.getOffhandStack(player);
        Battlegear.proxy.tryUseDynamicLight(player, offhand);
        // If we use a shield
        if (offhand != null && offhand.getItem() instanceof IShield) {
            if (mc.gameSettings.keyBindUseItem.getIsKeyPressed() && !player.isSwingInProgress && blockBar > 0) {
                player.motionX = player.motionX / 5;
                player.motionZ = player.motionZ / 5;
            }
        }

        // If we JUST swung an Item
        if (player.swingProgressInt == 1) {
            ItemStack mainhand = player.getCurrentEquippedItem();
            if (mainhand != null && mainhand.getItem() instanceof IExtendedReachWeapon) {
                float extendedReach = ((IExtendedReachWeapon) mainhand.getItem()).getReachModifierInBlocks(mainhand);
                if (extendedReach > 0) {
                    MovingObjectPosition mouseOver = Battlegear.proxy
                            .getMouseOver(partialTick, extendedReach + mc.playerController.getBlockReachDistance());
                    if (mouseOver != null && mouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
                        Entity target = mouseOver.entityHit;
                        if (target instanceof EntityLivingBase && target != player
                                && player.getDistanceToEntity(target) > mc.playerController.getBlockReachDistance()) {
                            if (target.hurtResistantTime != ((EntityLivingBase) target).maxHurtResistantTime) {
                                mc.playerController.attackEntity(player, target);
                            }
                        }
                    }
                }
            }
        }
    }

    public static void resetFlash() {
        INSTANCE.flashTimer = FLASH_MAX;
    }

    public static int getFlashTimer() {
        return INSTANCE.flashTimer;
    }

    public static float getBlockTime() {
        return INSTANCE.blockBar;
    }

    public static void reduceBlockTime(float value) {
        INSTANCE.blockBar -= value;
    }

    public static float getPartialTick() {
        return INSTANCE.partialTick;
    }

    public static ItemStack getPreviousMainhand(EntityPlayer player) {
        return player.inventory.getStackInSlot(INSTANCE.previousBattlemode);
    }

    public static ItemStack getPreviousOffhand(EntityPlayer player) {
        return player.inventory.getStackInSlot(INSTANCE.previousBattlemode + IInventoryPlayerBattle.WEAPON_SETS);
    }
}
