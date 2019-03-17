/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unicamp.ia941.ativ01;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;
import javax.swing.Timer;
import java.util.Random;
import javax.swing.JFrame;
import ws3dproxy.CommandExecException;
import ws3dproxy.WS3DProxy;
import ws3dproxy.model.*;
import ws3dproxy.util.Constants;
import ws3dproxy.util.Logger;

/**
 *
 * @author ia941
 */
public class WorldConnection extends JFrame implements KeyListener {
    public static byte MOTION_RIGHT = 1;
    public static byte MOTION_LEFT = 2;
    public static byte MOTION_FOWARD = 4;
    public static byte MOTION_BACK = 8;

    Logger log;
    Random randgen;
    public WS3DProxy proxy;
    float V_SPEED = 1f;
    Creature player;
    World w;
    byte motionDirection = 0;
    double actionDistance = 45.0;
    
    public WorldConnection() {
        proxy = new WS3DProxy();
        randgen = new Random();
    }

    public void initialize() {
        try {
            w = World.getInstance();
            w.reset();
            player = proxy.createCreature(100,450,0);
            
            WorldPoint position = player.getPosition();
            double pitch = player.getPitch();
            double fuel = player.getFuel();

            player.genLeaflet();
            World.setDeliverySpot(w.getEnvironmentWidth()/2.0, w.getEnvironmentHeight()/2.0);

            //main loop
            addKeyListener(this);
            createThingsSpawner();

        } catch (CommandExecException e) {
            System.out.println("Erro capturado");
            System.out.println(e.getMessage());
        }
        
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                this.motionDirection |= MOTION_LEFT;
                break;
            case KeyEvent.VK_RIGHT:
                this.motionDirection |= MOTION_RIGHT;
                break;
            case KeyEvent.VK_UP:
                this.motionDirection |= MOTION_FOWARD;
                break;
            case KeyEvent.VK_DOWN:
                this.motionDirection |= MOTION_BACK;
                break;
            default:
                break;
        }

        repaint();
        //System.out.println("New key pressed. speed: "+ V_X +", "+ V_Y);
        //log.info("Key Pressed - KeyCode: "+e.getKeyCode()+" char: "+e.getKeyChar() );
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                this.motionDirection &= ~MOTION_LEFT;
                break;
            case KeyEvent.VK_RIGHT:
                this.motionDirection &= ~MOTION_RIGHT;
                break;
            case KeyEvent.VK_UP:
                this.motionDirection &= ~MOTION_FOWARD;
                break;
            case KeyEvent.VK_DOWN:
                this.motionDirection &= ~MOTION_BACK;
                break;
            case KeyEvent.VK_Z:
                eat();
                break;
            case KeyEvent.VK_X:
                grab();
                break;
            case KeyEvent.VK_C:
                deliverLeaflet();
                break;
            case KeyEvent.VK_S:
                hideThing();
                break;
            case KeyEvent.VK_D:
                unhideThing();
                break;
            default:
                break;
        }

        repaint();
        //log.info("Key Release - KeyCode: "+e.getKeyCode()+" char: "+e.getKeyChar() );
        //System.out.println("New key released. speed: "+ V_X +", "+ V_Y);
    }
    
    public void createThingsSpawner() {
        Timer timer = new Timer(10000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                WorldPoint pos = World.getRandomTarget();
                double rand = randgen.nextDouble();
                try {
                    if (rand < 0.3) {
                        World.createFood(randgen.nextInt(2), pos.getX(), pos.getY());
                    } else {
                        World.createJewel(randgen.nextInt(6), pos.getX(), pos.getY());
                    }
                } catch (CommandExecException e1) {
                    e1.printStackTrace();
                }
            }
        });
        timer.start();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g); //To change body of generated methods, choose Tools | Templates.
        move();
    }

    void unhideThing() {
        try {
            player.updateState();
            List<Thing> things = player.getThingsInCameraFrustrum();
            System.out.println("Trying to unhide things. "+things.size()+" in sight.");
            for (int i = 0; i < things.size(); i++) {
                //if (distance(things.get(i).))
                Thing t = things.get(i);
                if (isThingActionable(t.getCategory())) {
                    double dist = this.distanceFromPoint(t.getCenterPosition());
                    if (t.hidden && (dist < this.actionDistance)) {
                        //grab just one item per action top.
                        player.unhideIt(t.getName());
                        System.out.println("Thing unhidden at: "+t.getCenterPosition()+" name: "+t.getName());
                        return;
                    }
                }
            }
        } catch(CommandExecException e) {
            System.out.println("Error during unhide action. "+e.getMessage());
            Logger.logException(WorldConnection.class.getName(), e);
        }
    }

    void hideThing() {
        try {
            player.updateState();
            List<Thing> things = player.getThingsInVision();
            for (int i = 0; i < things.size(); i++) {
                //if (distance(things.get(i).))
                Thing t = things.get(i);
                if (isThingActionable(t.getCategory()) && !t.hidden) {
                    double dist = this.distanceFromPoint(t.getCenterPosition());
                    if (dist < this.actionDistance) {
                        //grab just one item per action top.
                        player.hideIt(t.getName());
                        System.out.println("Thing hidden at: " + t.getCenterPosition() + " name: " + t.getName());
                        return;
                    }
                }
            }
        } catch(CommandExecException e) {
            System.out.println("Error during hide action. "+e.getMessage());
            Logger.logException(WorldConnection.class.getName(), e);
        }
    }

    void deliverLeaflet() {
        WorldPoint deliverPos = World.getDeliverySpot();
        try {
            player.updateState();
            if (distanceFromPoint(World.getDeliverySpot()) < actionDistance) {
                List<Leaflet> leafs = player.getLeaflets();
                for (int i = 0; i < leafs.size(); i++) {
                    player.deliverLeaflet(leafs.get(i).getID().toString());
                    System.out.println("Leaflets: "+leafs.get(i).toString());
                }
            }
        } catch(CommandExecException e) {
            System.out.println("Error during hide action. "+e.getMessage());
            Logger.logException(WorldConnection.class.getName(), e);
        }
    }

    void move() {
        try {
            // motion calculation
            float rotation = 0f;
            float V_X = 0;
            float V_Y = 0;
            if ((motionDirection&MOTION_RIGHT) != 0)
                V_X += 1;
            if ((motionDirection&MOTION_LEFT) != 0)
                V_X += -1;
            if ((motionDirection&MOTION_FOWARD) != 0)
                V_Y += 1;
            if ((motionDirection&MOTION_BACK) != 0)
                V_Y += -1;

            if (V_X > 0) {
                rotation = (float) (15.0 / Math.PI);
            } else if (V_X < 0) {
                rotation = (float) (-15.0 / Math.PI);
            }

            //send motion command
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

        } catch (CommandExecException ex) {
            Logger.logException(WorldConnection.class.getName(), ex);
        }

    }
    
    @Override
    public void keyTyped(KeyEvent e) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    boolean isThingActionable(int category) {
        if (    (category == Constants.categoryJEWEL) ||
                (category == Constants.categoryNPFOOD) ||
                (category == Constants.categoryPFOOD)) {
            return true;
        }
        return false;
    }

    void eat() {
        try {
            player.updateState();
            List<Thing> things = player.getThingsInVision();
            System.out.println("Trying to eat something. Things in sight: "+things.size());
            double fuel = player.getFuel();
            for (int i = 0; i < things.size(); i++) {
                //if (distance(things.get(i).))
                Thing t = things.get(i);
                if (!t.hidden &&
                        ((t.getCategory() == Constants.categoryNPFOOD) || (t.getCategory() == Constants.categoryPFOOD))) {
                    //is food, can eat
                    double dist = this.distanceFromPoint(t.getCenterPosition());
                    if (dist < this.actionDistance) {
                        player.eatIt(t.getName());
                        player.updateState();
                        System.out.println("Fuel before eating: "+fuel+" and after: "+player.getFuel());
                        //eat just one fruit
                        return;
                    }
                }
            }
            // no food in sight, but we may have something in the bag.
            Bag bag = player.getBag();
            if (bag.getTotalNumberFood() > 0) {
                //there is food in the bag, we should eat it.
                //Firstly, perishable food.
                if (bag.getNumberPFood() > 0) {

                } else {
                    //if food count > 0 and no perishable food, we can assume that there is non-perishable in the bag

                }

            }
        } catch(CommandExecException e) {
            System.out.println("Error during eat action. "+e.getMessage());
            Logger.logException(WorldConnection.class.getName(), e);
        }
        //player.eatIt();
    }

    void grab() {
        try {
            player.updateState();
            List<Thing> things = player.getThingsInVision();
            for (int i = 0; i < things.size(); i++) {
                Thing t = things.get(i);
                if (isThingActionable(t.getCategory()) && !t.hidden) {
                    double dist = this.distanceFromPoint(t.getCenterPosition());
                    if (dist < this.actionDistance) {
                        //grab just one item per action top.
                        player.putInSack(t.getName());
                        player.updateBag();
                        System.out.println("Bag content: "+player.getBag().printBag());
                        return;
                    }
                }
            }
        } catch(CommandExecException e) {
            System.out.println("Error during eat action. "+e.getMessage());
            Logger.logException(WorldConnection.class.getName(), e);
        }
        //player.eatIt();
    }
    
    double distanceFromPoint(WorldPoint p1) {
        WorldPoint position = player.getPosition();
        return Math.sqrt(Math.pow(p1.getX() - position.getX(),2)+Math.pow(p1.getY() - position.getY(), 2));
    }
}