package net.povstalec.stellarview.client.resourcepack;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.common.util.Color;
import net.povstalec.stellarview.common.util.UV;

public class Skybox
{
	public static final float DEFAULT_DISTANCE = 150.0F;
	
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

	private SkyboxFacade[] facades = new SkyboxFacade[6];
    
    public static final Codec<Skybox> CODEC = RecordCodecBuilder.create(instance -> instance.group(
    		SkyboxFacade.CODEC.fieldOf("top_facade").forGetter(Skybox::topFacade),
			SkyboxFacade.CODEC.fieldOf("north_facade").forGetter(Skybox::northFacade),
			SkyboxFacade.CODEC.fieldOf("east_facade").forGetter(Skybox::eastFacade),
			SkyboxFacade.CODEC.fieldOf("south_facade").forGetter(Skybox::southFacade),
			SkyboxFacade.CODEC.fieldOf("west_facade").forGetter(Skybox::westFacade),
			SkyboxFacade.CODEC.fieldOf("bottom_facade").forGetter(Skybox::bottomFacade)
			).apply(instance, Skybox::new));
	
	public Skybox(SkyboxFacade topFacade, SkyboxFacade northFacade, SkyboxFacade eastFacade, SkyboxFacade southFacade, SkyboxFacade westFacade, SkyboxFacade bottomFacade)
	{
		facades[0] = topFacade;
		facades[1] = northFacade;
		facades[2] = eastFacade;
		facades[3] = southFacade;
		facades[4] = westFacade;
		facades[5] = bottomFacade;
	}
	
	public SkyboxFacade topFacade()
	{
		return facades[0];
	}
	
	public SkyboxFacade northFacade()
	{
		return facades[1];
	}
	
	public SkyboxFacade eastFacade()
	{
		return facades[2];
	}
	
	public SkyboxFacade southFacade()
	{
		return facades[3];
	}
	
	public SkyboxFacade westFacade()
	{
		return facades[4];
	}
	
	public SkyboxFacade bottomFacade()
	{
		return facades[5];
	}
	
	public void render(ClientLevel level, float partialTicks, PoseStack stack, BufferBuilder bufferbuilder)
	{
		stack.pushPose();
        //stack.mulPose(Axis.YP.rotationDegrees(skyXAngle));
        //stack.mulPose(Axis.ZP.rotationDegrees(skyYAngle));
        //stack.mulPose(Axis.XP.rotationDegrees(skyZAngle));
        
        Matrix4f lastMatrix = stack.last().pose();

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.defaultBlendFunc();
        
        for(int i = 0; i < 6; i++)
        {
        	this.renderFacade(bufferbuilder, lastMatrix, facades[i], i);
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.defaultBlendFunc();
        stack.popPose();
	}
	
	protected void renderFacade(BufferBuilder bufferbuilder, Matrix4f lastMatrix, SkyboxFacade facade, int i)
	{
		UV.Quad uv = facade.uv();
		Color.IntRGBA rgba = facade.rgba();
		
		RenderSystem.setShaderTexture(0, facade.texture());
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferbuilder.vertex(lastMatrix, BOX_COORDS[i][0].x(), BOX_COORDS[i][0].y(), BOX_COORDS[i][0].z()).uv(uv.topLeft().u(), uv.topLeft().v()).color(rgba.red(), rgba.green(), rgba.blue(), rgba.alpha()).endVertex();
        bufferbuilder.vertex(lastMatrix, BOX_COORDS[i][1].x(), BOX_COORDS[i][1].y(), BOX_COORDS[i][1].z()).uv(uv.bottomLeft().u(), uv.bottomLeft().v()).color(rgba.red(), rgba.green(), rgba.blue(), rgba.alpha()).endVertex();
        bufferbuilder.vertex(lastMatrix, BOX_COORDS[i][2].x(), BOX_COORDS[i][2].y(), BOX_COORDS[i][2].z()).uv(uv.bottomRight().u(), uv.bottomRight().v()).color(rgba.red(), rgba.green(), rgba.blue(), rgba.alpha()).endVertex();
        bufferbuilder.vertex(lastMatrix, BOX_COORDS[i][3].x(), BOX_COORDS[i][3].y(), BOX_COORDS[i][3].z()).uv(uv.topRight().u(), uv.topRight().v()).color(rgba.red(), rgba.green(), rgba.blue(), rgba.alpha()).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
	}
	
	
	
	public static class SkyboxFacade
	{
		private final ResourceLocation texture;
		private final UV.Quad uv;
		private final Color.IntRGBA rgba;
	    
	    public static final Codec<SkyboxFacade> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				ResourceLocation.CODEC.fieldOf("texture").forGetter(SkyboxFacade::texture),
				UV.Quad.CODEC.fieldOf("uv").forGetter(SkyboxFacade::uv),
				Color.IntRGBA.CODEC.fieldOf("rgba").forGetter(SkyboxFacade::rgba)
				).apply(instance, SkyboxFacade::new));
		
		public SkyboxFacade(ResourceLocation texture, UV.Quad uv, Color.IntRGBA rgba)
		{
			this.texture = new ResourceLocation(texture.getNamespace(), "textures/" + texture.getPath());
			this.uv = uv;
			this.rgba = rgba;
		}
		
		public ResourceLocation texture()
		{
			return texture;
		}
		
		public UV.Quad uv()
		{
			return uv;
		}
		
		public Color.IntRGBA rgba()
		{
			return rgba;
		}
	}
}
