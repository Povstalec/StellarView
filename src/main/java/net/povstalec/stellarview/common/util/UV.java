package net.povstalec.stellarview.common.util;

public class UV
{
	private final float u;
	private final float v;
	
	public UV(float u, float v)
	{
		this.u = u;
		this.v = v;
	}
	
	public float u()
	{
		return u;
	}
	
	public float v()
	{
		return v;
	}
	
	
	
	public static class Quad
	{
		//TODO Check if you're flipping them correctly, maybe Y should be flipped and not X, I don't remember
		private final UV topLeft;
		private final UV bottomLeft;
		private final UV bottomRight;
		private final UV topRight;
		
		public Quad(UV topLeft, UV bottomLeft, UV bottomRight, UV topRight, boolean flipped)
		{
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
		}
		
		public Quad(UV topLeft, UV bottomLeft, UV bottomRight, UV topRight)
		{
			this(topLeft, bottomLeft, bottomRight, topRight, false);
		}
		
		public Quad(float topLeftX, float topLeftY, float bottomRightX, float bottomRightY, boolean flipped)
		{
			if(flipped)
			{
				this.topLeft = new UV(bottomRightX, topLeftY);
				this.bottomLeft = new UV(bottomRightX, bottomRightY);
				this.bottomRight = new UV(topLeftX, bottomRightY);
				this.topRight = new UV(topLeftX, topLeftY);
			}
			else
			{
				this.topLeft = new UV(topLeftX, topLeftY);
				this.bottomLeft = new UV(topLeftX, bottomRightY);
				this.bottomRight = new UV(bottomRightX, bottomRightY);
				this.topRight = new UV(bottomRightX, topLeftY);
			}
		}
		
		public Quad(float topLeftX, float topLeftY, float bottomRightX, float bottomRightY)
		{
			this(bottomRightY, bottomRightY, bottomRightY, bottomRightY, false);
		}
		
		// Full quad
		public Quad(boolean flipped)
		{
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
	}
}
