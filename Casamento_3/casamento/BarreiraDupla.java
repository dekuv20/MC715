package casamento;

import static casamento.SyncPrimitive.mutex;
import static casamento.SyncPrimitive.zk;
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
                        zk.create(root + "/processes", new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
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
        }
        
        int getBarrierCount() throws KeeperException, InterruptedException{
            return (zk.getChildren(root + "/processes", false).size());
        }
        
        boolean enter() throws KeeperException, InterruptedException{
            
            boolean semaforo = false;
            //Vai disparar watcher quando o node ready for criado
            try{
               while(!semaforo){//se semaforo for "true" ele prossegue
                synchronized(mutex){                 
                //se "/ready" nao existe ele seta um watcher nele, cria um node para si
                //e entra na barreira
                    Stat s = zk.exists(root + "/ready", false);
               if(s == null){
                    zk.exists(root + "/ready", true);
                   
           // O nome do node, hostname+ID unico, "synchronized" garante a unicidade do ID
            try {
                try {
                    name = InetAddress.getLocalHost().getCanonicalHostName().toString()+zk.getChildren(root+"/processes", false).size();
                } catch (        KeeperException | InterruptedException ex) {
                    Logger.getLogger(BarreiraDupla.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (UnknownHostException e) {
                System.out.println(e.toString());
            }
            String fullPath = root + "/processes/" + name;      
            //cria um node para o processo em questao      
                   System.out.println("cria o node "+fullPath);
                 zk.create(fullPath, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.EPHEMERAL);  
                 semaforo = true;
                 
               }else{
               //se "/ready" existe ele seta um watcher nele, nao cria um node para si
                 //e espera até ele deixar de existir
                   zk.exists(root + "/ready", true);
                   mutex.wait();                 
               }
                }
                }
               
            }catch(Exception e){
                return false;
            }
            
            while(true){
                synchronized(mutex){
                     List<String> list = zk.getChildren(root + "/processes", false);
                    if (list.size() < size) {
                        //espera evento do watcher
                        mutex.wait();
                    } else {
                        //dispara o watcher
                          Stat s = zk.exists(root + "/ready", false);
                             if(s == null){
                           zk.create(root + "/ready", new byte[0],
                                ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
                       }
                        return true;
                    }
                }         
            }
        }

        boolean leave() throws KeeperException, InterruptedException{
             String fullPath = root + "/processes/" + name;
                        
            while(true){
                synchronized(mutex){
                    List<String> list = zk.getChildren(root+ "/processes", false);              
                        if (list.isEmpty()) {                              
                            System.out.println("lista vazia, deleta node \"/ready\" e sai da barreira");
                            Stat s = zk.exists(root + "/ready", false);
                              if(s == null){              
                                   System.out.println("\"/ready\" nao existe ");
                            }else{
                                System.out.println("Deleta \"/ready\" ");
                                zk.delete(root + "/ready", -1);                                                     
                            }
                             System.out.println("Sai da barreira");
                           return true;                              
                        } else if((list.size() == 1)&&( list.get(0).equals(name))){  
                            System.out.println("so falta um elemento,"
                                    + " deleta "+fullPath+" e libera os outros");
                            zk.delete(fullPath, -1);  
                            
                        }else if( list.get(0).equals(name)){
                            System.out.println("Primeiro elemento da lista," +list.get(0)
                                    + " espera no ultimo " + list.get(list.size() - 1));
                            //espera no ultimo elemento 
                            zk.exists(root + "/processes/" + list.get(list.size() - 1),
                                    true);
                            mutex.wait();                                                   
                        }else{
                              Stat s = zk.exists(fullPath, false);
                               if(s != null){
                           //se for o ultimo elemento avisa o primeiro elemento   
                            //pelo watcher do exists
                                System.out.println(fullPath
                                        + " se deleta e espera no primeiro " + list.get(0));
                            zk.delete(fullPath, -1);  
                            //espera no primeiro elemento
                            zk.exists(root + "/processes/" + list.get(0),
                                    true);
                            mutex.wait();
                            }
                        }
                    }
                }
        }
    }