package net.povstalec.stellarview.compatibility.lunar;

import com.mrbysco.lunar.client.MoonHandler;
import net.minecraft.util.FastColor;
import net.povstalec.stellarview.common.util.Color;
import org.joml.Matrix4f;
import java.lang.reflect.Field;

public class LunarCompatibility {

    public static float getMoonSize(float defaultSize) {
        /* if(MoonHandler.isMoonScaled()){
            Matrix4f moonScale = MoonHandler.getMoonScale();
            try {
                return moonScale.m00() * defaultSize;
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                return defaultSize;
            }
        } */
        // BODGE FIX - LUNAR SCALE NOT IMPLEMENTED
        try {
            Field moonEventID = MoonHandler.class.getDeclaredField("moonID");
            moonEventID.setAccessible(true);
            String eventID = (String) moonEventID.get(moonEventID);
            float moonScale = 1;
            if(eventID.equals("lunar:big_moon")) { moonScale = 4f; }
            if(eventID.equals("lunar:tiny_moon")) { moonScale = 0.25f; }
            return moonScale * defaultSize;
        } catch (NoSuchFieldException | IllegalAccessException | NullPointerException e) {
            return defaultSize;
        }
    }

    public static Color.FloatRGBA getMoonColor() {
        /* Once Lunar updates to 0.2.3, there should be a getter for this,
        so I shouldn't need to steal a private float array. -NW */
        if (MoonHandler.isEventActive()) {
            try {
                Field moonColorField = MoonHandler.class.getDeclaredField("moonColor");
                moonColorField.setAccessible(true);
                float[] rawColor = (float[]) moonColorField.get(moonColorField);
                return new Color.FloatRGBA(rawColor[0], rawColor[1], rawColor[2]);
            } catch (NoSuchFieldException | IllegalAccessException | NullPointerException e) {
                return new Color.FloatRGBA(255, 255, 255);
            }
        }
        return new Color.FloatRGBA(255, 255, 255);
    }
}
