using UnityEngine;
using System.Collections;
using System.Collections.Generic;


public class ConfigSample : MonoBehaviour
{
    public static readonly string pluginClassString = "remote.vr.com.remote_android.PluginClass";

    public static readonly string WEB_SOCKET_SERVER_URL = "";

    public static ConfigSample config;

    public int sendHeadRotationInterval = 70;
    public float headRotationMinAngle = 0.1f;
    public int externalTextureUpdateInterval = 1;

    private void Awake()
    {
        if (ConfigSample.config == null)
        {
            ConfigSample.config = this;
        }
        else if (ConfigSample.config != this)
        {
            Destroy(this.gameObject);
        }
        DontDestroyOnLoad(this.gameObject);
    }
}
