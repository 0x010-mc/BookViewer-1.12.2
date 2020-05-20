package me.nullx.bookviewer;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.nullx.bookviewer.uiscreen.GuiScreenNewBook;
import net.labymod.api.LabyModAddon;
import net.labymod.settings.elements.*;
import net.labymod.utils.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.text.TextComponentString;

import java.util.List;

public class BookViewer extends LabyModAddon {

    private String commandName;

    @Override
    public void onEnable() {
        getApi().getEventManager().register(this::handleMessage);
    }

    private boolean handleMessage(String message) {
        if (commandName.equalsIgnoreCase(message.split(" ")[0])) {
            ItemStack itemStack = Minecraft.getMinecraft().player.inventory.getCurrentItem();
            if (itemStack.getItem() == Items.WRITTEN_BOOK) {
                new Thread(() -> {
                    NBTTagList cmp = itemStack.getTagCompound().getTagList("pages", 8);
                    String[] pages = new String[cmp.tagCount()];
                    Gson g = new Gson();
                    for (int i = 0; i < cmp.tagCount(); i++) {
                        NBTBase base = cmp.get(i);
                        if (base instanceof NBTTagString) {
                            NBTTagString s = (NBTTagString) base;
                            JsonObject o = g.fromJson(s.getString(), JsonObject.class);
                            pages[i] = o.get("text").getAsString();
                        }
                    }

                    try {
                        Thread.sleep(10);
                        Minecraft.getMinecraft().displayGuiScreen(new GuiScreenNewBook(pages));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            } else {
                Minecraft.getMinecraft().player.sendMessage(new TextComponentString("You need to hold a book."));
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

    @Override
    public void loadConfig() {
        loadCommandPrefix();
    }

    @Override
    protected void fillSettings(List<SettingsElement> list) {
        list.add(new StringElement("Command", this, new ControlElement.IconData(Material.BOOK), "command_name", commandName));
    }
}
