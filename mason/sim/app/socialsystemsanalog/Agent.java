/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package sim.app.socialsystemsanalog;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import sim.portrayal.*;
import sim.engine.*;
import sim.util.*;

// we extend OvalPortrayal2D to steal its hitObjects() code -- but 
// we override the draw(...) code to draw our own oval with a little line...

public /*strictfp*/ class Agent implements Steppable, Oriented2D
    {
    public static final boolean TOROIDAL_WORLD = true;
    //how much life has the agent left
    public int timeToLive;
    //hom much time the agent has left with food
    public int foodToEnd;
    //the agent current orientation
    public int orientation;
    //how many times the agent has collected food
    private double foodCollected;
    //if we want to use ICO learning
    private boolean isIcoLearner=true;
    //a low pass filter for the input reflex events
    private LowPassFilter lpx0;
    //a low pass filter for the input distal events
    private LowPassFilter lpx1;
    //Ico learning algorithm
    IcoLearner ico;
    Stoppable toDiePointer=null;
    //reflex array of 8 sectors for avoidance and attraction (short range)
    private double[] reflexAgentSensors = new double[8];  
    private double[] reflexFoodSensors = new double[8];  
    private double[] reflexAgentFoodSensors=new double[8];
    //distal array of 8 sectos for avoidance and attraction (long range)
    private double[] distalAgentSensors=new double[8];
    private double[] distalFoodSensors=new double[8];
    private double[] distalAgentFoodSensors=new double[8];
    
    private Color mycolor;
    private double last_time_try=0.0;
    private Message last_msg=null;

    //last error angle
    private double angle;

    MutableDouble3D reflexMux=new MutableDouble3D(0.0,0.0,0.0);
    MutableDouble3D distalMux=new MutableDouble3D(0.0,0.0,0.0);
            

    public double getHeading(){return angle;}
    public double getU0Agent(){return lpx0.getOutput(LowPassFilter.AGENT_SIGNAL);}
    public double getU0Food(){return lpx0.getOutput(LowPassFilter.FOOD_SIGNAL);}
    public double getU0AgentFood(){return lpx0.getOutput(LowPassFilter.AGENTFOOD_SIGNAL);}
    public double getU1Agent(){return lpx1.getOutput(LowPassFilter.AGENT_SIGNAL);}
    public double getU1Food(){return lpx1.getOutput(LowPassFilter.FOOD_SIGNAL);}
    public double getU1AgentFood(){return lpx1.getOutput(LowPassFilter.AGENTFOOD_SIGNAL);}
    
    public double getWeightAvoid(){return ico.getWeight(LowPassFilter.AGENT_SIGNAL);}
    public double getWeightFood(){return ico.getWeight(LowPassFilter.FOOD_SIGNAL);}
    public double getWeightParasite(){return ico.getWeight(LowPassFilter.AGENTFOOD_SIGNAL);}

    public double getFoodCollected(){return foodCollected;}
    public double getFoodToEnd(){return foodToEnd;}
    public int getOrientation(){return orientation;}
    public boolean getHasFoodItem() { return hasFoodItem; }
    public synchronized void setHasFoodItem(boolean val) { hasFoodItem = val; }
    public int getOutput() throws Exception{return ico.getDiscreteOutput();}
    public double orientation2D() { return angle*Math.PI/180; }
    
    //public int getPositiveTrust(){return memory_trust.positive_trust;}
    //public int getNegativeTrust(){return memory_trust.negative_trust;}
    //a discrete flag to indicate the food status
    public boolean hasFoodItem;
    //but from where did he get the food
    public int foodFrom;
    //the robot ID or in other words its name
    private int ID;
    //every robot has a FIFO queue of incoming messages called the inbox
    private LinkedList inbox = new LinkedList();
    //the maximum number of incoming messages is 1 because only agent is able 
    //to keep only 1 conversation per time
    private int INBOX_MAX=1;
    //every agent has a FIFO queue of outgoing messages called the outbox
    private LinkedList outbox=new LinkedList();
    //the maximum number of outcoming messages
    private int OUTBOX_MAX=100;
    //number of messages from the inbox that were processed
    private int number_read_msg;
    //number of messages sent
    private int number_sent_msg;
    boolean justCreated;

   
   
    //this is the agent location in the 2d space
    public double x;
    public double y;


    public Agent(double angle, double x, double y)
        {
        this.angle = angle; this.x = x; this.y = y;
        this.orientation=(int)Math.round(angle* (180/Math.PI));
        //this.orientation=angle;
        }
    
    public void initEntropy(int bits)
    {
        int nbins=(int)Math.pow(2,bits);
    
    }
    public Agent(double angle,int timeToLive,int foodt,int agentid )
        {
        this.angle = angle;
        this.timeToLive = timeToLive;
        this.foodToEnd=foodt;
        this.hasFoodItem = false;
        this.foodFrom=LowPassFilter.AGENTFOOD_SIGNAL;
        this.justCreated = true;
        //this.memory_trust=new MemoryTrust(AgentsForage.INITIAL_AGENTS);
        this.orientation=(int)Math.round(angle* (180/Math.PI));
        //this.orientation=angle;

        if(isIcoLearner)
        {
            MutableDouble3D maxinput=new MutableDouble3D();
            maxinput.setTo(1/AgentsForage.AGENT_DIAMETER);
            try {
                lpx0 = new LowPassFilter(0.8, maxinput);
            } catch (Exception ex) {
                Logger.getLogger(Agent.class.getName()).log(Level.SEVERE, null, ex);
            }
            maxinput.setTo(1/AgentsForage.REFLEX_AGENT_MAX);
            try {
                lpx1 = new LowPassFilter(0.8, maxinput);
            } catch (Exception ex) {
                Logger.getLogger(Agent.class.getName()).log(Level.SEVERE, null, ex);
            }
            ico=new IcoLearner(false);
            //ico.setFeedbackInhibition(true);

            ico.range=2*Math.PI;
            ico.saturation=Math.PI/2;
        }
        this.ID=agentid;
        this.number_read_msg=0;
        this.number_sent_msg=0;
        }
    
    public int getSentMessages()
    {
        return number_sent_msg;
    }

    public int getProcessedMessages()
    {
        return number_read_msg;
    }
    
    public void setPosition(double x,double y)
    {
                this.x = x; this.y = y;
        
    }
    
    public void step(SimState state)
        {
        final AgentsForage af = (AgentsForage)state;
        boolean collision=false;
        //compute the new orientation and communicate
        //collision=nearbyAGENTs(af, AgentsForage.maskRandomization[2]);
        reflexMux.zero();
        distalMux.zero();
        
        nearbyFOODs(af, false);
        //check if the agent got some food from the food points
        //if(currentSurface(af)>0) this.hasFoodItem=true;
        if(isIcoLearner)
        {

            if(!hasFoodItem)
            {//reflexMux.z=attraction to agents with food
            //reflexMux.y=attraction to food points
            //reflexMux.y=reflexFoodSensors[leftantennadir]-reflexFoodSensors[rightantennadir];
            //reflexMux.z=reflexAgentFoodSensors[orientation];
            //reflexMux.z=reflexAgentFoodSensors[leftantennadir]-reflexAgentFoodSensors[rightantennadir];
            }
            //here we need to use a proportional gain
            //reflexMux.x=avoidance vs agents without food
            //distalMux.x=distalAgentSensors[orientation];
            //distalMux.x=distalAgentSensors[rightantennadir]-distalAgentSensors[leftantennadir];

            if(!hasFoodItem)
            {   
            //reflexMux.y=attraction to food points
            //distalMux.y=distalFoodSensors[leftantennadir]-distalFoodSensors[rightantennadir];
            //reflexMux.z=attraction to agents with food only if it doesn't have food
            //distalMux.z=distalAgentFoodSensors[leftantennadir]-distalAgentFoodSensors[rightantennadir];
            }
            try {
                lpx0.updateFilter(reflexMux);
                lpx1.updateFilter(distalMux);
            } catch (Exception ex) {
                Logger.getLogger(Agent.class.getName()).log(Level.SEVERE, null, ex);
            }

            ico.calculate(lpx0.getOutput(), lpx1.getOutput());  
            
            try {
                //get the angular change discretized as multiple of 45 degrees
                if(af.OPEN_LOOP)
                    angle+=af.random.nextDouble();
                else
                    angle += ico.getOutput();
                
            } catch (Exception ex) {
                Logger.getLogger(Agent.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Fatal error aborting simulation");
                state.kill();
            }
        }
        //compute the new coordinates
        x += Math.cos(angle);
        y -= Math.sin(angle);
        
            //update the agent to the new position
            if (TOROIDAL_WORLD) {
                x = af.agentgrid.stx(x);
                y = af.agentgrid.stx(y);
                af.agentgrid.setObjectLocation(this, new Double2D(x, y));

            }//if we are in the boundary of the world evokes a reaction
            else {
                if (x >= af.agentgrid.width) {
                    x = af.agentgrid.width - 1;
                    angle += Math.PI;
                } else if (x < 0) {
                    x = 0;
                    angle += Math.PI;
                }
                if (y >= af.agentgrid.height) {
                    y = af.agentgrid.height - 1;
                    angle += Math.PI;
                } else if (y < 0) {
                    y = 0;
                   angle += Math.PI;
                }
                af.agentgrid.setObjectLocation(this, new Double2D(x, y));
            }

        //check if an agent must die
        if(!AgentsForage.IMMORTAL)
        {
            timeToLive--;
            if( timeToLive <= 0 )
                {
                die( state );
                return;
                }
        }
        
        //check if the agent has food left
        if(this.foodToEnd<1)
        {
             this.foodToEnd=AgentsForage.FOOD_RETENTION;
             this.hasFoodItem=false;
        }
        else if(this.hasFoodItem)this.foodToEnd--;

        //update the status
        this.orientation=(int)Math.round(angle* (180/Math.PI));
        }
    
    public void resetFoodStatus(int from)
    {
             this.foodToEnd=AgentsForage.FOOD_RETENTION;
             this.hasFoodItem=true; 
             this.foodFrom=from;
    }
    //another agent push a message in our inbox
    public synchronized boolean pushMessage(Message msg)
    {
        //if the buffer is not empty we accept the message
        if(inbox.size()<INBOX_MAX)
        {
            inbox.push(msg);
            return true;
        }
        else//if the buffer is full we cannot accept the message
        {
            return false;
        }

    }
    public boolean isBusy()
    {
        if(last_msg!=null) return true;
        else return false;
    }

    /*
    //this function process the inbox
    public void processInbox(final SimState state)
    {
        // fetch the first message in the FIFO queue and process it
        // data is retrieved from the head of queue
        if (!inbox.isEmpty()) {
            //[TODO] here it needs to send both weights argh!
            Message msg = (Message) inbox.removeFirst();
            //do we trust the agent enough to try the new weight?
            //if the agent is not trying anything at the moment:
            //and he trust the new agent try the new symbol!
            if (((state.schedule.getTime() - last_time_try) > AgentsForage.REFRACTORY_COMM) && memory_trust.decideTrust(msg.getSenderID())) {
                if(ico.getWeight(msg.getType())<msg.getValue())
                {//memorize the original weight in the last message object
                last_msg=new Message(msg.getSenderID(), ico.getWeight(LowPassFilter.FOOD_SIGNAL), LowPassFilter.FOOD_SIGNAL);
                //set the weight if the agent trust the alias and if the weight is bigger
                ico.setWeight(msg.getType(), msg.getValue());
                } else last_msg=null;
                
                //increase the number of total messages processed
                number_read_msg++;
                //updated when was the last time he tried a suggestion
                last_time_try = state.schedule.getTime();
            }
        }
        else{
            if ((state.schedule.getTime()-last_time_try)<=AgentsForage.TRIAL_COMM){
            //positive reward accept the weight and increase trust 
                if (hasFoodItem && last_msg != null)
                {          
                    memory_trust.addTrust(last_msg.getSenderID(), 1);
                    last_msg=null;
                }
            //negative reward reject the weight and decrease trust
            }else if ((state.schedule.getTime()-last_time_try)>=AgentsForage.TRIAL_COMM){
            if (!hasFoodItem && last_msg != null) {
                //[TODO] probably need to change the refractory period!
                    //reset the previous weights
                    ico.setWeight(last_msg.getType(), last_msg.getValue());
                    //and decrease trust
                    memory_trust.removeTrust(last_msg.getSenderID(), 1);
                    last_msg=null;
            }
            }
        }
    }    
*/
    
    public boolean nearbyFOODs(AgentsForage af, boolean needsCommunicate) {


        //find all the objects in the max visibile range
        final Bag nearbyFoods = af.ground.getObjectsWithinDistance(new Double2D(x, y), af.DISTAL_AGENT_MAX, false, false);
        if(nearbyFoods.numObjs>0)
        {
        double foodDistance = 1 / af.DISTAL_AGENT_MAX;
        DistAngle distArray[] = new DistAngle[nearbyFoods.numObjs];
        //then find the closest one
        for (int i = 0; i < nearbyFoods.numObjs; i++) {
            final Food fp = (Food) (nearbyFoods.objs[i]);
            double distance = Point2D.distance(fp.x, fp.y, x, y);
            double angle = sensorForAngle(fp.x, fp.y);
            distArray[i] = new DistAngle(distance, angle);

        }
        //now sort distances in ascending order
        Arrays.sort(distArray);

        // it's within reflex range
        if (distArray[0].getDistance() < af.REFLEX_AGENT_MAX && distArray[0].getDistance() > af.AGENT_DIAMETER) {
            if(!hasFoodItem)
            reflexMux.x=distArray[0].getAngle();
        }

        //is within distal range
        if (distArray[0].getDistance() > af.REFLEX_AGENT_MAX && distArray[0].getDistance() < af.DISTAL_AGENT_MAX) {
            distalMux.x=distArray[0].getAngle();
            
        }
        return true;
        }
        else
        {

            return false;

        }
    }


    /** find local agents and if possible communicate*/
    public boolean nearbyAGENTs(AgentsForage mavdemo,boolean needsCommunicate)
        {

        int ncollisions=0;
        final double dmaxreflex = mavdemo.REFLEX_AGENT_MAX;
        final double dmaxdistal= mavdemo.DISTAL_AGENT_MAX;
        final double dcollision=mavdemo.AGENT_DIAMETER;
        final Bag nearbyMavs = mavdemo.agentgrid.getObjectsWithinDistance(new Double2D(x,y),dmaxdistal,false,false);
        for(int i=0;i<nearbyMavs.numObjs;i++)
            {
            //System.out.println("Agent "+this.ID+" found "+nearbyMavs.numObjs);
            final Agent mav = (Agent)(nearbyMavs.objs[i]);
            final double mavDistance = Math.sqrt((mav.x-x)*(mav.x-x)+(mav.y-y)*(mav.y-y));
            if(this.ID!=mav.ID)//just double check we are not picking ourself!
            {
                //collision detected
                if(mavDistance <dcollision)
                    ncollisions++;
                //the agent has stolen food from the other
                if(mavDistance<mavdemo.foodCatchDiameter && mav.hasFoodItem)
                    resetFoodStatus(LowPassFilter.AGENTFOOD_SIGNAL);

                if (needsCommunicate && mavDistance < mavdemo.MAX_COMM_RANGE) {
                    //compose a message indicating that a food point was found and that
                    //the weights associated with the finding where those declared
                    Message msgFood = new Message(this.ID, ico.getWeight(LowPassFilter.FOOD_SIGNAL), LowPassFilter.FOOD_SIGNAL);
                    //only sends to others and not himself and only in learning mode
                    if(ico.getLearning() && !mav.isBusy())
                    {mav.pushMessage(msgFood);
                    this.number_sent_msg++;}
                }
                //check for reflex or distal signals
                if (mavDistance < dmaxreflex && mavDistance>dcollision)  // it's within reflex range
                {
                final int octant = 1;  // figure the octant

                if(!mav.hasFoodItem)
                reflexAgentSensors[octant] = /*Strict*/Math.max(reflexAgentSensors[octant],1/mavDistance);
                else
                reflexAgentFoodSensors[octant] = /*Strict*/Math.max(reflexAgentFoodSensors[octant],1/mavDistance);  
                }
                if(mavDistance>dmaxreflex && mavDistance<dmaxdistal)//is within distal range
                {
                final int octant = 1;  // figure the octant
                if(!mav.hasFoodItem)
                distalAgentSensors[octant] = /*Strict*/Math.max(distalAgentSensors[octant],1/mavDistance);
                else
                distalAgentFoodSensors[octant] = /*Strict*/Math.max(distalAgentFoodSensors[octant],1/mavDistance);  
                }
            }
            }//end for
            if(ncollisions>0)return true;
            else return false;
        }

    // in order to rotate 45/2 degrees counterclockwise around origin
    final double sinTheta = /*Strict*/Math.sin(45.0/2*/*Strict*/Math.PI/180);
    final double cosTheta = /*Strict*/Math.cos(45.0/2*/*Strict*/Math.PI/180);

    public double sensorForAngle(double px, double py)
    {
        // translate to origin of the robot
        px -= x; py -= y;
        
        // rotate 45/2 degrees clockwise about the origin
        final double xx = px * cosTheta + py * (sinTheta);
        final double yy = px * (-sinTheta) + py * cosTheta;
        //compute the angle
        double absangle= Math.atan2(yy, xx);

        //find the relative angle
        double relangle=absangle-angle;
        //check now if is in the visual field
        if(Math.abs(relangle) < Math.PI/2)
            return relangle;
        else
            return 0.0;
        
    }
    
    
        public void die( final SimState state )
        {
        AgentsForage agentsforage = (AgentsForage)state;
        agentsforage.numberOfAgents--;
        agentsforage.agentgrid.remove( this );
        if(toDiePointer!=null) toDiePointer.stop();
        }   
        
        public String toString()
        {
            if(ico.isSeeker())
                return "Tachikoma_"+ID+"_seeker";
            else
                return "Tachikoma_"+ID+"_parasite";
        }
        
        public Color getColor()
        {
            if(hasFoodItem){
               mycolor=Color.cyan;
            }
            else {
            if(ico.isSeeker()) mycolor=Color.red;
            else mycolor=Color.MAGENTA;
            }
            return mycolor;
        }

    public class DistAngle implements Comparable {

        double distance;
        double angle;

        public DistAngle(double dist,double theta)
        {
            this.distance=dist;
            this.angle=theta;
        }

        public double getDistance()
        {

            return this.distance;
        }

        public double getAngle()
        {
           return this.angle;
        }
        /* Overload compareTo method */
        public int compareTo(Object obj) {
            DistAngle tmp = (DistAngle) obj;
            if (this.distance < tmp.distance) {
                /* instance lt received */
                return -1;
            } else if (this.distance> tmp.distance) {
                /* instance gt received */
                return 1;
            }
            /* instance == received */
            return 0;
        }
    }

    }
    