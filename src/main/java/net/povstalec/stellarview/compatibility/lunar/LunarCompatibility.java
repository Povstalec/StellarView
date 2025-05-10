package net.povstalec.stellarview.compatibility.lunar;

import com.mrbysco.lunar.client.MoonHandler;
import net.minecraft.util.FastColor;
import net.povstalec.stellarview.common.util.Color;
import org.joml.Matrix4f;

public class LunarCompatibility {

    public static float getMoonSize(float defaultSize) {
        if(MoonHandler.isMoonScaled()){
            Matrix4f moonScale = MoonHandler.getMoonScale();
            try {
                return moonScale.m00() * defaultSize;
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                return defaultSize;
            }
        }
        return defaultSize;
    }

    public static Color.FloatRGBA getMoonColor() {
        if (MoonHandler.isEventActive()) {
            int moonColor = MoonHandler.getMoonColor();
            int r = FastColor.ARGB32.red(moonColor);
            int g = FastColor.ARGB32.green(moonColor);
            int b = FastColor.ARGB32.blue(moonColor);
            return new Color.FloatRGBA(r, g, b);
        }
        return new Color.FloatRGBA(1, 1, 1);
    }
}
