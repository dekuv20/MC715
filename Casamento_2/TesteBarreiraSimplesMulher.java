package casamento;

import org.apache.zookeeper.KeeperException;

public class TesteBarreiraSimplesMulher {
    /*   Os argumentos sao: 

    args[0]: endereço do zookeeper server(e.g., "zoo1.foo.com:2181")

    args[1]: se for "start" destroi a barreira e inicia os processos
     
    args[2]: numero da porta de comunicacao do socket, a porta do marido deve
    * casar com a porta da mulher com quem vai se casar
    */  
     public static void main(String[] args) {          
        Mulher mulher = null;
        BarreiraSimples bs = new BarreiraSimples(args[0], "/b2");
   
        int num = (int)Math.random();
        mulher = new Mulher("Mulher"+num,
                    "SobrenomeMulher"+num, new Integer(args[2]));           
        try{
            if(!"start".equals(args[1])){
              boolean flag = bs.barrier_wait();
             if(!flag) {
                 System.out.println("Erro ao esperar na Barreira Simples");
              }
            }else{
                bs.barrier_remove();
                System.out.println("Começa a execução!");
            }
         } catch (KeeperException | InterruptedException e){
         }             
        mulher.run();             
    }          
}
