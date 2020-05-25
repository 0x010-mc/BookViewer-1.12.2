package me.nullx.bookviewer.listener;

import me.nullx.bookviewer.BookViewer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

public class MouseInputListener {

    @SubscribeEvent
    public void onInput(InputEvent.MouseInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        ItemStack currentItem = mc.player.inventory.getCurrentItem();
        if (mc.gameSettings.keyBindUseItem.isPressed() && mc.currentScreen == null && currentItem.getItem() == Items.WRITTEN_BOOK) {
            BookViewer.openBook(currentItem);
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
        }
    }

}