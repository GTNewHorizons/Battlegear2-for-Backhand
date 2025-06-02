package mods.battlegear2.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import mods.battlegear2.Battlegear;
import mods.battlegear2.api.EnchantmentHelper;
import mods.battlegear2.api.core.IBattlePlayer;
import mods.battlegear2.api.quiver.QuiverArrowRegistry;
import mods.battlegear2.api.shield.IShield;
import mods.battlegear2.enchantments.BaseEnchantment;
import mods.battlegear2.packet.BattlegearAnimationPacket;
import mods.battlegear2.packet.BattlegearShieldBlockPacket;
import mods.battlegear2.utils.EnumBGAnimations;
import xonin.backhand.api.core.BackhandUtils;

public final class BattlegearClientTickHandeler {

    private static final int FLASH_MAX = 30;
    private final KeyBinding special;
    private final Minecraft mc;

    private float blockBar = 1;
    private float partialTick;
    private boolean wasBlocking = false;
    private int flashTimer;
    private boolean specialDone = false;
    public static final BattlegearClientTickHandeler INSTANCE = new BattlegearClientTickHandeler();

    private BattlegearClientTickHandeler() {
        special = new KeyBinding(I18n.format("key.special"), Keyboard.KEY_NONE, "key.categories.battlegear");
        ClientRegistry.registerKeyBinding(special);
        mc = FMLClientHandler.instance().getClient();
    }

    @SubscribeEvent
    public void keyDown(TickEvent.ClientTickEvent event) {
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
                    } else {
                        ItemStack offhand = BackhandUtils.getOffhandItem(player);

                        if (offhand != null && offhand.getItem() instanceof IShield) {
                            float shieldBashPenalty = 0.33F - 0.06F
                                    * EnchantmentHelper.getEnchantmentLevel(BaseEnchantment.bashWeight, offhand);

                            if (blockBar >= shieldBashPenalty) {
                                FMLProxyPacket p = new BattlegearAnimationPacket(EnumBGAnimations.SpecialAction, player)
                                        .generatePacket();
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
            }
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.player == mc.thePlayer) {
            if (event.phase == TickEvent.Phase.START) {
                tickStart(mc.thePlayer);
            }
        }
    }

    private void tickStart(EntityPlayer player) {
        ItemStack offhand = BackhandUtils.getOffhandItem(player);
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
                            Battlegear.packetHandler
                                    .sendPacketToServer(new BattlegearShieldBlockPacket(true, player).generatePacket());
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
                        Battlegear.packetHandler
                                .sendPacketToServer(new BattlegearShieldBlockPacket(false, player).generatePacket());
                    }
                    wasBlocking = false;
                    blockBar += ((IShield) offhand.getItem()).getRecoveryRate(offhand);
                    if (blockBar > 1) {
                        blockBar = 1;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            partialTick = event.renderTickTime;
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
}
