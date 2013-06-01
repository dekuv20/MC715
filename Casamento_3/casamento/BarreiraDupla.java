package casamento;

import static casamento.SyncPrimitive.zk;
import java.util.List;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;

    public class BarreiraDupla extends SyncPrimitive {
        int size;
        String name;

     public BarreiraDupla(String address, String root, int size) {
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
        
       public int getBarrierCount() throws KeeperException, InterruptedException{
            return (zk.getChildren(root + "/processes", false).size());
        }
        
        //por algum motivo nao ta sincronizando perfeitamente, alguns levantam excessao
       public synchronized void destroy() throws InterruptedException, KeeperException{
            Stat barr = zk.exists(root, false);
               if(barr != null){             
                    if(getBarrierCount()==0){
                    zk.delete(this.root + "/processes", -1);
                     Stat ready = zk.exists(root + "/ready", false);
                       if(ready != null){
                            zk.delete(root + "/ready", -1);
                       }
                    zk.delete(this.root, -1);              
                    }
               }
        }
        
       public boolean enter() throws KeeperException, InterruptedException{
            
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
       
            String fullPath = root + "/processes/node_";      
            //cria um node para o processo em questao                      
                name = zk.create(fullPath, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.PERSISTENT_SEQUENTIAL); 
                fullPath = name;
                name = fullPath.split("/")[fullPath.split("/").length - 1];
                System.out.println("cria o node "+name); 
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
                                ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                       }
                        return true;
                    }
                }         
            }
        }

       public boolean leave() throws KeeperException, InterruptedException{
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
							
							 //espera no primeiro elemento
                            zk.exists(root + "/processes/" + list.get(0),
                                    true);
									
                                System.out.println(fullPath
                                        + " se deleta e espera no primeiro " + list.get(0));
                            zk.delete(fullPath, -1);  
                           
                            mutex.wait();
                            }
                        }
                    }
                }
        }
    }