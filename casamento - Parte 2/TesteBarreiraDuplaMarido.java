/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package casamento;

import casamento.Pessoa.Listen;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.zookeeper.KeeperException;

public class TesteBarreiraDuplaMarido{
          /*Os argumentos sao: 

    args[0]:endereço do server zookeeper

    args[1]: tamanho do grupo de processos
     
    args[2]: numero da porta de comunicacao do socket
    */  
     public static void main(String[] args) {          
     
         BarreiraDupla bd = new BarreiraDupla(args[0], "/b1", new Integer(args[1]));        
         Pessoa marido = null;
        try {
            marido = new Pessoa("Marido"+bd.getBarrierCount(),
                    "SobrenomeMarido"+bd.getBarrierCount(), new Integer(args[2]));
        } catch (KeeperException | InterruptedException ex) {
            Logger.getLogger(Pessoa.class.getName()).log(Level.SEVERE, null, ex);
        }
        try{
            System.out.println("Marido");
            boolean flag = bd.enter(); 
            if(!flag) {
                System.out.println("Erro ao entrar na Barreira Dupla");
            }
        } catch (KeeperException | InterruptedException e){
            e.printStackTrace();
        }
        Listen listen = new Listen(marido.getNome(), marido.getSobrenome(), marido.getPort());
        marido.run(listen); 
        try{
            System.out.println(marido.getNome() + " tentando sair da Barreira Dupla...");
            bd.leave();
            marido.stopListening(listen);
            System.out.println(marido.getNome() + " saiu da Barreira Dupla");
        } catch (KeeperException | InterruptedException e){
            System.out.println("Erro ao sair da Barreira");
        }
        System.out.println("Terminou o teste da Barreira Dupla");
    }      
     
}
