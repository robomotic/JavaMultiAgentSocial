/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package sim.app.socialsystemsanalog;

import sim.engine.*;
import sim.display.*;
import sim.portrayal.grid.*;
import java.awt.*;
import javax.swing.*;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.portrayal.simple.CircledPortrayal2D;
import sim.portrayal.simple.LabelledPortrayal2D;
import sim.portrayal.simple.OrientedPortrayal2D;
import sim.portrayal.simple.OvalPortrayal2D;

public class AgentsForageWithUI extends GUIState
    {
    public Display2D display;
    public JFrame displayFrame;

    public Display2D display2;
    public JFrame displayFrame2;
    
    ContinuousPortrayal2D foodPortrayal = new ContinuousPortrayal2D();
    ContinuousPortrayal2D agentPortrayal = new ContinuousPortrayal2D();
    FastValueGridPortrayal2D summaryPortrayal = new FastValueGridPortrayal2D("Statistics");
    public static void main(String[] args)
        {
        AgentsForageWithUI antsForage = new AgentsForageWithUI();
        Console c = new Console(antsForage);
        c.setVisible(true);
        }
    
    public AgentsForageWithUI() { super(new AgentsForage(System.currentTimeMillis())); }
    public AgentsForageWithUI(SimState state) { super(state); }
    
    public static String getName() { return "Social System Analog version"; }
    
    public void setupPortrayals()
        {
        AgentsForage af = (AgentsForage)state;
        // obstacle portrayal needs no setup
        foodPortrayal.setField(af.ground);
        foodPortrayal.setPortrayalForAll(
            new CircledPortrayal2D(
                new LabelledPortrayal2D(
                       
                       new OvalPortrayal2D(Color.blue, af.FOOD_DIAMETER, true),
                        af.AGENT_DIAMETER, null, Color.red, true), // 'null' means to display toString() for underlying object, rather than a predefined label
                0, af.FOOD_DIAMETER+10, Color.DARK_GRAY, true));
       
        agentPortrayal.setField(af.agentgrid);
        agentPortrayal.setPortrayalForAll(
            new CircledPortrayal2D(
                new LabelledPortrayal2D(
                    new OrientedPortrayal2D(
                        new OvalPortrayal2D(af.AGENT_DIAMETER),
                        0,af.AGENT_DIAMETER), 
                        af.AGENT_DIAMETER, null, Color.blue, true), // 'null' means to display toString() for underlying object, rather than a predefined label
                0, af.AGENT_DIAMETER+10, Color.green, true));
        
        summaryPortrayal.setField(af.summarygrid);
        //summaryPortrayal.setPortrayalForAll( new sim.portrayal.simple.RectanglePortrayal2D(Color.green) );
        summaryPortrayal.setMap(new sim.util.gui.SimpleColorMap(
                                      0,
                                      100,
                                      new Color(0,0,0,0),
                                      new Color(0,255,0,255) ));

        
        // reschedule the displayer
        display.reset();
        display2.reset();
        // redraw the display
        display.repaint();
        display2.repaint();
        }
    
    public void start()
        {
        super.start();  // set up everything but replacing the display
        // set up our portrayals
        setupPortrayals();
        }
            
    public void load(SimState state)
        {
        super.load(state);
        // we now have new grids.  Set up the portrayals to reflect that
        setupPortrayals();
        }

    public void init(Controller c)
        {
        super.init(c);
        
        // Make the Display2D.  We'll have it display stuff later.
        display = new Display2D(500,500,this,1); // at 400x400, we've got 4x4 per array position
        displayFrame = display.createFrame();
        c.registerFrame(displayFrame);   // register the frame so it appears in the "Display" list
        displayFrame.setVisible(true);

        // attach the portrayals from bottom to top
        display.attach(foodPortrayal,"Food");
        display.attach(agentPortrayal,"Agents");
        displayFrame.setTitle("Agents");
        // specify the backdrop color  -- what gets painted behind the displays
        display.setBackdrop(Color.yellow);
         // Make the Display2D.  We'll have it display stuff later.
        display2 = new Display2D(400,400,this,1); // at 400x400, we've got 4x4 per array position
        displayFrame2 = display2.createFrame();
        displayFrame2.setTitle("Statistic");
        c.registerFrame(displayFrame2);   // register the frame so it appears in the "Display" list
        displayFrame2.setVisible(false);
        // specify the backdrop color  -- what gets painted behind the displays
        display2.setBackdrop(Color.GRAY);
        // attach the portrayals from bottom to top
        display2.attach(summaryPortrayal,"Summary");
        }
    //This method is necessary to show up the Model inspector!
    public Object getSimulationInspectedObject() { return state; }
    
    //this method is called when the user close the java window
    public void quit()
        {
        super.quit();
        
        // disposing the displayFrame automatically calls quit() on the display,
        // so we don't need to do so ourselves here.
        if (displayFrame!=null) {
            displayFrame.dispose();
            displayFrame2.dispose();
        }
        //displayFrame = null;  // let gc
        //displayFrame2=null;
        //display = null;       // let gc
        //display2=null;
        }
        
    }
    
