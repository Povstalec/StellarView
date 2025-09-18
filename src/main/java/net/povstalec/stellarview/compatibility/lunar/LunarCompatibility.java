package net.povstalec.stellarview.compatibility.lunar;

import com.mojang.math.Matrix4f;
import com.mrbysco.lunar.client.MoonHandler;
import net.minecraft.util.FastColor;
import net.povstalec.stellarview.common.util.Color;

public class LunarCompatibility {

    public static float getMoonSize(float defaultSize) {
        /* Have to do string conversion and manipulation to get
        the float I'm after. At least it is only in 1.19.2. -NW */
        if(MoonHandler.isMoonScaled()){
            Matrix4f moonScale = MoonHandler.getMoonScale();
            try {
                String[] elements = moonScale.toString().split("\\s+");
                float scale = Float.parseFloat(elements[1]); // elements[0] == "Matrix4f:", which isn't a float
                return scale * defaultSize;
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
        return new Color.FloatRGBA(255, 255, 255);
    }
}
