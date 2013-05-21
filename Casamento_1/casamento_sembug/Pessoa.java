/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package casamento;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.zookeeper.KeeperException;

public class Pessoa extends Thread {
    private String Nome;
    private String Sobrenome;
    private String EstadoCivil;
    
    public Pessoa(String nome, String sobrenome){
        Nome = nome;
        Sobrenome = sobrenome;
        EstadoCivil = "SOLTEIRO";
    }
    @Override
    public void run(){
        //escuta perguntas enquanto o processo está executando (fork)
        Listen listen = new Listen(this.getNome(), this.getSobrenome());
        listen.start();

        //casa
        this.setEstadoCivilCasado();
        System.out.println("Casou!");
        try {
            //para de escutar
           //travada em IO esperando datagrama UDP
            Pessoa.sleep(1000);                     //travada em IO esperando datagrama UDP
        } catch (InterruptedException ex) {
            System.out.println("erro no sleep");
            Logger.getLogger(Pessoa.class.getName()).log(Level.SEVERE, null, ex);
        }
        listen.stop();//nesse programa acho que nao tem problema usar isso
       /* try {
            listen.join();
        } catch (InterruptedException ex) {
            System.out.println("Erro ao parar de ouvir");
            Logger.getLogger(Pessoa.class.getName()).log(Level.SEVERE, null, ex);
        }*/
    }   
    public void casar(){
        this.setEstadoCivilCasado();
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
         public Listen(String nome, String sobrenome){
             super(nome, sobrenome);
         }
         
         @Override
         public void run(){
               DatagramSocket serverSocket = null;
             try {
                 serverSocket = new DatagramSocket(9879);
             } catch (SocketException ex) {
                 Logger.getLogger(Pessoa.class.getName()).log(Level.SEVERE, null, ex);
                 System.out.println("Erro ao criar o socket, provavelmente conflito de porta");
             }
            byte[] receiveData = new byte[1024];
            byte[] sendData = new byte[1024];
            //executa num loop até a thread pai dizer que deve parar
           
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
               }      
     }
         
    /*A tarefa Pessoa inicia quando o main é invocado, dai ela espera na barreira
    até que o numero em args[2] seja completado
    os argumentos sao: 

    args[0]: the address of a ZooKeeper server (e.g., "zoo1.foo.com:2181")

    "/b1": the path of the barrier node on ZooKeeper (e.g., "/b1")

    args[1]: the size of the group of processes
    */
     public static void main(String[] args) {
          Barrier b = new Barrier(args[0], "/b1", new Integer(args[1]));
          Pessoa marido = new Pessoa("Bruce","Wayne");
        try{
            boolean flag = b.enter();
            System.out.println(marido.getNome()+" "+marido.getSobrenome()+" entrou na barreira: " + args[1]);
            if(!flag) {
                System.out.println("Erro ao entrar na barreira");
            }
        } catch (KeeperException | InterruptedException e){
        }
        //pode fazer um if aqui de um args para pegar o nome
        
        marido.run();             
        try{
            System.out.println("Tentando sair da Barreira...");
            b.leave();
            System.out.println(marido.getNome()+" "+marido.getSobrenome()+" saiu da Barreira");
        } catch (KeeperException | InterruptedException e){
            System.out.println("Erro ao sair da Barreira");
        }
        System.out.println("Terminou");
    }      
     
}
