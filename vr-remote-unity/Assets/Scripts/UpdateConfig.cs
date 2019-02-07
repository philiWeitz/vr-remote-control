using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class UpdateConfig : MonoBehaviour
{

    public InputField SendHeadRoationInputField;
    public InputField HeadRotationMinAngleInputField;
    public InputField ExternalTextureUpdateInputField;

    void Start()
    {
        if (null != SendHeadRoationInputField) {
            SendHeadRoationInputField.text = 
                Config.config.sendHeadRotationInterval.ToString();
        }
        if (null != HeadRotationMinAngleInputField)
        {
            HeadRotationMinAngleInputField.text =
                Config.config.headRotationMinAngle.ToString();
        }
        if (null != ExternalTextureUpdateInputField)
        {
            ExternalTextureUpdateInputField.text =
                Config.config.externalTextureUpdateInterval.ToString();
        }
    }

    public void UpdateSendHeadRotationInterval() {
        Config.config.sendHeadRotationInterval =
            int.Parse(SendHeadRoationInputField.text);
    }

    public void UpdateHeadRotationMinAngle()
    {
        Config.config.headRotationMinAngle =
            float.Parse(HeadRotationMinAngleInputField.text);
    }

    public void UpdateExternalTextureUpdateInterval()
    {
        Config.config.externalTextureUpdateInterval =
            int.Parse(ExternalTextureUpdateInputField.text);
    }
}
