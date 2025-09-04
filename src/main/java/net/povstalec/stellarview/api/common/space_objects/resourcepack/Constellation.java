package net.povstalec.stellarview.api.common.space_objects.resourcepack;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.api.common.space_objects.OrbitingObject;
import net.povstalec.stellarview.api.common.space_objects.SpaceObject;
import net.povstalec.stellarview.api.common.space_objects.TexturedObject;
import net.povstalec.stellarview.common.util.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Constellation extends SpaceObject
{
	public static final StarDefinition BETELGEUSE = new StarDefinition(Either.right(new StellarCoordinates.Equatorial(new StellarCoordinates.RightAscension(5, 55, 10.30536), new StellarCoordinates.Declination(7, 24, 25.4304), new SpaceCoords.SpaceDistance(478))),
			new Color.IntRGB(255, 0, 0), (short) 255, 0.40F, 0, StarField.LOD_DISTANCE_HIGH);
	public static final StarDefinition RIGEL = new StarDefinition(Either.right(new StellarCoordinates.Equatorial(new StellarCoordinates.RightAscension(5, 14, 32.27210), new StellarCoordinates.Declination(-8, 12, 5.8981), new SpaceCoords.SpaceDistance(848))),
			new Color.IntRGB(255, 0, 0), (short) 255, 0.40F, 0, StarField.LOD_DISTANCE_HIGH);
	public static final StarDefinition BELLATRIX = new StarDefinition(Either.right(new StellarCoordinates.Equatorial(new StellarCoordinates.RightAscension(5, 25, 7.86325), new StellarCoordinates.Declination(6, 20, 58.9318), new SpaceCoords.SpaceDistance(250))),
			new Color.IntRGB(255, 0, 0), (short) 255, 0.40F, 0, StarField.LOD_DISTANCE_HIGH);
	public static final StarDefinition MINTAKA = new StarDefinition(Either.right(new StellarCoordinates.Equatorial(new StellarCoordinates.RightAscension(5, 32, 0.40009), new StellarCoordinates.Declination(-0, 17, 56.7424), new SpaceCoords.SpaceDistance(1200))),
			new Color.IntRGB(255, 0, 0), (short) 255, 0.40F, 0, StarField.LOD_DISTANCE_HIGH);
	public static final StarDefinition ALNILAM = new StarDefinition(Either.right(new StellarCoordinates.Equatorial(new StellarCoordinates.RightAscension(5, 36, 12.8), new StellarCoordinates.Declination(-1, 12, 6.9), new SpaceCoords.SpaceDistance(1180))),
			new Color.IntRGB(255, 0, 0), (short) 255, 0.40F, 0, StarField.LOD_DISTANCE_HIGH);
	public static final StarDefinition ALNITAK = new StarDefinition(Either.right(new StellarCoordinates.Equatorial(new StellarCoordinates.RightAscension(5, 40, 45.52666), new StellarCoordinates.Declination(-1, 56, 34.2649), new SpaceCoords.SpaceDistance(1260))),
			new Color.IntRGB(255, 0, 0), (short) 255, 0.40F, 0, StarField.LOD_DISTANCE_HIGH);
	public static final StarDefinition SAIPH = new StarDefinition(Either.right(new StellarCoordinates.Equatorial(new StellarCoordinates.RightAscension(5, 47, 45.38884), new StellarCoordinates.Declination(-9, 40, 10.5777), new SpaceCoords.SpaceDistance(650))),
			new Color.IntRGB(255, 0, 0), (short) 255, 0.40F, 0, StarField.LOD_DISTANCE_HIGH);
	public static final StarDefinition MEISSA = new StarDefinition(Either.right(new StellarCoordinates.Equatorial(new StellarCoordinates.RightAscension(5, 35, 8.27608), new StellarCoordinates.Declination(9, 56, 2.9913), new SpaceCoords.SpaceDistance(1320))),
			new Color.IntRGB(255, 0, 0), (short) 255, 0.40F, 0, StarField.LOD_DISTANCE_HIGH);
	
	public static final Constellation ORION = new Constellation(Optional.of(new ResourceLocation("a")), Either.left(new SpaceCoords()), new AxisRotation(),
			List.of(BETELGEUSE,
					RIGEL,
					BELLATRIX,
					MINTAKA,
					ALNILAM,
					ALNITAK,
					SAIPH,
					MEISSA));
	
	
	
	private ArrayList<StarDefinition> lod1stars = new ArrayList<>();
	private ArrayList<StarDefinition> lod2stars = new ArrayList<>();
	private ArrayList<StarDefinition> lod3stars = new ArrayList<>();
	
	public static final Codec<Constellation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ResourceLocation.CODEC.optionalFieldOf("parent").forGetter(Constellation::getParentLocation),
			Codec.either(SpaceCoords.CODEC, StellarCoordinates.Equatorial.CODEC).fieldOf("coords").forGetter(constellation -> Either.left(constellation.getCoords())),
			AxisRotation.CODEC.fieldOf("axis_rotation").forGetter(Constellation::getAxisRotation),
			
			StarDefinition.CODEC.listOf().fieldOf("stars").forGetter(constellation -> new ArrayList<>())
	).apply(instance, Constellation::new));
	
	public Constellation(Optional<ResourceLocation> parentLocation, Either<SpaceCoords, StellarCoordinates.Equatorial> coords, AxisRotation axisRotation, List<StarDefinition> stars)
	{
		super(parentLocation, coords, axisRotation);
		
		for(StarDefinition star : stars)
		{
			switch(StarField.LevelOfDetail.fromDistance(star.maxVisibleDistance()))
			{
				case LOD1:
					this.lod1stars.add(star);
					break;
				case LOD2:
					this.lod2stars.add(star);
					break;
				default:
					this.lod3stars.add(star);
					break;
			}
		}
	}
	
	public ArrayList<StarDefinition> lod1stars()
	{
		return lod1stars;
	}
	
	public ArrayList<StarDefinition> lod2stars()
	{
		return lod2stars;
	}
	
	public ArrayList<StarDefinition> lod3stars()
	{
		return lod3stars;
	}
	
	
	
	public static class StarDefinition
	{
		public static final String RGB = "rgb";
		public static final String SIZE = "size";
		public static final String ROTATION = "rotation";
		public static final String BRIGHTNESS = "brightness";
		public static final String MAX_VISIBLE_DISTANCE = "max_visible_distance";
		
		private SpaceCoords coords;
		private Color.IntRGB rgb;
		private short brightness;
		private float size;
		private double rotation;
		private long maxVisibleDistance;
		
		public static final Codec<StarDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.either(SpaceCoords.CODEC, StellarCoordinates.Equatorial.CODEC).fieldOf(COORDS).forGetter(starDefinition -> Either.left(starDefinition.coords)),
				Color.IntRGB.CODEC.fieldOf(RGB).forGetter(starDefinition -> starDefinition.rgb),
				Codec.SHORT.fieldOf(BRIGHTNESS).forGetter(starDefinition -> starDefinition.brightness),
				Codec.FLOAT.fieldOf(SIZE).forGetter(starDefinition -> starDefinition.size),
				Codec.DOUBLE.fieldOf(ROTATION).forGetter(starDefinition -> starDefinition.rotation),
				Codec.LONG.optionalFieldOf(MAX_VISIBLE_DISTANCE, Long.MAX_VALUE).forGetter(starDefinition -> starDefinition.maxVisibleDistance)
		).apply(instance, StarDefinition::new));
		
		public StarDefinition(Either<SpaceCoords, StellarCoordinates.Equatorial> coords, Color.IntRGB rgb, short brightness, float size, double rotation, long maxVisibleDistance)
		{
			if(coords.left().isPresent())
				this.coords = coords.left().get();
			else
				this.coords = coords.right().get().toGalactic().toSpaceCoords();
			
			this.rgb = rgb;
			this.brightness = brightness;
			this.size = size;
			this.rotation = Math.toRadians(rotation);
			this.maxVisibleDistance = maxVisibleDistance;
		}
		
		public SpaceCoords coords()
		{
			return coords;
		}
		
		public Color.IntRGB rgb()
		{
			return rgb;
		}
		
		public short brightness()
		{
			return brightness;
		}
		
		public float size()
		{
			return size;
		}
		
		public double rotation()
		{
			return rotation;
		}
		
		public long maxVisibleDistance()
		{
			return maxVisibleDistance;
		}
	}
}
