/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sim.app.socialsystems2;

import java.awt.Color;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Bag;
import sim.util.Double2D;

/**
 *
 * @author epokh
 */
public class Food implements Steppable {
    private double value;
    private double max_value;
    private double min_value;
    private int ID;
    public double x,y;
    private boolean agent_consumption=true;
    public Food(double xx,double yy)
    {
        value=100;
        min_value=1;
        max_value=100;
        x=xx;
        y=yy;
    }
    public void setPosition(double x,double y)
    {
                this.x = x; this.y = y;
        
    }  
    public void setFoodConsumption(boolean flag)
    {
        this.agent_consumption=flag;
    }
    public void step(SimState state) {
        //look for neighbouring agents and update its status
        final AgentsForage af = (AgentsForage)state;
        final Bag nearbyMavs = af.agentgrid.getObjectsWithinDistance(new Double2D(x,y),af.FOOD_DIAMETER/2,false,false);
        for(int i=0;i<nearbyMavs.numObjs;i++)
            {

            final Agent mav = (Agent)(nearbyMavs.objs[i]);
            mav.resetFoodStatus(LowPassFilter.FOOD_SIGNAL);
            //if the food place containes limited units
            if(agent_consumption)
            value=value-10;
            }
        //reset the food status if the food point is depleted
        if(value<1 && agent_consumption) value=max_value;
    }
    //@Override: this shows the string when object is selected in the simulator
    public String toString()
    {
        return "Quantity="+value;
    }
    
    public Color getColor()
    {
        int colorValue=(int) Math.round(value*255/max_value);
        return new Color(0,0,colorValue);
    }
    public boolean hasFoodItem()
    {
        if(value>0) return true;
        else return false;
    }

}
