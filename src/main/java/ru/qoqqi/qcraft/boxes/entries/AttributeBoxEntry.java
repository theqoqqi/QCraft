package ru.qoqqi.qcraft.boxes.entries;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
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
			new AttributeBoxEntry("generic.max_health", OperatorType.INCREASE_CONSTANT, 2),
			new AttributeBoxEntry("generic.knockback_resistance", OperatorType.INCREASE_CONSTANT, 0.1),
			new AttributeBoxEntry("generic.movement_speed", OperatorType.INCREASE_FACTOR, 0.1),
			new AttributeBoxEntry("generic.attack_damage", OperatorType.INCREASE_CONSTANT, 1),
			new AttributeBoxEntry("generic.attack_knockback", OperatorType.INCREASE_CONSTANT, 1),
			new AttributeBoxEntry("generic.attack_speed", OperatorType.INCREASE_FACTOR, 0.2),
			new AttributeBoxEntry("generic.armor", OperatorType.INCREASE_CONSTANT, 2),
			new AttributeBoxEntry("generic.armor_toughness", OperatorType.INCREASE_CONSTANT, 1),
			new AttributeBoxEntry("generic.luck", OperatorType.INCREASE_CONSTANT, 1)
	);
	
	public static final List<AttributeBoxEntry> DECREASE_ATTRIBUTE_ENTRIES = Arrays.asList(
			new AttributeBoxEntry("generic.max_health", OperatorType.DECREASE_CONSTANT, 2),
			new AttributeBoxEntry("generic.knockback_resistance", OperatorType.DECREASE_CONSTANT, 0.1),
			new AttributeBoxEntry("generic.movement_speed", OperatorType.DECREASE_FACTOR, 0.1),
			new AttributeBoxEntry("generic.attack_damage", OperatorType.DECREASE_CONSTANT, 1),
			new AttributeBoxEntry("generic.attack_knockback", OperatorType.DECREASE_CONSTANT, 1),
			new AttributeBoxEntry("generic.attack_speed", OperatorType.DECREASE_FACTOR, 0.2),
			new AttributeBoxEntry("generic.armor", OperatorType.DECREASE_CONSTANT, 2),
			new AttributeBoxEntry("generic.armor_toughness", OperatorType.DECREASE_CONSTANT, 1),
			new AttributeBoxEntry("generic.luck", OperatorType.DECREASE_CONSTANT, 1)
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
	public UnpackResult unpack(Level level, Player player, MinecraftServer server, BlockPos blockPos, ItemStack lootBox) {
		
		if (attribute == null) {
			return UnpackResult.resultFail(lootBox, player);
		}
		
		AttributeInstance attributeInstance = player.getAttribute(attribute);
		
		if (attributeInstance == null) {
			return UnpackResult.resultFail(lootBox, player);
		}
		
		double oldValue = attributeInstance.getBaseValue();
		double newValue = attribute.sanitizeValue(operator.applyAsDouble(oldValue));
		
		if (oldValue == newValue) {
			return UnpackResult.resultFail(lootBox, player);
		}
		
		attributeInstance.setBaseValue(newValue);
		
		return UnpackResult.resultSuccess(lootBox, player)
				.withChatMessage(getChatMessage(player, lootBox));
	}
	
	protected Component getChatMessage(Player player, ItemStack lootBox) {
		return Component.translatable(
				operatorType.getLocalizationKey(),
				player.getDisplayName(),
				lootBox.getDisplayName(),
				Component.translatable(attribute.getDescriptionId()),
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
