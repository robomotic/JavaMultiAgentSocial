/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package sim.app.socialsystems2;

import java.awt.Color;
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
    public static final boolean TOROIDAL_WORLD = false;
    //discretization of the orientation
    private static final int num_orientations=8;
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
    
    //reflex value for the agents avoidance left-right
    private int leftantennadir;
    private int rightantennadir;
    private Color mycolor;
    private double last_time_try=0.0;
    private Message last_msg=null;
    //memory for trust relationships
    private MemoryTrust memory_trust;
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

    /* various entropy debug infos
     * 
    public double getHInputAgent(){return entropyReflexAgent.getEntropy();}
    public double getHInputFood(){return entropyReflexFood.getEntropy();}
    public double getHInputAgentFood(){return entropyReflexAgentFood.getEntropy();}
    public double getHOutput(){return entropyOutput.getEntropy();}
    public double getHDiff(){return entropyOutput.getDerivative();}
    public int getU0FoodBinned(){return entropyReflexFood.getLastBin();}
    public int getU0AgentBinned(){return entropyReflexAgent.getLastBin();}
     * */
    
    public int getPositiveTrust(){return memory_trust.positive_trust;}
    public int getNegativeTrust(){return memory_trust.negative_trust;}
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
    //entropy calculator
    public Entropy1D entropyReflexAgent;
    public Entropy1D entropyDistalAgent;
    public Entropy1D entropyReflexFood;
    public Entropy1D entropyReflexAgentFood;
    public Entropy1D entropyDistalFood;
    public Entropy1D entropyDistalAgentFood;
    public Entropy1D entropyOutput;
    
    public final static double[] theta = new double[/* 8 */]
    {
    0*(/*Strict*/Math.PI/180),
    45*(/*Strict*/Math.PI/180),
    90*(/*Strict*/Math.PI/180),
    135*(/*Strict*/Math.PI/180),
    180*(/*Strict*/Math.PI/180),
    225*(/*Strict*/Math.PI/180),
    270*(/*Strict*/Math.PI/180),
    315*(/*Strict*/Math.PI/180)
    };
        
    public final static double[] xd = new double[/* 8 */]
    {
    /*Strict*/Math.cos(theta[0]),
    /*Strict*/Math.cos(theta[1]),
    /*Strict*/Math.cos(theta[2]),
    /*Strict*/Math.cos(theta[3]),
    /*Strict*/Math.cos(theta[4]),
    /*Strict*/Math.cos(theta[5]),
    /*Strict*/Math.cos(theta[6]),
    /*Strict*/Math.cos(theta[7]),
    };
        
    public final static double[] yd = new double[/* 8 */]
    {
    /*Strict*/Math.sin(theta[0]),
    /*Strict*/Math.sin(theta[1]),
    /*Strict*/Math.sin(theta[2]),
    /*Strict*/Math.sin(theta[3]),
    /*Strict*/Math.sin(theta[4]),
    /*Strict*/Math.sin(theta[5]),
    /*Strict*/Math.sin(theta[6]),
    /*Strict*/Math.sin(theta[7]),
    };
    public final static int[] lookupleft = new int[/* 8 */]
    {1,2,3,4,5,6,7,0};
    public final static int[] lookupright = new int[/* 8 */]
    {7,0,1,2,3,4,5,6};
    //this is the agent location in the 2d space
    public double x;
    public double y;

    public double orientation2D() { return theta[orientation]; }

    public Agent(int orientation, double x, double y)
        {
        this.orientation = orientation; this.x = x; this.y = y;
        this.memory_trust=new MemoryTrust(AgentsForage.INITIAL_AGENTS);
        }
    
    public void initEntropy(int bits)
    {
        int nbins=(int)Math.pow(2,bits);
        //3 entropy calculators with range [-1.1,1.1] and 256 intervals
        entropyReflexAgent=new Entropy1D(nbins, -1.1, 1.1);
        entropyDistalAgent=new Entropy1D(nbins, -1.1, 1.1);
        entropyReflexFood=new Entropy1D(nbins, -1.1, 1.1);
        entropyReflexAgentFood=new Entropy1D(nbins, -1.1, 1.1);
        entropyDistalFood=new Entropy1D(nbins, -1.1, 1.1);
        entropyDistalAgentFood=new Entropy1D(nbins, -1.1, 1.1);
        //the ouput is discretized in 8 directions
        entropyOutput=new Entropy1D(num_orientations);        
    }
    public Agent( int orientation,int timeToLive,int foodt,int agentid )
        {
        this.orientation = orientation;
        this.timeToLive = timeToLive;
        this.foodToEnd=foodt;
        this.hasFoodItem = false;
        this.foodFrom=LowPassFilter.AGENTFOOD_SIGNAL;
        this.justCreated = true;
        this.memory_trust=new MemoryTrust(AgentsForage.INITIAL_AGENTS);

        initEntropy(2);
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
            ico.setFeedbackInhibition(true);
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
    public static int get_left_antenna(int orientation)
    {
        return lookupleft[orientation];
    }
    public static int get_right_antenna(int orientation)
    {
        return lookupright[orientation];
    }
    
    public void step(SimState state)
        {
        final AgentsForage af = (AgentsForage)state;
        boolean collision=false;
        //compute the new orientation and communicate
        collision=nearbyAGENTs(af, AgentsForage.maskRandomization[2]);
        if(AgentsForage.maskRandomization[2])
        processInbox(state);
        nearbyFOODs(af, false);
        //check if the agent got some food from the food points
        //if(currentSurface(af)>0) this.hasFoodItem=true;
        if(isIcoLearner)
        {
            ico.range=num_orientations;
            ico.saturation=1.0;
            MutableDouble3D reflexMux=new MutableDouble3D(0.0,0.0,0.0);
            MutableDouble3D distalMux=new MutableDouble3D(0.0,0.0,0.0);
            //lookup table to optmize speed
            leftantennadir=get_left_antenna(orientation);
            rightantennadir=get_right_antenna(orientation);
            //if a collision was detected react but using a delayed response (to be fixed)
            //if(collision) reflexMux.x=1/(1.5* af.agentDiameter); 
            //reflexMux.x=avoidance vs agents without food
            //reflexMux.x=reflexAgentSensors[orientation];
            reflexMux.x=reflexAgentSensors[rightantennadir]-reflexAgentSensors[leftantennadir];

            if(!hasFoodItem)
            {//reflexMux.z=attraction to agents with food
            //reflexMux.y=attraction to food points
            reflexMux.y=reflexFoodSensors[leftantennadir]-reflexFoodSensors[rightantennadir];
            //reflexMux.z=reflexAgentFoodSensors[orientation];
            reflexMux.z=reflexAgentFoodSensors[leftantennadir]-reflexAgentFoodSensors[rightantennadir];
            }
            //here we need to use a proportional gain
            //reflexMux.x=avoidance vs agents without food
            //distalMux.x=distalAgentSensors[orientation];
            distalMux.x=distalAgentSensors[rightantennadir]-distalAgentSensors[leftantennadir];

            if(!hasFoodItem)
            {   
            //reflexMux.y=attraction to food points
            distalMux.y=distalFoodSensors[leftantennadir]-distalFoodSensors[rightantennadir];
            //reflexMux.z=attraction to agents with food only if it doesn't have food
            distalMux.z=distalAgentFoodSensors[leftantennadir]-distalAgentFoodSensors[rightantennadir];
            }
            try {
                lpx0.updateFilter(reflexMux);
                lpx1.updateFilter(distalMux);
            } catch (Exception ex) {
                Logger.getLogger(Agent.class.getName()).log(Level.SEVERE, null, ex);
            }

            ico.calculate(lpx0.getOutput(), lpx1.getOutput());  
            entropyReflexAgent.setData(lpx0.getOutput(LowPassFilter.AGENT_SIGNAL));
            entropyDistalAgent.setData(lpx1.getOutput(LowPassFilter.AGENT_SIGNAL));
            entropyReflexFood.setData(lpx0.getOutput(LowPassFilter.FOOD_SIGNAL));
            entropyReflexAgentFood.setData(lpx0.getOutput(LowPassFilter.AGENTFOOD_SIGNAL));
            entropyDistalFood.setData(lpx1.getOutput(LowPassFilter.FOOD_SIGNAL));
            entropyDistalAgentFood.setData(lpx1.getOutput(LowPassFilter.AGENTFOOD_SIGNAL));
            
            try {
                //get the angular change discretized as multiple of 45 degrees
                if(af.OPEN_LOOP)
                    orientation+=af.random.nextInt(8);
                else
                    orientation += ico.getDiscreteOutput();
            } catch (Exception ex) {
                Logger.getLogger(Agent.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Fatal error aborting simulation");
                state.kill();
            }
        }

        if (orientation > 7) orientation = 0;
        if (orientation < 0) orientation = 7;
        entropyOutput.setData(orientation);
        x += xd[orientation];
        y -= yd[orientation];
        //update the agent to the new position
        if(TOROIDAL_WORLD)
        {
          x=af.agentgrid.stx(x);
          y=af.agentgrid.stx(y);
          af.agentgrid.setObjectLocation(this,new Double2D(x,y));
           
        }//if we are in the boundary of the world evokes a reaction
        else
        {
        if (x >= af.agentgrid.width) 
        {
            x = af.agentgrid.width - 1;
            orientation=af.random.nextInt(8);
        }
        else if (x < 0) 
        {x = 0;orientation=af.random.nextInt(8);}
        if (y >= af.agentgrid.height) 
        {y = af.agentgrid.height - 1; orientation=af.random.nextInt(8);}
        else if (y < 0) 
        {y = 0;        orientation=af.random.nextInt(8);}
        af.agentgrid.setObjectLocation(this,new Double2D(x,y));
        }
        //decide what to do for the next step
        //act(af);
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

    public boolean nearbyFOODs(AgentsForage af,boolean needsCommunicate)
    {
           for(int i=0;i<num_orientations;i++) {
            reflexFoodSensors[i] = 0;
            distalFoodSensors[i] = 0;
        }    
        //final double dfood=af.FOOD_DIAMETER*2;
        final double dmaxreflex = af.REFLEX_AGENT_MAX;

        final Bag nearbyFoods = af.ground.getObjectsWithinDistance(new Double2D(x,y),af.DISTAL_AGENT_MAX,false,false);
        for(int i=0;i<nearbyFoods.numObjs;i++)
        {
            final Food fp = (Food)(nearbyFoods.objs[i]);
            final double foodDistance = Math.sqrt((fp.x-x)*(fp.x-x)+(fp.y-y)*(fp.y-y));  
            //[TO DO]the food status reset is made by food points and not here
            //if(foodDistance<(af.foodCatchDiameter) && fp.hasFoodItem())
                    //resetFoodStatus();
            if (foodDistance < dmaxreflex  && foodDistance>af.AGENT_DIAMETER)  // it's within reflex range
                {
                final int octant = sensorForPoint(fp.x,fp.y);  // figure the octant
                reflexFoodSensors[octant] = /*Strict*/Math.max(reflexFoodSensors[octant],1/foodDistance);
                }
            if(foodDistance>dmaxreflex && foodDistance<af.DISTAL_AGENT_MAX)//is within distal range
                {
                final int octant = sensorForPoint(fp.x,fp.y);  // figure the octant
                 distalFoodSensors[octant] = /*Strict*/Math.max(distalFoodSensors[octant],1/foodDistance);
                }
        }
        return true;
    }
    
    /** find local agents and if possible communicate*/
    public boolean nearbyAGENTs(AgentsForage mavdemo,boolean needsCommunicate)
        {
        for(int i=0;i<num_orientations;i++) {
            reflexAgentSensors[i] = 0;
            reflexAgentFoodSensors[i]=0;
            distalAgentSensors[i] = 0;
            distalAgentFoodSensors[i]=0;
        }
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
                final int octant = sensorForPoint(mav.x,mav.y);  // figure the octant
                if(!mav.hasFoodItem)
                reflexAgentSensors[octant] = /*Strict*/Math.max(reflexAgentSensors[octant],1/mavDistance);
                else
                reflexAgentFoodSensors[octant] = /*Strict*/Math.max(reflexAgentFoodSensors[octant],1/mavDistance);  
                }
                if(mavDistance>dmaxreflex && mavDistance<dmaxdistal)//is within distal range
                {
                final int octant = sensorForPoint(mav.x,mav.y);  // figure the octant
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

    public int sensorForPoint(double px, double py)
        {
        int o = 0;
        // translate to origin
        px -= x; py -= y;

        // rotate 45/2 degrees clockwise about the origin
        final double xx = px * cosTheta + py * (sinTheta);
        final double yy = px * (-sinTheta) + py * cosTheta;
        //double xx=px;
        //double yy=py;
        // Now we've divided it into octants of 0--45, 45--90, etc.
        // for each sensor area.  The border between octants is
        // arbitrarily, not evenly, assigned to the octants, because
        // it results in fewer if/then statements/
        
        if (!(xx == 0.0 && yy == 0.0))
            {
            if (xx > 0)         // right side
                {
                if (yy > 0)     // quadrant 1
                    {
                    if (xx > yy)  o = 7;
                    else o = 6;
                    }
                else            // quadrant 4
                    {
                    if (xx > -yy) o = 0;
                    else o = 1;
                    }
                }
            else                // left side
                {
                if (yy > 0)     // quadrant 2
                    {
                    if (-xx > yy)  o = 4;
                    else o = 5;
                    }
                else            // quadrant 3
                    {
                    if (-xx > -yy) o = 3;
                    else o = 2;
                    }
                }  // hope I got that right!
            }
            
        // now rotate to be relative to MAV's orientation
        //o += orientation; 
        if (o >= 8) o = o % 8;
        return o;
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

    }
    
