package remote.vr.com.remote_android.serial;

public class CarControlModel {

    public int left = 0;
    public int right = 0;

    public String toSerial() {
        return "CAR," + left + "," + right + "$";
    }
}
