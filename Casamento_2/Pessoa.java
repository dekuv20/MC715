package casamento;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Pessoa extends Thread {
    private String Nome;
    private String Sobrenome;
    private String EstadoCivil;
    private int ListenPort;
    
    public Pessoa(String nome, String sobrenome, int port){
        Nome = nome;
        Sobrenome = sobrenome;
        EstadoCivil = "SOLTEIRO";
        ListenPort = port;
    }
    public void stopListening(Listen listen){
        listen.stop();
    }
    public void run(Listen listen){
        //escuta perguntas enquanto o processo está executando (fork)
      //  Listen listen = new Listen(this.getNome(), this.getSobrenome(), this.getPort());
        listen.start();

        //casa
        this.setEstadoCivilCasado();
        System.out.println("Casou!");
        //para de escutar
     //   listen.stop();//nesse programa acho que nao tem problema usar isso
      
    }   
    public void casar(){
        this.setEstadoCivilCasado();
    }
    public int getPort(){
        return ListenPort;
    }
    
    public void setEstadoCivilCasado(){
        this.EstadoCivil = "CASADO";
    }
    public void setEstadoCivilSolteiro(){
        this.EstadoCivil = "SOLTEIRO";
    }
    public void setEstadoCivilViuvo(){
        this.EstadoCivil = "VIUVO";
    }
    public String getEstadoCivil(){
        return this.EstadoCivil;
    }
    public String getNome(){
        return this.Nome;
    }
    public String getSobrenome(){
        return this.Sobrenome;
    }
    public void setNome(String s){
        this.Nome = s;
    }
    public void setSobrenome(String s){
        this.Sobrenome = s;
    }
    
    //referente a tarefa filha que escuta perguntas usando UDP
     public static class Listen extends Pessoa{
         public Listen(String nome, String sobrenome, int port){
             super(nome, sobrenome, port);
         }
         
         public void run(){
               DatagramSocket serverSocket = null;
             try {
                 serverSocket = new DatagramSocket(super.getPort());
             } catch (SocketException ex) {
                 Logger.getLogger(Pessoa.class.getName()).log(Level.SEVERE, null, ex);
             }
            byte[] receiveData = new byte[1024];
            byte[] sendData = new byte[1024];
            //executa num loop até a thread pai dizer que deve parar
            while(true){
                  DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);            
                       try {
                           System.out.println("escutando...");
                           serverSocket.receive(receivePacket);
                       } catch (IOException ex) {
                           System.out.println("Erro ao receber a mensagem!");
                           Logger.getLogger(Pessoa.class.getName()).log(Level.SEVERE, null, ex);
                       }
               
                  String sentence = new String( receivePacket.getData());
                  System.out.println("Recebeu: " + sentence);
                  InetAddress IPAddress = receivePacket.getAddress();
                  int port = receivePacket.getPort();
                  if("Qual o seu sobrenome?".equals(sentence)){
                      System.out.println("respondendo...");
                       sendData = this.getSobrenome().getBytes();
                  DatagramPacket sendPacket =
                  new DatagramPacket(sendData, sendData.length, IPAddress, port);
                 try {
                     serverSocket.send(sendPacket);
                     System.out.println("Respondeu!");
                 } catch (IOException ex) {
                     Logger.getLogger(Pessoa.class.getName()).log(Level.SEVERE, null, ex);
                     System.out.println("Erro ao responder!");
                 }
                  }else{
                      System.out.println("Warning: Pergunta desconhecida");
                  }
               }
         }       
     }   
}
