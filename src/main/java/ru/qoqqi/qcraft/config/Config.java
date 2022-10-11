package ru.qoqqi.qcraft.config;

import net.minecraftforge.common.ForgeConfigSpec;

import org.apache.commons.lang3.tuple.Pair;

public class Config {
	
//	private static final boolean defaultEntityModificationsEnabled = true;
	
//	public final ConfigValue<Boolean> entityModificationsEnabled;
	
	public Config(ForgeConfigSpec.Builder builder) {
		builder.push("main");
		
//		this.entityModificationsEnabled = builder
//				.comment("Enables or disables entity modifications like damage, speed etc")
//				.define("entityModificationsEnabled", defaultEntityModificationsEnabled);
		
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
