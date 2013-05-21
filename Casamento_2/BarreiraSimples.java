package casamento;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;

    public class BarreiraSimples extends SyncPrimitive{
        String name;

        BarreiraSimples(String address, String root) {
            super(address);
            this.root = root;

            // Create barrier node
            if (zk != null) {
                try {
                    Stat s = zk.exists(root, false);
                    if (s == null) {
                        zk.create(root, ByteBuffer.allocate(4).putInt(0).array(),
                                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                                CreateMode.PERSISTENT);
                        
                    }
                } catch (KeeperException e) {
                    System.out
                            .println("KeeperException: "
                                    + e.toString());
                } catch (InterruptedException e) {
                    System.out.println("Interrupted exception");
                }
            }
            
             // My node name
            try {
                name = InetAddress.getLocalHost().getCanonicalHostName().toString();
            } catch (UnknownHostException e) {
                System.out.println(e.toString());
            }
        }
        
    
        
        void barrier_remove() throws InterruptedException, KeeperException{
            zk.delete(root, 0);  
        }

        boolean barrier_wait() throws KeeperException, InterruptedException{
            while(true){
                synchronized (mutex) {
                 if(zk.exists(root, true)== null){
                     System.out.println("Barreira removida, prosseguindo...");
                     return true;
                }else{
                     System.out.println("Esperando na barreira...");
                mutex.wait();
             }
              }
            }
        }
    }