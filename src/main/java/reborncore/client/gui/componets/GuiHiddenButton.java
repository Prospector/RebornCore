/*
 * Copyright (c) 2018 modmuss50 and Gigabit101
 *
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 *
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package reborncore.client.gui.componets;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.ButtonWidget;
import org.lwjgl.opengl.GL11;

public class GuiHiddenButton extends ButtonWidget {

	public GuiHiddenButton(int xPosition, int yPosition, String displayString) {
		super(xPosition, yPosition, 0, 0, displayString, var1 -> {});
	}

	public GuiHiddenButton(int id, int xPosition, int yPosition, int width, int height, String displayString) {
		super(xPosition, yPosition, width, height, displayString, var1 -> {});
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		if (this.visible) {
			TextRenderer fontrenderer = MinecraftClient.getInstance().textRenderer;
			MinecraftClient.getInstance().getTextureManager().bindTexture(WIDGETS_LOCATION);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.isHovered = mouseX >= this.x && mouseY >= this.y
				&& mouseX < this.x + this.width && mouseY < this.y + this.height;
			GL11.glEnable(GL11.GL_BLEND);
			RenderSystem.blendFuncSeparate(770, 771, 1, 0);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			int l = 14737632;

			if (!this.active) {
				l = 10526880;
			} else if (this.isHovered) {
				l = 16777120;
			}

			this.drawCenteredString(fontrenderer, this.getMessage(), this.x + this.width / 2,
				this.y + (this.height - 8) / 2, l);
		}
	}
}
