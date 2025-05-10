package net.povstalec.stellarview.compatibility.lunar;

import com.mojang.math.Matrix4f;
import com.mrbysco.lunar.client.MoonHandler;
import net.minecraft.client.multiplayer.ClientLevel;
import net.povstalec.stellarview.common.util.Color;

import java.lang.reflect.Field;

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

    public static Color.FloatRGBA getMoonColor(ClientLevel level) {
        /* There isn't a getter for this, so I get to steal a private array. -NW */
        if (MoonHandler.isEventActive()) {
            try {
                Field moonColorField = MoonHandler.class.getDeclaredField("moonColor");
                moonColorField.setAccessible(true);
                float[] rawColor = (float[]) moonColorField.get(moonColorField);
                return new Color.FloatRGBA(rawColor[0], rawColor[1], rawColor[2]);
            } catch (NoSuchFieldException | IllegalAccessException | NullPointerException e) {
                return new Color.FloatRGBA(1, 1, 1);
            }
        }
        return new Color.FloatRGBA(1, 1, 1);
    }
}
