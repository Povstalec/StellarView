package net.povstalec.stellarview.client.resourcepack;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.common.util.UV;

public class Skybox
{
	public static final float DEFAULT_DISTANCE = 150.0F;
	
	public static final UV.Quad UV = new UV.Quad(false);
	
	public static final Vector3f[][] BOX_COORDS =
			{
				{
					new Vector3f(-DEFAULT_DISTANCE, DEFAULT_DISTANCE, DEFAULT_DISTANCE),
					new Vector3f(-DEFAULT_DISTANCE, DEFAULT_DISTANCE, -DEFAULT_DISTANCE),
					new Vector3f(DEFAULT_DISTANCE, DEFAULT_DISTANCE, -DEFAULT_DISTANCE),
					new Vector3f(DEFAULT_DISTANCE, DEFAULT_DISTANCE, DEFAULT_DISTANCE)
				},
				{
					new Vector3f(DEFAULT_DISTANCE, DEFAULT_DISTANCE, DEFAULT_DISTANCE),
					new Vector3f(DEFAULT_DISTANCE, -DEFAULT_DISTANCE, DEFAULT_DISTANCE),
					new Vector3f(-DEFAULT_DISTANCE, -DEFAULT_DISTANCE, DEFAULT_DISTANCE),
					new Vector3f(-DEFAULT_DISTANCE, DEFAULT_DISTANCE, DEFAULT_DISTANCE)
				},
				{
					new Vector3f(-DEFAULT_DISTANCE, DEFAULT_DISTANCE, DEFAULT_DISTANCE),
					new Vector3f(-DEFAULT_DISTANCE, -DEFAULT_DISTANCE, DEFAULT_DISTANCE),
					new Vector3f(-DEFAULT_DISTANCE, -DEFAULT_DISTANCE, -DEFAULT_DISTANCE),
					new Vector3f(-DEFAULT_DISTANCE, DEFAULT_DISTANCE, -DEFAULT_DISTANCE)
				},
				{
					new Vector3f(-DEFAULT_DISTANCE, DEFAULT_DISTANCE, -DEFAULT_DISTANCE),
					new Vector3f(-DEFAULT_DISTANCE, -DEFAULT_DISTANCE, -DEFAULT_DISTANCE),
					new Vector3f(DEFAULT_DISTANCE, -DEFAULT_DISTANCE, -DEFAULT_DISTANCE),
					new Vector3f(DEFAULT_DISTANCE, DEFAULT_DISTANCE, -DEFAULT_DISTANCE)
				},
				{
					new Vector3f(DEFAULT_DISTANCE, DEFAULT_DISTANCE, -DEFAULT_DISTANCE),
					new Vector3f(DEFAULT_DISTANCE, -DEFAULT_DISTANCE, -DEFAULT_DISTANCE),
					new Vector3f(DEFAULT_DISTANCE, -DEFAULT_DISTANCE, DEFAULT_DISTANCE),
					new Vector3f(DEFAULT_DISTANCE, DEFAULT_DISTANCE, DEFAULT_DISTANCE)
				},
				{
					new Vector3f(-DEFAULT_DISTANCE, -DEFAULT_DISTANCE, -DEFAULT_DISTANCE),
					new Vector3f(-DEFAULT_DISTANCE, -DEFAULT_DISTANCE, DEFAULT_DISTANCE),
					new Vector3f(DEFAULT_DISTANCE, -DEFAULT_DISTANCE, DEFAULT_DISTANCE),
					new Vector3f(DEFAULT_DISTANCE, -DEFAULT_DISTANCE, -DEFAULT_DISTANCE)
				}
			};

	private ResourceLocation[] textures = new ResourceLocation[6];
    
    public static final Codec<Skybox> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ResourceLocation.CODEC.fieldOf("top_texture").forGetter(Skybox::topTexture),
			ResourceLocation.CODEC.fieldOf("north_texture").forGetter(Skybox::northTexture),
			ResourceLocation.CODEC.fieldOf("east_texture").forGetter(Skybox::eastTexture),
			ResourceLocation.CODEC.fieldOf("south_texture").forGetter(Skybox::southTexture),
			ResourceLocation.CODEC.fieldOf("west_texture").forGetter(Skybox::westTexture),
			ResourceLocation.CODEC.fieldOf("bottom_texture").forGetter(Skybox::bottomTexture)
			).apply(instance, Skybox::new));
	
	public Skybox(ResourceLocation topTexture, ResourceLocation northTexture, ResourceLocation eastTexture, ResourceLocation southTexture, ResourceLocation westTexture, ResourceLocation bottomTexture)
	{
		textures[0] = new ResourceLocation(topTexture.getNamespace(), "textures/" + topTexture.getPath());
		textures[1] = new ResourceLocation(northTexture.getNamespace(), "textures/" + northTexture.getPath());
		textures[2] = new ResourceLocation(eastTexture.getNamespace(), "textures/" + eastTexture.getPath());
		textures[3] = new ResourceLocation(southTexture.getNamespace(), "textures/" + southTexture.getPath());
		textures[4] = new ResourceLocation(westTexture.getNamespace(), "textures/" + westTexture.getPath());
		textures[5] = new ResourceLocation(bottomTexture.getNamespace(), "textures/" + bottomTexture.getPath());
	}
	
	public ResourceLocation topTexture()
	{
		return textures[0];
	}
	
	public ResourceLocation northTexture()
	{
		return textures[1];
	}
	
	public ResourceLocation eastTexture()
	{
		return textures[2];
	}
	
	public ResourceLocation southTexture()
	{
		return textures[3];
	}
	
	public ResourceLocation westTexture()
	{
		return textures[4];
	}
	
	public ResourceLocation bottomTexture()
	{
		return textures[5];
	}
	
	public void render(ClientLevel level, float partialTicks, PoseStack stack, BufferBuilder bufferbuilder)
	{
		
		stack.pushPose();
        //stack.mulPose(Axis.YP.rotationDegrees(skyXAngle));
        //stack.mulPose(Axis.ZP.rotationDegrees(skyYAngle));
        //stack.mulPose(Axis.XP.rotationDegrees(skyZAngle));
        
        Matrix4f lastMatrix = stack.last().pose();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

        RenderSystem.setShaderColor(0.5F, 0.5F, 0.5F, 0.5F);
        
        for(int i = 0; i < 6; i++)
        {
        	this.renderFacade(bufferbuilder, lastMatrix, textures[i], i);
        }
        
		RenderSystem.defaultBlendFunc();
        stack.popPose();
	}
	
	protected void renderFacade(BufferBuilder bufferbuilder, Matrix4f lastMatrix, ResourceLocation texture, int i)
	{
		RenderSystem.setShaderTexture(0, texture);
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(lastMatrix, BOX_COORDS[i][0].x, BOX_COORDS[i][0].y, BOX_COORDS[i][0].z).uv(UV.topLeft().u(), UV.topLeft().v()).endVertex();
        bufferbuilder.vertex(lastMatrix, BOX_COORDS[i][1].x, BOX_COORDS[i][1].y, BOX_COORDS[i][1].z).uv(UV.bottomLeft().u(), UV.bottomLeft().v()).endVertex();
        bufferbuilder.vertex(lastMatrix, BOX_COORDS[i][2].x, BOX_COORDS[i][2].y, BOX_COORDS[i][2].z).uv(UV.bottomRight().u(), UV.bottomRight().v()).endVertex();
        bufferbuilder.vertex(lastMatrix, BOX_COORDS[i][3].x, BOX_COORDS[i][3].y, BOX_COORDS[i][3].z).uv(UV.topRight().u(), UV.topRight().v()).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
	}
}
