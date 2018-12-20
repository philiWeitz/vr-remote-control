package remote.vr.com.remote_android.serial;


public class HeadRotationModel {
    public int vertical;
    public int horizontal;


    public int getVertical() {
        return vertical;
    }

    public void setVertical(int vertical) {
        this.vertical = vertical;
    }

    public int getHorizontal() {
        return horizontal;
    }

    public void setHorizontal(int horizontal) {
        this.horizontal = horizontal;
    }

    public String toSerial() {
        return "HEAD," + this.horizontal + "," + this.vertical + "$";
    }
}
