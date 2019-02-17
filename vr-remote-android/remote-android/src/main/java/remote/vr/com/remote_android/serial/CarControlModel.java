package remote.vr.com.remote_android.serial;

public class CarControlModel {

    public String direction = "";

    public String toSerial() {
        if(direction.equals("FORWARD")) {
            return "CAR,80,80";
        } else if(direction.equals("BACKWARDS")) {
            return "CAR,-80,-80";
        } else if(direction.equals("LEFT")) {
            return "CAR,-80,80";
        } else if(direction.equals("RIGHT")) {
            return "CAR,80,-80";
        } else {
            return "CAR,0,0";
        }
    }
}
