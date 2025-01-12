package net.povstalec.stellarview.common.util;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;

public class UV
{
	public static final String U = "u";
	public static final String V = "v";
	
	@Nullable
	private UV.PhaseHandler phaseHandler;
	
	private final float u;
	private final float v;
	
	public static final Codec<UV> CODEC = RecordCodecBuilder.create(instance -> instance.group(
    		Codec.FLOAT.fieldOf("u").forGetter(UV::u),
    		Codec.FLOAT.fieldOf("v").forGetter(UV::v)
			).apply(instance, UV::new));
	
	public UV(UV.PhaseHandler phaseHandler, float u, float v)
	{
		if(phaseHandler.doPhases())
			this.phaseHandler = phaseHandler;
		
		this.u = u;
		this.v = v;
	}
	
	public UV(float u, float v)
	{
		this(UV.PhaseHandler.DEFAULT_PHASE_HANDLER, u, v);
	}
	
	public float u()
	{
		return u;
	}
	
	public float u(long ticks)
	{
		return phaseHandler != null ? (float) (u + phaseHandler.u(ticks)) / phaseHandler.columns() : u;
	}
	
	public float v()
	{
		return v;
	}
	
	public float v(long ticks)
	{
		return phaseHandler != null ? (float) (v + phaseHandler.v(ticks)) / phaseHandler.rows() : v;
	}
	
	public static UV fromTag(CompoundTag tag, UV.PhaseHandler phaseHandler)
	{
		return new UV(phaseHandler, tag.getFloat(U), tag.getFloat(V));
	}
	
	
	
	public static class Quad
	{
		public static final String PHASE_HANDLER = "phase_handler";
		
		public static final String TOP_LEFT = "top_left";
		public static final String BOTTOM_LEFT = "bottom_left";
		public static final String BOTTOM_RIGHT = "bottom_right";
		public static final String TOP_RIGHT = "top_right";
		
		public static final String FLIP_UV = "flip_uv";
		
		public static final Quad DEFAULT_QUAD_UV = new Quad(false);
		
		private final UV.PhaseHandler phaseHandler;
		
		//TODO Check if you're flipping them correctly, maybe Y should be flipped and not X, I don't remember
		private final UV topLeft;
		private final UV bottomLeft;
		private final UV bottomRight;
		private final UV topRight;
		
		private final boolean flipped;
		
		public Quad(UV.PhaseHandler phaseHandler, UV topLeft, UV bottomLeft, UV bottomRight, UV topRight, boolean flipped)
		{
			this.phaseHandler = phaseHandler;
			
			if(flipped)
			{
				this.topLeft = topRight;
				this.bottomLeft = bottomRight;
				this.bottomRight = bottomLeft;
				this.topRight = topLeft;
			}
			else
			{
				this.topLeft = topLeft;
				this.bottomLeft = bottomLeft;
				this.bottomRight = bottomRight;
				this.topRight = topRight;
			}
			
			this.flipped = flipped;
		}
		
		public Quad(UV.PhaseHandler phaseHandler, UV topLeft, UV bottomLeft, UV bottomRight, UV topRight)
		{
			this(phaseHandler, topLeft, bottomLeft, bottomRight, topRight, false);
		}
		
		public Quad(UV.PhaseHandler phaseHandler, float topLeftX, float topLeftY, float bottomRightX, float bottomRightY, boolean flipped)
		{
			this.phaseHandler = phaseHandler;
			
			if(flipped)
			{
				this.topLeft = new UV(phaseHandler, bottomRightX, topLeftY);
				this.bottomLeft = new UV(phaseHandler, bottomRightX, bottomRightY);
				this.bottomRight = new UV(phaseHandler, topLeftX, bottomRightY);
				this.topRight = new UV(phaseHandler, topLeftX, topLeftY);
			}
			else
			{
				this.topLeft = new UV(phaseHandler, topLeftX, topLeftY);
				this.bottomLeft = new UV(phaseHandler, topLeftX, bottomRightY);
				this.bottomRight = new UV(phaseHandler, bottomRightX, bottomRightY);
				this.topRight = new UV(phaseHandler, bottomRightX, topLeftY);
			}
			
			this.flipped = flipped;
		}
		
		public Quad(UV.PhaseHandler phaseHandler, float topLeftX, float topLeftY, float bottomRightX, float bottomRightY)
		{
			this(phaseHandler, bottomRightY, bottomRightY, bottomRightY, bottomRightY, false);
		}
		
		public static final Codec<UV.Quad> CODEC = RecordCodecBuilder.create(instance -> instance.group(
	    		PhaseHandler.CODEC.optionalFieldOf("phase_handler", PhaseHandler.DEFAULT_PHASE_HANDLER).forGetter((quad) -> quad.phaseHandler),
	    		Codec.FLOAT.optionalFieldOf("x_start", 0F).forGetter((quad) -> quad.topLeft.u()),
	    		Codec.FLOAT.optionalFieldOf("y_start", 0F).forGetter((quad) -> quad.topLeft.v()),
	    		Codec.FLOAT.optionalFieldOf("x_end", 1F).forGetter((quad) -> quad.bottomRight.u()),
	    		Codec.FLOAT.optionalFieldOf("y_end", 1F).forGetter((quad) -> quad.bottomRight.v()),
	    		Codec.BOOL.optionalFieldOf("flip_uv", false).forGetter((quad) -> quad.flipped)
				).apply(instance, UV.Quad::new));
		
		// Phase dependant Quad UV
		public Quad(UV.PhaseHandler phaseHandler, boolean flipped)
		{
			this(phaseHandler, 0, 0, 1, 1, flipped);
		}
		
		// Full quad
		public Quad(boolean flipped)
		{
			this.phaseHandler = UV.PhaseHandler.DEFAULT_PHASE_HANDLER;
			
			if(flipped)
			{
				this.topLeft = new UV(1, 0);
				this.bottomLeft = new UV(1, 1);
				this.bottomRight = new UV(0, 1);
				this.topRight = new UV(0, 0);
			}
			else
			{
				this.topLeft = new UV(0, 0);
				this.bottomLeft = new UV(0, 1);
				this.bottomRight = new UV(1, 1);
				this.topRight = new UV(1, 0);
			}
			
			this.flipped = flipped;
		}
		
		public UV topLeft()
		{
			return topLeft;
		}
		
		public UV bottomLeft()
		{
			return bottomLeft;
		}
		
		public UV bottomRight()
		{
			return bottomRight;
		}
		
		public UV topRight()
		{
			return topRight;
		}
		
		public static Quad fromTag(CompoundTag tag)
		{
			PhaseHandler phaseHandler = PhaseHandler.fromTag(tag.getCompound(PHASE_HANDLER));
			
			return new Quad(phaseHandler, UV.fromTag(tag.getCompound(TOP_LEFT), phaseHandler), UV.fromTag(tag.getCompound(BOTTOM_LEFT), phaseHandler),
					UV.fromTag(tag.getCompound(BOTTOM_RIGHT), phaseHandler), UV.fromTag(tag.getCompound(TOP_RIGHT), phaseHandler));
		}
	}
	
	public static class PhaseHandler
	{
		public static final String TICKS_PER_PHASE = "ticks_per_phase";
		public static final String PHASE_TICK_OFFSET = "phase_tick_offset";
		public static final String COLUMNS = "columns";
		public static final String ROWS = "rows";
		
		private final int ticksPerPhase;
		private final int phaseTickOffset;
		private final int columns;
		private final int rows;
		
		private final int totalPhases;
		private final int tickPeriod;
		
		private final boolean doPhases;
		
		public static final PhaseHandler DEFAULT_PHASE_HANDLER = new PhaseHandler(24000, 0, 1, 1);
		
		public static final Codec<PhaseHandler> CODEC = RecordCodecBuilder.create(instance -> instance.group(
	    		Codec.intRange(1, Integer.MAX_VALUE).fieldOf("ticks_per_phase").forGetter((phaseHandler) -> phaseHandler.ticksPerPhase),
	    		Codec.INT.optionalFieldOf("phase_tick_offset", 0).forGetter((phaseHandler) -> phaseHandler.phaseTickOffset),
	    		Codec.INT.fieldOf("columns").forGetter((phaseHandler) -> phaseHandler.columns),
	    		Codec.INT.fieldOf("rows").forGetter((phaseHandler) -> phaseHandler.rows)
				).apply(instance, PhaseHandler::new));
	    
	    public PhaseHandler(int ticksPerPhase, int phaseTickOffset, int columns, int rows)
	    {
			this.ticksPerPhase = ticksPerPhase;
			this.phaseTickOffset = phaseTickOffset;
			this.columns = columns;
			this.rows = rows;
			
			this.totalPhases = columns * rows;
			this.tickPeriod = ticksPerPhase * totalPhases;
			
			this.doPhases = this.totalPhases != 1;
	    }
	    
	    public int phase(long ticks)
	    {
			return (int) ((ticks + phaseTickOffset) % tickPeriod * totalPhases) / tickPeriod;
	    }
	    
	    public int u(long ticks)
	    {
	    	return phase(ticks) % columns;
	    }
	    
	    public int v(long ticks)
	    {
	        return phase(ticks) / columns % rows;
	    }
	    
	    public int rows()
	    {
	    	return rows;
	    }
	    
	    public int columns()
	    {
	        return columns;
	    }
	    
	    public boolean doPhases()
	    {
	    	return doPhases;
	    }
		
		public static PhaseHandler fromTag(CompoundTag tag)
		{
			return new PhaseHandler(tag.getInt(TICKS_PER_PHASE), tag.getInt(PHASE_TICK_OFFSET), tag.getInt(COLUMNS), tag.getInt(ROWS));
		}
	}
}
