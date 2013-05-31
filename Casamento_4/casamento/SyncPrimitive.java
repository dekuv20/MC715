package casamento;

import java.io.IOException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

public class SyncPrimitive implements Watcher {

    static ZooKeeper zk = null;
    static final Integer mutex = new Integer(-1);;
  //  static final Integer mutex2 = new Integer(-1);;
  //  static final Integer mutex3 = new Integer(-1);;
    String root;

    SyncPrimitive(String address) {
        if(zk == null){
            try {
                System.out.println("Starting ZK:");
                zk = new ZooKeeper(address, 3000, this);          
                System.out.println("Finished starting ZK: " + zk);
            } catch (IOException e) {
                System.out.println(e.toString());
                zk = null;
            }
        }
    }

    @Override
    synchronized public void process(WatchedEvent event) {
        synchronized (mutex) {
            mutex.notifyAll();
        }
    }
}