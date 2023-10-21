package ru.qoqqi.qcraft.blockentities;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public interface ItemPedestal {

	/**
	 * Этот метод пришлось переименовать, чтобы он не пересекался с BlockEntity.getLevel().
	 * Если их названия совпадают, то почему-то при тестах на реальном майне игра вылетает.
	 * В сообщении об ошибке говорится, что такой метод не найден. Скорее всего, дело в том,
	 * что название метода BlockEntity.getLevel() изменяется маппингами.
	 */
	Level getLevel2();

	int getAge();

	float getHoverStart();

	@Nonnull
	ItemStack getItemStack();

	boolean hasItem();
}
