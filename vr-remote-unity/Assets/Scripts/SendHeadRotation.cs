using System.Collections;
using System;
using UnityEngine;

public class SendHeadRotation : MonoBehaviour
{
    public Camera CameraObject;
    private double timeLastExecution = 0;

    private Vector3 lastHeadPosition = new Vector3(100, 100, 100);

    private bool drivingModeActive = false;

    // Use this for initialization
    void Start()
    {
        if (CameraObject == null) {
            CameraObject = Camera.main;
        }
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


    void ConnectToMotionServer()
    {
        AndroidJavaClass plugin = new AndroidJavaClass(Config.pluginClassString);

        plugin.CallStatic("setWebSocketUrl", Config.WEB_SOCKET_SERVER_URL);
        plugin.CallStatic("connectToMotionWebSocket");
    }


    bool HeadPositionHasChanged(Vector3 currentHeadPosition)
    {
        Vector3 sub = currentHeadPosition - lastHeadPosition;

        return Mathf.Abs(sub.x) > Config.config.headRotationMinAngle ||
            Mathf.Abs(sub.y) > Config.config.headRotationMinAngle ||
            Mathf.Abs(sub.z) > Config.config.headRotationMinAngle;

        //float sum = Mathf.Abs(sub.x) + Mathf.Abs(sub.y) + Mathf.Abs(sub.z);
        //return (sum >= Config.config.headRotationMinAngle);
    }


    void SendHeadRotationToMotionServer()
    {
        if (Application.platform != RuntimePlatform.Android)
        {
            return;
        }

        double now = (DateTime.Now - new DateTime(1970, 1, 1)).TotalMilliseconds;

        if (Input.anyKey && !drivingModeActive) {
            drivingModeActive = true;

            string json = JsonUtility.ToJson(HeadRotation.centered());

            AndroidJavaClass plugin = new AndroidJavaClass(Config.pluginClassString);
            plugin.CallStatic("sendMessageToMotionWebSocket", json);

        } else if (!Input.anyKey && drivingModeActive) {
            drivingModeActive = false;
        }


        if (timeLastExecution < now && !drivingModeActive)
        {
            timeLastExecution = now + Config.config.sendHeadRotationInterval;

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
            timeLastExecution = now + Config.config.sendHeadRotationInterval;

            // get camera rotation
            Vector3 currentHeadPosition = CameraObject.gameObject.transform.rotation.eulerAngles;

            if (HeadPositionHasChanged(currentHeadPosition))
            {
                lastHeadPosition = currentHeadPosition;
                string json = JsonUtility.ToJson(HeadRotation.fromVector3(currentHeadPosition));
                Debug.Log(json);
            }
            Debug.Log("Position is the same");
        }
    }
}
