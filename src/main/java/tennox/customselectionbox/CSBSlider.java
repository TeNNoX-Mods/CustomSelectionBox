package tennox.customselectionbox;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptionSlider;
import net.minecraft.client.settings.GameSettings;

import org.lwjgl.opengl.GL11;

public class CSBSlider extends GuiButton {
	private float sliderValue;
	public boolean dragging;
	private static final String __OBFID = "CL_00000680";

	// GuiOptionSlider
	public CSBSlider(int i, int x, int y, float f) {
		super(i, x, y, 150, 20, "");
		this.sliderValue = f;
		this.displayString = getDisplayString(i);
	}

	@Override
	public int getHoverState(boolean flag) {
		return 0;
	}

	protected void mouseDragged(Minecraft par1Minecraft, int par2, int par3) {
		if (this.visible) {
			if (this.dragging) { // dragging
				this.sliderValue = ((float) (par2 - (this.xPosition + 4)) / (float) (this.width - 8));
				if (this.sliderValue < 0.0F) {
					this.sliderValue = 0.0F;
				}

				if (this.sliderValue > 1.0F) {
					this.sliderValue = 1.0F;
				}

				updateValue(this.id);
				this.displayString = getDisplayString(this.id);
			}

			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			drawTexturedModalRect(this.xPosition
					+ (int) (this.sliderValue * (this.width - 8)),
					this.yPosition, 0, 66, 4, 20);
			drawTexturedModalRect(this.xPosition
					+ (int) (this.sliderValue * (this.width - 8)) + 4,
					this.yPosition, 196, 66, 4, 20);
		}
	}

	public boolean mousePressed(Minecraft mc, int par2, int par3) {
		if (super.mousePressed(mc, par2, par3)) {
			this.sliderValue = ((float) (par2 - (this.xPosition + 4)) / (float) (this.width - 8));

			if (this.sliderValue < 0.0F) {
				this.sliderValue = 0.0F;
			}

			if (this.sliderValue > 1.0F) {
				this.sliderValue = 1.0F;
			}

			this.displayString = getDisplayString(this.id);
			this.dragging = true;
			return true;
		}
		return false;
	}

	private void updateValue(int id) {
		switch (id) {
		case 1:
			CSB.setRed(this.sliderValue);
			break;
		case 2:
			CSB.setGreen(this.sliderValue);
			break;
		case 3:
			CSB.setBlue(this.sliderValue);
			break;
		case 4:
			CSB.setAlpha(this.sliderValue);
			break;
		case 5:
			CSB.setThickness(this.sliderValue * 7.0F);
			break;
		case 7:
			CSB.setBlinkAlpha(this.sliderValue);
			break;
		case 8:
			CSB.setBlinkSpeed(this.sliderValue);
		}
	}

	private String getDisplayString(int id) {
		switch (id) {
		case 1:
			return "Red: " + Math.round(this.sliderValue * 255.0F);
		case 2:
			return "Green: " + Math.round(this.sliderValue * 255.0F);
		case 3:
			return "Blue: " + Math.round(this.sliderValue * 255.0F);
		case 4:
			return "Alpha: " + Math.round(this.sliderValue * 255.0F);
		case 5:
			return "Thickness: " + Math.round(this.sliderValue * 7.0F);
		case 7:
			return "Blink Alpha: " + Math.round(this.sliderValue * 255.0F);
		case 8:
			return "Blink Speed: " + Math.round(this.sliderValue * 100.0F);
		}
		return "option error?! (" + id + ")";
	}

	public void mouseReleased(int p_146118_1_, int p_146118_2_) {
		this.dragging = false;
	}
}