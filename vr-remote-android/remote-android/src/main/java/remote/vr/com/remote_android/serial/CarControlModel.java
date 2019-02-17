package remote.vr.com.remote_android.serial;

public class CarControlModel {

    public String direction;

    public String toSerial() {
        if(direction.equals("FORWARD")) {
            return "CAR,80,80";
        } else {
            return "CAR,0,0";
        }
    }
}
