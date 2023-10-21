package ru.qoqqi.qcraft.particles;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.ParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import ru.qoqqi.qcraft.entities.JellyBlob;

@OnlyIn(Dist.CLIENT)
public class JellyBlobPieceParticle extends TextureSheetParticle {

	private static final Logger LOGGER = LogUtils.getLogger();

	public static Codec<JellyBlobPieceParticleOption> codec(ParticleType<JellyBlobPieceParticleOption> type) {
		return Codec.STRING.xmap(
				blobTypeName -> new JellyBlobPieceParticleOption(type, blobTypeName),
				JellyBlobPieceParticleOption::getBlobTypeName
		);
	}

	public JellyBlobPieceParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, JellyBlob.JellyBlobType blobType) {
		super(level, x, y, z, xSpeed, ySpeed, zSpeed);
		this.gravity = 0.5F;

		this.rCol = blobType.getRed(null);
		this.gCol = blobType.getGreen(null);
		this.bCol = blobType.getBlue(null);
	}

	@NotNull
	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_LIT;
	}

	@OnlyIn(Dist.CLIENT)
	public static class Provider implements ParticleProvider<JellyBlobPieceParticleOption> {

		private final SpriteSet sprite;

		public Provider(SpriteSet pSprites) {
			this.sprite = pSprites;
		}

		public Particle createParticle(JellyBlobPieceParticleOption options, @NotNull ClientLevel level,
		                               double x, double y, double z,
		                               double xSpeed, double ySpeed, double zSpeed) {

			var blobTypeName = options.getBlobTypeName();
			var blobType = JellyBlob.JellyBlobType.get(blobTypeName);
			var particle = new JellyBlobPieceParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, blobType);

			particle.pickSprite(this.sprite);
			particle.scale(3f + level.random.nextFloat() * 2);

			return particle;
		}
	}
}
