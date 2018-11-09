using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class WebRtcTarget : MonoBehaviour
{
    public int INTERVAL_IN_MS = 1;

    public String RoomId = "";


    private double timeLastExecution = 0;

    private Texture2D nativeTexture = null;

    private bool isFlippedHorizontally = false;


    // Use this for initialization
    void Start()
    {
        //FlipComponentVertical();
        FlipComponentHorizontal();

        SetupWebRtcCall();
        //RenderExternalTexture();
        //RenderExternalAlpha8Texture();   
        RenderExternalArgbTexture();
    }


    private void OnMouseUp()
    {
        if(isFlippedHorizontally) {
            Debug.Log("Flipping vertically");
            FlipComponentHorizontal();
            FlipComponentVertical();

        } else {
            Debug.Log("Flipping horizontally");
            FlipComponentVertical();
            FlipComponentHorizontal();
        }
    }


    void Update()
    {
        //RenderExternalTexture();
        //RenderExternalAlpha8Texture();
        RenderExternalArgbTexture();
    }


    private void OnApplicationPause(bool pause)
    {
        if (Application.platform == RuntimePlatform.Android && pause == true)
        {
            AndroidJavaClass plugin = new AndroidJavaClass(Config.pluginClassString);
            plugin.CallStatic("closeWebRtcConnection");
        }
    }


    void FlipComponentVertical()
    {
        Vector3 scale = this.GetComponent<Renderer>().transform.localScale;
        scale.x = -scale.x;
        this.GetComponent<Renderer>().transform.localScale = scale;

        isFlippedHorizontally = false;
    }


    void FlipComponentHorizontal()
    {
        Vector3 scale = this.GetComponent<Renderer>().transform.localScale;
        scale.z = -scale.z;
        this.GetComponent<Renderer>().transform.localScale = scale;

        isFlippedHorizontally = true;
    }


    void SetupWebRtcCall() {
        AndroidJavaClass playerClass = new AndroidJavaClass("com.unity3d.player.UnityPlayer");
        AndroidJavaObject activity = playerClass.GetStatic<AndroidJavaObject>("currentActivity");

        // set the main activity
        AndroidJavaClass plugin = new AndroidJavaClass(Config.pluginClassString);
        plugin.SetStatic<AndroidJavaObject>("mainActivity", activity);

        RoomId = PlayerPrefs.GetString("roomId");
        object[] webRtcParam = new object[1] { RoomId };

        // setup call environment
        plugin.CallStatic("setupCallView", webRtcParam);
        // start the call
        plugin.CallStatic("startCallView");
    }


    void RenderExternalTexture()
    {
        double now = (DateTime.Now - new DateTime(1970, 1, 1)).TotalMilliseconds;

        if (timeLastExecution < now)
        {
            timeLastExecution = now + INTERVAL_IN_MS;

            AndroidJavaClass plugin = new AndroidJavaClass(Config.pluginClassString);
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

    /*void RenderExternalAlpha8Texture()
    {
        if (Application.platform != RuntimePlatform.Android)
        {
            return;
        }

        double now = (DateTime.Now - new DateTime(1970, 1, 1)).TotalMilliseconds;

        if (timeLastExecution < now)
        {
            timeLastExecution = now + INTERVAL_IN_MS;

            AndroidJavaClass plugin = new AndroidJavaClass(Config.pluginClassString);
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
                nativeTexture.filterMode = FilterMode.Point;
            }

            this.GetComponent<Renderer>().material.mainTexture = nativeTexture;
            nativeTexture.UpdateExternalTexture((IntPtr)texPtr);
        }
    }*/

    void RenderExternalArgbTexture()
    {
        if (Application.platform != RuntimePlatform.Android)
        {
            return;
        }

        double now = (DateTime.Now - new DateTime(1970, 1, 1)).TotalMilliseconds;

        if (timeLastExecution < now)
        {
            timeLastExecution = now + INTERVAL_IN_MS;

            AndroidJavaClass plugin = new AndroidJavaClass(Config.pluginClassString);
            AndroidJavaObject returnedObject = plugin.CallStatic<AndroidJavaObject>("getArgbTextureResult");

            Int32 texPtr = returnedObject.Get<Int32>("texturePtr");
            Int32 width = returnedObject.Get<Int32>("width");
            Int32 height = returnedObject.Get<Int32>("height");

            if (width <= 0 || height <= 0 || texPtr <= 0)
            {
                return;
            }

            if (null == nativeTexture || nativeTexture.width != width || nativeTexture.height != height)
            {
                nativeTexture = Texture2D.CreateExternalTexture(
                    width, height, TextureFormat.ARGB32, false, false, (IntPtr)texPtr);
                nativeTexture.filterMode = FilterMode.Point;
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
