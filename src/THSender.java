import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by albertllauradomiralles
 */
public class THSender extends Thread {

    private Transaction t;
    private InetAddress ha;
    private DatagramSocket s;

    public THSender(Transaction t, InetAddress ha, DatagramSocket s, int id) {
        Data d = new Data(t.getData().getVersion(), t.getData().getInfo());
        this.t = new Transaction(t.getType(), d, id);
        this.ha = ha;
        this.s = s;
    }

    public void run() {
        Node.sendTransaction(this.t, this.ha, this.s);
    }

}
