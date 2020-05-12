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
public class Client {

    public static final int PORT = 4100;

    public static void main(String[] args) throws Exception {

        String file = "client_transactions.txt";
        ArrayList<Transaction> transactions = readTransactionsFile(file);

        InetAddress hostAddress = InetAddress.getByName("localhost");
        DatagramSocket s = new DatagramSocket(PORT);

        for (Transaction t : transactions){
            Node.sendTransaction(t, hostAddress, s);
            System.out.println("T: type(" + t.getType() + ") \t id [" + t.getId_dst() + "]");
            if (t.getType() == Transaction.TYPE_R){
                try {
                    byte[] buf = new byte[1000];
                    DatagramPacket dgp = new DatagramPacket(buf, buf.length);
                    s.receive(dgp);

                    ByteArrayInputStream bis = new ByteArrayInputStream(dgp.getData());
                    ObjectInputStream ois = new ObjectInputStream(bis);
                    Data d = (Data)ois.readObject();
                    System.out.println("\tRead: Data (v{" + d.getVersion() + "}, i{" + Arrays.toString(d.getInfo()) + "})");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            TimeUnit.MILLISECONDS.sleep(2000);
        }
    }

    public static ArrayList readTransactionsFile(String file){
        ArrayList<Transaction> t = new ArrayList();

        int layer;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                layer = 0;
                //System.out.println(line);
                String[] transactions = line.split(", ");
                for (String s : transactions) {
                    //System.out.println(s);
                    int node;
                    Transaction tr;
                    Data d;
                    switch (s.charAt(0)){
                        case 'b':
                            if (s.length() > 1){ // Layer?
                                layer = Integer.parseInt("" + s.charAt(1));
                            }
                            //System.out.println("--- [Start] ---");
                            break;
                        case 'r':
                            String[] sR = s.split("\\(");
                            node = Integer.parseInt(sR[1].split("\\)")[0]);
                            d = Data.EMPTY_DATA;
                            tr = new Transaction(Transaction.TYPE_R, d, Node.calculateID(layer, node));
                            t.add(tr);
                            //System.out.println("R: N(" + layer + ", " + node + ")");
                            break;
                        case 'w':
                            String[] sW = s.split("\\(");
                            String[] w = (sW[1].split("\\)")[0].split(","));
                            node = Integer.parseInt(w[0]);
                            int index = Integer.parseInt(w[1]);
                            int info = Integer.parseInt(w[2]);
                            tr = new Transaction(Transaction.TYPE_W, index, info, Node.calculateID(layer, node));
                            t.add(tr);
                            //System.out.println("W: N(" + layer + ", " + node + "), " + info);
                            break;
                        case 'c':
                            //System.out.println("---- [End] ----");
                            break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return t;
    }

    /*public static void sendTransaction(Transaction t, InetAddress hostAddress, DatagramSocket s, int id){
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(6400);
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(t);
            byte[] data = baos.toByteArray();

            int PORT = PORT_base + id;

            DatagramPacket packet = new DatagramPacket(data, data.length, hostAddress, PORT);
            s.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
}
