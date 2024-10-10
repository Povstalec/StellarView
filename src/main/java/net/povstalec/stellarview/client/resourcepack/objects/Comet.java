package net.povstalec.stellarview.client.resourcepack.objects;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.Collections;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceKey;
import net.povstalec.stellarview.client.resourcepack.ViewCenter;
import net.povstalec.stellarview.common.util.*;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Comet extends OrbitingObject {
    public static final Codec<Comet> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RESOURCE_KEY_CODEC.optionalFieldOf("parent").forGetter(Comet::getParentKey),
            Codec.either(SpaceCoords.CODEC, StellarCoordinates.Equatorial.CODEC).fieldOf("coords").forGetter(object -> Either.left(object.getCoords())),
            AxisRotation.CODEC.fieldOf("axis_rotation").forGetter(Comet::getAxisRotation),
            OrbitInfo.CODEC.optionalFieldOf("orbit_info").forGetter(Comet::getOrbitInfo),
            TextureLayer.CODEC.listOf().fieldOf("texture_layers").forGetter(Comet::getTextureLayers),
            SublimationTransitionRange.CODEC.optionalFieldOf("sublimation_transition_range").forGetter(Comet::getSublimationTransitionRange),
            SpaceObject.FadeOutHandler.CODEC.optionalFieldOf("fade_out_handler", SpaceObject.FadeOutHandler.DEFAULT_PLANET_HANDLER).forGetter(Comet::getFadeOutHandler)
    ).apply(instance, Comet::new));

    private final Optional<SublimationTransitionRange> sublimationTransitionRange;

    public final List<Float> initialAlpha = textureLayers.stream()
            .map(textureLayer -> textureLayer.rgba().alpha())
            .collect(Collectors.toList());

    public Comet(Optional<ResourceKey<SpaceObject>> parent, Either<SpaceCoords, StellarCoordinates.Equatorial> coords, AxisRotation axisRotation,
                 Optional<OrbitInfo> orbitInfo, List<TextureLayer> textureLayers,
                 Optional<SublimationTransitionRange> sublimationTransitionRange,
                 FadeOutHandler fadeOutHandler) {
        super(parent, coords, axisRotation, orbitInfo, textureLayers, fadeOutHandler);
        this.sublimationTransitionRange = sublimationTransitionRange;
    }

    public Optional<SublimationTransitionRange> getSublimationTransitionRange() {
        return sublimationTransitionRange;
    }

    public void adjustAlpha(ViewCenter viewCenter, long ticks, float partialTicks) {
        for (int i = 1; i < textureLayers.size(); i++) {
            Vector3f cometPosition = this.getPosition(viewCenter, ticks, partialTicks);
            Vector3f parentPosition = this.getParent().get().getPosition(viewCenter, ticks, partialTicks);
            float dist = cometPosition.distance(parentPosition);

            float originalAlpha = initialAlpha.get(i);
            float distanceAlphaFactor = sublimationTransitionRange
                    .map(range -> range.calculateAlphaFactor(dist))
                    .orElse(1.0f); // Default to full alpha if no range is defined
            textureLayers.get(i).rgba().setAlpha((int) (originalAlpha * distanceAlphaFactor));
        }
    }

    @Override
    protected void renderTextureLayer(TextureLayer textureLayer, ViewCenter viewCenter, ClientLevel level, Camera camera, BufferBuilder bufferbuilder, Matrix4f lastMatrix, SphericalCoords sphericalCoords, long ticks, double distance, float partialTicks)
    {
        if(textureLayer.rgba().alpha() <= 0)
            return;

        // Compute the direction to the parent and the required rotation
        Vector3f cometPosition = this.getPosition(viewCenter, ticks, partialTicks);
        Vector3f parentPosition = this.getParent().get().getPosition(viewCenter, ticks, partialTicks);
        Vector3f directionToParent = new Vector3f(parentPosition.x - cometPosition.x,
                parentPosition.y - cometPosition.y,
                parentPosition.z - cometPosition.z);
        directionToParent.normalize();

        float angleToParent = (float) Math.atan2(directionToParent.z, directionToParent.x);
        float angleInDegrees = (float) Math.toDegrees(angleToParent);
        float textureDefaultAngle = 153f; // Tail angle in the texture
        float rotationAngle = angleInDegrees - textureDefaultAngle;

        float size = (float) textureLayer.mulSize(distanceSize(distance));

        if(size < textureLayer.minSize())
        {
            if(textureLayer.clampAtMinSize())
                size = (float) textureLayer.minSize();
            else
                return;
        }

        float rotation = rotationAngle + (float) textureLayer.rotation();
        adjustAlpha(viewCenter, ticks, partialTicks);

        renderOnSphere(textureLayer.rgba(), Color.FloatRGBA.DEFAULT, textureLayer.texture(), textureLayer.uv(),
                level, camera, bufferbuilder, lastMatrix, sphericalCoords,
                ticks, distance, partialTicks, dayBrightness(viewCenter, size, ticks, level, camera, partialTicks), size, rotation, textureLayer.shoulBlend());
    }

    public static class SublimationTransitionRange {
        public static final Codec<SublimationTransitionRange> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.FLOAT.fieldOf("upper_bound").forGetter(SublimationTransitionRange::getUpperBound),
                Codec.FLOAT.fieldOf("lower_bound").forGetter(SublimationTransitionRange::getLowerBound)
        ).apply(instance, SublimationTransitionRange::new));

        private final float upperBound;
        private final float lowerBound;

        public SublimationTransitionRange(float upperBound, float lowerBound) {
            this.upperBound = upperBound;
            this.lowerBound = lowerBound;
        }

        public float getUpperBound() { return upperBound; }

        public float getLowerBound() { return lowerBound; }

        public float calculateAlphaFactor(double distance) {
            if (distance >= upperBound) return 0.0f;
            if (distance <= lowerBound) return 1.0f;

            // Linear interpolation between min_alpha and max_alpha
            return (float) (1.0 - (distance - lowerBound) / (upperBound - lowerBound));
        }
    }
}