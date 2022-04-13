package ru.qoqqi.qcraft.boxes.entries;

import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;
import java.util.List;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleFunction;
import java.util.function.DoubleUnaryOperator;

import javax.annotation.Nonnull;

import ru.qoqqi.qcraft.boxes.entries.util.IBoxEntry;

public class AttributeBoxEntry implements IBoxEntry {
	
	public static final List<AttributeBoxEntry> INCREASE_ATTRIBUTE_ENTRIES = Arrays.asList(
			new AttributeBoxEntry("generic.max_health", AttributeBoxEntry.OperatorType.INCREASE_CONSTANT, 2),
			new AttributeBoxEntry("generic.knockback_resistance", AttributeBoxEntry.OperatorType.INCREASE_CONSTANT, 0.1),
			new AttributeBoxEntry("generic.movement_speed", AttributeBoxEntry.OperatorType.INCREASE_FACTOR, 0.1),
			new AttributeBoxEntry("generic.attack_damage", AttributeBoxEntry.OperatorType.INCREASE_CONSTANT, 1),
			new AttributeBoxEntry("generic.attack_knockback", AttributeBoxEntry.OperatorType.INCREASE_CONSTANT, 1),
			new AttributeBoxEntry("generic.attack_speed", AttributeBoxEntry.OperatorType.INCREASE_FACTOR, 0.2),
			new AttributeBoxEntry("generic.armor", AttributeBoxEntry.OperatorType.INCREASE_CONSTANT, 2),
			new AttributeBoxEntry("generic.armor_toughness", AttributeBoxEntry.OperatorType.INCREASE_CONSTANT, 1),
			new AttributeBoxEntry("generic.luck", AttributeBoxEntry.OperatorType.INCREASE_CONSTANT, 1)
	);
	
	public static final List<AttributeBoxEntry> DECREASE_ATTRIBUTE_ENTRIES = Arrays.asList(
			new AttributeBoxEntry("generic.max_health", AttributeBoxEntry.OperatorType.DECREASE_CONSTANT, 2),
			new AttributeBoxEntry("generic.knockback_resistance", AttributeBoxEntry.OperatorType.DECREASE_CONSTANT, 0.1),
			new AttributeBoxEntry("generic.movement_speed", AttributeBoxEntry.OperatorType.DECREASE_FACTOR, 0.1),
			new AttributeBoxEntry("generic.attack_damage", AttributeBoxEntry.OperatorType.DECREASE_CONSTANT, 1),
			new AttributeBoxEntry("generic.attack_knockback", AttributeBoxEntry.OperatorType.DECREASE_CONSTANT, 1),
			new AttributeBoxEntry("generic.attack_speed", AttributeBoxEntry.OperatorType.DECREASE_FACTOR, 0.2),
			new AttributeBoxEntry("generic.armor", AttributeBoxEntry.OperatorType.DECREASE_CONSTANT, 2),
			new AttributeBoxEntry("generic.armor_toughness", AttributeBoxEntry.OperatorType.DECREASE_CONSTANT, 1),
			new AttributeBoxEntry("generic.luck", AttributeBoxEntry.OperatorType.DECREASE_CONSTANT, 1)
	);
	
	private final OperatorType operatorType;
	
	private final DoubleUnaryOperator operator;
	
	private final double operand;
	
	private final Attribute attribute;
	
	public AttributeBoxEntry(String name, OperatorType operatorType, double operand) {
		this.operatorType = operatorType;
		this.operand = operand;
		this.operator = operatorType.getUnaryOperator(operand);
		this.attribute = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(name));
	}
	
	@Nonnull
	@Override
	public UnpackResult unpack(World world, PlayerEntity player, MinecraftServer server, BlockPos blockPos, ItemStack lootBox) {
		
		if (attribute == null) {
			return UnpackResult.resultFail(lootBox, player);
		}
		
		ModifiableAttributeInstance attributeInstance = player.getAttribute(attribute);
		
		if (attributeInstance == null) {
			return UnpackResult.resultFail(lootBox, player);
		}
		
		double oldValue = attributeInstance.getBaseValue();
		double newValue = attribute.clampValue(operator.applyAsDouble(oldValue));
		
		if (oldValue == newValue) {
			return UnpackResult.resultFail(lootBox, player);
		}
		
		attributeInstance.setBaseValue(newValue);
		
		return UnpackResult.resultSuccess(lootBox, player)
				.withChatMessage(getChatMessage(player, lootBox));
	}
	
	protected ITextComponent getChatMessage(PlayerEntity player, ItemStack lootBox) {
		return new TranslationTextComponent(
				operatorType.getLocalizationKey(),
				player.getDisplayName(),
				lootBox.getTextComponent(),
				new TranslationTextComponent(attribute.getAttributeName()),
				operatorType.getLocalizedOperand(operand)
		);
	}
	
	public enum OperatorType {
		INCREASE_FACTOR(
				"lootBox.attribute.increase.factor",
				(value, factor) -> value * (1f + factor),
				operand -> String.valueOf(operand * 100)
		),
		INCREASE_CONSTANT(
				"lootBox.attribute.increase.constant",
				Double::sum,
				String::valueOf
		),
		DECREASE_FACTOR(
				"lootBox.attribute.decrease.factor",
				(value, factor) -> value / (1f + factor),
				operand -> String.valueOf(operand * 100)
		),
		DECREASE_CONSTANT(
				"lootBox.attribute.decrease.constant",
				(value, constant) -> value - constant,
				String::valueOf
		);
		
		private final String localizationKey;
		
		private final DoubleBinaryOperator operator;
		
		private final DoubleFunction<String> operandLocalizer;
		
		OperatorType(String localizationKey, DoubleBinaryOperator operator, DoubleFunction<String> operandLocalizer) {
			this.localizationKey = localizationKey;
			this.operator = operator;
			this.operandLocalizer = operandLocalizer;
		}
		
		public DoubleUnaryOperator getUnaryOperator(double operand) {
			return value -> operator.applyAsDouble(value, operand);
		}
		
		public String getLocalizationKey() {
			return localizationKey;
		}
		
		public String getLocalizedOperand(double operand) {
			return operandLocalizer.apply(operand);
		}
	}
}
