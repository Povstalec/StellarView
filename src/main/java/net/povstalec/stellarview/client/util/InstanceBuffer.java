package net.povstalec.stellarview.client.util;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix4f;
import org.lwjgl.opengl.*;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;

public class InstanceBuffer implements AutoCloseable
{
	public static final ByteBuffer INSTANCE_BUFFER = instanceBuffer(4);
	
	private int instanceBufferId;
	private int vertexBufferId;
	private int indexBufferId;
	private int arrayObjectId;
	@Nullable
	private VertexFormat format;
	@Nullable
	private RenderSystem.AutoStorageIndexBuffer sequentialIndices;
	private VertexFormat.IndexType indexType;
	private int indexCount;
	private VertexFormat.Mode mode;
	
	public InstanceBuffer()
	{
		RenderSystem.assertOnRenderThread();
		this.instanceBufferId = GlStateManager._glGenBuffers();
		this.vertexBufferId = GlStateManager._glGenBuffers();
		this.indexBufferId = GlStateManager._glGenBuffers();
		this.arrayObjectId = GlStateManager._glGenVertexArrays();
	}
	
	public static BufferBuilder.RenderedBuffer createStarMesh(BufferBuilder builder)
	{
		builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		
		for(int i = 0; i < 4; i++)
		{
			float x = (float) ((i & 2) - 1);
			float y = (float) ((i + 1 & 2) - 1);
			
			builder.vertex(x, y, 2); //TODO Don't forget about the 3rd coordinate
			
			/*if(hasTexture)
			{
				if(VertexOrder.texColor())
					builder.uv((x + 1) / 2F, (y + 1) / 2F).color(255, 255, 255, 255);
				else
					builder.color(255, 255, 255, 255).uv((x + 1) / 2F, (y + 1) / 2F);
			}
			else
				builder.color(255, 255, 255, 255);*/
			builder.color(255, 255, 255, 255);
			
			builder.endVertex();
		}
		
		return builder.end();
	}
	
	public static ByteBuffer instanceBuffer(int size)
	{
		ByteBuffer instanceBuffer = MemoryTracker.create(size * 3 * Float.BYTES);
		
		// Set up positions
		for(int x = 0; x < size; x++)
		{
			instanceBuffer.putFloat((float) x);
			instanceBuffer.putFloat(0);
			instanceBuffer.putFloat(0);
		}
		
		return instanceBuffer;
	}
	
	
	
	public void upload(BufferBuilder.RenderedBuffer buffer)
	{
		if (!this.isInvalid())
		{
			RenderSystem.assertOnRenderThread();
			try
			{
				BufferBuilder.DrawState bufferbuilder$drawstate = buffer.drawState();
				this.format = this.uploadVertexBuffer(bufferbuilder$drawstate, buffer.vertexBuffer());
				this.sequentialIndices = this.uploadIndexBuffer(bufferbuilder$drawstate, buffer.indexBuffer());
				this.indexCount = bufferbuilder$drawstate.indexCount();
				this.indexType = bufferbuilder$drawstate.indexType();
				this.mode = bufferbuilder$drawstate.mode();
				
				//uploadInstanceBuffer(instanceBuffer(4));
			}
			finally
			{
				buffer.release();
			}
			
		}
	}
	
	public static float[] instances()
	{
		return new float[]
				{
						0, 0, 0,
						3, 0, 0,
						6, 0, 0,
						9, 0, 0
				};
	}
	
	public static float[] createVertices()
	{
		return new float[]
				{
						-1f, 1f, 2f,	1.0f, 0.0f, 0.0f, 1.0f,
						1f, -1f, 2f,	0.0f, 1.0f, 0.0f, 1.0f,
						-1f, -1f, 2f,	0.0f, 0.0f, 1.0f, 1.0f,
						
						1f, -1f, 2f,	0.0f, 1.0f, 0.0f, 1.0f,
						-1f, 1f, 2f,	1.0f, 0.0f, 0.0f, 1.0f,
						1f, 1f, 2f,		0.0f, 1.0f, 1.0f, 1.0f
				};
	}
	
	public void upload(float[] vertices, float[] instances)
	{
		GL20C.glBindBuffer(GL15C.GL_ARRAY_BUFFER, instanceBufferId);
		GL20C.glBufferData(GL15C.GL_ARRAY_BUFFER, instances, GL15C.GL_STATIC_DRAW);
		GL20C.glBindBuffer(GL15C.GL_ARRAY_BUFFER, 0);
		
		
		GL30C.glBindVertexArray(arrayObjectId);
		GL30C.glBindBuffer(GL15C.GL_ARRAY_BUFFER, vertexBufferId);
		GL15.glBufferData(GL15C.GL_ARRAY_BUFFER, vertices, GL15C.GL_STATIC_DRAW);
		GL20C.glEnableVertexAttribArray(0);
		GL20C.glVertexAttribPointer(0, 3, GL20C.GL_FLOAT, false, 7 * Float.BYTES, 0);
		GL20C.glEnableVertexAttribArray(1);
		GL20C.glVertexAttribPointer(1, 4, GL20C.GL_FLOAT, false, 7 * Float.BYTES, 3 * Float.BYTES);
		
		// Set instance data
		GL20C.glEnableVertexAttribArray(2);
		GlStateManager._glBindBuffer(GL15C.GL_ARRAY_BUFFER, instanceBufferId); // This attribute comes from a different vertex buffer
		GL20C.glVertexAttribPointer(2, 3, GL20C.GL_FLOAT, false, 3 * Float.BYTES, 0);
		GlStateManager._glBindBuffer(GL15C.GL_ARRAY_BUFFER, 0);
		GL43C.glVertexBindingDivisor(2, 1); // Tells OpenGL this is an instanced vertex attribute (6 vertices per 1 instance)
	}
	
	private void uploadInstanceBuffer(ByteBuffer instanceBuffer)
	{
		GlStateManager._glBindBuffer(GL15C.GL_ARRAY_BUFFER, this.instanceBufferId);
		RenderSystem.glBufferData(GL15C.GL_ARRAY_BUFFER, instanceBuffer, GL15C.GL_STATIC_DRAW);
		GlStateManager._glBindBuffer(GL15C.GL_ARRAY_BUFFER, 0);
		
		GL20C.glEnableVertexAttribArray(2);
		GlStateManager._glBindBuffer(GL15C.GL_ARRAY_BUFFER, this.instanceBufferId);
		GL20C.glVertexAttribPointer(2, 3, GL20C.GL_FLOAT, false, 3 * Float.BYTES, 0);
		GlStateManager._glBindBuffer(GL15C.GL_ARRAY_BUFFER, 0);
		//GL33C.glVertexAttribDivisor(2, 1);
		//GL43C.glVertexBindingDivisor(2, 4);
		ARBVertexAttribBinding.glVertexBindingDivisor(2, 4); // Tells OpenGL this is an instanced vertex attribute
	}
	
	private VertexFormat uploadVertexBuffer(BufferBuilder.DrawState drawState, ByteBuffer vertexBuffer)
	{
		boolean formatEquals = false;
		if(!drawState.format().equals(this.format))
		{
			if(this.format != null)
				this.format.clearBufferState();
			
			GlStateManager._glBindBuffer(GL15C.GL_ARRAY_BUFFER, this.vertexBufferId);
			drawState.format().setupBufferState();
			formatEquals = true;
		}
		
		if(!drawState.indexOnly())
		{
			if(!formatEquals)
				GlStateManager._glBindBuffer(GL15C.GL_ARRAY_BUFFER, this.vertexBufferId);
			
			RenderSystem.glBufferData(GL15C.GL_ARRAY_BUFFER, vertexBuffer, GL15C.GL_STATIC_DRAW);
			
			
			/*GL20C.glEnableVertexAttribArray(0);
			GL20C.glVertexAttribPointer(0, 3, GL20C.GL_FLOAT, false, 7 * Float.BYTES, 0);
			GL20C.glEnableVertexAttribArray(1);
			GL20C.glVertexAttribPointer(1, 4, GL20C.GL_FLOAT, false, 7 * Float.BYTES, 3 * Float.BYTES);*/
		}
		
		return drawState.format();
	}
	
	@Nullable
	private RenderSystem.AutoStorageIndexBuffer uploadIndexBuffer(BufferBuilder.DrawState drawState, ByteBuffer indexBuffer)
	{
		if(!drawState.sequentialIndex())
		{
			GlStateManager._glBindBuffer(GL15C.GL_ELEMENT_ARRAY_BUFFER, this.indexBufferId);
			RenderSystem.glBufferData(GL15C.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL15C.GL_STATIC_DRAW);
			return null;
		}
		else
		{
			RenderSystem.AutoStorageIndexBuffer rendersystem$autostorageindexbuffer = RenderSystem.getSequentialBuffer(drawState.mode());
			if (rendersystem$autostorageindexbuffer != this.sequentialIndices || !rendersystem$autostorageindexbuffer.hasStorage(drawState.indexCount()))
				rendersystem$autostorageindexbuffer.bind(drawState.indexCount());
			
			return rendersystem$autostorageindexbuffer;
		}
	}
	
	public void bind()
	{
		BufferUploader.invalidate();
		GlStateManager._glBindVertexArray(this.arrayObjectId);
		//GlStateManager._glBindBuffer(GL15C.GL_ARRAY_BUFFER, this.instanceBufferId);
	}
	
	public static void unbind()
	{
		BufferUploader.invalidate();
		GlStateManager._glBindVertexArray(0);
		//GlStateManager._glBindBuffer(GL15C.GL_ARRAY_BUFFER, 0);
	}
	
	public void draw()
	{
		//GL31C.glDrawElementsInstanced(this.mode.asGLMode, this.indexCount, this.getIndexType().asGLType, 0L, 4);
		GL31C.glDrawArraysInstanced(GL20C.GL_TRIANGLES, 0, 6, 4);
	}
	
	private VertexFormat.IndexType getIndexType()
	{
		RenderSystem.AutoStorageIndexBuffer rendersystem$autostorageindexbuffer = this.sequentialIndices;
		return rendersystem$autostorageindexbuffer != null ? rendersystem$autostorageindexbuffer.type() : this.indexType;
	}
	
	public void drawWithShader(Matrix4f modelViewMatrix, Matrix4f projectionMatrix, ShaderInstance shaderInstance)
	{
		if(!RenderSystem.isOnRenderThread())
		{
			RenderSystem.recordRenderCall(() ->
			{
				this._drawWithShader(new Matrix4f(modelViewMatrix), new Matrix4f(projectionMatrix), shaderInstance);
			});
		}
		else
			this._drawWithShader(modelViewMatrix, projectionMatrix, shaderInstance);
		
	}
	
	private void _drawWithShader(Matrix4f modelViewMatrix, Matrix4f projectionMatrix, ShaderInstance shaderInstance)
	{
		for(int i = 0; i < 12; ++i)
		{
			int j = RenderSystem.getShaderTexture(i);
			shaderInstance.setSampler("Sampler" + i, j);
		}
		
		if(shaderInstance.MODEL_VIEW_MATRIX != null)
			shaderInstance.MODEL_VIEW_MATRIX.set(modelViewMatrix);
		
		if(shaderInstance.PROJECTION_MATRIX != null)
			shaderInstance.PROJECTION_MATRIX.set(projectionMatrix);
		
		if(shaderInstance.INVERSE_VIEW_ROTATION_MATRIX != null)
			shaderInstance.INVERSE_VIEW_ROTATION_MATRIX.set(RenderSystem.getInverseViewRotationMatrix());
		
		if(shaderInstance.COLOR_MODULATOR != null)
			shaderInstance.COLOR_MODULATOR.set(RenderSystem.getShaderColor());
		
		if(shaderInstance.FOG_START != null)
			shaderInstance.FOG_START.set(RenderSystem.getShaderFogStart());
		
		if(shaderInstance.FOG_END != null)
			shaderInstance.FOG_END.set(RenderSystem.getShaderFogEnd());
		
		if(shaderInstance.FOG_COLOR != null)
			shaderInstance.FOG_COLOR.set(RenderSystem.getShaderFogColor());
		
		if(shaderInstance.FOG_SHAPE != null)
			shaderInstance.FOG_SHAPE.set(RenderSystem.getShaderFogShape().getIndex());
		
		if(shaderInstance.TEXTURE_MATRIX != null)
			shaderInstance.TEXTURE_MATRIX.set(RenderSystem.getTextureMatrix());
		
		if(shaderInstance.GAME_TIME != null)
			shaderInstance.GAME_TIME.set(RenderSystem.getShaderGameTime());
		
		if(shaderInstance.SCREEN_SIZE != null)
		{
			Window window = Minecraft.getInstance().getWindow();
			shaderInstance.SCREEN_SIZE.set((float)window.getWidth(), (float)window.getHeight());
		}
		
		if(shaderInstance.LINE_WIDTH != null && (this.mode == VertexFormat.Mode.LINES || this.mode == VertexFormat.Mode.LINE_STRIP))
			shaderInstance.LINE_WIDTH.set(RenderSystem.getShaderLineWidth());
		
		RenderSystem.setupShaderLights(shaderInstance);
		shaderInstance.apply();
		this.draw();
		shaderInstance.clear();
	}
	
	@Override
	public void close()
	{
		if(this.vertexBufferId >= 0)
		{
			RenderSystem.glDeleteBuffers(this.vertexBufferId);
			this.vertexBufferId = -1;
		}
		
		if(this.indexBufferId >= 0)
		{
			RenderSystem.glDeleteBuffers(this.indexBufferId);
			this.indexBufferId = -1;
		}
		
		if(this.arrayObjectId >= 0)
		{
			RenderSystem.glDeleteVertexArrays(this.arrayObjectId);
			this.arrayObjectId = -1;
		}
	}
	
	public VertexFormat getFormat()
	{
		return this.format;
	}
	
	public boolean isInvalid()
	{
		return this.arrayObjectId == -1;
	}
	
	/*public static void setup()
	{
		instanceBufferId = GlStateManager._glGenBuffers();
		vertexBufferId = GlStateManager._glGenBuffers();
		arrayObjectId = GlStateManager._glGenVertexArrays();
		
		
		
		GL30C.glBindVertexArray(arrayObjectId);
		GL30C.glBindBuffer(GL15C.GL_ARRAY_BUFFER, vertexBufferId);
		GL15.glBufferData(GL15C.GL_ARRAY_BUFFER, quadVertices(), GL15C.GL_STATIC_DRAW);
		GL20C.glEnableVertexAttribArray(0);
		GL20C.glVertexAttribPointer(0, 2, GL20C.GL_FLOAT, false, 6 * Float.BYTES, 0);
		GL20C.glEnableVertexAttribArray(1);
		GL20C.glVertexAttribPointer(1, 3, GL20C.GL_FLOAT, false, 6 * Float.BYTES, 3);
		// Set instance data
		GL20C.glEnableVertexAttribArray(2);
		GlStateManager._glBindBuffer(GL15C.GL_ARRAY_BUFFER, instanceBufferId); // This attribute comes from a different vertex buffer
		GL20C.glVertexAttribPointer(2, 3, GL20C.GL_FLOAT, false, 3 * Float.BYTES, 0);
		GlStateManager._glBindBuffer(GL15C.GL_ARRAY_BUFFER, 0);
		//GL33C.glVertexAttribDivisor(4, 1); // Tells OpenGL this is an instanced vertex attribute (4 vertices per 1 instance)
		GL43C.glVertexBindingDivisor(4, 1);
	}
	
	public static void render()
	{
		GlStateManager._glBindVertexArray(arrayObjectId);
		GL31C.glDrawArraysInstanced(mode.asGLMode, 0, 6, 4);
		//GL31C.glDrawElementsInstanced(mode.asGLMode, this.indexCount, this.getIndexType().asGLType, 0L, 1);
		GlStateManager._glBindVertexArray(0);
	}*/
}
