package net.povstalec.stellarview.client.util;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix4f;
import org.lwjgl.opengl.*;

public class CelestialInstancedBuffer implements AutoCloseable
{
	public static final float[] STAR_VERTICES = new float[]
			{
					-1F, 1F, 0F,	0.0F, 1.0F,
					1F, -1F, 0F,	1.0F, 0.0F,
					-1F, -1F, 0F,	0.0F, 0.0F,
					
					1F, -1F, 0F,	1.0F, 0.0F,
					-1F, 1F, 0F,	0.0F, 1.0F,
					1F, 1F, 0F,		1.0F, 1.0F,
			};
	
	private int instanceBufferId;
	private int vertexBufferId;
	private int indexBufferId;
	private int arrayObjectId;
	
	public CelestialInstancedBuffer()
	{
		RenderSystem.assertOnRenderThread();
		this.instanceBufferId = GlStateManager._glGenBuffers();
		this.vertexBufferId = GlStateManager._glGenBuffers();
		this.indexBufferId = GlStateManager._glGenBuffers();
		this.arrayObjectId = GlStateManager._glGenVertexArrays();
	}
	
	//TODO Add an index buffer
	/*public static ByteBuffer indexBuffer()
	{
		ByteBuffer instanceBuffer = MemoryTracker.create(6);
		
		instanceBuffer.put((byte) 0);
		instanceBuffer.put((byte) 1);
		instanceBuffer.put((byte) 2);
		instanceBuffer.put((byte) 1);
		instanceBuffer.put((byte) 0);
		instanceBuffer.put((byte) 3);
		
		return instanceBuffer;
	}*/
	
	public void upload(float[] instances)
	{
		// Instance Buffer setup
		GL20C.glBindBuffer(GL15C.GL_ARRAY_BUFFER, this.instanceBufferId);
		GL20C.glBufferData(GL15C.GL_ARRAY_BUFFER, instances, GL15C.GL_STATIC_DRAW);
		GL20C.glBindBuffer(GL15C.GL_ARRAY_BUFFER, 0);
		
		// Vertex Buffer and Array Object setup
		GL30C.glBindVertexArray(this.arrayObjectId);
		GL30C.glBindBuffer(GL15C.GL_ARRAY_BUFFER, this.vertexBufferId);
		GL15.glBufferData(GL15C.GL_ARRAY_BUFFER, STAR_VERTICES, GL15C.GL_STATIC_DRAW);
		// Position
		GL20C.glEnableVertexAttribArray(0);
		GL20C.glVertexAttribPointer(0, 3, GL20C.GL_FLOAT, false, 5 * Float.BYTES, 0);
		// UV
		GL20C.glEnableVertexAttribArray(1);
		GL20C.glVertexAttribPointer(1, 2, GL20C.GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);
		
		//TODO Index Buffer setup
		//GlStateManager._glBindBuffer(GL15C.GL_ELEMENT_ARRAY_BUFFER, this.indexBufferId);
		//RenderSystem.glBufferData(GL15C.GL_ELEMENT_ARRAY_BUFFER, indexBuffer(), GL15C.GL_STATIC_DRAW);
		
		// Set instance data
		// Position Offset
		GL20C.glEnableVertexAttribArray(2);
		GlStateManager._glBindBuffer(GL15C.GL_ARRAY_BUFFER, this.instanceBufferId); // This attribute comes from the instance buffer, rather than the vertex buffer
		GL20C.glVertexAttribPointer(2, 2, GL20C.GL_FLOAT, false, 3 * Float.BYTES, 0);
		GlStateManager._glBindBuffer(GL15C.GL_ARRAY_BUFFER, 0);
		GL43C.glVertexBindingDivisor(2, 1); // Tells OpenGL this is an instanced vertex attribute
	}
	
	public void bind()
	{
		BufferUploader.invalidate();
		GlStateManager._glBindVertexArray(this.arrayObjectId);
	}
	
	public static void unbind()
	{
		BufferUploader.invalidate();
		GlStateManager._glBindVertexArray(0);
	}
	
	public void draw()
	{
		//GL31C.glDrawElementsInstanced(GL20C.GL_TRIANGLES, 6, GL20C.GL_UNSIGNED_BYTE, 0L, 4);
		GL31C.glDrawArraysInstanced(GL20C.GL_TRIANGLES, 0, 6, 4);
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
		
		RenderSystem.setupShaderLights(shaderInstance);
		shaderInstance.apply();
		this.draw();
		shaderInstance.clear();
	}
	
	@Override
	public void close()
	{
		if(this.instanceBufferId >= 0)
		{
			RenderSystem.glDeleteBuffers(this.instanceBufferId);
			this.instanceBufferId = -1;
		}
		
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
	
	public boolean isInvalid()
	{
		return this.arrayObjectId == -1;
	}
}
