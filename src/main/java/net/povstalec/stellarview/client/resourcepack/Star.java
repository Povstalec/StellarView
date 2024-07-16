package net.povstalec.stellarview.client.resourcepack;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceKey;
import net.povstalec.stellarview.common.config.StellarViewConfig;
import net.povstalec.stellarview.common.util.AxisRotation;
import net.povstalec.stellarview.common.util.SpaceCoords;
import net.povstalec.stellarview.common.util.TextureLayer;

public class Star extends OrbitingObject
{
	private static final short CONTRAST = 8;
	
	private static final float MIN_STAR_SIZE = 0.15F;
	private static final float MAX_STAR_SIZE = 0.25F;
	
	private static final short MIN_STAR_BRIGHTNESS = 170; // 0xAA
	private static final short MAX_STAR_BRIGHTNESS = 255; // 0xFF
	
	public static final Codec<Star> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			RESOURCE_KEY_CODEC.optionalFieldOf("parent").forGetter(OrbitingObject::getParentKey),
			SpaceCoords.CODEC.fieldOf("coords").forGetter(OrbitingObject::getCoords),
			AxisRotation.CODEC.fieldOf("axis_rotation").forGetter(OrbitingObject::getAxisRotation),
			OrbitInfo.CODEC.optionalFieldOf("orbit_info").forGetter(OrbitingObject::getOrbitInfo),
			TextureLayer.CODEC.listOf().fieldOf("texture_layers").forGetter(OrbitingObject::getTextureLayers)
			).apply(instance, Star::new));
	
	public Star(Optional<ResourceKey<SpaceObject>> parent, SpaceCoords coords, AxisRotation axisRotation, Optional<OrbitInfo> orbitInfo, List<TextureLayer> textureLayers)
	{
		super(parent, coords, axisRotation, orbitInfo, textureLayers);
	}
	
	
	
	public enum Type
	{
		O(255 - (CONTRAST * 6), 255 - (CONTRAST * 6), 255, MAX_STAR_SIZE, MAX_STAR_SIZE, MAX_STAR_BRIGHTNESS, MAX_STAR_BRIGHTNESS), // 0.00003%
		B(255 - (CONTRAST * 4), 255 - (CONTRAST * 4), 255, MIN_STAR_SIZE + 0.09F, MAX_STAR_SIZE, MIN_STAR_BRIGHTNESS + 65, MAX_STAR_BRIGHTNESS), // 0.12%
		A(255 - (CONTRAST * 2), 255 - (CONTRAST * 2), 255, MIN_STAR_SIZE + 0.05F, MIN_STAR_SIZE + 0.07F, MIN_STAR_BRIGHTNESS + 45, MIN_STAR_BRIGHTNESS + 65), // 0.61%
		F(255, 255, 255, MIN_STAR_SIZE + 0.035F, MIN_STAR_SIZE + 0.05F, MIN_STAR_BRIGHTNESS + 30, MIN_STAR_BRIGHTNESS + 45), // 3.0%
		G(255, 255, 255 - (CONTRAST * 4), MIN_STAR_SIZE + 0.02F, MIN_STAR_SIZE + 0.035F, MIN_STAR_BRIGHTNESS + 20, MIN_STAR_BRIGHTNESS + 30), // 7.6%
		K(255, 255 - (CONTRAST * 2), 255 - (CONTRAST * 4), MIN_STAR_SIZE + 0.01F, MIN_STAR_SIZE + 0.02F, MIN_STAR_BRIGHTNESS + 10, MIN_STAR_BRIGHTNESS + 20), // 12%
		M(255, 255 - (CONTRAST * 4), 255 - (CONTRAST * 4), MIN_STAR_SIZE, MIN_STAR_SIZE + 0.01F, MIN_STAR_BRIGHTNESS, MIN_STAR_BRIGHTNESS + 10); // 76%
		
		private final short red;
		private final short green;
		private final short blue;

		private final float minSize;
		private final float maxSize;

		private final short minBrightness;
		private final short maxBrightness;
		
		Type(int red, int green, int blue, float minSize, float maxSize, int minBrightness, int maxBrightness)
		{
			this.red = (short) red;
			this.green = (short) green;
			this.blue = (short) blue;

			this.minSize = minSize;
			this.maxSize = maxSize;

			this.minBrightness = (short) minBrightness;
			this.maxBrightness = (short) maxBrightness;
		}
		
		public short red()
		{
			return red;
		}
		
		public short green()
		{
			return green;
		}
		
		public short blue()
		{
			return blue;
		}
		
		public float randomSize(long seed)
		{
			if(minSize == maxSize)
				return maxSize;
			
			Random random = new Random(seed);
			
			return random.nextFloat(minSize, maxSize);
		}
		
		public short randomBrightness(long seed)
		{
			if(minBrightness == maxBrightness)
				return maxBrightness;
			
			Random random = new Random(seed);
			
			return (short) random.nextInt(minBrightness, maxBrightness);
		}
		
		public static Type randomSpectralType(long seed)
		{
			Random random = new Random(seed);
			
			if(StellarViewConfig.equal_spectral_types.get())
			{
				Type[] spectralTypes = Type.values();
				return spectralTypes[random.nextInt(0, spectralTypes.length)];
			}
			
			int value = random.nextInt(0, 100);
			
			// Slightly adjusted percentage values that can be found in SpectralType comments
			if(value < 74)
				return M;
			else if(value < (74 + 12))
				return K;
			else if(value < (74 + 12 + 7))
				return G;
			else if(value < (74 + 12 + 7 + 3))
				return F;
			else if(value < (74 + 12 + 7 + 3 + 1))
				return A;
			else if(value < (74 + 12 + 7 + 3 + 1 + 1))
				return B;
			
			return O;
		}
	}
}
