/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package sim.app.socialsystem;

import sim.portrayal.*;
import sim.portrayal.simple.*;
import sim.util.*;
import sim.engine.*;
import java.awt.*;
import java.util.LinkedList;

public /*strictfp*/ class Agent extends OvalPortrayal2D implements Steppable
    {

    public static final boolean TOROIDAL_WORLD = true;

    // type of Agent
    public static final int ORIENTED_AGENT = 0;
    public static final int NSEW_AGENT = 1;
    public static final int EIGHT_NEIGHBOURS_AGENT = 2;
    //definition of signal directions and ranges
    public static final int REFLEX=1;
    public static final int DISTAL=2;
    public static final int CENTER=0;
    public static final int LEFT=1;
    public static final int RIGHT=2;
    //what agent type we are using
    public static final int AGENT_TYPE = ORIENTED_AGENT;
    //how many directions we have
    public static final int N = 0;
    public static final int NE = 1;
    public static final int E = 2;
    public static final int SE = 3;
    public static final int S = 4;
    public static final int SW = 5;
    public static final int W = 6;
    public static final int NW = 7;
    private static final int num_orientations=8;
    //how much life has the agent left
    public int timeToLive;
    //hom much time the agent has left with food
    public int foodToEnd;
    //the agent current orientation
    public int orientation;
    //how many times the agent has collected food
    private double foodCollected;
    //old coordinates of the agent
    public int oldx;
    public int oldy;
    //if we want to use Q learning
    private boolean isQlearner=false;
    private QBrain qbrain;
    //if we want to use ICO learning
    private boolean isIcoLearner=true;
    //a low pass filter for the input reflex events
    private LowPassFilter lpx0;
    //a low pass filter for the input distal events
    private LowPassFilter lpx1;
    //Ico learning algorithm
    IcoLearner ico=new IcoLearner();
    
    public double getAntennaFood(){return lpx0.getOutput(DecisionMaker.B_FOOD);}
    public double getAntennaAgent(){return lpx0.getOutput(DecisionMaker.B_AGENT);}
    public double getAntennaParasite(){return lpx0.getOutput(DecisionMaker.B_AGENTFOOD);}

    public double getWeightFood(){return ico.getWeight(DecisionMaker.B_FOOD);}
    public double getWeightAgent(){return ico.getWeight(DecisionMaker.B_AGENT);}
    public double getWeightParasite(){return ico.getWeight(DecisionMaker.B_AGENTFOOD);}

    public double getFoodCollected(){return foodCollected;}
    public double getFoodToEnd(){return foodToEnd;}
    public int getOrientation(){return orientation;}
    public boolean getHasFoodItem() { return hasFoodItem; }
    public void setHasFoodItem(boolean val) { hasFoodItem = val; }
    public boolean hasFoodItem;
    //the robot ID or in other words its name
    private int ID;
    //every robot has a FIFO queue of incoming messages called the inbox
    private LinkedList inbox = new LinkedList();
    //the maximum number of incoming messages
    private int INBOX_MAX=100;
    //every agent has a FIFO queue of outgoing messages called the outbox
    private LinkedList outbox=new LinkedList();
    //the maximum number of outcoming messages
    private int OUTBOX_MAX=100;
    //the weights relative to the distal signal
    private MutableDouble3D x1_weights;
    //number of messages from the inbox that were processed
    private int number_read_msg;
    //number of messages sent
    private int number_sent_msg;

    public int getSentMessages()
    {
        return number_sent_msg;
    }

    public int getProcessedMessages()
    {
        return number_read_msg;
    }
    boolean justCreated;

    public Agent( int orientation,
                int timeToLive,int foodt,int agentid )
        {
        this.orientation = orientation;
        this.timeToLive = timeToLive;
        this.foodToEnd=foodt;
        hasFoodItem = false;
        justCreated = true;
        lpx0=new LowPassFilter(0.5);
        lpx1=new LowPassFilter(0.9);
        //initially the distal signal does not contribute
        x1_weights=new MutableDouble3D(0.0, 0.0, 0.0);
        if(isQlearner)
        {
            qbrain=new QBrain(9,num_orientations);
            qbrain.initActions(num_orientations);
            qbrain.initStates();
        }
        this.ID=agentid;
        this.number_read_msg=0;
        this.number_sent_msg=0;
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
    //this function process the inbox
    public void processInbox()
    {
        // fetch the first message in the FIFO queue and process it
        // data is retrieved from the head of queue
        if(!inbox.isEmpty()){
        Message msg = (Message)inbox.removeFirst();
        //if the agent is not trying anything at the moment:

        //and he trust the new agent try the new symbol!
        switch(msg.getType())
        {
            case DecisionMaker.B_AGENT:
                x1_weights.x=msg.getValue();
                break;

            case DecisionMaker.B_AGENTFOOD:
                x1_weights.y=msg.getValue();
                break;

            case DecisionMaker.B_FOOD:
                x1_weights.z=msg.getValue();
                break;
        }
        number_read_msg++;
        }


    }
    
    protected void addInformation( final SimState state, int x, int y,int signaltype,int direction )
        {
        final AgentsForage af = (AgentsForage)state;
        final DecisionInfo di = af.decisionInfo;
        final DecisionMaker decisionMaker = af.decisionMaker;

        //if the world is toroidal we need to find the information in the other
        //side
        if( TOROIDAL_WORLD )
            {
            x = (x+AgentsForage.GRID_WIDTH)%AgentsForage.GRID_WIDTH;
            y = (y+AgentsForage.GRID_HEIGHT)%AgentsForage.GRID_HEIGHT;
            }
        else
            {
            //if the world is limited there is no information to add
            if( x < 0 || x >= AgentsForage.GRID_WIDTH || y < 0 || y >= AgentsForage.GRID_HEIGHT )
                return;
            }
        //if there are some agents in that direction
        if(af.buggrid.getObjectsAtLocation(x,y)!=null)
        {
            //there should be only 1 agent per location
            //if the neighbouring agent has food we go for it
            if(af.buggrid.getObjectsAtLocation(x,y).numObjs > 0)
            {
                di.reset();
                di.setDirection(direction);
                di.isAgent=true;
                if(signaltype==REFLEX) di.setReflex();
                else di.setDistal();
                di.setUniformDistance((af.buggrid.getObjectLocation(this)).toPoint(),new Point(x,y));
                decisionMaker.addInfo(di);
            }
            else//otherwise we don't go for it!
            {

            }
        }
        
        if(af.foodgrid.field[x][y] > 0.0 )
            {
             di.reset();
               di.setDirection(direction);
             //if the current agent has food the point is an obstacle
             if(hasFoodItem)
             {
                 di.isFood=false;
                 di.isAgent=true;
             }
             else
             {
                 di.isFood=true;
                 di.isAgent=false;
             }

             di.foodAmount=1.0;
             if(signaltype==REFLEX) di.setReflex();
             else di.setDistal();
             di.setUniformDistance((af.buggrid.getObjectLocation(this)).toPoint(),new Point(x,y));
             decisionMaker.addInfo(di);

            }
        

        }
         

    public void decideAction( final SimState state, final int myx, final int myy)
        {

        final AgentsForage af = (AgentsForage)state;
        final DecisionMaker decisionMaker = af.decisionMaker;

        decisionMaker.reset();

        // collect the sensory information for the new grid model
        // this should be done separately in the model, but whatever....

        switch( AGENT_TYPE )
            {
            case ORIENTED_AGENT:
                switch( orientation )
                    {
                    case N:
                        addInformation( state, myx-1, myy-1, REFLEX,LEFT );  // forward-left
                        addInformation( state, myx-1, myy-4, DISTAL,LEFT );  // forward-left
                        //addInformation(state, myx-1,myy-2, REFLEX,LEFT);
                        //addInformation( state, myx+1, myy-1, REFLEX,RIGHT );  // forward-right
                        addInformation( state, myx+1, myy-4, DISTAL,RIGHT );  // forward-left
                        addInformation(state, myx+1,myy-2, REFLEX,RIGHT);
                        break;
                    case NE:
                        addInformation( state, myx,   myy-1, REFLEX,LEFT);  // forward-left
                        //addInformation( state, myx,   myy-2, REFLEX,LEFT);  // forward-left
                        //addInformation( state, myx+1, myy+1, REFLEX,CENTER );        // forward
                        //addInformation( state, myx+1, myy, REFLEX,RIGHT);  // forward-right
                        addInformation( state, myx+2, myy, REFLEX,RIGHT);  // forward-right
                        break;
                    case E:
                        addInformation( state, myx+1, myy-1, REFLEX,LEFT );  // forward-left
                        //addInformation( state, myx+2, myy-1, REFLEX,LEFT );  // forward-left
                       // addInformation( state, myx+1, myy,   REFLEX,CENTER);        // forward
                        //addInformation( state, myx+1, myy+1,REFLEX,RIGHT );  // forward-right
                        addInformation( state, myx+2, myy+1,REFLEX,RIGHT );  // forward-right
                        break;
                    case SE:
                        addInformation( state, myx+1, myy, REFLEX,LEFT );  // forward-left
                        addInformation( state, myx,   myy+1, REFLEX,RIGHT );  // forward-right
                      
                        break;
                    case S:
                        addInformation( state, myx+1, myy+1,REFLEX,LEFT );  // forward-left
                        addInformation( state, myx+1, myy+4,DISTAL,RIGHT);  // forward-right
                       // addInformation( state, myx,   myy-1,REFLEX,CENTER);        // forward
                        addInformation( state, myx-1, myy+1,REFLEX,RIGHT);  // forward-right
                         addInformation( state, myx-1, myy+4,DISTAL,RIGHT);  // forward-right
                        break;
                    case SW:
                        addInformation( state, myx,   myy+1,REFLEX,LEFT );  // forward-left
                       // addInformation( state, myx-1, myy-1,REFLEX,CENTER );        // forward
                        addInformation( state, myx-1, myy, REFLEX,RIGHT);  // forward-right
                        break;
                    case W: addInformation( state, myx-1, myy+1, REFLEX,LEFT  );  // forward-left
                      //  addInformation( state, myx-1, myy, REFLEX,CENTER );        // forward
                        addInformation( state, myx-1, myy-1,REFLEX,RIGHT );  // forward-right
                        break;
                    case NW: addInformation( state, myx-1, myy,REFLEX,LEFT  );  // forward-left

                      //  addInformation( state, myx-1, myy+1,REFLEX,CENTER );        // forward
                        addInformation( state, myx,   myy-1,REFLEX,RIGHT );  // forward-right
                        break;
                    }
                break;
            }

        }

    public void step( final SimState state )
        {
        final AgentsForage af = (AgentsForage)state;
        final DecisionMaker decisionMaker = af.decisionMaker;
        decisionMaker.reset();
        Int2D location = af.buggrid.getObjectLocation(this);

        int bestx = location.x;
        int besty = location.y;

        //if the agent found a food point communicates to others
        if(af.foodgrid.field[bestx][besty]>0)
        {    this.foodCollected+=1.0;
             this.hasFoodItem=true;
             this.foodToEnd=AgentsForage.FOOD_RETENTION;

             //compose a message indicating that a food point was found and that
             //the weights associated with the finding where those declared
             Message msgFood=new Message(this.ID, x1_weights.y, DecisionMaker.B_FOOD);
             //find the neihbours
             Bag neighbors=new Bag();
             IntBag xpos=new IntBag();
             IntBag ypos=new IntBag();
             af.buggrid.getNeighborsMaxDistance(location.x, location.y, af.MAX_COMM_RANGE, false,neighbors,xpos,ypos);
            for(int i=0;i<neighbors.numObjs;i++)
            {
                Agent receiver=(Agent)neighbors.get(i);

                //only sends to others and not himself!
                if(receiver.ID!=this.ID)
                {
                    receiver.pushMessage(msgFood);
                    this.number_sent_msg++;
                }

            }
        }
        decisionMaker.setFood(this.hasFoodItem);
        if(this.foodToEnd<1)
        {
             this.foodToEnd=AgentsForage.FOOD_RETENTION;
             this.hasFoodItem=false;
        }
        else if(this.hasFoodItem)this.foodToEnd--;
        
        if( justCreated )
            {
            //this.orientation = N;//go north
            justCreated = false;
            }

        //gather data
        decideAction(state, location.x, location.y);

        //compute the next location where to move according to the last orientation
        switch( AGENT_TYPE )
            {
            case ORIENTED_AGENT:
                switch( orientation )
                    {
                    case N:
                        bestx=location.x;besty=location.y-1;
                        break;
                    case NE:
                        bestx=location.x+1;besty=location.y-1;
                        break;
                    case E:
                         bestx=location.x+1;besty=location.y;
                        break;
                    case SE:
                         bestx=location.x+1;besty=location.y+1;
                        break;
                    case S:
                         bestx=location.x;besty=location.y+1;
                        break;
                    case SW:
                           bestx=location.x-1;besty=location.y+1;
                        break;
                    case W:
                         bestx=location.x-1;besty=location.y;
                        break;
                    case NW:
                          bestx=location.x-1;besty=location.y-1;
                        break;
                    default:
                        bestx=location.x;besty=location.y;
                    }
                break;

            }

        //process the inbox
        processInbox();
        
        decisionMaker.computeError();
        if(isIcoLearner)
        {
            //low pass filtering on the inputs
            lpx0.updateFilter(decisionMaker.getX0Error());
            lpx1.updateFilter(decisionMaker.getX1Error());
            //adjust the weights
            ico.calculate(decisionMaker.getX0Error(),decisionMaker.getX1Error());
        }
        /*
        if(isQlearner)
        {

            //update the Q Values according to the last choice!
            qbrain.updateQvalues(decisionMaker.getX0Error(), decisionMaker.getX1Error());
             //compute the best action after reading the new status
            this.orientation=qbrain.chooseAction(decisionMaker.getX0Error(), decisionMaker.getX1Error());
        }
        //compute the best action according to the information acquired and the current orientation
        else
        {

        }
        */
        decisionMaker.computeTotalDecision(this.orientation);
        //update the next orientation of the agent with constant speed
        this.orientation=decisionMaker.getNextOrientation();
        Bag p = null;
        // update the position of the agent on the grid
        if(TOROIDAL_WORLD)
        {    af.buggrid.setObjectLocation(this,af.buggrid.stx(bestx),af.buggrid.sty(besty));
             p= af.buggrid.getObjectsAtLocation(new Int2D(af.buggrid.stx(bestx),af.buggrid.sty(besty)));
             //if a collision is detected the agent changes orientation and goes back to
             //the original position in the grid
            if (p!=null && p.numObjs > 1)
            {
            this.orientation=af.random.nextInt(8);
            af.buggrid.setObjectLocation(this,location.x,location.y);
            }

        }
        else
        {
            af.buggrid.setObjectLocation(this,bestx,besty);
            p= af.buggrid.getObjectsAtLocation(bestx,besty);

             if (p!=null && p.numObjs > 1)
            {
            this.orientation=af.random.nextInt(8);
            af.buggrid.setObjectLocation(this,oldx,oldy);
            }
        }

        
        //check if we the agent is outside the grid only if not toroidal world
        if(!TOROIDAL_WORLD)
        {
            if( ( besty >= AgentsForage.GRID_HEIGHT) || ( besty <= 0 ))
            {
                this.orientation = (orientation+4)%8; // rotate 180
                 af.buggrid.setObjectLocation(this,location.x,location.y);

            }
            if(( bestx >= AgentsForage.GRID_WIDTH ) || ( bestx <= 0 ) )
            {
                 this.orientation = (orientation+4)%8; // rotate 180
                  af.buggrid.setObjectLocation(this,location.x,location.y);
            }
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
        }

    // color code to identify if the agent has food or not
    private Color noFoodColor = Color.black;
    private Color foodColor = Color.red;

    public final void draw(Object object, Graphics2D graphics, DrawInfo2D info)
        {
        if( hasFoodItem )
            graphics.setColor( foodColor );
        else
            graphics.setColor( noFoodColor );
        
        // this code was stolen from OvalPortrayal2D
        int x = (int)(info.draw.x - info.draw.width / 2.0);
        int y = (int)(info.draw.y - info.draw.height / 2.0);
        int xctr = (int)(info.draw.x);
        int yctr = (int)(info.draw.y);
        int width = (int)(info.draw.width);
        int height = (int)(info.draw.height);
        graphics.fillOval(x,y,width, height);

         switch( AGENT_TYPE )
            {
            case ORIENTED_AGENT:
                switch( this.orientation )
                    {
                    case N:
                        //plot reflex antennas
                        graphics.setColor(Color.blue);
                        graphics.drawLine(xctr, yctr, xctr+4,yctr-2);
                        graphics.drawLine(xctr, yctr, xctr-4,yctr-2);
                        //plot distal antennas
                        graphics.setColor(Color.green);
                        graphics.drawLine(xctr, yctr, xctr+2,yctr-10);
                        graphics.drawLine(xctr, yctr, xctr-2,yctr-10);
                        graphics.drawLine(xctr, yctr, xctr+4,yctr-10);
                        graphics.drawLine(xctr, yctr, xctr-4,yctr-10);
                        break;
                    case NE:
                        //plot reflex antennas
                        graphics.setColor(Color.blue);
                        graphics.drawLine(xctr, yctr, xctr+2,yctr-4);
                        graphics.drawLine(xctr, yctr, xctr+4,yctr-2);
                        //plot distal antennas
                        graphics.setColor(Color.green);
                        graphics.drawLine(xctr, yctr, xctr,yctr-10);
                        graphics.drawLine(xctr, yctr, xctr+2,yctr-10);
                        graphics.drawLine(xctr, yctr, xctr+10,yctr);
                        graphics.drawLine(xctr, yctr, xctr+10,yctr-4);
            
                        break;
                    case E:
                        //plot reflex antennas
                        graphics.setColor(Color.blue);
                        graphics.drawLine(xctr, yctr, xctr+2,yctr-4);
                        graphics.drawLine(xctr, yctr, xctr+2,yctr+4);
                        //plot distal antennas
                        graphics.setColor(Color.green);
                        graphics.drawLine(xctr, yctr, xctr+10,yctr-6);
                        graphics.drawLine(xctr, yctr, xctr+10,yctr-4);
                        graphics.drawLine(xctr, yctr, xctr+10,yctr+4);
                        graphics.drawLine(xctr, yctr, xctr+10,yctr+6);
                        break;
                    case SE:
                        //plot reflex antennas
                        graphics.setColor(Color.blue);
                        graphics.drawLine(xctr, yctr, xctr+4,yctr+2);
                        graphics.drawLine(xctr, yctr, xctr,yctr+4);
                        //plot distal antennas
                        graphics.setColor(Color.green);
                        graphics.drawLine(xctr, yctr, xctr+10,yctr+2);
                        graphics.drawLine(xctr, yctr, xctr+10,yctr+4);
                        graphics.drawLine(xctr, yctr, xctr+2,yctr+10);
                        graphics.drawLine(xctr, yctr, xctr,yctr+10);
                        break;
                    case S:
                        //plot reflex antennas
                        graphics.setColor(Color.blue);
                        graphics.drawLine(xctr, yctr, xctr+4,yctr+2);
                        graphics.drawLine(xctr, yctr, xctr-4,yctr+2);
                        //plot distal antennas
                        graphics.setColor(Color.green);
                        graphics.drawLine(xctr, yctr, xctr+2,yctr+10);
                        graphics.drawLine(xctr, yctr, xctr-2,yctr+10);
                        graphics.drawLine(xctr, yctr, xctr+4,yctr+10);
                        graphics.drawLine(xctr, yctr, xctr-4,yctr+10);
                        break;
                    case SW:
                        //plot reflex antennas
                        graphics.setColor(Color.blue);
                        graphics.drawLine(xctr, yctr, xctr,yctr+4);
                        graphics.drawLine(xctr, yctr, xctr-4,yctr+4);
                        //plot distal antennas
                        graphics.setColor(Color.green);
                        graphics.drawLine(xctr, yctr, xctr-2,yctr+2);
                        graphics.drawLine(xctr, yctr, xctr-10,yctr+4);
                        graphics.drawLine(xctr, yctr, xctr-2,yctr+10);
                        graphics.drawLine(xctr, yctr, xctr-4,yctr+10);
                        break;
                    case W:
                        //plot reflex antennas
                        graphics.setColor(Color.blue);
                        graphics.drawLine(xctr, yctr, xctr,yctr+4);
                        graphics.drawLine(xctr, yctr, xctr-4,yctr+4);
                        //plot distal antennas
                        graphics.setColor(Color.green);
                        graphics.drawLine(xctr, yctr, xctr-10,yctr-2);
                        graphics.drawLine(xctr, yctr, xctr-10,yctr-4);
                        graphics.drawLine(xctr, yctr, xctr-10,yctr+2);
                        graphics.drawLine(xctr, yctr, xctr-10,yctr+4);
                        break;
                    case NW:
                        //plot reflex antennas
                        graphics.setColor(Color.blue);
                        graphics.drawLine(xctr, yctr, xctr,yctr-4);
                        graphics.drawLine(xctr, yctr, xctr-4,yctr-2);
                        //plot distal antennas
                        graphics.setColor(Color.green);
                        graphics.drawLine(xctr, yctr, xctr-10,yctr-4);
                        graphics.drawLine(xctr, yctr, xctr-10,yctr-2);
                        graphics.drawLine(xctr, yctr, xctr+2,yctr-10);
                        graphics.drawLine(xctr, yctr, xctr,yctr-10);
                        break;
                    }
                break;
            }


        }

    
    public Stoppable toDiePointer = null;
    public void die( final SimState state )
        {
        AgentsForage agentsforage = (AgentsForage)state;
        agentsforage.numberOfAgents--;
        agentsforage.buggrid.remove( this );
        if(toDiePointer!=null) toDiePointer.stop();
        }

    }
