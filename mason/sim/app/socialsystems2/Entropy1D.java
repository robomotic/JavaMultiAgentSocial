/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sim.app.socialsystems2;

import org.mozilla.javascript.tools.idswitch.Main;

/**
 *
 * @author epokh
 */
public class Entropy1D extends Histogram{
private double H=0;
private double[] p = null;
private double h_prev=0;
private double h_delta=0;
Entropy1D(int nBins, double xmin, double xmax)
{
    super(nBins,xmin,xmax);
    p = new double[nBins];
}
Entropy1D(int nBins)
{
    super(nBins);
    p = new double[nBins];
}
public static void main(String[] args)
{
    Entropy1D test1=new Entropy1D(4,-1.1,1.1);
    System.out.println("Test Entropy");
    test1.setData(-1);
    test1.setData(0);
    test1.setData(1);
    System.out.println("H= "+test1.computeEntropy(2));
    System.out.println("L= "+test1.underFlows+" S="+test1.overFlows);
    
    Entropy1D test=new Entropy1D(4,-1.0,1.0);
    test.setData(-0.5);
    test.setData(-0.6);
    test.setData(0.0);
    test.setData(0.0);
    test.setData(0.5);
    test.setData(0.8);
    System.out.println(test.printIt());
}
public void normalizeFrequency()
{
    for(int i=0;i<bins.length;i++)
    {
        p[i]=(double)bins[i]/T;
    }    
    
}
public double computeEntropy(int base)
{
    //reset the entropy
    H=0;
    //normalize the frequencies
    normalizeFrequency();
    for(int i=0;i<bins.length;i++)
    {
        //if the base is an integer bigger than 0
        if(base>0)
        {
         //[TO FIX]: this is very wrong the asymptotic limit of entropy arghh
         if(p[i]>0)
            H-=p[i]*(Math.log(p[i])/Math.log(base));   
        }//else compute the natural logarithm
        else H-=p[i]*Math.log(p[i]);
    }
    //calculate the derivative of the entropy
    h_delta=H-h_prev;
    h_prev=H;
    //return the entropy of this discretization
    return H;
}
public double getEntropy()
{
    return -H;
}
public double getDerivative()
{
    return h_delta;
}

public void resetEntropy()
{
    reset();
    H=0;
    p = new double[nBins];
}
}
