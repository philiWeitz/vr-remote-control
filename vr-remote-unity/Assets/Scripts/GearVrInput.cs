using System;
using UnityEngine;
using UnityEngine.UI;


[System.Serializable]
public class CarControl {
    public string direction = "";

    public CarControl(string direction) {
        this.direction = direction;
    }
}


public class GearVrInput : MonoBehaviour {

    public static GearVrInput input;

    public Text TextField;

    private string lastAction = "";


    private void Awake()
    {
        if (GearVrInput.input == null)
        {
            GearVrInput.input = this;
        }
        else if (GearVrInput.input != this)
        {
            Destroy(this.gameObject);
        }
        DontDestroyOnLoad(this.gameObject);
    }

	void Update () {
        DebugPrint();

        if(HasCarControlChanged()) {
            string json = JsonUtility.ToJson(new CarControl(GetCarControl()));

            if (Application.platform == RuntimePlatform.Android)
            {
                AndroidJavaClass plugin = new AndroidJavaClass(Config.pluginClassString);
                plugin.CallStatic("sendMessageToMotionWebSocket", json);
            } else {
                Debug.Log("Car control send: " + json);
            }
        }
    }

    public string GetCarControl() {
        float dpadX = Input.GetAxis("Oculus_GearVR_DpadX");
        float dpadY = Input.GetAxis("Oculus_GearVR_DpadY");

        if (dpadX >= 0.9) {
            return "FORWARD";
        } else if (dpadX <= 0.1 && dpadX > 0) {
            return "BACKWARDS";
        } else if (dpadY >= 0.9) {
            return "LEFT";
        } else if (dpadY <= 0.1 && dpadY > 0) {
            return "RIGHT";
        }
        return "STOP";
    }

    public bool HasCarControlChanged() {
        if(GetCarControl() != lastAction) {
            lastAction = GetCarControl();
            return true;
        }
        return false;
    }

    void DebugPrint() {
        Vector2 touchPosition = OVRInput.Get(OVRInput.Axis2D.PrimaryTouchpad);

        string text = "";
        text += "DpadX: " + Input.GetAxis("Oculus_GearVR_DpadX") + "\n";
        text += "DpadY: " + Input.GetAxis("Oculus_GearVR_DpadY") + "\n";
        text += "Touch Position: " + touchPosition.x;
        text += "Any button down: " + Input.anyKey.ToString() + "\n";
        text += "Mouse button down: " + Input.GetMouseButtonDown(0).ToString() + '\n';
        text += "Mouse button back: " + Input.GetMouseButtonDown(1).ToString() + '\n';
        text += "Car control: " + GetCarControl();
        TextField.text = text;
    }
}
