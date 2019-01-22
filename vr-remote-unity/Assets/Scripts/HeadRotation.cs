//#define RASPBERRY_PI

using System.Collections;
using System;
using UnityEngine;


[System.Serializable]
public class HeadRotation
{
    public int vertical;
    public int horizontal;

    public static HeadRotation fromVector3(Vector3 vector)
    {
        HeadRotation rotation = new HeadRotation();

#if RASPBERRY_PI
        rotation.vertical = EulerToPWM(vector.x, -500);
        rotation.horizontal = EulerToPWM(vector.y);
#else
        rotation.vertical = EulerToPWM(vector.x);
        rotation.horizontal = EulerToPWM(vector.y);
#endif

        return rotation;
    }

    private static int EulerToPWM(float eulerAngle, int pwmOffset = 0)
    {
        float value = 0;

        // map to 0 - 180 degree
        if (eulerAngle < 180)
        {
            value = Mathf.Max(0, 90f - eulerAngle);
        }
        else
        {
            value = Mathf.Min(180f, 90f + Math.Abs(eulerAngle - 360f));
        }

        // Raspberry PI only: map to 0 - 1800 + 500 offset
#if RASPBERRY_PI
        int pwmValue = (int)Mathf.Round((1800f * value) / 180f) + 500 + pwmOffset;
        return Math.Min(2300, Math.Max(500, pwmValue));
#else
        return (int) value;
#endif
    }
}