package tennox.customselectionbox;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.GameSettings;

public class CSBSettingsGUI extends GuiScreen {
	GuiScreen parent;

	public CSBSettingsGUI(GuiScreen p) {
		this.parent = p;
	}

	@SuppressWarnings("unchecked")
	public void initGui() {
		this.buttonList.clear();
		
		// left
		this.buttonList.add(new CSBSlider(1, 4, this.height / 2 + -62, CSB.getRed()));
		this.buttonList.add(new CSBSlider(2, 4, this.height / 2 + -38, CSB.getGreen()));
		this.buttonList.add(new CSBSlider(3, 4, this.height / 2 + -14, CSB.getBlue()));
		this.buttonList.add(new CSBSlider(4, 4, this.height / 2 + 10, CSB.getAlpha()));
		this.buttonList.add(new CSBSlider(5, 4, this.height / 2 + 34, CSB.getThickness() / 7.0F));

		// right
		this.buttonList.add(new CSBButton(9, this.width - 154, this.height / 2 - 52, 150, 20, "Disable Depth: " + (CSB.disableDepthBuffer ? "ON" : "OFF")));
		this.buttonList.add(new CSBButton(10, this.width - 154, this.height / 2 - 30, 150, 20, "Break Animation: " + getBreakAnimationName()));
		this.buttonList.add(new CSBSlider(7, this.width - 154, this.height / 2 + 2, CSB.getBlinkAlpha()));
		this.buttonList.add(new CSBSlider(8, this.width - 154, this.height / 2 + 26, CSB.getBlinkSpeed()));

		this.buttonList.add(new CSBButton(20, this.width / 2 - 100, this.height - 48, "Done"));
		this.buttonList.add(new CSBButton(21, this.width / 2 - 100, this.height - 24, 95, 20, "CSB defaults"));
		this.buttonList.add(new CSBButton(22, this.width / 2 + 5, this.height - 24, 95, 20, "MC defaults"));
	}

	private String getBreakAnimationName() {
		if (CSB.breakAnimation == 0)
			return "NONE";
		if (CSB.breakAnimation == 1)
			return "SHRINK";
		if (CSB.breakAnimation == 2)
			return "DOWN";
		if (CSB.breakAnimation == 3)
			return "ALPHA";
		return "";
	}

	public void actionPerformed(GuiButton button) {
		if (button.id == 20) {
			CSB.save();
			this.mc.displayGuiScreen((GuiScreen) null);
		} else if (button.id == 21) {
			CSB.reset(false);
			initGui();
		} else if (button.id == 22) {
			CSB.reset(true);
			initGui();
		} else if (button.id == 9) {
			CSB.disableDepthBuffer = !CSB.disableDepthBuffer;
			button.displayString = "Disable Depth: " + (CSB.disableDepthBuffer ? "ON" : "OFF");
		} else if (button.id == 10) {
			CSB.setBreakAnimation(CSB.breakAnimation == CSB.LASTANIMATION ? 0 : CSB.breakAnimation + 1);

			button.displayString = "Break Animation: " + getBreakAnimationName();
		}
	}

	protected void keyTyped(char par1, int par2) {
		if (par2 == 1) {
			CSB.save();
			this.mc.displayGuiScreen((GuiScreen) null);
		}
	}

	public void drawScreen(int par1, int par2, float par3) {
		drawGradientRect(0, 0, this.width, 48 - 4, -1072689136, -804253680); // top
		drawGradientRect(0, this.height / 2 - 67, 158, this.height / 2 + 59, -1072689136, -804253680); // left
		drawGradientRect(this.width - 158, this.height / 2 - 56, this.width, this.height / 2 + 50, -1072689136, -804253680); // right
		drawGradientRect(0, this.height - 48 - 4, this.width, this.height, -1072689136, -804253680); // bottom

		// drawString(this.fontRendererObj, "Blink:", this.width - 153, this.height / 2 + 0, 16777215);
		drawCenteredString(this.fontRendererObj, "Custom Selection Box", this.width / 2, (this.height - (this.height + 4 - 48)) / 2 - 4, 16777215);

		super.drawScreen(par1, par2, par3);
	}
}