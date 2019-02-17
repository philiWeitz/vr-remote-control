using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class Keyboard : MonoBehaviour
{
    public string Character;

    public InputField RoomInputField;


    public void OnRoomInputFieldClick() {
        Debug.Log("'" + Character + "' was clicked");
        RoomInputField.text = RoomInputField.text + Character;
    }

    public void OnDeleteClick() {
        if (RoomInputField.text.Length > 0)
        {
            RoomInputField.text =
                RoomInputField.text.Remove(RoomInputField.text.Length - 1, 1);
        }
    }

    // Start is called before the first frame update
    void Start()
    {

    }

    // Update is called once per frame
    void Update()
    {
        
    }
}
