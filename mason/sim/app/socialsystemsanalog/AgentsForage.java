/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package sim.app.socialsystemsanalog;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import sim.engine.*;
import sim.field.continuous.Continuous2D;
import sim.field.grid.*;
import sim.util.Bag;
import sim.util.Double2D;
import sim.util.Int2D;
import sim.util.MutableDouble2D;


public /*strictfp*/ class AgentsForage extends SimState
    {
    public static final boolean IMMORTAL=true;
    public static final int TIME_TO_LIVE = 1000;
    public static final int FOOD_RETENTION=60;
    public static final int NEW_AGENTS_PER_TIME_STEP = 1;
    public static final int MAX_AGENTS = 0;
    public static final int INITIAL_AGENTS = 1;
    public static final int REFRACTORY_COMM=100;
    public static final int TRIAL_COMM=80;
    //learning parameters
    public static boolean isLearning=false;
    //learning speed
    public static double mu=1.0;
    //the size of the world
    public static final int GRID_HEIGHT = 300;
    public static final int GRID_WIDTH = 300;
    //how many food points there are in the field
    public static final int FOOD_POINTS = 1;
    //max distance for inter-agent communication
    public int MAX_COMM_RANGE=40;
    //the max distance for the reflex
    public static double REFLEX_AGENT_MAX = 40;
    //the max distance for the distal
    public static double DISTAL_AGENT_MAX=60;
    //the agent diameter
    public static double AGENT_DIAMETER=10;
    //bitmask flags for different randomization techiniques
    public static boolean[] maskRandomization={false,false,false};
    //food distribution types
    public static int[] foodStarDistribution={0,1,0,1,1,1,0,1,0};
   
    //open loop behaviour to compute empowerment
    public static boolean OPEN_LOOP=false;
    //min distance for inter-agent food interaction
    public final double foodCatchDiameter=20;
    //the food point diameter
    public final double FOOD_DIAMETER=20;
    //artificial population separation
    public final boolean IS_ARTIFICIAL=false;
    //the ratio for differentiation
    public final double RATIO=2;
    //the continous field
    public DoubleGrid2D foodgrid = new DoubleGrid2D(GRID_WIDTH, GRID_HEIGHT, 0);
    public Continuous2D ground;
    public Continuous2D agentgrid;
    public DoubleGrid2D summarygrid= new DoubleGrid2D(GRID_WIDTH, GRID_HEIGHT,0);
    //the number of agents and specialization
    public int numberOfAgents = 0;
    public int numberOfSeekers=0;
    public int numberOfParasites=0;
    //the society
    public Bag society=null;
    //and the resources
    public Bag resources=null;
    //enable/disable food consumption
    public boolean FOOD_CONSUMPTION=false;
    //estimate entropy at runtime
    private boolean ENTROPY_RUNTIME=false;
    
    private int positiveTrust;
    private int negativeTrust;
            
    //file logger for all agents
    public CsvWriter cvslogger_avoid=null;
    public CsvWriter cvslogger_output=null;
    public CsvWriter cvslogger_class=null;
    public CsvWriter cvslogger_attraction_agent=null;
    private CsvWriter cvslogger_attraction_food=null;
    
    public boolean getOpenLoop()
    {
        return OPEN_LOOP;
    }
    
    public void setOpenLoop(boolean flag)
    {
        OPEN_LOOP=flag;
    }
    public boolean getRandomizeOutput()
    {
        return maskRandomization[0];
    }
    
    public void setRandomizeOutput(boolean flag)
    {
        maskRandomization[0]=flag;
    }
   
    public boolean getFullLog()
    {
        return maskRandomization[1];
    }
    
    public void setFullLog(boolean flag)
    {
        maskRandomization[1]=flag;
    }
    public boolean getSocialMedia()
    {
        return maskRandomization[2];
    }
    
    public void setSocialMedia(boolean flag)
    {
        maskRandomization[2]=flag;
    }
    public int getMaxCommunicationRange()
    {
        return MAX_COMM_RANGE;
    }
    public void setMaxCommunicationRange(int range)
    {
        this.MAX_COMM_RANGE=range;
    }
    public boolean getAllLearning()
    {
        return isLearning;
    }
    
    public int getPositiveTrust()
    {
        return positiveTrust;
    }
    
    public int getNegativeTrust()
    {
        return negativeTrust;
    }
    public void setAllLearning(boolean flag)
    {
        if(flag ^ isLearning)
        {
            if(society!=null && society.numObjs>0)
            {
                for(int k=0;k<society.numObjs;k++)
                {                
                    Agent pointer=(Agent) society.get(k);

                }
                System.out.println("Entropy measure reset");
        }
        }
        isLearning=flag;
    }
    public int getNumberOfSeekers()
    {
        return numberOfSeekers;
    }
    public int getNumberOfParasites()
    {
        return numberOfParasites;
    }
    
    public double getLearningRate()
    {
        return mu;
    }
    public void setLearningRate(double rate)
    {
        this.mu=rate;
    }
    public boolean getFoodConsumption()
    {
        return FOOD_CONSUMPTION;
    }
    
    public void setFoodConsumption(boolean flag)
    {
        FOOD_CONSUMPTION=flag;
    }
    public AgentsForage(long seed)
        { 
        super(seed);
        random.setSeed(seed);
        }
        
    public int foodCollected = 0;

    private synchronized void uniformFoodPoints()
    {
        //reset the food field
        foodgrid.setTo(0.0);
        MutableDouble2D point=new MutableDouble2D();
        //allocate N food points in the bag
        resources=new Bag(FOOD_POINTS);
        //step for food distribution
        float xstep=GRID_WIDTH/4;
        float ystep=GRID_HEIGHT/4;
        //a counter for the shape
        int counter=0;
        
        //place food points in the world but try to keep a uniform distribution
        //to avoid aggregations
        for( int k = 1 ; k <= 3 ; k++ )
        {
            point.x=xstep*k;

            for(int l=1;l<=3;l++)
            {
            point.y=ystep*l;
            if(foodStarDistribution[counter]==1)
            {
            //generate a colorful food patch with index and proper shape
            Food fp=new Food(point.x,point.y);
            fp.setFoodConsumption(FOOD_CONSUMPTION);
            resources.add(fp);
            ground.setObjectLocation(fp,new Double2D(point));
            schedule.scheduleRepeating(fp);
            }
            counter=counter+1;
            }
 
        }

        System.out.println("Food points succesfully randomized\n");
    }
    
    private synchronized void randomizeFoodPoints()
    {
        //reset the food field
        foodgrid.setTo(0.0);
        MutableDouble2D point=new MutableDouble2D();
        //allocate N food points in the bag
        resources=new Bag(FOOD_POINTS);
        //place food points in the world but try to keep a uniform distribution
        //to avoid aggregations
        for( int k = 0 ; k < FOOD_POINTS ; k++ )
        {
            if(findFreeSpace(point)==false)
            {
            //generate a colorful food patch with index and proper shape
            Food fp=new Food(point.x,point.y);
            fp.setFoodConsumption(FOOD_CONSUMPTION);
            resources.add(fp);
            ground.setObjectLocation(fp,new Double2D(point));
            schedule.scheduleRepeating(fp);
            }
        }
        System.out.println("Food points succesfully randomized\n");
    }

    private boolean findFreeSpace(MutableDouble2D point)
    {
            //find the neihbours
            point.x=random.nextDouble()*GRID_WIDTH;
            point.y=random.nextDouble()*GRID_HEIGHT;
            //one point is in the area
            boolean onePoint=false;
            //find the mutual distances with the other points
            Bag nearbyFoods = ground.getObjectsWithinDistance(new Double2D(point.x,point.y),FOOD_DIAMETER*4,Agent.TOROIDAL_WORLD,false);
            if(nearbyFoods.numObjs>1)
                onePoint=true;
            //recursive call to find free points
            if(onePoint)
                findFreeSpace(point);
            else return onePoint;
            return onePoint;

    }
    
    public void start()
        {
        super.start();  // clear out the schedule

        // Use a Continuous2D for the MAVs.  We need to
        // compute a good discretization: such that the width of the buckets
        // is twice the width of the sensors, plus a little bit more for overlap.
        // Since the MAVs are schedulable, we'll load them into the schedule to be
        // fired each time as well.
        agentgrid = new Continuous2D(AGENT_DIAMETER * 2, GRID_WIDTH, GRID_HEIGHT);   
        foodgrid = new DoubleGrid2D(GRID_WIDTH, GRID_HEIGHT);
        summarygrid = new DoubleGrid2D(GRID_WIDTH, GRID_HEIGHT);

        foodCollected = 0;
        summarygrid.setTo(0);
        
        // We'll use a Continuous2D field for the ground regions -- but in fact there are only
        // a relatively few ground regions and so when we do hit testing etc. (see surfaceAtPoint(...)),
        // we'll just scan through the region[] array rather than go through the overhead of the
        // field.  Why dump them in the field then?  Simply so we can use a field portrayal.
        // So we make a field portrayal with one big discretization -- so it surely will draw ALL objects
        // objects during every redraw.  This also lets us just set the location of the objects to 0,0,
        // and use the objects' internal shape coordinates for handling their drawing.
        ground = new Continuous2D(FOOD_DIAMETER*2, GRID_WIDTH, GRID_HEIGHT);

        uniformFoodPoints();

        // generate a certain number of agents for every time step
        Steppable updateAgents = new Steppable()
            {
            public void step(SimState state)
                {
                numberOfSeekers=0;
                numberOfParasites=0;
                positiveTrust=0;
                negativeTrust=0;
                //log the input signals for avoidance behaviour and the corresponding weight
                String[] record_avoid=new String[society.numObjs*3+1];
                //log the category of every agent 1 is a seeker and 1 is a parasite
                String[] record_class=new String[society.numObjs+1];
                //log the input signals for agent attraction behaviour and the corresponding weight
                String[] record_agent_attraction=new String[society.numObjs*3+1];
                //log the input signals for food attraction behaviour and the corresponding weight
                String[] record_food_attraction=new String[society.numObjs*3+1];
                //log the input signals for food attraction behaviour and the corresponding weight
                String[] record_output=new String[society.numObjs+1];
                final String timestamp=Double.toString(schedule.getTime());
                record_avoid[0]=timestamp;
                record_agent_attraction[0]=timestamp;
                record_class[0]=timestamp;
                record_food_attraction[0]=timestamp;
                record_output[0]=timestamp;
                //make a bag of all the agents
                for(int k=0;k<resources.numObjs;k++)
                {
                    final Food food_obj=(Food) resources.get(k);  
                    food_obj.setFoodConsumption(FOOD_CONSUMPTION);
                }
                for(int k=0;k<society.numObjs;k++)
                {
                    final Agent agent_obj=(Agent) society.get(k);

                    //update learning flag
                    agent_obj.ico.setLearning(isLearning);
                    agent_obj.ico.setLearningRate(mu);
                    if (schedule.getTime() == 200000 && IS_ARTIFICIAL) {
                        agent_obj.ico.setWeight(LowPassFilter.AGENT_SIGNAL, 1.0 + random.nextDouble() / RATIO);
                        if (k < 5) {
                            agent_obj.ico.setWeight(LowPassFilter.FOOD_SIGNAL, 1.0 + random.nextDouble() / RATIO);
                            agent_obj.ico.setWeight(LowPassFilter.AGENTFOOD_SIGNAL, 2.0 + random.nextDouble() / RATIO);
                        } else {
                            agent_obj.ico.setWeight(LowPassFilter.FOOD_SIGNAL, 2.0 + random.nextDouble() / RATIO);
                            agent_obj.ico.setWeight(LowPassFilter.AGENTFOOD_SIGNAL, 1.0 + random.nextDouble() / RATIO);                 
                        }
                    }
                    //update the population ratio
                    if(agent_obj.ico.isSeeker())
                    {
                        numberOfSeekers++; record_class[k+1]="1";
                    }
                    else 
                    {
                        numberOfParasites++;record_class[k+1]="0";
                    }

                    //distal and reflex signal for agent avoidance
                    /*
                    record_avoid[3 * k + 1] = Integer.toString();
                    record_avoid[3 * k + 2] = Integer.toString();
                    record_avoid[3 * k + 3] = Double.toString(agent_obj.ico.getWeight();

                    record_agent_attraction[3 * k + 1] = Integer.toString();
                    record_agent_attraction[3 * k + 2] = Integer.toString();
                    record_agent_attraction[3 * k + 3] = Double.toString();

                    record_food_attraction[3 * k + 1] = Integer.toString();
                    record_food_attraction[3 * k + 2] = Integer.toString();
                    record_food_attraction[3 * k + 3] = Double.toString();
                     */
                    try {
                        record_output[k + 1] = Integer.toString(agent_obj.ico.getDiscreteOutput());
                    } catch (Exception ex) {
                        Logger.getLogger(AgentsForage.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                if(maskRandomization[1]&&initLogger())
                {
                try {
                    //write the record
                    cvslogger_avoid.writeRecord(record_avoid);
                    //end terminate it, very important otherwise WEKA doesn't like it
                    cvslogger_class.writeRecord(record_class);
                    cvslogger_attraction_agent.writeRecord(record_agent_attraction);
                    cvslogger_attraction_food.writeRecord(record_food_attraction);
                    cvslogger_output.writeRecord(record_output);
                    
                } catch (IOException ex) {
                    Logger.getLogger(AgentsForage.class.getName()).log(Level.SEVERE, null, ex);
                }
                }
                 
                for(int x=0 ; x<NEW_AGENTS_PER_TIME_STEP && numberOfAgents<MAX_AGENTS ; x++)
                    {
                    Agent agent = new Agent( random.nextInt(8),TIME_TO_LIVE,FOOD_RETENTION,x );
                    society.add(agent);
                    Int2D rndposition=new Int2D(random.nextInt(GRID_WIDTH),random.nextInt(GRID_HEIGHT));
                    agentgrid.setObjectLocation(agent,new Double2D(rndposition));
                    agent.toDiePointer = schedule.scheduleRepeating(agent);
                    numberOfAgents++;
                    }
                }
            };
            
        Steppable randomizeSociety = new Steppable()
            {
            public void step(SimState state)
                {
                //System.out.print("Randomizing society actions");
                if(OPEN_LOOP)
                    OPEN_LOOP=false;
                else
                    OPEN_LOOP=true;
                /*
                for(int k=0;k<society.numObjs;k++)
                {
                    try {
                        Agent current = (Agent) society.get(k);
                        //if the output entropy is low the agent is doing nothing
                        if (current.entropyOutput.getDerivative() <= 0.0) {
                            current.orientation = random.nextInt(8);
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(AgentsForage.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } */
                }
                
            };
            
        numberOfAgents = 0;
        society=new Bag(INITIAL_AGENTS);
        // generate all the agents in the same time in random positions
        for(int x=0;x<INITIAL_AGENTS;x++)
            {
            //creat an agent and set into the world
            Agent agent = new Agent(random.nextDouble(),TIME_TO_LIVE,FOOD_RETENTION,x );
            society.add(agent);
            Int2D rndposition=new Int2D(random.nextInt(GRID_WIDTH),random.nextInt(GRID_HEIGHT));
            agentgrid.setObjectLocation(agent,new Double2D(rndposition));
            agent.toDiePointer = schedule.scheduleRepeating(agent);
            agent.setPosition(rndposition.x, rndposition.y);
            numberOfAgents++;
            }

        // first update agent statistics
        schedule.scheduleRepeating(Schedule.EPOCH,1,updateAgents,1);
        //and then if we want a random action behaviour randomize society every 1000 simulation steps
        if(getRandomizeOutput())
        {
            schedule.scheduleRepeating(Schedule.EPOCH,2,randomizeSociety,1000);

        }
        }

    private boolean initLogger()
    {
        if(maskRandomization[1])
        {
         if(cvslogger_avoid==null)
               cvslogger_avoid=new CsvWriter("/home/epokh/DataSim/sim_avoidance.csv");
         if(cvslogger_output==null)
            cvslogger_output=new CsvWriter("/home/epokh/DataSim/sim_output.csv");
         if(cvslogger_class==null)
            cvslogger_class=new CsvWriter("/home/epokh/DataSim/sim_class.csv");
         if(cvslogger_attraction_agent==null)
            cvslogger_attraction_agent=new CsvWriter("/home/epokh/DataSim/sim_attraction_agent.csv");
         if(cvslogger_attraction_food==null)
            cvslogger_attraction_food=new CsvWriter("/home/epokh/DataSim/sim_attraction_food.csv");
        return true;
        } else return false;
    }
    
    public static void main(String[] args)
        {
        doLoop(AgentsForage.class, args);
        System.exit(0);
        }    
    }
    
    
    
    
    
