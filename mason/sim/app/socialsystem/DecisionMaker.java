/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package sim.app.socialsystem;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import sim.util.MutableInt3D;

public /*strictfp*/ class DecisionMaker implements java.io.Serializable
    {

    private Vector list=null;
    public Hashtable lookupReflex = null;
    /*there are 3 components for the left, right and error signal
    *
     *
     *
     */
    private MutableInt3D x0l;
    private MutableInt3D x0r;
    private MutableInt3D x0_error;
    private MutableInt3D x1l;
    private MutableInt3D x1r;
    private MutableInt3D x1_error;

    public int max_dist_int=2;
    //there are 3 behaviours ordered as
     public static final int B_AGENT=0;
     public static final int B_FOOD=1;
     public static final int B_AGENTFOOD=2;

    private int neworientation;
    private int hasFood=0;
 
        public DecisionMaker()
        {
            list=new Vector();
            lookupReflex=new Hashtable();
            //init the signals
            x0l=new MutableInt3D(0, 0, 0);
            x0r=new MutableInt3D(0, 0, 0);
            x0_error=new MutableInt3D(0, 0, 0);
            x1l=new MutableInt3D(0, 0, 0);
            x1r=new MutableInt3D(0, 0, 0);
            x1_error=new MutableInt3D(0, 0, 0);
            initLookupReflex();
        }

        //build a lookup reflex for the agent so that he knows
        //what orientation to choose according to the error
        public void initLookupReflex()
        {
            lookupReflex.put(new Integer(-3), new Integer(0));
            lookupReflex.put(new Integer(-2), new Integer(1));
            lookupReflex.put(new Integer(-1), new Integer(2));
            lookupReflex.put(new Integer(0), new Integer(0));
            lookupReflex.put(new Integer(1), new Integer(-2));
            lookupReflex.put(new Integer(-2), new Integer(-1));
            lookupReflex.put(new Integer(-3), new Integer(0));
        }

        //find the reaction give the error from the hashtable
        public int getReaction(int deltaerror)
        {
            Integer temp=new Integer(0);
            if(lookupReflex.containsKey(new Integer(deltaerror)))
                temp=(Integer)lookupReflex.get(new Integer(deltaerror));
            return temp.intValue();
            
        }

        public void setFood(boolean f)
        {
            hasFood=1;
        }
        public DecisionMaker(double w1,double w2,double w3)
        {
            this();

            hasFood=0;
        }

         public void reset() { 
             list.clear();
             x0l.setTo(0,0,0);
             x0r.setTo(0,0,0);
             x0_error.setTo(0,0,0);
             x1l.setTo(0,0,0);
             x1r.setTo(0,0,0);
             x1_error.setTo(0,0,0);
             hasFood=0;
         }

        public void addInfo( DecisionInfo di )
        {
            list.add(di);
        }

        public void computeError()
        {
            Enumeration e=list.elements();
            DecisionInfo di=null;
            while(e.hasMoreElements()){
              di=(DecisionInfo)(e.nextElement());
              switch(di.direction)
              {
                  case DecisionInfo.LEFT:
                      if(di.isAgent && !di.isFood)
                      {    x0l.x+=di.intdistance;x1l.x+=di.intdistance;}
                      else if(!di.isAgent && di.isFood)
                      {x0l.y+=di.intdistance;x1l.y+=di.intdistance;}
                      else if(di.isAgent && di.isFood)
                      {x0l.z+=di.intdistance;x1l.z+=di.intdistance;}
                      break;

                  case DecisionInfo.RIGHT:
                      if(di.isAgent && !di.isFood)
                      {    x0r.x+=di.intdistance;x1r.x+=di.intdistance;}
                      else if(!di.isAgent && di.isFood)
                      {    x0r.y+=di.intdistance;x1r.y+=di.intdistance;}
                      else if(di.isAgent && di.isFood)
                      {    x0r.z+=di.intdistance;x1r.z+=di.intdistance;}
                      break;
                  default:
                      break;

              }
            }
            //subtract component by component the left - right signals
            x0_error=x0l.subtract(x0l, x0r);
            x1_error=x1l.subtract(x1l, x1r);
            //saturation();
        }

        public void computeTotalDecision(int orientation)
        {
            /* avoidance (+):
             * obstacle on the left, turn clockwise=increase orientation
             * obstacle on the right, turn anticlockwise=decrease orientation
             * attraction for food (-):
             * food point on the left, turn anticlockwise
             * food point on the right, turn clockwise
             * attraction for agent with food (-)
             */
            neworientation=orientation+getReaction(x0_error.x);
            if(hasFood==0)
                neworientation=neworientation+getReaction(x0_error.y)+getReaction(x0_error.z);
            if(neworientation<Agent.N)
                neworientation=8+neworientation;
            neworientation=neworientation%8;
            System.out.println("O:"+orientation+"N:"+neworientation+"E:"+x0_error.y);
        }

        private void saturation()
        {
            if(x0_error.x>1)
                x0_error.x=1;
            else if(x0_error.x<-1)
                x0_error.x=-1;
            if(x0_error.y>1)
                x0_error.y=1;
            else if(x0_error.y<-1)
                x0_error.y=-1;
            if(x0_error.z>1)
                x0_error.z=1;
            else if(x0_error.z<-4)
                x0_error.z=-1;
        }
        public int getNextOrientation()
        {
            return neworientation;
        }

        public MutableInt3D getX0Error()
        {
            return x0_error;
        }

        public MutableInt3D getX1Error()
        {
            return x1_error;
        }

    }
