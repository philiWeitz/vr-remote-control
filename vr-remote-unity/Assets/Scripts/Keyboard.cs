using System;
using UnityEngine;
using UnityEngine.UI;

public class Keyboard : MonoBehaviour
{
    public string Character;

    public InputField RoomInputField;

    private double timeLastExecution = 0;

    public void OnRoomInputFieldClick() {
        double now = (DateTime.Now - new DateTime(1970, 1, 1)).TotalMilliseconds;

        if (timeLastExecution < now)
        {
            timeLastExecution = now + 200;

            Debug.Log("'" + Character + "' was clicked");
            RoomInputField.text = RoomInputField.text + Character;
        }
    }

    public void OnDeleteClick() {
        double now = (DateTime.Now - new DateTime(1970, 1, 1)).TotalMilliseconds;

        if (timeLastExecution < now)
        {
            timeLastExecution = now + 200;

            if (RoomInputField.text.Length > 0)
            {
                RoomInputField.text =
                    RoomInputField.text.Remove(RoomInputField.text.Length - 1, 1);
            }
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
