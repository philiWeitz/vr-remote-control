using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class WebRtcTarget : MonoBehaviour
{
    private readonly string pluginClassString = "com.vrremote.PluginClass";

    private long timeLastExecution = 0;

    private readonly int INTERVAL = 40000;

    private Texture2D nativeTexture = null;


    // Use this for initialization
    void Start()
    {
        Vector3 scale = this.GetComponent<Renderer>().transform.localScale;
        scale.x = -scale.x;
        this.GetComponent<Renderer>().transform.localScale = scale;

        SetupWebRtcCall();
        //RenderExternalTexture();
        RenderExternalAlpha8Texture();   
    }


    void Update()
    {
        //RenderExternalTexture();
        RenderExternalAlpha8Texture();
    }


    void SetupWebRtcCall() {
        AndroidJavaClass playerClass = new AndroidJavaClass("com.unity3d.player.UnityPlayer");
        AndroidJavaObject activity = playerClass.GetStatic<AndroidJavaObject>("currentActivity");

        // set the main activity
        AndroidJavaClass plugin = new AndroidJavaClass(pluginClassString);
        plugin.SetStatic<AndroidJavaObject>("mainActivity", activity);

        // setup call environment
        plugin.CallStatic("setupCallView");
        // start the call
        plugin.CallStatic("startCallView");
    }


    void RenderExternalTexture()
    {
        if (timeLastExecution < DateTime.UtcNow.ToFileTimeUtc())
        {
            timeLastExecution = DateTime.UtcNow.ToFileTimeUtc() + INTERVAL;

            AndroidJavaClass plugin = new AndroidJavaClass(pluginClassString);
            AndroidJavaObject returnedObject = plugin.CallStatic<AndroidJavaObject>("getTextureResult");

            Int32 texPtr = returnedObject.Get<Int32>("texturePtr");
            Int32 width = returnedObject.Get<Int32>("width");
            Int32 height = returnedObject.Get<Int32>("height");

            Debug.Log("texture pointer: " + texPtr + ", width: " + width + ", height: " + height);

            if (width <= 0 || height <= 0 || texPtr <= 0)
            {
                return;
            }

            if (null == nativeTexture)
            {
                nativeTexture = Texture2D.CreateExternalTexture(
                    width, height, TextureFormat.ARGB32, false, false, (IntPtr)texPtr);
            }

            this.GetComponent<Renderer>().material.mainTexture = nativeTexture;
            nativeTexture.UpdateExternalTexture((IntPtr)texPtr);
        }
    }

    void RenderExternalAlpha8Texture()
    {
        if (Application.platform != RuntimePlatform.Android)
        {
            return;
        }

        if (timeLastExecution < DateTime.UtcNow.ToFileTimeUtc())
        {
            timeLastExecution = DateTime.UtcNow.ToFileTimeUtc() + INTERVAL;

            AndroidJavaClass plugin = new AndroidJavaClass(pluginClassString);
            AndroidJavaObject returnedObject = plugin.CallStatic<AndroidJavaObject>("getAlpha8TextureResult");

            Int32 texPtr = returnedObject.Get<Int32>("texturePtr");
            Int32 width = returnedObject.Get<Int32>("width");
            Int32 height = returnedObject.Get<Int32>("height");

            // Debug.Log("texture pointer: " + texPtr + ", width: " + width + ", height: " + height);

            if (width <= 0 || height <= 0 || texPtr <= 0)
            {
                return;
            }

            if (null == nativeTexture || nativeTexture.width != width || nativeTexture.height != height)
            {
                nativeTexture = Texture2D.CreateExternalTexture(
                    width, height, TextureFormat.Alpha8, false, false, (IntPtr)texPtr);
            }

            this.GetComponent<Renderer>().material.mainTexture = nativeTexture;
            nativeTexture.UpdateExternalTexture((IntPtr)texPtr);
        }
    }

    void RenderTestAlpha8Texture() {
        Texture2D rgbTexture =
            new Texture2D(100, 100, TextureFormat.Alpha8, false, false);

        var fillColorArray = rgbTexture.GetPixels();
     
        for (var i = 0; i < fillColorArray.Length; ++i)
        {
            fillColorArray[i] = new Color(0,0,0,0.5f);
        }

        rgbTexture.SetPixels(fillColorArray);
        rgbTexture.Apply();

        this.GetComponent<Renderer>().material.mainTexture = rgbTexture;
    }
}
