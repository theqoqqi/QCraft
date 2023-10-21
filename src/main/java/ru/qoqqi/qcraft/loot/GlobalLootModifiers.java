package ru.qoqqi.qcraft.loot;

import com.mojang.serialization.Codec;

import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import ru.qoqqi.qcraft.QCraft;

public class GlobalLootModifiers {

	public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> GLMS =
			DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, QCraft.MOD_ID);

	public static final RegistryObject<Codec<LootInjectionModifier>> LOOT_INJECTION =
			GLMS.register("loot_injection", () -> LootInjectionModifier.CODEC);

	public static void register(IEventBus eventBus) {
		GLMS.register(eventBus);
	}
}
