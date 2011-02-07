/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sim.app.socialsystem;

import sim.util.MutableDouble3D;
import sim.util.MutableInt3D;

/**
 *
 * @author epokh
 */
public class LowPassFilter {
    double beta;
    private MutableDouble3D buffer0;
    private MutableDouble3D buffer1;

    public LowPassFilter(double b)
    {
        buffer0=new MutableDouble3D(0.0, 0.0, 0.0);
        buffer1=new MutableDouble3D(0.0, 0.0, 0.0);
        beta=b;
    }

    //update the 3 filters in parallel very useful!
    public void updateFilter(MutableInt3D input)
    {
        //implements the equation
        // x(t)=b*x(t-1)+(1-b)*input
        // a first order differential equation
        buffer0.multiplyIn(beta);
        MutableDouble3D dinput=new MutableDouble3D(input);
        dinput.multiplyIn(1-beta);
        buffer1.add(buffer0, dinput);
        //buffer[1]=beta*(buffer[0])+(1-beta)*input;
        buffer0.setTo(buffer1);
        //buffer[0]=buffer[1];
        //return buffer[1];
    }

    //get the output from every signal
    public double getOutput(int index)
    {
        switch(index)
        {
            case DecisionMaker.B_AGENT:
                return buffer1.x;
            case DecisionMaker.B_FOOD:
                return buffer1.y;
            case DecisionMaker.B_AGENTFOOD:
                return buffer1.z;
            default:
                return buffer1.x;
        }
    }
   
}
