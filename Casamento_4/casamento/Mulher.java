package casamento;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

//classe referente ao processo que muda o nome quando casa
public class Mulher extends Pessoa{
    public Mulher(String nome, String sobrenome, int portNumber){
        super(nome, sobrenome, portNumber);
    }
    //executa o processo
    @Override
    public void run(){     
         if(this.getEstadoCivil() == "SOLTEIRO"){
        try {
            this.sleep(2000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Mulher.class.getName()).log(Level.SEVERE, null, ex);
        }
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
        } catch (UnknownHostException ex) {
            Logger.getLogger(Mulher.class.getName()).log(Level.SEVERE, null, ex);
        }
      byte[] sendData = new byte[1024];
      byte[] receiveData = new byte[1024];
      String sentence = "Qual o seu sobrenome?";
      sendData = sentence.getBytes();
      DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, this.getPort());
      
        try {
            System.out.println("enviando...");
            clientSocket.send(sendPacket);
            System.out.println("Enviou!");
        } catch (IOException ex) {
            Logger.getLogger(Mulher.class.getName()).log(Level.SEVERE, null, ex);
        }
      DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        try {
            System.out.println("esperando resposta do Marido");
            //espera 5 segundo se nao responde sai do metodo
            clientSocket.setSoTimeout(5000);
            clientSocket.receive(receivePacket);   
           
        }catch(SocketException soex){
            //marido na responde
            System.out.println("Excedeu o timeout, marido nao responde!");
            clientSocket.close(); 
            return;
            
        }catch (IOException ex) {
                 Logger.getLogger(Mulher.class.getName()).log(Level.SEVERE, null, ex);
                 clientSocket.close();
             }
      String sobrenomeMarido = new String(receivePacket.getData());
      System.out.println("Resposta do Marido:" + sobrenomeMarido);
      clientSocket.close();   
      
      //casa e muda o nome para nome de casada
        this.casar(sobrenomeMarido);
        System.out.println("Casou!");
         }else{
             //separa
             System.out.println("Separou!");
            this.setEstadoCivilSolteiro();
         }
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
}
