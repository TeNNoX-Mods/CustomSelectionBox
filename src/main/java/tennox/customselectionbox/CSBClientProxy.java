package tennox.customselectionbox;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

public class CSBClientProxy extends CSBCommonProxy {
	public void registerTickHandler() {
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onTick(TickEvent event) {
		if (event.side != Side.CLIENT || !event.phase.equals(TickEvent.Phase.START))
			return;
		if (event.type.equals(TickEvent.Type.RENDER)) {
			Minecraft mc = Minecraft.getMinecraft();
			if ((mc.currentScreen instanceof GuiOptions)) {
				GuiOptions gui = (GuiOptions) mc.currentScreen;
				List buttons = getList(gui);
				if (buttons == null) {
					CSB.logger.warn("no buttonsList?! " + gui);
					doDebug(gui);
					return;
				}
				for (Iterator i$ = buttons.iterator(); i$.hasNext();) {
					Object o = i$.next();
					if ((o instanceof CSBButton)) {
						return;
					}
				}
				if (CSB.diffButtonLoc)
					buttons.add(new CSBButton(404, gui.width - 150, 0, 150, 20, "Custom Selection Box"));
				else  // see GuiOptions.initGui
					buttons.add(new CSBButton(404, gui.width / 2 - 75, gui.height / 6 + 24 - 6, 150, 20, "Custom Selection Box"));
			}
		}
	}

	public List getList(GuiOptions gui) {
		try {
			Field field;
			try {
				field = gui.getClass().getSuperclass().getDeclaredField("buttonList");
			} catch (NoSuchFieldException e) {
				field = gui.getClass().getSuperclass().getDeclaredField("field_146292_n");
			}
			field.setAccessible(true);
			return (List) field.get(gui);
		} catch (NoSuchFieldException e) {
			System.err.println("CSB: NoSuchField");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void doDebug(GuiScreen gui) {
		try {
			Class clazz = gui.getClass().getSuperclass();
			Field[] fields = clazz.getDeclaredFields();
			for (int i = 0; i < fields.length; i++) {
				CSB.logger.warn(i + " name=" + fields[i].getName() + "\ttype=" + fields[i].getType() + "\tgenerictype=" + fields[i].getGenericType() + "\taccessible="
						+ fields[i].isAccessible());
			}
			CSB.logger.warn(Arrays.toString(fields));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}