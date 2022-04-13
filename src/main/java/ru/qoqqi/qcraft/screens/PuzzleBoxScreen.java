package ru.qoqqi.qcraft.screens;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.play.client.CPlayerTryUseItemOnBlockPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nonnull;

import ru.qoqqi.qcraft.QCraft;
import ru.qoqqi.qcraft.blocks.PuzzleBoxBlock;
import ru.qoqqi.qcraft.containers.PuzzleBoxContainer;

public class PuzzleBoxScreen extends ContainerScreen<PuzzleBoxContainer> {
	
	private static final int SOLVE_BUTTON_X = 141;
	
	private static final int SOLVE_BUTTON_Y = 157;
	
	private static final ResourceLocation PUZZLE_BOX_TEXTURE = new ResourceLocation(QCraft.MOD_ID, "textures/gui/container/puzzle_box.png");
	
	private static final ITextComponent craftingTitle = new TranslationTextComponent("screen.puzzleBox.crafting");
	
	private static final ITextComponent ingredientsTitle = new TranslationTextComponent("screen.puzzleBox.ingredients");
	
	private static final ITextComponent solutionTitle = new TranslationTextComponent("screen.puzzleBox.solution");
	
	public static final int CRAFTING_TITLE_X = 33;
	
	public static final int CRAFTING_TITLE_Y = 5;
	
	public static final int INGREDIENTS_TITLE_X = 11;
	
	public static final int INGREDIENTS_TITLE_Y = 72;
	
	public static final int SOLUTION_TITLE_X = 16;
	
	public static final int SOLUTION_TITLE_Y = 143;
	
	public PuzzleBoxScreen(PuzzleBoxContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
		super(screenContainer, inv, titleIn);
		passEvents = false;
		xSize = 176;
		ySize = 192;
	}
	
	@Override
	protected void init() {
		super.init();
		addSolveButton();
	}
	
	private void addSolveButton() {
		int x = guiLeft + SOLVE_BUTTON_X;
		int y = guiTop + SOLVE_BUTTON_Y;
		int width = 24;
		int height = 20;
		int textureX = 0;
		int textureY = 194;
		int hoverOffsetY = 22;
		
		addButton(new ImageButton(x, y, width, height, textureX, textureY, hoverOffsetY, PUZZLE_BOX_TEXTURE, (button) -> {
			ClientPlayerEntity player = (ClientPlayerEntity) this.playerInventory.player;
			
			Hand hand = Hand.MAIN_HAND;
			BlockPos pos = PuzzleBoxBlock.getLastActivatedPos();
			Vector3d vector = new Vector3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
			Vector3d diff = vector.subtract(player.getPositionVec());
			Direction direction = Direction.getFacingFromVector(diff.x, diff.y, diff.z);
			BlockRayTraceResult rayTrace = new BlockRayTraceResult(vector, direction, pos, false);
			
			player.connection.sendPacket(new CPlayerTryUseItemOnBlockPacket(hand, rayTrace));
		}));
	}
	
	@Override
	public void render(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(matrixStack);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.renderHoveredTooltip(matrixStack, mouseX, mouseY);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(@Nonnull MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
		//noinspection deprecation
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		TextureManager textureManager = getMinecraft().getTextureManager();
		
		int x = (this.width - this.xSize) / 2;
		int y = (this.height - this.ySize) / 2;
		
		textureManager.bindTexture(PUZZLE_BOX_TEXTURE);
		
		this.blit(matrixStack, x, y, 0, 0, this.xSize, this.ySize);
		
		for (int i = 0; i < container.getSolutionSize(); i++) {
			int borderWidth = 5;
			int slotX = x - borderWidth + container.solutionInventoryX + i * container.solutionInventorySpacing;
			int slotY = y - borderWidth + container.solutionInventoryY;
			this.blit(matrixStack, slotX, slotY, 26, 194, 26, 26);
		}
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(@Nonnull MatrixStack matrixStack, int x, int y) {
		int textColor = 4210752;
		
		this.font.drawText(matrixStack, craftingTitle, CRAFTING_TITLE_X, CRAFTING_TITLE_Y, textColor);
		this.font.drawText(matrixStack, ingredientsTitle, INGREDIENTS_TITLE_X, INGREDIENTS_TITLE_Y, textColor);
		this.font.drawText(matrixStack, solutionTitle, SOLUTION_TITLE_X, SOLUTION_TITLE_Y, textColor);
	}
}
