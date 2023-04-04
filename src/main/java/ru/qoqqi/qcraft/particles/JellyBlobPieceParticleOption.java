package ru.qoqqi.qcraft.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;

import org.jetbrains.annotations.NotNull;

public class JellyBlobPieceParticleOption implements ParticleOptions {
	
	public static final Deserializer<JellyBlobPieceParticleOption> DESERIALIZER = new Deserializer<>() {
		
		@NotNull
		public JellyBlobPieceParticleOption fromCommand(
				@NotNull ParticleType<JellyBlobPieceParticleOption> particleType,
				StringReader reader
		) throws CommandSyntaxException {
			
			reader.expect(' ');
			return new JellyBlobPieceParticleOption(particleType, reader.readString());
		}
		
		@NotNull
		public JellyBlobPieceParticleOption fromNetwork(
				@NotNull ParticleType<JellyBlobPieceParticleOption> particleType,
				FriendlyByteBuf byteBuf
		) {
			
			return new JellyBlobPieceParticleOption(particleType, byteBuf.readUtf());
		}
	};
	
	private final ParticleType<JellyBlobPieceParticleOption> type;
	
	private final String blobTypeName;
	
	public JellyBlobPieceParticleOption(ParticleType<JellyBlobPieceParticleOption> type, String blobTypeName) {
		this.type = type;
		this.blobTypeName = blobTypeName;
	}
	
	public void writeToNetwork(FriendlyByteBuf p_123640_) {
		p_123640_.writeUtf(blobTypeName);
	}
	
	@NotNull
	public String writeToString() {
		return BuiltInRegistries.PARTICLE_TYPE.getKey(type) + " " + blobTypeName;
	}
	
	@NotNull
	public ParticleType<JellyBlobPieceParticleOption> getType() {
		return type;
	}
	
	public String getBlobTypeName() {
		return blobTypeName;
	}
}
