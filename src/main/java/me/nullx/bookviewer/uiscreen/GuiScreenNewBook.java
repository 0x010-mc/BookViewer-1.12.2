package me.nullx.bookviewer.uiscreen;

import com.google.common.collect.Lists;
import com.google.gson.JsonParseException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemWrittenBook;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Cursor;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GuiScreenNewBook extends GuiScreen {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ResourceLocation BOOK_GUI_TEXTURES = new ResourceLocation("textures/gui/book.png");


    /**
     * Whether the book is signed or can still be edited
     */
    private final boolean bookIsUnsigned;

    /**
     * Update ticks since the gui was opened
     */
    private final int bookImageWidth = 192;
    private final int bookImageHeight = 192;
    private int bookTotalPages = 1;
    private int currPage;
    private NBTTagList bookPages;
    private List<String> pages;
    private String bookTitle = "";
    private List<ITextComponent> cachedComponents;
    private int cachedPage = -1;
    private GuiScreenNewBook.NextPageButton buttonNextPage;
    private GuiScreenNewBook.NextPageButton buttonPreviousPage;
    private GuiButton buttonDone;

    public GuiScreenNewBook(String... pages) {
        this.bookIsUnsigned = false;

        //this.bookPages = pages.copy();
        this.pages = Arrays.asList(pages);
        //this.bookTotalPages = this.bookPages.tagCount();
        this.bookTotalPages = this.pages.size();

        if (this.bookTotalPages < 1) {
            this.bookTotalPages = 1;
        }
    }

    /**
     * Called from the main game loop to update the screen.
     */
    public void updateScreen() {
        super.updateScreen();
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui() {
        this.buttonList.clear();
        Keyboard.enableRepeatEvents(true);

        this.buttonDone = this.addButton(new GuiButton(0, this.width / 2 - 100, 196, 200, 20, I18n.format("gui.done")));

        int i = (this.width - 192) / 2;
        this.buttonNextPage = this.addButton(new GuiScreenNewBook.NextPageButton(1, i + 120, 156, true));
        this.buttonPreviousPage = this.addButton(new GuiScreenNewBook.NextPageButton(2, i + 38, 156, false));
        this.updateButtons();
    }

    /**
     * Called when the screen is unloaded. Used to disable keyboard repeat events
     */
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    private void updateButtons() {
        this.buttonNextPage.visible = (this.currPage < this.bookTotalPages - 1 || this.bookIsUnsigned);
        this.buttonPreviousPage.visible = this.currPage > 0;
        this.buttonDone.visible = !this.bookIsUnsigned;
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.enabled) {
            if (button.id == 0) {
                mc.displayGuiScreen(null);
            } else if (button.id == 1) {
                if (this.currPage < this.bookTotalPages - 1) {
                    ++this.currPage;
                }
            } else if (button.id == 2) {
                if (this.currPage > 0) {
                    --this.currPage;
                }
            }

            this.updateButtons();
        }
    }

    /**
     * Draws the screen and all the components in it.
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        try {
            this.mc.getTextureManager().bindTexture(BOOK_GUI_TEXTURES);
        } catch (Throwable t) { return; }
        int i = (this.width - 192) / 2;
        int j = 2;
        this.drawTexturedModalRect(i, 2, 0, 0, 192, 192);


        try {
            Mouse.setNativeCursor(Mouse.getNativeCursor());
        } catch (LWJGLException e) {
            e.printStackTrace();
        }

        String s4 = I18n.format("book.pageIndicator", this.currPage + 1, this.bookTotalPages);
        String s5 = "";

        if (this.currPage >= 0 && this.currPage < this.pages.size()) {
            s5 = this.pages.get(this.currPage);
        }

        int j1 = this.fontRenderer.getStringWidth(s4);
        this.fontRenderer.drawString(s4, i - j1 + 192 - 44, 18, 0);

        if (this.cachedComponents == null) {
            this.fontRenderer.drawSplitString(s5, i + 36, 34, 116, 0);
        } else {
            int k1 = Math.min(128 / this.fontRenderer.FONT_HEIGHT, this.cachedComponents.size());

            for (int l1 = 0; l1 < k1; ++l1) {
                ITextComponent itextcomponent2 = this.cachedComponents.get(l1);
                this.fontRenderer.drawString(itextcomponent2.getUnformattedText(), i + 36, 34 + l1 * this.fontRenderer.FONT_HEIGHT, 0);
            }

            ITextComponent itextcomponent1 = this.getClickedComponentAt(mouseX, mouseY);

            if (itextcomponent1 != null) {
                this.handleComponentHover(itextcomponent1, mouseX, mouseY);
            }
        }


        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    /**
     * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
     */
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0) {
            ITextComponent itextcomponent = this.getClickedComponentAt(mouseX, mouseY);

            if (itextcomponent != null && this.handleComponentClick(itextcomponent)) {
                return;
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    /**
     * Executes the click event specified by the given chat component
     */
    public boolean handleComponentClick(ITextComponent component) {
        ClickEvent clickevent = component.getStyle().getClickEvent();

        if (clickevent == null) {
            return false;
        } else if (clickevent.getAction() == ClickEvent.Action.CHANGE_PAGE) {
            String s = clickevent.getValue();

            try {
                int i = Integer.parseInt(s) - 1;

                if (i >= 0 && i < this.bookTotalPages && i != this.currPage) {
                    this.currPage = i;
                    this.updateButtons();
                    return true;
                }
            } catch (Throwable var5) {
                ;
            }

            return false;
        } else {
            boolean flag = super.handleComponentClick(component);

            if (flag && clickevent.getAction() == ClickEvent.Action.RUN_COMMAND) {
                this.mc.displayGuiScreen((GuiScreen) null);
            }

            return flag;
        }
    }

    @Nullable
    public ITextComponent getClickedComponentAt(int p_175385_1_, int p_175385_2_) {
        if (this.cachedComponents == null) {
            return null;
        } else {
            int i = p_175385_1_ - (this.width - 192) / 2 - 36;
            int j = p_175385_2_ - 2 - 16 - 16;

            if (i >= 0 && j >= 0) {
                int k = Math.min(128 / this.fontRenderer.FONT_HEIGHT, this.cachedComponents.size());

                if (i <= 116 && j < this.mc.fontRenderer.FONT_HEIGHT * k + k) {
                    int l = j / this.mc.fontRenderer.FONT_HEIGHT;

                    if (l >= 0 && l < this.cachedComponents.size()) {
                        ITextComponent itextcomponent = this.cachedComponents.get(l);
                        int i1 = 0;

                        for (ITextComponent itextcomponent1 : itextcomponent) {
                            if (itextcomponent1 instanceof TextComponentString) {
                                i1 += this.mc.fontRenderer.getStringWidth(((TextComponentString) itextcomponent1).getText());

                                if (i1 > i) {
                                    return itextcomponent1;
                                }
                            }
                        }
                    }

                    return null;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
    }

    static class NextPageButton extends GuiButton {
        private final boolean isForward;

        public NextPageButton(int p_i46316_1_, int p_i46316_2_, int p_i46316_3_, boolean p_i46316_4_) {
            super(p_i46316_1_, p_i46316_2_, p_i46316_3_, 23, 13, "");
            this.isForward = p_i46316_4_;
        }

        public void drawButton(Minecraft p_191745_1_, int p_191745_2_, int p_191745_3_, float p_191745_4_) {
            if (this.visible) {
                boolean flag = p_191745_2_ >= this.x && p_191745_3_ >= this.y && p_191745_2_ < this.x + this.width && p_191745_3_ < this.y + this.height;
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                p_191745_1_.getTextureManager().bindTexture(GuiScreenNewBook.BOOK_GUI_TEXTURES);
                int i = 0;
                int j = 192;

                if (flag) {
                    i += 23;
                }

                if (!this.isForward) {
                    j += 13;
                }

                this.drawTexturedModalRect(this.x, this.y, i, j, 23, 13);
            }
        }
    }
}
