package tennox.customselectionbox;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptionSlider;
import net.minecraft.client.settings.GameSettings;

import org.lwjgl.opengl.GL11;

public class CSBSlider extends GuiButton {
	private float sliderValue;
	private boolean dragging;
	private CSBFloat property;

	// GuiOptionSlider
	public CSBSlider(int i, int x, int y, CSBFloat property) {
		super(i, x, y, 150, 20, "");
		this.property = property;
		this.sliderValue = property.sliderVal();
		this.displayString = property.displayString();
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

				updateValue();
				this.displayString = property.displayString();
			}

			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			drawTexturedModalRect(this.xPosition + (int) (this.sliderValue * (this.width - 8)), this.yPosition, 0, 66, 4, 20);
			drawTexturedModalRect(this.xPosition + (int) (this.sliderValue * (this.width - 8)) + 4, this.yPosition, 196, 66, 4, 20);
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

			this.displayString = property.displayString();
			this.dragging = true;
			return true;
		}
		return false;
	}

	private void updateValue() {
		property.setFromSlider(sliderValue);
		System.out.println("Updated Value " + property + " to " + property.get() + " (in=" + sliderValue + ")");
	}

	public void mouseReleased(int p_146118_1_, int p_146118_2_) {
		this.dragging = false;
	}
}