/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sim.app.socialsystemsanalog;

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
    //this corresponds to the x component
    public static int AGENT_SIGNAL=0;
    //this corresponds to the y component
    public static int FOOD_SIGNAL=1;
    //this corresponds to the z component
    public static int AGENTFOOD_SIGNAL=2;
    //a normalization constant for the low pass filter so that the output is max=1.0
    private MutableDouble3D normconst=null;
    private MutableDouble3D next_output=new MutableDouble3D();
    private boolean isnormalized;
    public LowPassFilter(double b)
    {
        buffer0=new MutableDouble3D(0.0, 0.0, 0.0);
        buffer1=new MutableDouble3D(0.0, 0.0, 0.0);
        beta=b;
        isnormalized=true;
    }
    
    public LowPassFilter(double b, MutableDouble3D delta) throws Exception
    {
        buffer0=new MutableDouble3D(0.0, 0.0, 0.0);
        buffer1=new MutableDouble3D(0.0, 0.0, 0.0);
        beta=b;
        normconst=new MutableDouble3D();
        isnormalized=false;
        normalizeFilter(delta);
        isnormalized=true;

    }

    //normalize the filter to have a max peak of 1.0
    public void normalizeFilter( MutableDouble3D maxinput ) throws Exception
    {
        resetBuffer();
        MutableDouble3D zeroinput=new MutableDouble3D();
        for(int k=0;k<10000;k++)
        {
          if(k>=10 && k<=1000) zeroinput.setTo(maxinput);
          else zeroinput.setTo(0.0);
          updateFilter(zeroinput);
          normconst.maxima(next_output);
        }
        resetBuffer();
        if(normconst.length()<=0)
            normconst.setTo(1.0);
    }
    public void resetBuffer()
    {
        buffer1.zero();
        buffer0.zero();
        next_output.zero();
    }
    //update the 3 filters in parallel very useful!
    public void updateFilter(MutableDouble3D dinput) throws Exception
    {
        next_output.zero();
        //implements the equation
        // x(t)=b*x(t-1)+(1-b)*input
        // a first order differential equation
        buffer0.multiplyIn(beta);
        dinput.multiplyIn(1-beta);
        buffer1.add(buffer0, dinput);
        //buffer[1]=beta*(buffer[0])+(1-beta)*input;
        buffer0.setTo(buffer1);
        //buffer[0]=buffer[1];
        //return buffer[1];
        if(isnormalized)
        {    next_output.divideIn(buffer1,normconst);
             if(normconst.x==0 || normconst.y==0 || normconst.z==0)
                 throw new Exception("Division by 0 in output filter");
        }
        else
            next_output=buffer1;

    }
    public MutableDouble3D getOutput()
    {
        return next_output;
    }
                

    //get the output from every signal
    public double getOutput(int index)
    {
        switch(index)
        {
            case 0:
                return next_output.x;
            case 1:
                return next_output.y;
            case 2:
                return next_output.z;
            default:
                return next_output.x;
        }
    }
   
}
