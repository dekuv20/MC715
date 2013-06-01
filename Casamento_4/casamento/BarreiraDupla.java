package casamento;

import static casamento.SyncPrimitive.zk;
import java.util.Arrays;
import java.util.List;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;

    public class BarreiraDupla extends SyncPrimitive {
        int size;
        String name;
        String queueName;

     public BarreiraDupla(String address, String root, int size) {
            super(address);
            this.root = root;
            this.size = size;

            // cria o barrier node
            if (zk != null) {
                try {
                    Stat s = zk.exists(root, false);
                    if (s == null) {
                       //cria o node raiz da barreira
                        zk.create(root, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
                                CreateMode.PERSISTENT);
                        //cria o node dos processos que estao na barreira
                        zk.create(root + "/processes", new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
                                CreateMode.PERSISTENT);
                        //cria o node dos processos que estao esperando para entrar na barreira
                        zk.create(root + "/queue", new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
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
                   
                   //deleta todos os nodes de todos os processos  
                   for(String s : zk.getChildren(this.root + "/processes", false)){    
                        zk.delete(this.root + "/processes" + s, -1);
                    }
                     zk.delete(this.root + "/processes", -1);
                  
                     //deleta a fila e todos os nodes dela
                    for(String s : zk.getChildren(this.root + "/queue", false)){
                        zk.delete(root+"/queue/" +s, -1);
                    }                       
                    zk.delete(this.root + "/queue", -1);
                    
                    //deleta o node ready se ele existir 
                    Stat ready = zk.exists(root + "/ready", false);
                       if(ready != null){
                            zk.delete(root + "/ready", -1);
                       }
                      
                    //deleta o node raiz   
                    zk.delete(this.root, -1);              
                    }
               
        }
        
       public boolean enter() throws KeeperException, InterruptedException{
           //faz fila na entrada e deixa passar apenas o tamanho da barreira
           //por vez
           String queuePath = root + "/queue/queue_";
            //cria um node para o processo em questao                      
                queueName = zk.create(queuePath, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.EPHEMERAL_SEQUENTIAL); 
                queuePath = queueName;
                queueName = queuePath.split("/")[queuePath.split("/").length - 1];
                System.out.println("cria o node na queue "+queueName);  
                int queueNumber = Integer.parseInt(queueName.split("_")[1]);
            boolean semaforo = false;
            //Vai disparar watcher quando o node ready for criado
            try{
               while(!semaforo){//se semaforo for "true" ele prossegue
                synchronized(mutex){                 
               
                    //O semaforo permite entrar na barreira somente se ele for um dos
                    //"size" primeiros elementos da fila cuja ordem eh definida
                    //pelos numeros sequenciais crescentes.
               Stat s = zk.exists(root + "/ready", false);
               if(s == null){                 
                    List<String> list = zk.getChildren(root + "/queue", false);
                    int[] fila = new int[list.size()];
                    int i = 0;
                    for(String str: list){
                        fila[i] = Integer.parseInt(str.split("_")[1]);
                        i++;
                    }
                    Arrays.sort(fila);
                    boolean condition = false;
                    if(size>fila.length){
                         condition = true;
                    }else{
                        for(int k = 0; k < size; k++){
                            if(queueNumber == fila[k]){
                                condition = true;
                            }
                        }                      
                    }
                   if(condition){
                    semaforo = true;
                   }else{
                       System.out.println("Espera na fila...");
                       zk.exists(root + "/ready", true);
                       mutex.wait();   
                   }
               }else{
               //se "/ready" existe ele seta um watcher nele, nao cria um node para si
                 //e espera até ele deixar de existir
                   System.out.println("Espera o ready ser deletado...");
                   zk.exists(root + "/ready", true);
                   mutex.wait();                 
               }
                }
                }
               //seta o watcher e cria o node para o processo
                zk.exists(root + "/ready", true);     
                String fullPath = root + "/processes/proc_";                           
                name = zk.create(fullPath, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.EPHEMERAL_SEQUENTIAL); 
                fullPath = name;
                name = fullPath.split("/")[fullPath.split("/").length - 1];
                System.out.println("cria o node "+name); 
               
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
                                    + " deleta " + fullPath + " e libera os outros");
                            zk.delete(fullPath, -1);  
                            
                            System.out.println("Deleta " + queueName);
                            zk.delete(root + "/queue/" + queueName, -1);  
                            
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
                            
                            System.out.println("Deleta " + queueName);
                            zk.delete(root + "/queue/" + queueName, -1);
                            
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