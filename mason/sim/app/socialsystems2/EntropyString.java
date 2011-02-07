/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sim.app.socialsystems2;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;

/**
 *
 * @author epokh
 */
public class EntropyString {
    private static double[] inputDist;
//a word made of symbols from an alphabet
private String Xword;
private String lastMotorWord;
private String Yword;
private String lastSensorWord;
private String XYword;
//the word size= how long is the word in terms of symbols
private int sensor_wordsize=0;
//the word size= how long is the word in terms of symbols
private int motor_wordsize=0;
//a frequency table to remember symbol occurencies for predictive signal
private Hashtable Yf ;
//a frequency table to remember symbol occurencies for reflex signal
private Hashtable Xf ;
//a frequency table for joint occurencies for the X and Y word
private Hashtable XYf ;
//number of samples for the X variable
private int NX=0;
//number of samples for the Y variable
private int NY=0;
//number of samples for the XY variable
private int NXY=0;
//entropy for X and Y variables
private double HX=0;
private double HY=0;
//joint entropy H(X,Y)
private double HXY=0;
//binary representation length: default is 8 bits
private int BASE=8;
private boolean normalize;
private double estimate=0;
private double sigma=0;;
private double nybias;
private String approach;
private double upperbound=Math.pow(2,BASE);
private double lowerbound=0;
private double lambda;
private double nxbias;
private double nxybias;
private int X_BASE;
private int Y_BASE;
private SparseMatrix XYsparse;
static double acceptableError = 0.000000000001;
static int noOfIterations = 5000;
EntropyString(int multiple, boolean flag, String method)
{
    //initialize the hastable for frequency lookup
    Yf=new Hashtable();
    Xf=new Hashtable();
    XYf=new Hashtable();
    //the X and Y word have the same word size for consistency
    sensor_wordsize=multiple*BASE;
    motor_wordsize=multiple*BASE;
    X_BASE=BASE;
    Y_BASE=BASE;
    XYsparse=new SparseMatrix(motor_wordsize, sensor_wordsize);
    Xword=new String();
    Yword=new String();
    XYword=new String();
    normalize=flag;
    approach=method.substring(0, 1);
    approach.toLowerCase();
}
public void setBase(int xwordbase,int ywordbase,int motor_wordsize,int sensor_wordsize)
{
      X_BASE=xwordbase;
      Y_BASE=ywordbase;
      this.motor_wordsize=X_BASE*motor_wordsize;
      this.sensor_wordsize=Y_BASE*sensor_wordsize;
      XYsparse=new SparseMatrix(X_BASE*motor_wordsize, Y_BASE*sensor_wordsize);
}
//add a symbol in the buffer and when the word size is reached put in the table
public boolean lastMotorWord(int value)
{
    String symbol=toLZ(value, X_BASE);
    //[FIX] the symbol must be enforced: symbol must belong in the alphabet
    if(Xword.length()==0)
    {
        Xword=symbol;
    }
    else if(Xword.length()<motor_wordsize)
    {
        Xword=Xword+symbol;
    }    
    
    if(Xword.length()==motor_wordsize)
    {
        //push_X(Xword);
        lastMotorWord=Xword;
        Xword="";
        return true;
    }else return false;
}
//add a symbol in the buffer and when the word size is reached put in the table
public boolean addSensorSymbol(int value)
{
    String symbol=toLZ(value, Y_BASE);
    //[FIX] the symbol must be enforced: symbol must belong in the alphabet
    if(Yword.length()==0)
    {
        Yword=symbol;
    }
    else if(Yword.length()<sensor_wordsize)
    {
        Yword=Yword+symbol;
    }    
    
    if(Yword.length()==sensor_wordsize)
    {
        //push_Y(Yword);
        lastSensorWord=Yword;
        Yword="";
        return true;
    }
    else return false;
}
//add a symbol in the buffer and when the word size is reached put in the table
    public void addEmpoweredSymbol(int motor, int sensor) {
        //every n motor words are pushed add the sensor word
        if (lastMotorWord(motor)) {
            addSensorSymbol(sensor);
            String symbol = lastMotorWord + lastSensorWord;
            //[FIX] the symbol must be enforced: symbol must belong in the alphabet
            if (XYword.length() == 0) {
                XYword = symbol;
            } else if (XYword.length() < (motor_wordsize + sensor_wordsize)) {
                XYword = XYword + symbol;
            }
            if (XYword.length() == (motor_wordsize + sensor_wordsize)) {
                //push_XY(XYword);
                XYsparse.add(lastSensorWord, lastMotorWord, 1);
                XYword = "";
            }
 

        }
    }
//add a symbol in the buffer and when the word size is reached put in the table
public void addXYSymbol(int xvalue,int yvalue)
{
    lastMotorWord(xvalue);
    addSensorSymbol(yvalue);
    String symbol_x=toLZ(xvalue, BASE);
    String symbol_y=toLZ(yvalue, BASE);
    String symbol=symbol_y+symbol_x;
    //[FIX] the symbol must be enforced: symbol must belong in the alphabet
    if(XYword.length()==0)
    {
        XYword=symbol;
    }
    else if(XYword.length()<(motor_wordsize+sensor_wordsize))
    {
        XYword=XYword+symbol;
    }    
    
    if(XYword.length()==(motor_wordsize+sensor_wordsize))
    {
        push_XY(XYword);
        XYword="";
    }
}

//push the joint variable XY in the frequency table: used for joint distribution
private void push_XY(String word)
{
 //if this word has already occurred incremente the frequency
    if(XYf.containsKey(word))
    {
        //increase the frequency count
        Integer frequency=(Integer)XYf.get(word);
        frequency++;
        XYf.remove(word);
        XYf.put(word,frequency);
    }//if it has not yet occurred
    else
    {   //set to 1
        XYf.put(word, new Integer(1));
    }
    //increment the number of total observations
    NXY++;   
    
}
private void push_X(String word)
{
    //if this word has already occurred incremente the frequency
    if(Xf.containsKey(word))
    {
        //increase the frequency count
        Integer frequency=(Integer)Xf.get(word);
        frequency++;
        Xf.remove(word);
        Xf.put(word,frequency);
    }//if it has not yet occurred
    else
    {   //set to 1
        Xf.put(word, new Integer(1));
    }
    //increment the number of total observations
    NX++;
}
private void push_Y(String word)
{
    //if this word has already occurred incremente the frequency
    if(Yf.containsKey(word))
    {
        //increase the frequency count
        Integer frequency=(Integer)Yf.get(word);
        frequency++;
        Yf.remove(word);
        Yf.put(word,frequency);
    }//if it has not yet occurred
    else
    {   //set to 1
        Yf.put(word, new Integer(1));
    }
    //increment the number of total observations
    NY++;
}

public double getYEntropy_old(int base)
{
    //reset the main variables
    estimate=0;
    sigma=0;
    NY=0;
    upperbound=Math.pow(2, Y_BASE);
    //get the list of words
    Enumeration e = Yf.keys();
    double nbins=Yf.size();
    while (e.hasMoreElements()) {
        //the number of occurencies for that word
        Integer hn = (Integer) Yf.get(e.nextElement());
        //the histogram count
        double logf = 0;
        if (hn != 0) {
            logf = Math.log(hn);
        }
        NY = NY + hn;
        estimate = estimate - hn * logf;
        sigma = sigma + hn * logf * logf;
    }
    //return the biased estimate of entropy
    estimate=estimate/(double)NY;
    sigma   =Math.sqrt( (sigma/(double)NY-Math.pow(estimate,2))/((double)NY-1) );
    estimate=estimate+Math.log(NY);//+Math.log((upperbound-lowerbound)/nbins);
    nybias   =-(nbins-1)/(2*(double)NY);
    //conversion to unbiased estimate
    if (approach.equals("u"))
    {
        estimate=estimate-nybias;
        nybias=0;
    }
    //conversion to minimum mse estimate
    if(approach.equals("m"))
    {estimate=estimate-nybias;
    nybias=0;
    lambda=Math.pow(estimate,2)/(Math.pow(estimate,2)+Math.pow(sigma,2));
    nybias   =(1-lambda)*estimate;
    estimate=lambda*estimate;
    sigma   =lambda*sigma;
    }
    //base transformation
    if(base>0)
    {estimate=estimate/Math.log(base);
    nybias   =nybias   /Math.log(base);
    sigma   =sigma   /Math.log(base);
    }
    HY=estimate;
    return estimate;
}
public double getXEntropy(boolean biased, int base)
{
    return XYsparse.getXEntropy(biased, base);
}

public double getYEntropy(boolean biased,int base)
{
    return XYsparse.getYEntropy(biased, base);
}
public double getXEntropy_old(int base)
{
       //reset the main variables
    estimate=0;
    sigma=0;
    NX=0;
    upperbound=Math.pow(2, X_BASE);
    //get the list of words
    Enumeration e = Xf.keys();
    double nbins=Xf.size();
    while (e.hasMoreElements()) {
        //the number of occurencies for that word
        Integer hn = (Integer) Xf.get(e.nextElement());
        //the histogram count
        double logf = 0;
        if (hn != 0) {
            logf = Math.log(hn);
        }
        NX = NX + hn;
        estimate = estimate - hn * logf;
        sigma = sigma + hn * logf * logf;
    }
    //return the biased estimate of entropy
    estimate=estimate/(double)NX;
    sigma   =Math.sqrt( (sigma/(double)NX-Math.pow(estimate,2))/((double)NX-1) );
    estimate=estimate+Math.log(NX);//Math.log((upperbound-lowerbound)/nbins);
    nxbias   =-(nbins-1)/(2*(double)NX);
    //conversion to unbiased estimate
    if (approach.equals("u"))
    {
        estimate=estimate-nxbias;
        nxbias=0;
    }
    //conversion to minimum mse estimate
    if(approach.equals("m"))
    {estimate=estimate-nybias;
    nxbias=0;
    lambda=Math.pow(estimate,2)/(Math.pow(estimate,2)+Math.pow(sigma,2));
    nxbias   =(1-lambda)*estimate;
    estimate=lambda*estimate;
    sigma   =lambda*sigma;
    }
    //base transformation
    if(base>0)
    {estimate=estimate/Math.log(base);
    nxbias   =nxbias   /Math.log(base);
    sigma   =sigma   /Math.log(base);
    }
    HX=estimate;
    return estimate;
}
public double getXYEntropy(boolean biased,int base)
{
    return XYsparse.computeJointEntropy(biased, base);
}

public double getXYEntropy_old(boolean biased,int base)
{
    //reset the main variables
    estimate=0;
    sigma=0;
    NXY=0;
    //get the list of words
    Enumeration e = XYf.keys();
    double nbins=XYf.size();
    while( e. hasMoreElements() ){
        //the number of occurencies for that word
        Integer hn=(Integer) XYf.get(e.nextElement());
        //the histogram count
        double logf=0;
         if(hn!=0) logf=Math.log(hn);
         else logf=0;
        NXY=NXY+hn;
        estimate=estimate-hn*logf;
        sigma=sigma+hn*logf*logf;
    }
    //return the biased estimate of entropy
    estimate=estimate/(double)NXY;
    sigma   =Math.sqrt( (sigma/(double)NXY-Math.pow(estimate,2))/((double)NXY-1) );
    estimate=estimate+Math.log(NXY);//+Math.log((upperbound-lowerbound)/nbins);
    nxybias   =-(nbins-1)/(2*(double)NXY);
    //conversion to unbiased estimate
    if (approach.equals("u"))
    {
        estimate=estimate-nxybias;
        nxybias=0;
    }
    //conversion to minimum mse estimate
    if(approach.equals("m"))
    {estimate=estimate-nxybias;
    nxybias=0;
    lambda=Math.pow(estimate,2)/(Math.pow(estimate,2)+Math.pow(sigma,2));
    nxybias   =(1-lambda)*estimate;
    estimate=lambda*estimate;
    sigma   =lambda*sigma;
    }
    //base transformation
    if(base>0)
    {estimate=estimate/Math.log(base);
    nxybias   =nxybias   /Math.log(base);
    sigma   =sigma   /Math.log(base);
    }
    HXY=estimate;
    return estimate;
}
public double getConditionedEntropy(int base)
{
    double hxy=-getXEntropy(false,base)+getXYEntropy(false,base);
    return hxy;
}

public double getMutualInformation(boolean biased,int base)
{
    //double hxy=getXEntropy(base)+getYEntropy(base)-getXYEntropy(base);
    return XYsparse.computeMutualInfo(biased, 2);
    
    //return getXEntropy(base)+getYEntropy(base)-getXYEntropy(base);
}

public static String toLZ( int discretized, int len )
   {
   // converts integer to left-zero padded string, len  chars long.
   String s = Integer.toBinaryString(discretized);
   if ( s.length() > len ) return s.substring(0,len);
   else if ( s.length() < len ) // pad on left with zeros
      return "000000000000000000000000000".substring(0, len - s.length ()) + s;
   else return s;
   } // end toLZ

public static String toLZ( int[] discretized, int len )
   {
   // converts integer to left-zero padded string, len  chars long.
   String compose="";
   for(int k=0;k<discretized.length;k++)
   {String s=Integer.toBinaryString(discretized[k]);
   if ( s.length() > len ) compose=compose + s.substring(0,len);
   else if ( s.length() < len ) // pad on left with zeros
      compose=compose+("000000000000000000000000000".substring(0, len - s.length ()) + s);
   else compose=compose+s;}
   return compose;
   } // end toLZ

public static void generateFrequencies(int p00,int p01,int p10,int p11, EntropyString test)
{
     for (int k = 1; k <= 100; k++) {
        if (p00 > 0) {
            test.addXYSymbol(0, 0);
            p00--;
        }
        if (p01 > 0) {
            test.addXYSymbol(0, 1);
            p01--;
        }
        if (p10 > 0) {
            test.addXYSymbol(1, 0);
            p10--;
        }
        if (p11 > 0) {
            test.addXYSymbol(1, 1);
            p11--;
        }
    }   
}
public double getChannelCapacity()
{
    return XYsparse.computeChannelCapacity("p(sensor|action)");
}
    public static void testChannelCapacity() {
        // Read the channel matrix using the ReadChannelFile Class
        boolean readFromChanFile = true; 
        boolean miUniform = false; 
        ReadFile channelFileReader = new ReadFile("/home/epokh/test.csv");
        channelFileReader.readChannel();
        Channel channel = channelFileReader.getChannel();
        
        // Switch on the kind of channel and calculate capacity accordingly.
        // Channels can be basic, conditional or multi-user. 
        BlahutArimoto ba = new BlahutArimoto(channel, acceptableError, noOfIterations);
        double result = 0.0;
        switch (channel.kind) {
            case (Channel.BASIC):
                // Find capacity or mutual information of a basic channel
                if (miUniform == true) {
                    result = IT.MIuniformInput(channel.getMatrix());
                    inputDist = IT.unifromDist(channel.noOfInputs());
                } else {
                    ba.calculateCapacity();
                    result = ba.getCapacity();
                    inputDist = ba.getMaxInputDist();
                }
                break;

            default:
        }
        if (ba.possibleError == 0) {
            System.out.println("  Complete, after " + ba.iteration + " iterations");
        } else if (ba.possibleError <= ba.acceptableError) {
            System.out.println("  Capacity calculated to within acceptable error, in " + ba.iteration + " iterations");
        } else if (ba.possibleError > ba.acceptableError) {
            System.out.println("  NOT COMPLETE: Performed the maximum number of iterations: " + ba.iteration + "\n" +
                    " and still not with acceptable error rate\n " +
                    " increase the maximum number of iterations (with flag -i <int>)\n" +
                    " or increase the acceptable error (with flag -e <double>");
        }

        System.out.printf("  The Channel Capacity is: %1$6.5g +/- %2$6.5g", (result + (ba.possibleError / 2)), (ba.possibleError / 2));
        System.out.printf("  and there are " + channel.noOfInputs() + " possible input events\n");
    }
public static void testLearningEntropy()
{
    EntropyString test=new EntropyString(1,true,"biased");
    //before learning as in paper: 40% and 10% in a total of 100 samples
    int p00=40;
    int p01=10;
    int p10=10;
    int p11=40;
    generateFrequencies(p00, p01, p10, p11, test);
    System.out.println("H(X)="+test.getXEntropy(true,2));
    System.out.println("H(Y)="+test.getYEntropy(true,2));
    System.out.println("H(X,Y)="+test.getXYEntropy(true,2));
    System.out.println("I(X,Y)="+test.getMutualInformation(true,2));
    test=new EntropyString(1,true,"biased");
    p00=40;
    p01=40;
    p10=10;
    p11=10;
   generateFrequencies(p00, p01, p10, p11, test);
    System.out.println("Good learning");
    System.out.println("H(X)="+test.getXEntropy(true,2));
    System.out.println("H(Y)="+test.getYEntropy(true,2));
    System.out.println("H(X,Y)="+test.getXYEntropy(true,2));
    System.out.println("I(X,Y)="+test.getMutualInformation(true,2));
    test=new EntropyString(1,true,"biased");
    p00=50;
    p01=50;
    p10=0;
    p11=0;
   generateFrequencies(p00, p01, p10, p11, test);
    System.out.println("Perfect learning");
    System.out.println("H(X)="+test.getXEntropy(true,2));
    System.out.println("H(Y)="+test.getYEntropy(true,2));
    System.out.println("H(X,Y)="+test.getXYEntropy(true,2));
    System.out.println("H(X|Y)="+test.getConditionedEntropy(2));
    System.out.println("I(X,Y)="+test.getMutualInformation(true,2));
    test=new EntropyString(1,true,"biased");
    p00=25;
    p01=25;
    p10=25;
    p11=25;
   generateFrequencies(p00, p01, p10, p11, test);
    System.out.println("Imperfect learning");
    System.out.println("H(X)="+test.getXEntropy(true,2));
    System.out.println("H(Y)="+test.getYEntropy(true,2));
    System.out.println("H(X,Y)="+test.getXYEntropy(true,2));
    System.out.println("H(X|Y)="+test.getConditionedEntropy(2));
    System.out.println("I(X,Y)="+test.getMutualInformation(true,2));
    
    
}

public static void testMutualInfo()
{
    EntropyString test=new EntropyString(0,true,"biased");
    test.setBase(3,2,1,1);
    Random generator = new Random();
    for(int k=1;k<=100;k++)
    {
     test.addEmpoweredSymbol(generator.nextInt(8), generator.nextInt(4));       
    }
    System.out.println("MI: "+test.getMutualInformation(true,2));
    System.out.println("H(X,Y): "+test.getXYEntropy(true,2));
    System.out.println("H(X): "+test.getXEntropy(true,2));
    System.out.println("H(Y): "+test.getYEntropy(true,2));
    test=new EntropyString(0,true,"biased");
    test.setBase(3,2,1,1);
    for(int k=1;k<=100;k++)
    {
     if(generator.nextBoolean())
        test.addEmpoweredSymbol(0, 1);
     else
        test.addEmpoweredSymbol(1, 1);
         
    }
    System.out.println("MI: "+test.getMutualInformation(true,2));
     System.out.println("H(X,Y): "+test.getXYEntropy(true,2));
     System.out.println("H(X): "+test.getXEntropy(true,2));
     System.out.println("H(Y): "+test.getYEntropy(true,2));

}
public static void main(String[] args){
testMutualInfo();
}


}
