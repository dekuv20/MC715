package casamento;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.zookeeper.KeeperException;

//classe referente ao processo que muda o nome quando casa
public class Mulher extends Pessoa{
    public Mulher(String nome, String sobrenome){
        super(nome, sobrenome);
    }
    //executa o processo
    @Override
    public void run(){        
        //pergunta para o marido o sobrenome dele usando UDP
      DatagramSocket clientSocket = null;
        try {
            clientSocket = new DatagramSocket();
        } catch (SocketException ex) {
            Logger.getLogger(Mulher.class.getName()).log(Level.SEVERE, null, ex);
        }
      InetAddress IPAddress = null;
        try {
            IPAddress = InetAddress.getByName("localhost");
            System.out.println("IP: "+IPAddress);
        } catch (UnknownHostException ex) {
            Logger.getLogger(Mulher.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("erro ao setar IP para o envio");
        }
      byte[] sendData = new byte[1024];
      byte[] receiveData = new byte[1024];
      String sentence = "Qual o seu sobrenome?";
      sendData = sentence.getBytes();
      DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9879);
        try {
            for(int i=0;i<10;i++){
            System.out.println("enviando pacote " + i +" ..." );
            clientSocket.send(sendPacket);
            System.out.println("Enviou pacote "+i+"!");
                try {
                    Mulher.sleep(4);
                } catch (InterruptedException ex) {
                    System.out.println("Erro ao dar sleep na mulher");
                    Logger.getLogger(Mulher.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Mulher.class.getName()).log(Level.SEVERE, null, ex);
        }
      DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        try {
            System.out.println("esperando resposta do Marido");
            clientSocket.receive(receivePacket);
        } catch (IOException ex) {
            Logger.getLogger(Mulher.class.getName()).log(Level.SEVERE, null, ex);
        }
      String sobrenomeMarido = new String(receivePacket.getData());
      System.out.println("Resposta do Marido:" + sobrenomeMarido);
      clientSocket.close();   
      
      //casa e muda o nome para nome de casada
        this.casar(sobrenomeMarido);
        System.out.println("Casou e mudou o nome para "+this.getNome()+" "
                +this.getSobrenome()+"!");
    }
    
    //muda o estado civil 
    public void casar(String sobrenomeMarido){
        if("SOLTEIRO".equals(this.getEstadoCivil()) || "VIUVO".equals(this.getEstadoCivil())){
            this.setSobrenome(sobrenomeMarido);
            this.setEstadoCivilCasado();
        }else{
            System.out.println("Essa mulher já é casada");
        }
    }
    
     /*A tarefa Pessoa inicia quando o main é invocado, dai ela espera na barreira
    até que o numero em args[2] seja completado
    os argumentos sao: 

    args[1]: the address of a ZooKeeper server (e.g., "zoo1.foo.com:2181")

    "/b1": the path of the barrier node on ZooKeeper (e.g., "/b1")

    args[2]: the size of the group of processes
    */
     public static void main(String[] args) {
          Barrier b = new Barrier(args[0], "/b1", new Integer(args[1]));
          Mulher mulher = new Mulher("Selena","Kyle");
        try{
            boolean flag = b.enter();
            System.out.println(mulher.getNome()+" "+mulher.getSobrenome()+" entrou na barreira: " + args[1]);
            if(!flag) {
                System.out.println("Erro ao entrar na barreira");
            }
        } catch (KeeperException | InterruptedException e){
        }
        //pode fazer um if aqui de um args para pegar o nome
        
        mulher.run();             
        try{
            System.out.println("Tentando sair da Barreira...");
            b.leave();
            System.out.println("Mulher saiu da barreira");
        } catch (KeeperException | InterruptedException e){
            System.out.println("Erro ao sair da barreira");
        }
        System.out.println("Terminou");
    }    
    
}
