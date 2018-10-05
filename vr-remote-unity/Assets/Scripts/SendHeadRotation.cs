using System.Collections;
using System;
using UnityEngine;

public class SendHeadRotation : MonoBehaviour
{
    private readonly string pluginClassString = "com.vrremote.PluginClass";

    private readonly int INTERVAL = 40000;

    private long timeLastExecution = 0;

    // Use this for initialization
    void Start()
    {
        ConnectToMotionServer();
    }


    private void Update()
    {
        SendHeadRotationToMotionServer();
    }


    void ConnectToMotionServer() {
        AndroidJavaClass plugin = new AndroidJavaClass(pluginClassString);

        plugin.CallStatic("setWebSocketUrl", "ws://vr-remote-control-ws-server.herokuapp.com");
        plugin.CallStatic("connectToMotionWebSocket");
    }


    void SendHeadRotationToMotionServer()
    {
        if (Application.platform != RuntimePlatform.Android)
        {
            return;
        }

        if (timeLastExecution < DateTime.UtcNow.ToFileTimeUtc())
        {
            timeLastExecution = DateTime.UtcNow.ToFileTimeUtc() + INTERVAL;

            AndroidJavaClass plugin = new AndroidJavaClass(pluginClassString);

            // get camera rotation
            Vector3 rotation = Camera.main.gameObject.transform.rotation.eulerAngles;

            string json = JsonUtility.ToJson(HeadRotation.fromVector3(rotation));
            plugin.CallStatic("sendMessageToMotionWebSocket", json);
        }
    }

}
