package me.nullx.bookviewer;

import me.nullx.bookviewer.uiscreen.GuiScreenUnclickableBook;
import net.labymod.api.LabyModAddon;
import net.labymod.settings.elements.ControlElement;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.settings.elements.StringElement;
import net.labymod.utils.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;

import java.util.List;

public class BookViewer extends LabyModAddon {

    private String commandName;

    @Override
    public void onEnable() {
        getApi().getEventManager().register(this::handleMessage);
    }

    public static void openBook(ItemStack book) {
        new Thread(() -> {
            try {
                Thread.sleep(10);
                Minecraft.getMinecraft().displayGuiScreen(new GuiScreenUnclickableBook(book));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private boolean handleMessage(String message) {
        if (commandName.equalsIgnoreCase(message.split(" ")[0])) {
            ItemStack itemStack = Minecraft.getMinecraft().player.inventory.getCurrentItem();
            if (itemStack.getItem() == Items.WRITTEN_BOOK) {
                openBook(itemStack);
            } else {
                Minecraft.getMinecraft().player.sendMessage(new TextComponentString("You need to hold a written book."));
            }
            return true;
        }
        return false;
    }

    private void loadCommandPrefix() {
        if (!getConfig().has("command_name")) {
            getConfig().addProperty("command_name", "/readbook");
        }
        commandName = getConfig().get("command_name").getAsString();
    }

    public static void handleMouseInput() {
        Minecraft mc = Minecraft.getMinecraft();
        ItemStack currentItem = mc.player.inventory.getCurrentItem();
        KeyBinding useItem = mc.gameSettings.keyBindUseItem;
        if (useItem.isPressed() && mc.currentScreen == null && currentItem.getItem() == Items.WRITTEN_BOOK) {
            KeyBinding.setKeyBindState(useItem.getKeyCode(), false);
            BookViewer.openBook(currentItem);
        }
    }

    @Override
    public void loadConfig() {
        loadCommandPrefix();
    }

    @Override
    protected void fillSettings(List<SettingsElement> list) {
        list.add(new StringElement("Command", this, new ControlElement.IconData(Material.BOOK), "command_name", commandName));
    }
}