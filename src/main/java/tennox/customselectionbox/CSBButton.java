package tennox.customselectionbox;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

public class CSBButton extends GuiButton {
	public CSBButton(int i, int x, int y, String s) {
		this(i, x, y, 200, 20, s);
	}

	public CSBButton(int i, int x, int y, int w, int h, String s) {
		super(i, x, y, w, h, s);
	}

	public boolean mousePressed(Minecraft mc, int i, int j) {
		boolean flag = super.mousePressed(mc, i, j);
		if ((flag) && (this.id == 404)) {
			CSB.openSettingsGUI();
			return false;
		}
		return flag;
	}
}