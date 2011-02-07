/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package sim.app.socialsystem;
import java.awt.*;

public class DecisionInfo implements java.io.Serializable
    {

    public static final int CENTER=0;
    public static final int LEFT=1;
    public static final int RIGHT=2;


    public int direction;
    public int intdistance=0;
    public double doubledistance=0.0;
    public boolean isReflex=false;
    public boolean isDistal=false;
    public boolean isAgent=false;
    public boolean isFood=false;
    public double foodAmount=0.0;

    public DecisionInfo(){
        direction=CENTER;

    }

    public void reset()
    {
     direction=CENTER;
    intdistance=0;
    doubledistance=0.0;
    isReflex=false;
    isDistal=false;
    isAgent=false;
    isFood=false;
    foodAmount=0.0;
    }
    public void setDirection(int dir){
        if(dir!= LEFT && dir!=RIGHT)
        {
            this.direction=CENTER;
        }
        else this.direction=dir;
    }

    public void setReflex() {
        isReflex=true;
        isDistal=false;
    }

    public void setDistal() {
        isReflex=false;
        isDistal=true;
    }

    public void setUniformDistance(Point origin,Point target)
    {
        intdistance=Math.max(Math.abs(origin.x-target.x), Math.abs(origin.y-target.y));
    }

        
    }
