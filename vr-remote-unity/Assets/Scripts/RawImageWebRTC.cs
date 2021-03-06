﻿using System;
using UnityEngine;
using UnityEngine.UI;


public class RawImageWebRTC : MonoBehaviour
{
    // dimensions for Galaxy S7:
    // left/right: 44.5
    // top/bottom: 130

    public String RoomId = "";


    private double timeLastExecution = 0;

    private Texture2D nativeTexture = null;

    private bool isFlippedHorizontally = false;

    
    void Start()
    {
        SetupWebRtcCall();  
        RenderExternalArgbTexture();

        Debug.Log("Screen height: " + Screen.height);
        Debug.Log("Screen width: " + Screen.width);
        Debug.Log("Screen dpi: " + Screen.dpi);
        Debug.Log("Screen height: " + Screen.currentResolution);

        Screen.sleepTimeout = SleepTimeout.NeverSleep;
    }


    public void OnRawImageClick()
    {
        if (isFlippedHorizontally)
        {
            FlipComponentHorizontal();
            FlipComponentVertical();

        }
        else
        {
            FlipComponentVertical();
            FlipComponentHorizontal();
        }
    }


    void Update()
    {
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
        Debug.Log("Flipping vertically");

        RawImage img = this.GetComponent<RawImage>();
        Rect rect = new Rect(0, 0, img.uvRect.width * -1, img.uvRect.height);
        img.uvRect = rect;

        isFlippedHorizontally = false;
    }


    void FlipComponentHorizontal()
    {
        Debug.Log("Flipping horizontally");

        RawImage img = this.GetComponent<RawImage>();
        Rect rect = new Rect(0, 0, img.uvRect.width, img.uvRect.height * -1);
        img.uvRect = rect;

        isFlippedHorizontally = true;
    }


    void SetupWebRtcCall()
    {
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


    void RenderExternalArgbTexture()
    {
        if (Application.platform != RuntimePlatform.Android)
        {
            return;
        }

        double now = (DateTime.Now - new DateTime(1970, 1, 1)).TotalMilliseconds;

        if (timeLastExecution < now)
        {
            timeLastExecution = now + Config.config.externalTextureUpdateInterval;
            RenderTexture();
        }
    }


    void RenderTexture() {
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

        this.GetComponent<RawImage>().texture = nativeTexture;
        nativeTexture.UpdateExternalTexture((IntPtr)texPtr);
    }


    void NewFrameReceived(string key) {
        // RenderTexture();
    }
}
