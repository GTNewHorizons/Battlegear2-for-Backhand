package mods.battlegear2.client.gui;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.registry.ClientRegistry;

public final class BattlegearGuiKeyHandler {

    private final KeyBinding battleInv;
    public static final BattlegearGuiKeyHandler INSTANCE = new BattlegearGuiKeyHandler();

    private BattlegearGuiKeyHandler() {
        battleInv = new KeyBinding(I18n.format("key.battleInv"), Keyboard.KEY_I, "key.categories.battlegear");
        ClientRegistry.registerKeyBinding(battleInv);
    }

}
