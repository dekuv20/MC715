/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package casamento;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.zookeeper.KeeperException;

/**
 *
 * @author Guilherme Lanna
 */
public class SimpleTest {
     public static void main(String args[]) {
        BarreiraDupla b = new BarreiraDupla(args[0], "/b1", new Integer(args[1]));
         for(int k =0; k<3; k++){
             System.out.println("Loop numero " + k);
        try{
            boolean flag = b.enter();
            System.out.println("Entered barrier");
            if(!flag) System.out.println("Error when entering the barrier");
        } catch (KeeperException e){

        } catch (InterruptedException e){

        }

        // Generate random integer
        Random rand = new Random();
        int r = rand.nextInt(100);
        // Loop for rand iterations
        for (int i = 0; i < r; i++) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {

            }
        }
        try{
            b.leave();
        } catch (KeeperException e){

        } catch (InterruptedException e){

        }
        System.out.println("Left barrier");
    }
     /*    try {
             b.destroy();
         } catch (InterruptedException ex) {
             Logger.getLogger(SimpleTest.class.getName()).log(Level.SEVERE, null, ex);
         } catch (KeeperException ex) {
             Logger.getLogger(SimpleTest.class.getName()).log(Level.SEVERE, null, ex);
         }*/
     }
}
