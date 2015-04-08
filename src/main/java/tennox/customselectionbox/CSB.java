package tennox.customselectionbox;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map.Entry;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAnvil;
import net.minecraft.block.BlockButton;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.BlockLever;
import net.minecraft.block.BlockRedstoneDiode;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.BlockTripWireHook;
import net.minecraft.block.BlockVine;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

@Mod(modid = CSB.MODID, version = CSB.VERSION)
public class CSB {
	public static final String MODID = "TeNNoX_CustomSelectionBox";
	public static final String VERSION = "1.4";

	@SidedProxy(clientSide = "tennox.customselectionbox.CSBClientProxy", serverSide = "tennox.customselectionbox.CSBCommonProxy")
	public static CSBCommonProxy proxy;
	private static Configuration config;
	public static Logger logger;
	public static CSBFloat red = new CSBFloat("Red", 255, 0f, 0f);
	public static CSBFloat green = new CSBFloat("Green", 255, 0f, 0f);
	public static CSBFloat blue = new CSBFloat("Blue", 255, 0f, 0f);
	public static CSBFloat alpha = new CSBFloat("Alpha", 255, 0.4f, 1.0f);
	public static CSBFloat thickness = new CSBFloat("Thickness", 7, 2f, 4f).setMinMax(0.1f, 7f);
	public static CSBFloat blinkalpha = new CSBFloat("Blink Alpha", 255, 0f, 0.4f);
	public static CSBFloat blinkspeed = new CSBFloat("Blink Speed", 100, 0f, 0.2f);
	public static boolean diffButtonLoc;
	public static boolean disableDepthBuffer;
	public static int breakAnimation;

	public static int NONE = 0;
	public static int SHRINK = 1;
	public static int DOWN = 2;
	public static int ALPHA = 3;
	public static int LASTANIMATION_INDEX = 3;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		proxy.registerTickHandler();
		MinecraftForge.EVENT_BUS.register(this);

		logger = event.getModLog();
		config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();
		int r = config.get("general", "red", 0).getInt();
		int g = config.get("general", "green", 0).getInt();
		int b = config.get("general", "blue", 0).getInt();
		int a = config.get("general", "alpha", 255).getInt();
		int t = config.get("general", "thickness", 4).getInt();
		int ba = config.get("general", "blink_alpha", 100).getInt();
		int bs = config.get("general", "blink_speed", 30).getInt();
		diffButtonLoc = config.get("general", "different config button location", false).getBoolean(false);
		disableDepthBuffer = config.get("general", "disable_depth", false).getBoolean(false);
		int bra = config.get("general", "break_animation", 0).getInt();
		config.save();

		red.setFromInt(r);
		green.setFromInt(g);
		blue.setFromInt(b);
		alpha.setFromInt(a);
		thickness.setFromInt(t);
		blinkalpha.setFromInt(ba);
		blinkspeed.setFromInt(bs);
		setBreakAnimation(bra);

		logger.info("SAVED: red=" + red.get() + " green=" + green.get() + " blue=" + blue.get() + " alpha=" + alpha.get());
		logger.info("SAVED: thickness=" + thickness.get() + " diffbuttonloc=" + diffButtonLoc + " blinkalpha=" + blinkalpha.get() + " blinkspeed=" + blinkspeed.get());
	}

	public static void save() {
		config.load();
		config.get("general", "red", 0).set(red.getAsInt());
		config.get("general", "green", 0).set(green.getAsInt());
		config.get("general", "blue", 0).set(blue.getAsInt());
		config.get("general", "alpha", 255).set(alpha.getAsInt());
		config.get("general", "thickness", 4).set(thickness.getAsInt());
		config.get("general", "blink_alpha", 100).set(blinkalpha.getAsInt());
		config.get("general", "blink_speed", 30).set(blinkspeed.getAsInt());
		config.get("general", "disable_depth", false).set(disableDepthBuffer);
		config.get("general", "break_animation", 0).set(breakAnimation);
		config.save();
		logger.info("SAVED: red=" + red.get() + " green=" + green.get() + " blue=" + blue.get() + " alpha=" + alpha.get());
		logger.info("SAVED: thickness=" + thickness.get() + " blinkalpha=" + blinkalpha.get() + " blinkspeed=" + blinkspeed.get());
	}

	public static void reset(boolean mc) {
		if (mc) {
			red.mcDefault();
			green.mcDefault();
			blue.mcDefault();
			alpha.mcDefault();
			thickness.mcDefault();
			blinkalpha.mcDefault();
			blinkspeed.mcDefault();
		} else {
			red.csbDefault();
			green.csbDefault();
			blue.csbDefault();
			alpha.csbDefault();
			thickness.csbDefault();
			blinkalpha.csbDefault();
			blinkspeed.csbDefault();
		}
		disableDepthBuffer = false;
		setBreakAnimation(0);
		save();
	}

	public static void openSettingsGUI() {
		Minecraft mc = Minecraft.getMinecraft();
		mc.gameSettings.saveOptions();
		mc.displayGuiScreen(new CSBSettingsGUI(mc.currentScreen));
	}

	@SubscribeEvent
	public void onDrawBlockSelectionBox(DrawBlockHighlightEvent e) {
		drawSelectionBox(e.player, e.target, e.subID, e.currentItem, e.partialTicks);
		e.setCanceled(true);
	}

	// see RenderGlobal.drawSelectionBox
	public static void drawSelectionBox(EntityPlayer player, MovingObjectPosition mops, int subID, ItemStack par4ItemStack, float partialTicks) {
		World world = player.worldObj;

		if (subID == 0 && mops.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
			// get the blockdamage progess
			float breakProgress = getBlockDamage(player, mops);

			// maybe disable depth buffer
			if (disableDepthBuffer) {
				GL11.glDisable(GL11.GL_DEPTH_TEST);
			}

			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
			GlStateManager.color(0.0F, 0.0F, 0.0F, 0.4F);
			GL11.glLineWidth(thickness.get());
			GlStateManager.disableTexture2D();
			GlStateManager.depthMask(false);
			float f1 = 0.002F;
			BlockPos blockpos = mops.getBlockPos();
			Block block = world.getBlockState(blockpos).getBlock();

			if (block.getMaterial() != Material.air && world.getWorldBorder().contains(blockpos)) {
				block.setBlockBoundsBasedOnState(world, blockpos);
				double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) partialTicks;
				double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) partialTicks;
				double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) partialTicks;

				AxisAlignedBB bb = block.getSelectedBoundingBox(world, blockpos).expand(0.0020000000949949026D, 0.0020000000949949026D, 0.0020000000949949026D)
						.offset(-d0, -d1, -d2);

				// modify BB
				if (breakAnimation == DOWN)
					bb = bb.expand(0f, -breakProgress / 2, 0f).offset(0f, -breakProgress / 2, 0f);
				if (breakAnimation == SHRINK)
					bb = bb.expand(-breakProgress / 2, -breakProgress / 2, -breakProgress / 2);

				// draw blinking block
				drawBlinkingBlock(bb, (breakAnimation == ALPHA) ? breakProgress : blinkalpha.get());

				// set the color back to original and draw outline
				GL11.glColor4f(red.get(), green.get(), blue.get(), alpha.get());
				drawOutlinedBoundingBox(bb, -1);

				// draw the blockplace indicator //
				if (true) {
					bb = getCurrentBlockBB(world, player, blockpos.add(mops.sideHit.getDirectionVec()), mops.sideHit,
							mops.hitVec.subtract(blockpos.getX(), blockpos.getY(), blockpos.getZ())); // the hitVec in mops is world-related, we need
																										// block-related

					if (bb != null) {
						bb = bb.expand(0.0020000000949949026D, 0.0020000000949949026D, 0.0020000000949949026D).offset(-d0, -d1, -d2);
						GL11.glColor4f(red.get(), green.get(), blue.get(), alpha.get() / 3);
						drawOutlinedBoundingBox(bb, -1);
					}
				}
			}

			GlStateManager.depthMask(true);
			GlStateManager.enableTexture2D();
			GlStateManager.disableBlend();

			// renable depth
			if (disableDepthBuffer) {
				GL11.glEnable(GL11.GL_DEPTH_TEST);
			}
		}
	}

	private static AxisAlignedBB getCurrentBlockBB(World world, EntityPlayer player, BlockPos pos, EnumFacing side, Vec3 hitPos) {
		ItemStack currentItem = player.inventory.getCurrentItem();

		AxisAlignedBB bb;
		if (currentItem != null && Block.getBlockFromItem(currentItem.getItem()) != null) {
			Block inUse = Block.getBlockFromItem(currentItem.getItem());

			return getBlockSpecificBB(world, player, pos, inUse, side, hitPos);
		} else { // no block in hand
			return null;
			// return Blocks.air.getSelectedBoundingBox(world, pos);
		}
	}

	private static AxisAlignedBB getBlockSpecificBB(World world, EntityPlayer player, BlockPos pos, Block block, EnumFacing side, Vec3 hitPos) {
		if (!block.canPlaceBlockOnSide(world, pos, side)) {
			return null;
		}

		IBlockState state = null;
		try {
			state = block.onBlockPlaced(world, pos, side, (float) hitPos.xCoord, (float) hitPos.yCoord, (float) hitPos.zCoord, 0, player);
			block.setBlockBoundsBasedOnState(world, pos);
		} catch (Exception e) {
			// if (!(block instanceof BlockSnow) && !(block instanceof BlockAnvil) && !(block instanceof BlockVine) && !(block instanceof BlockButton)
			// && !(block instanceof BlockLever))
			// logger.catching(e);
		}

		if (block instanceof BlockLadder) { // Ladder
			float f = 0.125F;
			switch (((EnumFacing) state.getValue(BlockLadder.FACING))) {
			case NORTH:
				block.setBlockBounds(0.0F, 0.0F, 1.0F - f, 1.0F, 1.0F, 1.0F);
				break;
			case SOUTH:
				block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, f);
				break;
			case WEST:
				block.setBlockBounds(1.0F - f, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
				break;
			case EAST:
			default:
				block.setBlockBounds(0.0F, 0.0F, 0.0F, f, 1.0F, 1.0F);
			}
		} else if (block instanceof BlockTorch) { // Torch
			float f = 0.15F;

			if (side == EnumFacing.EAST) {
				block.setBlockBounds(0.0F, 0.2F, 0.5F - f, f * 2.0F, 0.8F, 0.5F + f);
			} else if (side == EnumFacing.WEST) {
				block.setBlockBounds(1.0F - f * 2.0F, 0.2F, 0.5F - f, 1.0F, 0.8F, 0.5F + f);
			} else if (side == EnumFacing.SOUTH) {
				block.setBlockBounds(0.5F - f, 0.2F, 0.0F, 0.5F + f, 0.8F, f * 2.0F);
			} else if (side == EnumFacing.NORTH) {
				block.setBlockBounds(0.5F - f, 0.2F, 1.0F - f * 2.0F, 0.5F + f, 0.8F, 1.0F);
			} else {
				f = 0.1F;
				block.setBlockBounds(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, 0.6F, 0.5F + f);
			}
		} else if (block instanceof BlockSlab && !((BlockSlab) block).isDouble()) { // Slabs
			if (state.getBlock() == block) {
				boolean top = state.getValue(BlockSlab.HALF) == BlockSlab.EnumBlockHalf.TOP;
				block.setBlockBounds(0.0F, top ? 0.5F : 0.0F, 0.0F, 1.0F, top ? 1.0F : 0.5F, 1.0F);
			}
		} else if (block instanceof BlockSnow) { // Snow
			block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1 / 8F, 1.0F);
		} else if (block instanceof BlockAnvil) { // Anvil
			if (player.getHorizontalFacing().rotateY().getAxis() == EnumFacing.Axis.X) {
				block.setBlockBounds(0.0F, 0.0F, 0.125F, 1.0F, 1.0F, 0.875F);
			} else {
				block.setBlockBounds(0.125F, 0.0F, 0.0F, 0.875F, 1.0F, 1.0F);
			}
		} else if (block instanceof BlockVine) { // Vine
			float f = 0.0625F;
			float f1 = 1.0F;
			float f2 = 1.0F;
			float f3 = 1.0F;
			float f4 = 0.0F;
			float f5 = 0.0F;
			float f6 = 0.0F;
			boolean flag = false;
			if ((Boolean) state.getValue(BlockVine.WEST)) {
				f4 = Math.max(f4, 0.0625F);
				f1 = 0.0F;
				f2 = 0.0F;
				f5 = 1.0F;
				f3 = 0.0F;
				f6 = 1.0F;
				flag = true;
			}
			if ((Boolean) state.getValue(BlockVine.EAST)) {
				f1 = Math.min(f1, 0.9375F);
				f4 = 1.0F;
				f2 = 0.0F;
				f5 = 1.0F;
				f3 = 0.0F;
				f6 = 1.0F;
				flag = true;
			}
			if ((Boolean) state.getValue(BlockVine.NORTH)) {
				f6 = Math.max(f6, 0.0625F);
				f3 = 0.0F;
				f1 = 0.0F;
				f4 = 1.0F;
				f2 = 0.0F;
				f5 = 1.0F;
				flag = true;
			}
			if ((Boolean) state.getValue(BlockVine.SOUTH)) {
				f3 = Math.min(f3, 0.9375F);
				f6 = 1.0F;
				f1 = 0.0F;
				f4 = 1.0F;
				f2 = 0.0F;
				f5 = 1.0F;
				flag = true;
			}
			Block blockAbove = world.getBlockState(pos.up()).getBlock();
			if (!flag && blockAbove.isFullCube() && blockAbove.getMaterial().blocksMovement()) {
				f2 = Math.min(f2, 0.9375F);
				f5 = 1.0F;
				f1 = 0.0F;
				f4 = 1.0F;
				f3 = 0.0F;
				f6 = 1.0F;
			}
			block.setBlockBounds(f1, f2, f3, f4, f5, f6);
		} else if (block instanceof BlockButton) { // Button
			EnumFacing enumfacing = (EnumFacing) state.getValue(BlockButton.FACING);
			boolean flag = ((Boolean) state.getValue(BlockButton.POWERED)).booleanValue();
			float f = 0.25F;
			float f1 = 0.375F;
			float f2 = (float) (flag ? 1 : 2) / 16.0F;
			float f3 = 0.125F;
			float f4 = 0.1875F;
			switch (enumfacing) {
			case EAST:
				block.setBlockBounds(0.0F, 0.375F, 0.3125F, f2, 0.625F, 0.6875F);
				break;
			case WEST:
				block.setBlockBounds(1.0F - f2, 0.375F, 0.3125F, 1.0F, 0.625F, 0.6875F);
				break;
			case SOUTH:
				block.setBlockBounds(0.3125F, 0.375F, 0.0F, 0.6875F, 0.625F, f2);
				break;
			case NORTH:
				block.setBlockBounds(0.3125F, 0.375F, 1.0F - f2, 0.6875F, 0.625F, 1.0F);
				break;
			case UP:
				block.setBlockBounds(0.3125F, 0.0F, 0.375F, 0.6875F, 0.0F + f2, 0.625F);
				break;
			case DOWN:
				block.setBlockBounds(0.3125F, 1.0F - f2, 0.375F, 0.6875F, 1.0F, 0.625F);
			}
		} else if (block instanceof BlockLever) { // Lever
			float f = 0.1875F;

			switch (((BlockLever.EnumOrientation) state.getValue(BlockLever.FACING))) {
			case EAST:
				block.setBlockBounds(0.0F, 0.2F, 0.5F - f, f * 2.0F, 0.8F, 0.5F + f);
				break;
			case WEST:
				block.setBlockBounds(1.0F - f * 2.0F, 0.2F, 0.5F - f, 1.0F, 0.8F, 0.5F + f);
				break;
			case SOUTH:
				block.setBlockBounds(0.5F - f, 0.2F, 0.0F, 0.5F + f, 0.8F, f * 2.0F);
				break;
			case NORTH:
				block.setBlockBounds(0.5F - f, 0.2F, 1.0F - f * 2.0F, 0.5F + f, 0.8F, 1.0F);
				break;
			case UP_Z:
			case UP_X:
				f = 0.25F;
				block.setBlockBounds(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, 0.6F, 0.5F + f);
				break;
			case DOWN_X:
			case DOWN_Z:
				f = 0.25F;
				block.setBlockBounds(0.5F - f, 0.4F, 0.5F - f, 0.5F + f, 1.0F, 0.5F + f);
			}
		} else if (block instanceof BlockTrapDoor) { // trapdoor
			((BlockTrapDoor) block).setBounds(state);
		} else if (block instanceof BlockTripWireHook) { // tripwirehook
			float f = 0.1875F;
			switch ((EnumFacing) state.getValue(BlockTripWireHook.FACING)) {
			case EAST:
				block.setBlockBounds(0.0F, 0.2F, 0.5F - f, f * 2.0F, 0.8F, 0.5F + f);
				break;
			case WEST:
				block.setBlockBounds(1.0F - f * 2.0F, 0.2F, 0.5F - f, 1.0F, 0.8F, 0.5F + f);
				break;
			case SOUTH:
				block.setBlockBounds(0.5F - f, 0.2F, 0.0F, 0.5F + f, 0.8F, f * 2.0F);
				break;
			case NORTH:
				block.setBlockBounds(0.5F - f, 0.2F, 1.0F - f * 2.0F, 0.5F + f, 0.8F, 1.0F);
			default:
			}
		}
		return block.getSelectedBoundingBox(world, pos);
	}

	private static float getBlockDamage(EntityPlayer player, MovingObjectPosition block) {
		try {
			Field f;
			try {
				f = RenderGlobal.class.getDeclaredField("damagedBlocks");
			} catch (NoSuchFieldException e) {
				f = RenderGlobal.class.getDeclaredField("field_72738_E");
			}
			f.setAccessible(true);
			HashMap<Integer, DestroyBlockProgress> map = (HashMap<Integer, DestroyBlockProgress>) f.get(Minecraft.getMinecraft().renderGlobal);
			for (Entry<Integer, DestroyBlockProgress> entry : map.entrySet()) {
				DestroyBlockProgress prg = entry.getValue();
				if (prg.getPosition().equals(block.getBlockPos())) {
					if (prg.getPartialBlockDamage() >= 0 && prg.getPartialBlockDamage() <= 10)
						return prg.getPartialBlockDamage() / 10f;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0f;
	}

	private static void drawOutlinedBoundingBox(AxisAlignedBB boundingBox, int color) {
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();
		worldrenderer.startDrawing(3);

		if (color != -1) {
			worldrenderer.setColorOpaque_I(color);
		}

		worldrenderer.addVertex(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
		worldrenderer.addVertex(boundingBox.maxX, boundingBox.minY, boundingBox.minZ);
		worldrenderer.addVertex(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
		worldrenderer.addVertex(boundingBox.minX, boundingBox.minY, boundingBox.maxZ);
		worldrenderer.addVertex(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
		tessellator.draw();
		worldrenderer.startDrawing(3);

		if (color != -1) {
			worldrenderer.setColorOpaque_I(color);
		}

		worldrenderer.addVertex(boundingBox.minX, boundingBox.maxY, boundingBox.minZ);
		worldrenderer.addVertex(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
		worldrenderer.addVertex(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
		worldrenderer.addVertex(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
		worldrenderer.addVertex(boundingBox.minX, boundingBox.maxY, boundingBox.minZ);
		tessellator.draw();
		worldrenderer.startDrawing(1);

		if (color != -1) {
			worldrenderer.setColorOpaque_I(color);
		}

		worldrenderer.addVertex(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
		worldrenderer.addVertex(boundingBox.minX, boundingBox.maxY, boundingBox.minZ);
		worldrenderer.addVertex(boundingBox.maxX, boundingBox.minY, boundingBox.minZ);
		worldrenderer.addVertex(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
		worldrenderer.addVertex(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
		worldrenderer.addVertex(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
		worldrenderer.addVertex(boundingBox.minX, boundingBox.minY, boundingBox.maxZ);
		worldrenderer.addVertex(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
		tessellator.draw();
	}

	private static void drawBlinkingBlock(AxisAlignedBB par1AxisAlignedBB, float alpha) {
		Tessellator tessellator = Tessellator.getInstance();

		if (alpha > 0.0F) {
			if (blinkspeed.get() > 0 && CSB.breakAnimation != ALPHA)
				alpha *= (float) Math.abs(Math.sin(Minecraft.getSystemTime() / 100.0D * blinkspeed.get()));

			GL11.glColor4f(red.get(), green.get(), blue.get(), alpha);
			renderDown(par1AxisAlignedBB);
			renderUp(par1AxisAlignedBB);
			renderNorth(par1AxisAlignedBB);
			renderSouth(par1AxisAlignedBB);
			renderWest(par1AxisAlignedBB);
			renderEast(par1AxisAlignedBB);
		}
	}

	public static void renderUp(AxisAlignedBB par1AxisAlignedBB) {
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();

		worldrenderer.startDrawingQuads();
		worldrenderer.addVertex(par1AxisAlignedBB.minX, par1AxisAlignedBB.minY, par1AxisAlignedBB.minZ);
		worldrenderer.addVertex(par1AxisAlignedBB.maxX, par1AxisAlignedBB.minY, par1AxisAlignedBB.minZ);
		worldrenderer.addVertex(par1AxisAlignedBB.maxX, par1AxisAlignedBB.minY, par1AxisAlignedBB.maxZ);
		worldrenderer.addVertex(par1AxisAlignedBB.minX, par1AxisAlignedBB.minY, par1AxisAlignedBB.maxZ);
		worldrenderer.addVertex(par1AxisAlignedBB.minX, par1AxisAlignedBB.minY, par1AxisAlignedBB.minZ);
		tessellator.draw();
	}

	public static void renderDown(AxisAlignedBB par1AxisAlignedBB) {
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();

		worldrenderer.startDrawingQuads();
		worldrenderer.addVertex(par1AxisAlignedBB.minX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.minZ);
		worldrenderer.addVertex(par1AxisAlignedBB.minX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.maxZ);
		worldrenderer.addVertex(par1AxisAlignedBB.maxX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.maxZ);
		worldrenderer.addVertex(par1AxisAlignedBB.maxX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.minZ);
		worldrenderer.addVertex(par1AxisAlignedBB.minX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.minZ);

		tessellator.draw();
	}

	public static void renderNorth(AxisAlignedBB par1AxisAlignedBB) {
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();

		worldrenderer.startDrawingQuads();
		worldrenderer.addVertex(par1AxisAlignedBB.minX, par1AxisAlignedBB.minY, par1AxisAlignedBB.minZ);
		worldrenderer.addVertex(par1AxisAlignedBB.minX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.minZ);
		worldrenderer.addVertex(par1AxisAlignedBB.maxX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.minZ);
		worldrenderer.addVertex(par1AxisAlignedBB.maxX, par1AxisAlignedBB.minY, par1AxisAlignedBB.minZ);
		worldrenderer.addVertex(par1AxisAlignedBB.minX, par1AxisAlignedBB.minY, par1AxisAlignedBB.minZ);
		tessellator.draw();
	}

	public static void renderSouth(AxisAlignedBB par1AxisAlignedBB) {
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();

		worldrenderer.startDrawingQuads();
		worldrenderer.addVertex(par1AxisAlignedBB.minX, par1AxisAlignedBB.minY, par1AxisAlignedBB.maxZ);
		worldrenderer.addVertex(par1AxisAlignedBB.maxX, par1AxisAlignedBB.minY, par1AxisAlignedBB.maxZ);
		worldrenderer.addVertex(par1AxisAlignedBB.maxX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.maxZ);
		worldrenderer.addVertex(par1AxisAlignedBB.minX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.maxZ);
		worldrenderer.addVertex(par1AxisAlignedBB.minX, par1AxisAlignedBB.minY, par1AxisAlignedBB.maxZ);
		tessellator.draw();
	}

	public static void renderWest(AxisAlignedBB par1AxisAlignedBB) {
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();

		worldrenderer.startDrawingQuads();
		worldrenderer.addVertex(par1AxisAlignedBB.minX, par1AxisAlignedBB.minY, par1AxisAlignedBB.minZ);
		worldrenderer.addVertex(par1AxisAlignedBB.minX, par1AxisAlignedBB.minY, par1AxisAlignedBB.maxZ);
		worldrenderer.addVertex(par1AxisAlignedBB.minX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.maxZ);
		worldrenderer.addVertex(par1AxisAlignedBB.minX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.minZ);
		worldrenderer.addVertex(par1AxisAlignedBB.minX, par1AxisAlignedBB.minY, par1AxisAlignedBB.minZ);
		tessellator.draw();
	}

	public static void renderEast(AxisAlignedBB par1AxisAlignedBB) {
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();

		worldrenderer.startDrawingQuads();
		worldrenderer.addVertex(par1AxisAlignedBB.maxX, par1AxisAlignedBB.minY, par1AxisAlignedBB.minZ);
		worldrenderer.addVertex(par1AxisAlignedBB.maxX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.minZ);
		worldrenderer.addVertex(par1AxisAlignedBB.maxX, par1AxisAlignedBB.maxY, par1AxisAlignedBB.maxZ);
		worldrenderer.addVertex(par1AxisAlignedBB.maxX, par1AxisAlignedBB.minY, par1AxisAlignedBB.maxZ);
		worldrenderer.addVertex(par1AxisAlignedBB.maxX, par1AxisAlignedBB.minY, par1AxisAlignedBB.minZ);
		tessellator.draw();
	}

	public static void setBreakAnimation(int index) {
		breakAnimation = between(index, 0, LASTANIMATION_INDEX);
	}

	private static int between(int i, int x, int y) {
		if (i < x)
			i = x;
		if (i > y)
			i = y;
		return i;
	}
}