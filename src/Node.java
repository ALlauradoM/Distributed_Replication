import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Created by albertllauradomiralles
 */
public class Node {

    public static final int LAYER_CORE = 0;
    public static final int LAYER_1 = 1;
    public static final int LAYER_2 = 2;

    // ----------------------- CONSTANTS --------------------------
    public static final int[] ID_LC = {0, 1, 2};
    public static final int[] ID_L1 = {3, 4};
    public static final int[] ID_L2 = {5, 6};

    public static final int[][] CONN_LC_L1 = {{1, 3},{2, 4}};
    public static final int[][] CONN_L1_L2 = {{4, 5}, {4, 6}};
    // ------------------------------------------------------------

    public static final int PORT_base = 4000;

    public static void main(String[] args) throws Exception { // args = String[id, Layer]
        if (args.length < 2) {
            System.out.println("--- Not a valid execution! ---");
            return;
        }

        int layer = Integer.parseInt(args[0]);
        int id = Integer.parseInt(args[1]);
        int version = 0;
        int[] info = new int[10];
        int num_transactions = 0;

        if (layer != Node.LAYER_CORE && layer != Node.LAYER_1 && layer != Node.LAYER_2){
            System.out.println("--- Not a valid layer! ---");
            return;
        }

        int PORT = PORT_base + id;
        InetAddress hostAddress = InetAddress.getByName("localhost");
        DatagramSocket s = new DatagramSocket();
        Transaction t;

        DatagramSocket sk = new DatagramSocket(PORT);

        long startTime = System.nanoTime();
        double last_time = 0;

        BufferedWriter bw = null;

        String filename = "log_n"+id+".txt";
        System.out.println("[PORT " + PORT + ": N(" + layer + "," + id + ") up]");

        while (true) {
            t = readTransaction(sk);
            System.out.println("Transaction(" + num_transactions + ") - t[" + t.getType() + "]");

            String str;
            switch (t.getType()){
                case Transaction.TYPE_D:
                    num_transactions++;
                    // Update Version!
                    version = t.getData().getVersion();
                    t.getData().setVersion(version);
                    info = t.getData().getInfo();
                    //System.out.println("\t[Updated info: " + info + "]");
                    str = "[Version: " + version + ", Info: " + Arrays.toString(info) + "]";
                    System.out.println(str);
                    try {
                        // APPEND MODE SET HERE
                        bw = new BufferedWriter(new FileWriter(filename, true));
                        bw.write(str);
                        bw.newLine();
                        bw.flush();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    } finally {                       // always close the file
                        if (bw != null) try {
                            bw.close();
                        } catch (IOException ioe2) {
                            // just ignore it
                        }
                    }
                    break;
                case Transaction.TYPE_W:
                    num_transactions ++;
                    // Update Version!
                    version++;
                    info[t.getIndex()] = t.getValue();
                    System.out.println("TYPE_W value: " + t.getValue());
                    //t.getData().setVersion(version);
                    //System.out.println("\t[Updated info: " + info + "]");
                    str = "[Version: " + version + ", Info: " + Arrays.toString(info) + "]";
                    System.out.println(str);
                    try {
                        // APPEND MODE SET HERE
                        bw = new BufferedWriter(new FileWriter(filename, true));
                        bw.write(str);
                        bw.newLine();
                        bw.flush();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    } finally {                       // always close the file
                        if (bw != null) try {
                            bw.close();
                        } catch (IOException ioe2) {
                            // just ignore it
                        }
                    }
                    break;
                case Transaction.TYPE_R:
                    //num_transactions ++;
                    Data d = new Data(version, info);
                    //System.out.println("\t[Sending data: v" + d.getVersion() + ", " + d.getInfo() + "]");
                    sendData(d, hostAddress, s, 4100);
                    break;
            }

            switch (layer){
                case LAYER_CORE:
                    if (t.getType() == Transaction.TYPE_W) { // Tipus que indica que NO ve d'un altre Node [evitem rebots infinits]
                        Transaction tr = new Transaction(Transaction.TYPE_D, new Data(version, info), 0);
                        updateCoreLayer(id, tr, hostAddress, s); // Update the layer!
                    }

                    if ((num_transactions % 10) == 0) { // Cada 10 actualitzacions de la variable
                        THSender sender;
                        for (int[] conn : CONN_LC_L1) { // Send down!
                            //System.out.println("conn[0]=" + conn[0] + "\tconn[1]=" + conn[1]);
                            if (id == conn[0]){
                                t.setType(Transaction.TYPE_D);
                                System.out.println("[Sending down(t" + t.getType() +", v" + version + "): conn[0]=" + conn[0] + "\tconn[1]=" + conn[1]);
                                sender = new THSender(t, hostAddress, s, conn[1]);
                                sender.start();
                            }
                        }
                    }
                    break;

                case LAYER_1:
                    double time = (double)(System.nanoTime() - startTime) / 1000000000.0;
                    double elapsed_time = time - last_time;
                    //System.out.println("[t = " + elapsed_time + "s] \t {" + time + " - " + last_time + "}");
                    if (elapsed_time >= 10) { // Cada 10 segons
                        last_time = time;
                        THSender sender;
                        for (int[] conn : CONN_L1_L2) { // Send down!
                            //System.out.println("conn[0]=" + conn[0] + "\tconn[1]=" + conn[1]);
                            if (id == conn[0]) {
                                t.setType(Transaction.TYPE_D);
                                //t.setId_dst(conn[1]);
                                System.out.println("[Sending down(t" + t.getType() +", v" + version + "): conn[0]=" + conn[0] + "\tconn[1]=" + conn[1]);
                                sender = new THSender(t, hostAddress, s, conn[1]);
                                sender.start();
                            }
                        }
                    }
                    break;
            }
        }
    }

    public static Transaction readTransaction(DatagramSocket sk){
        try {
            byte[] buf = new byte[1000];
            DatagramPacket dgp = new DatagramPacket(buf, buf.length);
            sk.receive(dgp);

            ByteArrayInputStream bis = new ByteArrayInputStream(dgp.getData());
            ObjectInputStream ois = new ObjectInputStream(bis);
            Transaction t = (Transaction) ois.readObject();
            return  t;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Transaction.ERROR_TRANSACTION;
    }

    public static void updateCoreLayer(int sender_id, Transaction t, InetAddress hostAddress, DatagramSocket s){
        //System.out.println("\t[Updating Core Layer...]");
        t.setType(Transaction.TYPE_D);
        for (int id: ID_LC) {
            if (sender_id != id) {
                //System.out.println("\t Updating N(0," + id + ")");
                t.setId_dst(id);
                sendTransaction(t, hostAddress, s);
            }
        }
    }

    public static void sendTransaction(Transaction t, InetAddress hostAddress, DatagramSocket s){
        try {
            //System.out.println("\tST{t" + t.getType() + "\tv" + t.getData().getVersion() + "\tn" + t.getId_dst() + "}");
            ByteArrayOutputStream baos = new ByteArrayOutputStream(6400);
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(t);
            byte[] data = baos.toByteArray();

            int PORT = PORT_base + t.getId_dst();

            DatagramPacket packet = new DatagramPacket(data, data.length, hostAddress, PORT);
            s.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendData(Data d, InetAddress hostAddress, DatagramSocket s, int port){
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(6400);
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(d);
            byte[] data = baos.toByteArray();

            DatagramPacket packet = new DatagramPacket(data, data.length, hostAddress, port);
            s.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int calculateID(int layer, int node){
        int res = 0;
        res += node;
        if (layer == LAYER_1 || layer == LAYER_2)
            res += ID_LC.length;
        if (layer == LAYER_2)
            res += ID_L1.length;
        return res;
    }
}
