/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sim.portrayal.simple;

import java.awt.Color;
import sim.app.socialsystemsanalog.*;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.simple.OvalPortrayal2D;

/**
 *
 * @author epokh
 */
public class FoodOvalPortrayal2D  extends OvalPortrayal2D{

    public FoodOvalPortrayal2D(Color blue, double FOOD_DIAMETER, boolean b) {
        super(blue,FOOD_DIAMETER, b);
    }
public void draw(Food object, Graphics2D graphics, DrawInfo2D info)
        {
        Rectangle2D.Double draw = info.draw;
        final double width = draw.width*scale + offset;
        final double height = draw.height*scale + offset;

        graphics.setPaint(paint);
        //here we should set the color from the object
        graphics.setColor(object.getColor());
        // we are doing a simple draw, so we ignore the info.clip

        if (info.precise)
            {
            preciseEllipse.setFrame(info.draw.x - width/2.0, info.draw.y - height/2.0, width, height);
            if (filled) graphics.fill(preciseEllipse);
            else graphics.draw(preciseEllipse);
            return;
            }
            
        final int x = (int)(draw.x - width / 2.0);
        final int y = (int)(draw.y - height / 2.0);
        int w = (int)(width);
        int h = (int)(height);
                
        // draw centered on the origin
        if (filled)
            graphics.fillOval(x,y,w,h);
        else
            graphics.drawOval(x,y,w,h);
        }
}
