package ru.qoqqi.qcraft.screens;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;

import ru.qoqqi.qcraft.QCraft;
import ru.qoqqi.qcraft.blocks.PuzzleBoxBlock;
import ru.qoqqi.qcraft.containers.PuzzleBoxMenu;

public class PuzzleBoxScreen extends AbstractContainerScreen<PuzzleBoxMenu> {

	private static final int SOLVE_BUTTON_X = 141;

	private static final int SOLVE_BUTTON_Y = 157;

	private static final ResourceLocation PUZZLE_BOX_TEXTURE = new ResourceLocation(QCraft.MOD_ID, "textures/gui/container/puzzle_box.png");

	private static final ResourceLocation BUTTON_SPRITE = new ResourceLocation(QCraft.MOD_ID, "container/puzzle_box/button");

	private static final ResourceLocation BUTTON_HIGHLIGHTED_SPRITE = new ResourceLocation(QCraft.MOD_ID, "container/puzzle_box/button_highlighted");

	private static final WidgetSprites BUTTON_SPRITES = new WidgetSprites(BUTTON_SPRITE, BUTTON_HIGHLIGHTED_SPRITE);

	private static final Component craftingTitle = Component.translatable("screen.puzzleBox.crafting");

	private static final Component ingredientsTitle = Component.translatable("screen.puzzleBox.ingredients");

	private static final Component solutionTitle = Component.translatable("screen.puzzleBox.solution");

	public static final int CRAFTING_TITLE_X = 33;

	public static final int CRAFTING_TITLE_Y = 5;

	public static final int INGREDIENTS_TITLE_X = 11;

	public static final int INGREDIENTS_TITLE_Y = 72;

	public static final int SOLUTION_TITLE_X = 16;

	public static final int SOLUTION_TITLE_Y = 143;

	public PuzzleBoxScreen(PuzzleBoxMenu screenContainer, Inventory inv, Component titleIn) {
		super(screenContainer, inv, titleIn);
		imageWidth = 176;
		imageHeight = 192;
	}

	@Override
	protected void init() {
		super.init();
		addSolveButton();
	}

	private void addSolveButton() {
		int x = leftPos + SOLVE_BUTTON_X;
		int y = topPos + SOLVE_BUTTON_Y;
		int width = 24;
		int height = 20;

		addRenderableWidget(new ImageButton(x, y, width, height, BUTTON_SPRITES, (button) -> {
			if (minecraft == null || minecraft.player == null) {
				return;
			}

			LocalPlayer player = this.minecraft.player;

			InteractionHand hand = InteractionHand.MAIN_HAND;
			BlockPos pos = PuzzleBoxBlock.getLastActivatedPos();
			Vec3 vector = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
			Vec3 diff = vector.subtract(player.getPosition(0));
			Direction direction = Direction.getNearest(diff.x, diff.y, diff.z);
			BlockHitResult rayTrace = new BlockHitResult(vector, direction, pos, false);

			player.connection.send(new ServerboundUseItemOnPacket(hand, rayTrace, 0));
		}));
	}

	@Override
	public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		this.renderTooltip(guiGraphics, mouseX, mouseY);
	}

	@Override
	protected void renderBg(@Nonnull GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		int x = (this.width - this.imageWidth) / 2;
		int y = (this.height - this.imageHeight) / 2;

		guiGraphics.blit(PUZZLE_BOX_TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

		for (int i = 0; i < menu.getSolutionSize(); i++) {
			int borderWidth = 5;
			int slotX = x - borderWidth + menu.solutionInventoryX + i * menu.solutionInventorySpacing;
			int slotY = y - borderWidth + menu.solutionInventoryY;

			guiGraphics.blit(PUZZLE_BOX_TEXTURE, slotX, slotY, 26, 194, 26, 26);
		}
	}

	@Override
	protected void renderLabels(@Nonnull GuiGraphics guiGraphics, int x, int y) {
		int textColor = 4210752;

		guiGraphics.drawString(font, craftingTitle, CRAFTING_TITLE_X, CRAFTING_TITLE_Y, textColor, false);
		guiGraphics.drawString(font, ingredientsTitle, INGREDIENTS_TITLE_X, INGREDIENTS_TITLE_Y, textColor, false);
		guiGraphics.drawString(font, solutionTitle, SOLUTION_TITLE_X, SOLUTION_TITLE_Y, textColor, false);
	}
}
