using UnityEngine;
using UnityEngine.UI;

public class JoinRoomInputField : MonoBehaviour {

    private InputField input;

    // Use this for initialization
    void Start () {
        input = this.GetComponent<InputField>();
        input.text = PlayerPrefs.GetString("roomId");
        input.keyboardType = TouchScreenKeyboardType.Default;
    }

    public void ChangeScene() {
        string roomId = input.text;

        if (string.IsNullOrEmpty(roomId)) {
            roomId = "autoRoom1";
        }

        PlayerPrefs.SetString("roomId", roomId);
        UnityEngine.SceneManagement.SceneManager.LoadScene(1);
    }

    public void FocusInputField() {
        input.ActivateInputField();
    }
}