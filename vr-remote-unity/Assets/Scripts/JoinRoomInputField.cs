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
        PlayerPrefs.SetString("roomId", input.text);
        UnityEngine.SceneManagement.SceneManager.LoadScene(1);
    }

    public void FocusInputField() {
        Debug.Log("ddddddddddd");
        input.ActivateInputField();
    }
}