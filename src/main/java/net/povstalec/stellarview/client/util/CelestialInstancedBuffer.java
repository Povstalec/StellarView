package net.povstalec.stellarview.client.util;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import net.povstalec.stellarview.client.render.SpaceRenderer;
import net.povstalec.stellarview.client.render.shader.CelestialShaderInstance;
import net.povstalec.stellarview.common.util.SpaceCoords;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public class CelestialInstancedBuffer implements AutoCloseable
{
	private static final MemoryUtil.MemoryAllocator ALLOCATOR = MemoryUtil.getAllocator(false);
	private static final long INSTANCE_ADDRESS = ALLOCATOR.malloc(6);
	
	public static final boolean INSTANCING_PREREQUISITES = GL.getCapabilities().GL_ARB_vertex_attrib_binding && GL.getCapabilities().GL_ARB_explicit_uniform_location;
	
	public static final int POS_SIZE = 3;
	public static final int COLOR_SIZE = 4;
	public static final int ROTATION_SIZE = 1;
	public static final int SIZE_SIZE = 1;
	public static final int MAX_DISTANCE_SIZE = 1;
	public static final int INSTANCE_SIZE = POS_SIZE + COLOR_SIZE + ROTATION_SIZE + SIZE_SIZE + MAX_DISTANCE_SIZE;
	
	public static final float[] STAR_VERTICES = new float[]
			{
					-1F, -1F, 0F,
					-1F, 1F, 0F,
					1F, 1F, 0F,
					1F, -1F, 0F,
			};
	
	public static final float[] STAR_TEX_VERTICES = new float[]
			{
					-1F, -1F, 0F,	0.0F, 0.0F,
					-1F, 1F, 0F,	0.0F, 1.0F,
					1F, 1F, 0F,		1.0F, 1.0F,
					1F, -1F, 0F,	1.0F, 0.0F,
			};
	
	public static final ByteBuffer INDEX_BUFFER = indexBuffer(INSTANCE_ADDRESS);
	
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
	
	public static ByteBuffer indexBuffer(long address)
	{
		ByteBuffer instanceBuffer = MemoryUtil.memByteBuffer(address, 6);
		
		// For some reason using just "put((byte) 0)" leads to an OUT_OF_MEMORY_ERROR, but put(0, (byte) 0) works just fine
		instanceBuffer.put(0, (byte) 0);
		instanceBuffer.put(1, (byte) 1);
		instanceBuffer.put(2, (byte) 2);
		instanceBuffer.put(3, (byte) 2);
		instanceBuffer.put(4, (byte) 3);
		instanceBuffer.put(5, (byte) 0);
		
		return instanceBuffer;
	}
	
	private static void attribute(int index, int size, int stride, int offset)
	{
		GL20C.glEnableVertexAttribArray(index);
		GL20C.glVertexAttribPointer(index, size, GL20C.GL_FLOAT, false, stride, offset);
		GL43C.glVertexBindingDivisor(index, 1); // Tells OpenGL this is an instanced vertex attribute
	}
	
	public void upload(float[] instances, boolean hasTexture)
	{
		// Instance Buffer setup
		GL15C.glBindBuffer(GL15C.GL_ARRAY_BUFFER, this.instanceBufferId);
		GL15C.glBufferData(GL15C.GL_ARRAY_BUFFER, instances, GL15C.GL_STATIC_DRAW);
		GL15C.glBindBuffer(GL15C.GL_ARRAY_BUFFER, 0);
		
		// Vertex Buffer and Array Object setup
		GL30C.glBindVertexArray(this.arrayObjectId);
		GL15C.glBindBuffer(GL15C.GL_ARRAY_BUFFER, this.vertexBufferId);
		GL15.glBufferData(GL15C.GL_ARRAY_BUFFER, hasTexture ? STAR_TEX_VERTICES : STAR_VERTICES, GL15C.GL_STATIC_DRAW);
		
		GL15.glBindBuffer(GL15C.GL_ELEMENT_ARRAY_BUFFER, this.indexBufferId);
		GL15.glBufferData(GL15C.GL_ELEMENT_ARRAY_BUFFER, INDEX_BUFFER, GL15C.GL_STATIC_DRAW);
		
		// Position
		GL20C.glEnableVertexAttribArray(0);
		GL20C.glVertexAttribPointer(0, 3, GL20C.GL_FLOAT, false, hasTexture ? 5 * Float.BYTES : 3 * Float.BYTES, 0);
		
		if(hasTexture)
		{
			// UV
			GL20C.glEnableVertexAttribArray(1);
			GL20C.glVertexAttribPointer(1, 2, GL20C.GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);
		}
		
		int attributeStart = hasTexture ? 2 : 1;
		// Set instance data
		GL15.glBindBuffer(GL15C.GL_ARRAY_BUFFER, this.instanceBufferId); // This attribute comes from the instance buffer, rather than the vertex buffer
		// StarPos (Position Offset)
		attribute(attributeStart, POS_SIZE, INSTANCE_SIZE * Float.BYTES, 0);
		// Color
		attribute(attributeStart + 1, COLOR_SIZE, INSTANCE_SIZE * Float.BYTES, 3 * Float.BYTES);
		// Rotation
		attribute(attributeStart + 2, ROTATION_SIZE, INSTANCE_SIZE * Float.BYTES, 7 * Float.BYTES);
		// Size
		attribute(attributeStart + 3, SIZE_SIZE, INSTANCE_SIZE * Float.BYTES, 8 * Float.BYTES);
		// Max Distance
		attribute(attributeStart + 4, MAX_DISTANCE_SIZE, INSTANCE_SIZE * Float.BYTES, 9 * Float.BYTES);
		
		GL15.glBindBuffer(GL15C.GL_ARRAY_BUFFER, 0);
		GL30C.glBindVertexArray(0);
		GL15C.glBindBuffer(GL15C.GL_ELEMENT_ARRAY_BUFFER, 0);
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
	
	public void draw(int instances)
	{
		//GL31C.glDrawArraysInstanced(GL20C.GL_TRIANGLES, 0, 6, 4);
		GL31C.glDrawElementsInstanced(GL20C.GL_TRIANGLES, 6, GL20C.GL_UNSIGNED_BYTE, 0L, instances);
	}
	
	public void drawWithShader(Matrix4f modelViewMatrix, Matrix4f projectionMatrix, SpaceCoords relativeSpacePos, CelestialShaderInstance shaderInstance, int instances)
	{
		Vector3f relativeVectorLy = new Vector3f((float) relativeSpacePos.x().ly(), (float) relativeSpacePos.y().ly(), (float) relativeSpacePos.z().ly());
		Vector3f relativeVectorKm = new Vector3f((float) relativeSpacePos.x().km(), (float) relativeSpacePos.y().km(), (float) relativeSpacePos.z().km());
		
		if(!RenderSystem.isOnRenderThread())
		{
			RenderSystem.recordRenderCall(() ->
			{
				this._drawWithShader(new Matrix4f(modelViewMatrix), new Matrix4f(projectionMatrix), relativeVectorLy, relativeVectorKm, shaderInstance, instances);
			});
		}
		else
			this._drawWithShader(modelViewMatrix, projectionMatrix, relativeVectorLy, relativeVectorKm, shaderInstance, instances);
	}
	
	private void _drawWithShader(Matrix4f modelViewMatrix, Matrix4f projectionMatrix, Vector3f relativeSpaceLy, Vector3f relativeSpaceKm, CelestialShaderInstance shaderInstance, int instances)
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
		
		if(shaderInstance.COLOR_MODULATOR != null)
			shaderInstance.COLOR_MODULATOR.set(RenderSystem.getShaderColor());
		
		if(shaderInstance.RELATIVE_SPACE_LY != null)
			shaderInstance.RELATIVE_SPACE_LY.set(relativeSpaceLy);
		
		if(shaderInstance.RELATIVE_SPACE_KM != null)
			shaderInstance.RELATIVE_SPACE_KM.set(relativeSpaceKm);
		
		if(shaderInstance.LENSING_MAT != null)
			shaderInstance.LENSING_MAT.set(SpaceRenderer.lensingMatrix);
		
		if(shaderInstance.LENSING_MAT_INV != null)
			shaderInstance.LENSING_MAT_INV.set(SpaceRenderer.lensingMatrixInv);
		
		if(shaderInstance.LENSING_INTENSITY != null)
			shaderInstance.LENSING_INTENSITY.set(SpaceRenderer.lensingIntensity);
		
		RenderSystem.setupShaderLights(shaderInstance);
		shaderInstance.apply();
		this.draw(instances);
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
