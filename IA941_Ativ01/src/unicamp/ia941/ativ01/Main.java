/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unicamp.ia941.ativ01;

/**
 *
 * @author "Marcelo F. Rigon"
 * @since 03/11/2019
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        WorldConnection wc = new WorldConnection();
        wc.setVisible(true);
        wc.initialize();
    }
}
