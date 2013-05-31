package casamento;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.zookeeper.KeeperException;

public class TesteBarreiraDuplaMulher {
      /*Os argumentos sao: 

    args[0]:endereço do server zookeeper

    args[1]: tamanho do grupo de processos
     
    args[2]: numero da porta de comunicacao do socket
     
    args[3]: numero total de vezes que a mulher casa ou separa
    */  
     public static void main(String[] args) {
         int n = new Integer(args[3]);
          BarreiraDupla bd = new BarreiraDupla(args[0], "/b1", new Integer(args[1]));        
         Mulher mulher = null;
         try {
            mulher = new Mulher("Mulher"+bd.getBarrierCount(),
                    "SobrenomeMulher"+bd.getBarrierCount(), new Integer(args[2]));
        } catch (KeeperException | InterruptedException ex) {
            Logger.getLogger(Pessoa.class.getName()).log(Level.SEVERE, null, ex);
        }
       
      ////////////////////////////////////////////////////////////////////////
      ///////////////////////////////EXECUTA /////////////////////////////////
      ////////////////////////////////////////////////////////////////////////         
       for(int k =1; k<(n+1); k++){   
            System.out.println("Loop numero " +k +"/"+n);
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
       }
        System.out.println("Terminou o teste da Barreira Dupla");
    }                  
}
