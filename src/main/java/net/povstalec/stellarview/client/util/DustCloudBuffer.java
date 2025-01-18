package net.povstalec.stellarview.client.util;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.povstalec.stellarview.client.render.shader.DustCloudShaderInstance;
import net.povstalec.stellarview.client.render.SpaceRenderer;
import net.povstalec.stellarview.common.util.SpaceCoords;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL15C;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;

public class DustCloudBuffer implements AutoCloseable
{
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
	
	public DustCloudBuffer()
	{
		RenderSystem.assertOnRenderThread();
		this.vertexBufferId = GlStateManager._glGenBuffers();
		this.indexBufferId = GlStateManager._glGenBuffers();
		this.arrayObjectId = GlStateManager._glGenVertexArrays();
	}
	
	public void upload(MeshData mesh)
	{
		if(!this.isInvalid())
		{
			RenderSystem.assertOnRenderThread();
			try
			{
				final var drawState = mesh.drawState();
				this.format = this.uploadVertexBuffer(mesh, mesh.vertexBuffer());
				this.sequentialIndices = this.uploadIndexBuffer(mesh, mesh.indexBuffer());
				this.indexCount = drawState.indexCount();
				this.indexType = drawState.indexType();
				this.mode = drawState.mode();
			}
			finally
			{
				mesh.close();
			}
		}
	}
	
	private VertexFormat uploadVertexBuffer(MeshData mesh, ByteBuffer vertexBuffer)
	{
		final var drawState = mesh.drawState();
		boolean formatEquals = false;
		if(!drawState.format().equals(this.format))
		{
			if(this.format != null)
				this.format.clearBufferState();
			
			GlStateManager._glBindBuffer(GL15C.GL_ARRAY_BUFFER, this.vertexBufferId);
			drawState.format().setupBufferState();
			formatEquals = true;
		}
		
		if(mesh.indexBuffer() == null)
		{
			if(!formatEquals)
				GlStateManager._glBindBuffer(GL15C.GL_ARRAY_BUFFER, this.vertexBufferId);
			
			RenderSystem.glBufferData(GL15C.GL_ARRAY_BUFFER, vertexBuffer, 35044);
		}
		
		return drawState.format();
	}
	
	@Nullable
	private RenderSystem.AutoStorageIndexBuffer uploadIndexBuffer(MeshData mesh, ByteBuffer indexBuffer)
	{
		final var drawState = mesh.drawState();
		if(mesh.vertexBuffer() == null)
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
	}
	
	public static void unbind()
	{
		BufferUploader.invalidate();
		GlStateManager._glBindVertexArray(0);
	}
	
	public void draw()
	{
		RenderSystem.drawElements(this.mode.asGLMode, this.indexCount, this.getIndexType().asGLType);
	}
	
	private VertexFormat.IndexType getIndexType()
	{
		RenderSystem.AutoStorageIndexBuffer rendersystem$autostorageindexbuffer = this.sequentialIndices;
		return rendersystem$autostorageindexbuffer != null ? rendersystem$autostorageindexbuffer.type() : this.indexType;
	}
	
	public void drawWithShader(Matrix4f modelViewMatrix, Matrix4f projectionMatrix, SpaceCoords relativeSpacePos, DustCloudShaderInstance shaderInstance)
	{
		Vector3f relativeVectorLy = new Vector3f((float) relativeSpacePos.x().ly(), (float) relativeSpacePos.y().ly(), (float) relativeSpacePos.z().ly());
		Vector3f relativeVectorKm = new Vector3f((float) relativeSpacePos.x().km(), (float) relativeSpacePos.y().km(), (float) relativeSpacePos.z().km());
		
		if(!RenderSystem.isOnRenderThread())
		{
			RenderSystem.recordRenderCall(() ->
			{
				this._drawWithShader(new Matrix4f(modelViewMatrix), new Matrix4f(projectionMatrix), relativeVectorLy, relativeVectorKm, shaderInstance);
			});
		}
		else
			this._drawWithShader(modelViewMatrix, projectionMatrix, relativeVectorLy, relativeVectorKm, shaderInstance);
	}
	
	private void _drawWithShader(Matrix4f modelViewMatrix, Matrix4f projectionMatrix, Vector3f relativeSpaceLy, Vector3f relativeSpaceKm, DustCloudShaderInstance shaderInstance)
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
		this.draw();
		shaderInstance.clear();
	}
	
	public void close()
	{
		if (this.vertexBufferId >= 0)
		{
			RenderSystem.glDeleteBuffers(this.vertexBufferId);
			this.vertexBufferId = -1;
		}
		
		if (this.indexBufferId >= 0)
		{
			RenderSystem.glDeleteBuffers(this.indexBufferId);
			this.indexBufferId = -1;
		}
		
		if (this.arrayObjectId >= 0)
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
}
