package ru.qoqqi.qcraft.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class LootBoxGeneratorParticle extends TextureSheetParticle {
	
	protected LootBoxGeneratorParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
		super(level, x, y, z);
		this.xd = xSpeed;
		this.yd = ySpeed;
		this.zd = zSpeed;
		this.x = x;
		this.y = y;
		this.z = z;
		
		float ringRadius = getRingRadius();
		float f = (ringRadius - 0.2f) * 2.5f;
		this.rCol = 0.5f + 0.5F * f;
		this.gCol = 0.3f * f;
		this.bCol = 1f;
		this.quadSize = (0.04f + this.random.nextFloat() * 0.01f) * (ringRadius);
		this.lifetime = (int) (((Math.random() * 30.0) + 120) * ringRadius);
	}
	
	private float getRingRadius() {
		double dx = x - 0.5 - Mth.floor(x);
		double dz = z - 0.5 - Mth.floor(z);
		
		return (float) Math.sqrt(dx * dx + dz * dz);
	}
	
	@NotNull
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
	}
	
	public void move(double pX, double pY, double pZ) {
		this.setBoundingBox(this.getBoundingBox().move(pX, pY, pZ));
		this.setLocationFromBoundingbox();
	}
	
	public float getQuadSize(float pScaleFactor) {
		float f = ((float) this.age + pScaleFactor) / (float) this.lifetime;
		f = 1.0f - f;
		f *= f;
		f = 1.0f - f;
		return this.quadSize * f;
	}
	
	public void tick() {
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
		
		if (this.age++ >= this.lifetime) {
			this.remove();
			
		} else {
			this.x += this.xd;
			this.y += this.yd;
			this.z += this.zd;
			
			this.setPos(this.x, this.y, this.z);
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class Provider implements ParticleProvider<SimpleParticleType> {
		
		private final SpriteSet sprite;
		
		public Provider(SpriteSet pSprites) {
			this.sprite = pSprites;
		}
		
		public Particle createParticle(@NotNull SimpleParticleType type, @NotNull ClientLevel level,
		                               double x, double y, double z,
		                               double xSpeed, double ySpeed, double zSpeed) {
			
			LootBoxGeneratorParticle particle =
					new LootBoxGeneratorParticle(level, x, y, z, xSpeed, ySpeed, zSpeed);
			
			particle.pickSprite(this.sprite);
			
			return particle;
		}
	}
}
