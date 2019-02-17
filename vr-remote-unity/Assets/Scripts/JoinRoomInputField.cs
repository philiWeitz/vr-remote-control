using UnityEngine;
using UnityEngine.XR;
using UnityEngine.UI;

public class JoinRoomInputField : MonoBehaviour {

    public InputField input;

    // Use this for initialization
    void Start () {
        input.text = PlayerPrefs.GetString("roomId");
        input.keyboardType = TouchScreenKeyboardType.Default;

        // Disable VR for input
        XRSettings.enabled = false;
    }

    public void ChangeScene() {
        string roomId = input.text;

        if (string.IsNullOrEmpty(roomId)) {
            roomId = "autoRoom1";
        }

        XRSettings.enabled = true;
        PlayerPrefs.SetString("roomId", roomId);
        UnityEngine.SceneManagement.SceneManager.LoadScene(2);
    }

}