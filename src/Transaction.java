import java.io.Serializable;

/**
 * Created by albertllauradomiralles on 1/1/18.
 */
public class Transaction implements Serializable {

    public static final int TYPE_D = 0;
    public static final int TYPE_R = 1;
    public static final int TYPE_W = 2;
    public static final int TYPE_E = -1;
    public static final Transaction ERROR_TRANSACTION = new Transaction(TYPE_E, Data.DATA_ERROR, -1);

    private int type;
    private Data data;
    private int index;
    private int value;
    private int id_dst;

    public Transaction(int type, int index, int value, int id_dst) {
        this.type = type;
        this.index = index;
        this.value = value;
        this.id_dst = id_dst;
    }

    public Transaction(int type, Data data, int id_dst) {
        this.type = type;
        this.data = data;
        this.id_dst = id_dst;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getId_dst() {
        return id_dst;
    }

    public void setId_dst(int id_dst) {
        this.id_dst = id_dst;
    }
}
