using System;
using UnityEngine;
using UnityEngine.UI;


[System.Serializable]
public class CarControl {
    public int left = 0;
    public int right = 0;

    public CarControl() {

    }

    public CarControl(double left, double right) {
        this.left = (int) left;
        this.right = (int) right;
    }
}


public class GearVrInput : MonoBehaviour {

    public static GearVrInput input;

    public Text TextField;

    private double timeLastExecution = 0;

    private const float MAX_SPEED_FACTOR = 0.25F;

    private const double MAX_PWM_VALUE = 100;

    private const double MIN_PWM_SPEED_VALUE = 20;

    private const double MAX_PWM_SPEED_VALUE = MAX_PWM_VALUE - MIN_PWM_SPEED_VALUE;

    private const int INTERVAL = 200;

    private bool drivingModeActive = false;


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

        double now = (DateTime.Now - new DateTime(1970, 1, 1)).TotalMilliseconds;

        if (drivingModeActive && !Input.anyKey)
        {
            // send motor stop command
            SendCarControl(new CarControl());
            drivingModeActive = false;
        }

        if (timeLastExecution < now && Input.anyKey)
        {
            timeLastExecution = now + INTERVAL;

            drivingModeActive = true;
            SendCarControl(GetSpeedControl());
        }
    }

    private void SendCarControl(CarControl control) {
        string json = JsonUtility.ToJson(control);

        if (Application.platform == RuntimePlatform.Android)
        {
            AndroidJavaClass plugin = new AndroidJavaClass(Config.pluginClassString);
            plugin.CallStatic("sendCarControlMessage", json);
        }
        else
        {
            Debug.Log("Car control send: " + json);
        }
    }

    private CarControl GetSpeedControl() {
        Vector2 touchPosition = OVRInput.Get(OVRInput.Axis2D.PrimaryTouchpad);

        float x = Math.Abs(touchPosition.x);
        float y = Math.Abs(touchPosition.y);

        double c = Math.Sqrt(x * x + y * y);
        double alpha = Math.Asin(x / c) * 180 / Math.PI;

        double forwardSpeed = (c * MAX_PWM_SPEED_VALUE / MAX_SPEED_FACTOR) + MIN_PWM_SPEED_VALUE;
        double backwardsSpeed = (alpha * forwardSpeed) / 90;
        backwardsSpeed = (-forwardSpeed) + 2 * backwardsSpeed;

        forwardSpeed = Math.Min(MAX_PWM_VALUE, Math.Max(-MAX_PWM_VALUE, forwardSpeed));
        backwardsSpeed = Math.Min(MAX_PWM_VALUE, Math.Max(-MAX_PWM_VALUE, backwardsSpeed));

        // forward driving
        if (touchPosition.x >= 0) {
            if (touchPosition.y > 0) {
                return new CarControl(
                  backwardsSpeed,
                  forwardSpeed
              );
            } else {
                return new CarControl(
                  forwardSpeed,
                  backwardsSpeed
                );
            }
        // backwards driving
        } else if(touchPosition.x < -0.07) {
            return new CarControl(
                Math.Abs(forwardSpeed) * -1,
                Math.Abs(forwardSpeed) * -1
            );
        } else {
            return new CarControl();
        }
    }

    void DebugPrint() {
        Vector2 touchPosition = OVRInput.Get(OVRInput.Axis2D.PrimaryTouchpad);
        CarControl control = GetSpeedControl();

        string text = "";
        text += "Touch Position Forward: " + touchPosition.x + '\n';
        text += "Touch Position Left Right: " + touchPosition.y + '\n';

        text += "Any button down: " + Input.anyKey.ToString() + "\n";
        text += "Mouse button down: " + Input.GetMouseButtonDown(0).ToString() + '\n';
        text += "Mouse button back: " + Input.GetMouseButtonDown(1).ToString() + '\n';
        text += "Car Control Left: " + control.left + "\n";
        text += "Car Control Right: " + control.right;
        TextField.text = text;
    }
}


/*

const maxSpeedFactor = 25
const maxPWM = 100
const maxPWMSpeedFactor = 70
const minPWMSpeedFactor = 30


function calculateForwardBackwards(xIn, yIn) {
  const x = Math.abs(xIn)
  const y = Math.abs(yIn)
  
  const c = Math.sqrt(x*x + y*y)
  const alpha = Math.asin(x/c) * 180 / Math.PI

  const forwardSpeed = (c * maxPWMSpeedFactor / maxSpeedFactor) + minPWMSpeedFactor

  let backwardsSpeed = (alpha * forwardSpeed) / 90
  backwardsSpeed = (-forwardSpeed) + 2 * backwardsSpeed

  return {
    forward: Math.min(maxPWM, Math.max(-maxPWM, forwardSpeed)),
    backwards: Math.min(maxPWM, Math.max(-maxPWM, backwardsSpeed))
  }
}

const x = 1
const y = 1

let result = calculateForwardBackwards(x, y)
//console.log(result)

let res2 = {}

if(x > 0 && y > 0) {
  res2 = {
    left: result.forward,
    right: result.backwards
  }
} else if(x > 0 && y <= 0) {
  res2 = {
    left: result.backwards,
    right: result.forward
  }
} else if(x < -0.1) {
   res2 = {
    left: Math.abs(result.forward) * -1,
    right: Math.abs(result.forward) * -1,
  }
}

console.log("Direction:")
console.log(result)

 */
