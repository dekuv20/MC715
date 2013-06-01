package casamento;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;

    public class BarreiraDupla extends SyncPrimitive {
        int size;
        String name;

        BarreiraDupla(String address, String root, int size) {
            super(address);
            this.root = root;
            this.size = size;

            // cria o barrier node
            if (zk != null) {
                try {
                    Stat s = zk.exists(root, false);
                    if (s == null) {
                        zk.create(root, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
                                CreateMode.PERSISTENT);
                    }
                } catch (KeeperException e) {
                    System.out
                            .println("Keeper exception when instantiating queue: "
                                    + e.toString());
                } catch (InterruptedException e) {
                    System.out.println("Interrupted exception");
                }
            }

            // O nome do node, hostname+ID
            try {
                try {
                    name = InetAddress.getLocalHost().getCanonicalHostName().toString()+zk.getChildren(root, false).size();
                } catch (        KeeperException | InterruptedException ex) {
                    Logger.getLogger(BarreiraDupla.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (UnknownHostException e) {
                System.out.println(e.toString());
            }

        }
        
        int getBarrierCount() throws KeeperException, InterruptedException{
            return (zk.getChildren(root, false).size());
        }
        
        boolean enter() throws KeeperException, InterruptedException{
            String fullPath = root + "/" + name;
            //Vai disparar watcher quando o node ready for criado
            try{
                zk.exists(root + "/ready", true);
                 zk.create(fullPath, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.EPHEMERAL);  
           
            }catch(Exception e){
                return false;
            }
            //cria um node para o processo em questao                      
            if(zk.exists(fullPath, false) != null){
                System.out.println(" entrou na barreira dupla /b1");
            }else{
                System.out.println(" nao entrou na barreira");
            }
            while(true){
                synchronized (mutex) {
                     List<String> list = zk.getChildren(root, false);
                    if (list.size() < size) {
                        //espera evento do watcher
                        mutex.wait();
                    } else {
                        //dispara o watcher
                        zk.create(root + "/ready", new byte[0],
                                ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                        return true;
                    }
                }         
            }
        }

        boolean leave() throws KeeperException, InterruptedException{
             String fullPath = root + "/" + name;
                        
            while (true) {
                synchronized (mutex) {
                    List<String> list = zk.getChildren(root, false);              
                        if (list.isEmpty()) {
                             System.out.println("lista vazia, sai da barreira");
                           return true;                          
                        } else if((list.size() == 1)&&( list.get(0).equals(name))){                            
                            zk.delete(fullPath, 0);  
                            System.out.println("só falta um elemento,"
                                    + " deleta e libera os outros");
                        }else if( list.get(0).equals(name)){
                            System.out.println("Primeiro elemento da lista,"
                                    + " espera no ultimo");
                            //espera no ultimo elemento 
                            zk.exists(root + "/" + list.get(list.size() - 1),
                                    true);
                            mutex.wait();                                                   
                        }else{
                            if(zk.exists(fullPath, false) != null){
                           //se for o ultimo elemento avisa o primeiro elemento   
                            //pelo watcher do exists
                           
                            zk.exists(root + "/" + list.get(0),
                                    true);
									
									 zk.delete(fullPath, 0);  
                            //espera no primeiro elemento
                            mutex.wait();
                            }
                        }
                    }
                }
        }
    }