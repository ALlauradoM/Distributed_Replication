import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by albertllauradomiralles
 */
public class Data implements Serializable {

    public static final int EMPTY_VERSION = -1;
    public static final int ERROR_VERSION = -2;

    public static final Data DATA_ERROR = new Data(ERROR_VERSION,null);
    public static final Data EMPTY_DATA= new Data(EMPTY_VERSION,null);

    private int version;
    private int[] info;

    public Data() {
        this.version = 0;
        this.info = new int[10];
    }

    public Data(int[] info) {
        this.version = EMPTY_VERSION;
        this.info = info;
    }

    public Data(int version, int[] info) {
        this.version = version;
        this.info = info;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int[] getInfo() {
        return info;
    }

    public void setInfo(int[] info) {
        this.info = info;
    }

    public void setInfoAt(int index, int data){
        this.info[index] =  data;
    }
}
