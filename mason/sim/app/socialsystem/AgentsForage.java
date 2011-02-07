/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package sim.app.socialsystem;

import sim.engine.*;
import sim.field.grid.*;
import sim.util.Bag;
import sim.util.Int2D;
import sim.util.IntBag;
import sim.util.MutableInt2D;


public /*strictfp*/ class AgentsForage extends SimState
    {

    public static final int HOME_XMIN = 75;
    public static final int HOME_XMAX = 75;
    public static final int HOME_YMIN = 75;
    public static final int HOME_YMAX = 75;
    public static final boolean IMMORTAL=true;
    public static final int FOOD_XMIN = 25;
    public static final int FOOD_XMAX = 25;
    public static final int FOOD_YMIN = 25;
    public static final int FOOD_YMAX = 25;

    public static final int MAX_AGENTS_PER_LOCATION = 10;
    public static final int TIME_TO_LIVE = 1000;
    public static final int FOOD_RETENTION=30;
    public static final int NEW_AGENTS_PER_TIME_STEP = 1;
    public static final int MAX_AGENTS = 0;
    public static final int INITIALANTS = 1;
    //the size of the world
    public static final int GRID_HEIGHT = 100;
    public static final int GRID_WIDTH = 100;
    //how many food points there are in the field
    public static final int FOOD_POINTS = 100;
    //how far an agent can communicate in terms of blocks
    public int MAX_COMM_RANGE=10;
    //the minimum allowed distance between food points
    public int MIN_FOOD_RANGE=8;

    public DoubleGrid2D foodgrid = new DoubleGrid2D(GRID_WIDTH, GRID_HEIGHT, 0);
    public SparseGrid2D buggrid = new SparseGrid2D(GRID_WIDTH, GRID_HEIGHT);
    public DoubleGrid2D summarygrid= new DoubleGrid2D(GRID_WIDTH, GRID_HEIGHT,0);

    // a couple of objects to be shared by all ants in the simulation
    DecisionMaker decisionMaker = new DecisionMaker();
    DecisionInfo decisionInfo = new DecisionInfo();

    public int getMaxCommunicationRange()
    {
        return MAX_COMM_RANGE;
    }
    public void setMaxCommunicationRange(int range)
    {
        this.MAX_COMM_RANGE=range;
    }

    public int getMinFoodDistance()
    {
        return MIN_FOOD_RANGE;
    }

    public void setMinFoodDistance(int dist)
    {
        MIN_FOOD_RANGE=dist;
    }
    
    public AgentsForage(long seed)
        { 
        super(seed);
        random.setSeed(seed);
        }
        
    public int foodCollected = 0;

    private void randomizeFoodPoints()
    {
        //reset the food field
        foodgrid.setTo(0.0);
        MutableInt2D point=new MutableInt2D();
        //place food points in the world but try to keep a uniform distribution
        //to avoid aggreagations
        for( int k = 0 ; k < FOOD_POINTS ; k++ )
        {
            if(findFreeSpace(point)==false)
            foodgrid.field[point.x][point.y] = 1.0;

        }
    }

    private boolean findFreeSpace(MutableInt2D point)
    {
            //find the neihbours
            point.x=random.nextInt(GRID_WIDTH);
            point.y=random.nextInt(GRID_HEIGHT);
            
            //one point is in the area
            boolean onePoint=false;
            for( int l = point.x-MIN_FOOD_RANGE ; l < point.x+MIN_FOOD_RANGE ; l++ )
            {
                for( int m = point.y-MIN_FOOD_RANGE ; m < point.y+MIN_FOOD_RANGE ; m++ )
                {
                    if(foodgrid.field[foodgrid.stx(l)][foodgrid.sty(m)]>0.0)
                        onePoint=true;
                }
            }
            //recursive call to find free points
            if(onePoint)
                findFreeSpace(point);
            else return onePoint;
            return onePoint;

    }
    public void start()
        {
        super.start();  // clear out the schedule

        // make new grids
        buggrid = new SparseGrid2D(GRID_WIDTH, GRID_HEIGHT);
        foodgrid = new DoubleGrid2D(GRID_WIDTH, GRID_HEIGHT);
        summarygrid = new DoubleGrid2D(GRID_WIDTH, GRID_HEIGHT);

        foodCollected = 0;

        summarygrid.setTo(0);
        randomizeFoodPoints();
        // generate a certain number of agents for every time step
        Steppable antFarm = new Steppable()
            {
            public void step(SimState state)
                {
                for(int x=0 ; x<NEW_AGENTS_PER_TIME_STEP && numberOfAgents<MAX_AGENTS ; x++)
                    {
                    Agent bug = new Agent( random.nextInt(8),TIME_TO_LIVE,FOOD_RETENTION,x );
                    buggrid.setObjectLocation(bug,random.nextInt(GRID_WIDTH),random.nextInt(GRID_HEIGHT));
                    bug.toDiePointer = schedule.scheduleRepeating(bug);
                    numberOfAgents++;
                    }
                }
            };

        // generate a certain number of agents for every time step
        Steppable avgField = new Steppable()
            {
            public void step(SimState state)
                {

                }
            };

        numberOfAgents = 0;


        // generate all the agents in the same time in random positions
        for(int x=0;x<INITIALANTS;x++)
            {
            Agent bug = new Agent(random.nextInt(8),TIME_TO_LIVE,FOOD_RETENTION,x );
            buggrid.setObjectLocation(bug,random.nextInt(GRID_WIDTH),random.nextInt(GRID_HEIGHT));
            bug.toDiePointer = schedule.scheduleRepeating(bug);
            numberOfAgents++;
            }

        // Schedule the ant farm to happen after the AntsForage
        schedule.scheduleRepeating(Schedule.EPOCH,1,antFarm,1);
        schedule.scheduleRepeating(Schedule.EPOCH,1,avgField,1);

        }

    public int numberOfAgents = 0;

    public static void main(String[] args)
        {
        doLoop(AgentsForage.class, args);
        System.exit(0);
        }    
    }
    
    
    
    
    
