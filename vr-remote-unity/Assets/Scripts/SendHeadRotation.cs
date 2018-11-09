using System.Collections;
using System;
using UnityEngine;

public class SendHeadRotation : MonoBehaviour
{
    public int INTERVAL_IN_MS = 50;

    public float MAX_ANGLE_DIFFERENCE = 2;

    private double timeLastExecution = 0;

    private Vector3 lastHeadPosition = new Vector3(100,100,100);


    // Use this for initialization
    void Start()
    {
        ConnectToMotionServer();
    }


    private void Update()
    {
        SendHeadRotationToMotionServer();
        //SendHeadRotationToMotionServerMock();
    }


    private void OnApplicationPause(bool pause)
    {
        if (Application.platform == RuntimePlatform.Android && pause == true)
        {
            AndroidJavaClass plugin = new AndroidJavaClass(Config.pluginClassString);

            string json = JsonUtility.ToJson(HeadRotation.fromVector3(new Vector3(0, 0, 0)));
            plugin.CallStatic("sendMessageToMotionWebSocket", json);
        }
    }


    void ConnectToMotionServer() {
        AndroidJavaClass plugin = new AndroidJavaClass(Config.pluginClassString);

        plugin.CallStatic("setWebSocketUrl", Config.WEB_SOCKET_SERVER_URL);
        plugin.CallStatic("connectToMotionWebSocket");
    }


    bool HeadPositionHasChanged(Vector3 currentHeadPosition) {
        Vector3 sub = currentHeadPosition - lastHeadPosition;
        return (sub.x >= MAX_ANGLE_DIFFERENCE
                || sub.y >= MAX_ANGLE_DIFFERENCE 
                || sub.z >= MAX_ANGLE_DIFFERENCE);
    }


    void SendHeadRotationToMotionServer()
    {
        if (Application.platform != RuntimePlatform.Android)
        {
            return;
        }

        double now = (DateTime.Now - new DateTime(1970, 1, 1)).TotalMilliseconds;

        if (timeLastExecution < now)
        {
            timeLastExecution = now + INTERVAL_IN_MS;

            // get camera rotation
            Vector3 currentHeadPosition = Camera.main.gameObject.transform.rotation.eulerAngles;

            if (HeadPositionHasChanged(currentHeadPosition))
            {
                lastHeadPosition = currentHeadPosition;

                string json = JsonUtility.ToJson(HeadRotation.fromVector3(currentHeadPosition));

                AndroidJavaClass plugin = new AndroidJavaClass(Config.pluginClassString);
                plugin.CallStatic("sendMessageToMotionWebSocket", json);
            }
        }
    }


    void SendHeadRotationToMotionServerMock()
    {
        double now = (DateTime.Now - new DateTime(1970, 1, 1)).TotalMilliseconds;

        if (timeLastExecution < now)
        {
            timeLastExecution = now + INTERVAL_IN_MS;

            // get camera rotation
            Vector3 currentHeadPosition = Camera.main.gameObject.transform.rotation.eulerAngles;

            if(HeadPositionHasChanged(currentHeadPosition)) {
                lastHeadPosition = currentHeadPosition;
                string json = JsonUtility.ToJson(HeadRotation.fromVector3(currentHeadPosition));
                Debug.Log(json);
            }
            Debug.Log("Position is the same");
        }
    }
}
