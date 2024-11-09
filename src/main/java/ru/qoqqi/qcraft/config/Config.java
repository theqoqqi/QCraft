package ru.qoqqi.qcraft.config;

import net.minecraftforge.common.ForgeConfigSpec;

import org.apache.commons.lang3.tuple.Pair;

public class Config {

	private static final boolean defaultJourneyEnabled = true;

	public final ForgeConfigSpec.ConfigValue<Boolean> journeyEnabled;

	public Config(ForgeConfigSpec.Builder builder) {
		builder.push("main");

		this.journeyEnabled = builder
				.comment("Enables or disables journeys")
				.define("journeyEnabled", defaultJourneyEnabled);

		builder.pop();
	}

	public static final Config COMMON;

	public static final ForgeConfigSpec COMMON_SPEC;

	static {
		Pair<Config, ForgeConfigSpec> commonSpecPair = new ForgeConfigSpec.Builder().configure(Config::new);

		COMMON = commonSpecPair.getLeft();
		COMMON_SPEC = commonSpecPair.getRight();
	}
}
