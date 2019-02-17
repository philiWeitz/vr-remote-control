using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class GearVrRoomSelection : MonoBehaviour
{
    public InputField roomInputField;

    public int NextSceneId = 1;


    // Start is called before the first frame update
    void Start()
    {
        roomInputField.text = PlayerPrefs.GetString("roomId");
    }

    // Update is called once per frame
    void Update()
    {

    }

    public void ChangeRoom() {
        string roomId = roomInputField.text;

        if (string.IsNullOrEmpty(roomId))
        {
            roomId = "autoRoom1";
        }

        PlayerPrefs.SetString("roomId", roomId);
        UnityEngine.SceneManagement.SceneManager.LoadScene(NextSceneId);
    }
}
