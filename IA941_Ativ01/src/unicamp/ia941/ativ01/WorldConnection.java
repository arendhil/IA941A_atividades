/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unicamp.ia941.ativ01;

import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import ws3dproxy.CommandExecException;
import ws3dproxy.WS3DProxy;
import ws3dproxy.model.Creature;
import ws3dproxy.model.Thing;
import ws3dproxy.model.World;
import ws3dproxy.model.WorldPoint;
import ws3dproxy.util.Constants;

/**
 *
 * @author ia941
 */
public class WorldConnection extends JFrame implements KeyListener {
    Logger log;
    public WS3DProxy proxy;
    float V_SPEED = 1f;
    Creature player;
    World w;
    float V_X = 0;
    float V_Y = 0;
    float eatDistance = 2f;
    
    public WorldConnection() {
        log = Logger.getLogger("WorldConnection");
        proxy = new WS3DProxy();
        try {
            w = World.getInstance();
            w.reset();
            World.createFood(0, 350, 75);
            World.createFood(0, 100, 220);
            World.createFood(0, 250, 210);
            player = proxy.createCreature(100,450,0);
            
            WorldPoint position = player.getPosition();
            double pitch = player.getPitch();
            double fuel = player.getFuel();
            
            //main loop
            addKeyListener(this);
        } catch (CommandExecException e) {
            System.out.println("Erro capturado");
            System.out.println(e.getMessage());
        }
        
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g); //To change body of generated methods, choose Tools | Templates.
        try {
            // motion
            float rotation = 0f;
            if (V_X > 0) {
                rotation = (float) (15.0 / Math.PI);
            } else if (V_X < 0) {
                rotation = (float) (-15.0 / Math.PI);
            }
            
            if (V_Y <= 0) {
                if (rotation == 0)
                    player.stop();
                else {
                    player.rotate(rotation);
                    player.start();
                }
            } else {
                player.move(V_Y, V_Y, rotation);
                player.start();
            }

            //player.move(V_SPEED, V_X, V_X);
        } catch (CommandExecException ex) {
            Logger.getLogger(WorldConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void keyTyped(KeyEvent e) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    void eat() {
        log.info("Trying to eat something.");
        List<Thing> things = player.getThingsInVision();
        for (int i = 0; i < things.size(); i++) {
            //if (distance(things.get(i).))
            Thing t = things.get(i);
            if ((t.getCategory() == Constants.categoryNPFOOD) || (t.getCategory() == Constants.categoryPFOOD)) {
                //is food, can eat
                log.info("Found food in sight! Yummy");
                log.info(t.getAttributes().toString());
            }
        }
        //player.eatIt();
    }
    
    double distance(float x, float y) {
        WorldPoint position = player.getPosition();
        return Math.sqrt(Math.pow(x - position.getX(),2)-Math.pow(y - position.getY(), 2));
    }

    @Override
    public void keyPressed(KeyEvent e) {
        V_X = 0;
        V_Y = 0;
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            V_X += -1;
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            V_X += +1;
        } else if (e.getKeyCode() == KeyEvent.VK_UP) {
            V_Y += +1;
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            V_Y += -1;
        }
        
        repaint();
        //System.out.println("New key pressed. speed: "+ V_X +", "+ V_Y);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            V_X += +1;
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            V_X += -1;
        } else if (e.getKeyCode() == KeyEvent.VK_UP) {
            V_Y += -1;
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            V_Y += +1;
        }

        if (e.getKeyChar()=='e')
            eat();
        
        repaint();
        //System.out.println("New key released. speed: "+ V_X +", "+ V_Y);
    }
}
