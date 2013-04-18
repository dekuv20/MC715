package casamento;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.zookeeper.KeeperException;

public class TesteBarreiraDuplaMulher {
      /*Os argumentos sao: 

    args[0]:endereço do server zookeeper

    args[1]: tamanho do grupo de processos
     
    args[2]: numero da porta de comunicacao do socket
    */  
     public static void main(String[] args) {
          BarreiraDupla bd = new BarreiraDupla(args[0], "/b1", new Integer(args[1]));        
         Mulher mulher = null;
         try {
            mulher = new Mulher("Mulher"+bd.getBarrierCount(),
                    "SobrenomeMulher"+bd.getBarrierCount(), new Integer(args[2]));
        } catch (KeeperException | InterruptedException ex) {
            Logger.getLogger(Pessoa.class.getName()).log(Level.SEVERE, null, ex);
        }
        try{
            System.out.println("Mulher");
            boolean flag = bd.enter();
            if(!flag) {
                System.out.println("Erro ao entrar na Barreira Dupla");
            }
        } catch (KeeperException | InterruptedException e){
        }
       
        mulher.run(); 
        try{
            System.out.println(mulher.getNome() + " tentando sair da Barreira Dupla...");
            bd.leave();
            System.out.println(mulher.getNome() +" "+mulher.getSobrenome() + " saiu da Barreira Dupla");
        } catch (KeeperException | InterruptedException e){
            System.out.println("Erro ao sair da Barreira");
        }
        System.out.println("Terminou o teste da Barreira Dupla");
    }                  
}
