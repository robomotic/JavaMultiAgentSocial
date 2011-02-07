/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sim.app.socialsystems2;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author epokh
 */
public class SparseMatrix {
    private final int motor_wordsize;           // column word size
    private final int sensor_wordsize;            // row word size
    private final int nrows;
    private int N=0;                  //the number of total samples
    private SparseVector[] rows;   // the rows, each row is a sparse vector
    private SparseVector Psensor;   // frequencies of motor actions (generally large)
    private SparseVector Pmotor;   // frequencies of the sensor values (generally small)
    private Channel channel;
    private String[] outputNames;
    private String[] inputNames;
    private double[][] channelMatrix;
    static double acceptableError = 0.000000000001;
static int noOfIterations = 5000;
    // initialize an N-by-N matrix of all 0s
    public SparseMatrix(int motor_wordsize,int sensor_wordsize) {
        this.motor_wordsize  = motor_wordsize;
        this.sensor_wordsize  = sensor_wordsize;
        this.nrows=(int) Math.pow(2, this.sensor_wordsize);
        this.rows = new SparseVector[nrows];
        for (int i = 0; i < nrows; i++) rows[i] = new SparseVector(this.motor_wordsize);
        this.Psensor=new SparseVector(this.sensor_wordsize);
        this.Pmotor=new SparseVector(this.motor_wordsize);
    }

    public double getXEntropy(boolean biased,int base)
    {
      return computeEntropy(this.Psensor,biased,2);  
    }
    
    public double getYEntropy(boolean biased,int base)
    {
      return computeEntropy(this.Pmotor,biased,2);  
    }
        
    private double computeEntropy(SparseVector V,boolean biased,int base)
    {
        //observed entropy
        double Hobs = 0;
        //number of bins where pi!=0
        int Bstar = 0;
        for (String word : V.st) {
            double ni = V.st.get(word);
            double pi = ni / V.getN();
            if (ni > 0) {
                Hobs -= pi * Math.log(pi);
            }
        }
        if (base > 0) {
            Hobs = Hobs / Math.log(base);
        //nxbias   =nxbias   /Math.log(base);
        //sigma   =sigma   /Math.log(base);
        }
       if(!biased)
            Hobs=Hobs+((double)V.getBstar()-1)/(2*(double)V.getN());
        return Hobs;
    }
    // put A[i][j] = value
    public void put(String x, String y, double value) {
        if (x.length()<=0 || x.length() > sensor_wordsize) throw new RuntimeException("Illegal index");
        if (y.length() <= 0 || y.length()> motor_wordsize) throw new RuntimeException("Illegal index");
        rows[Integer.parseInt(x, 2)].put(y, value);
        Psensor.put(x,1.0);
        Pmotor.put(y,1.0);
    }
    
    // put A[i][j] = value
    public void add(String sensor, String motor, double value) {
        if (sensor.length()<=0 || sensor.length() > sensor_wordsize) throw new RuntimeException("Illegal index");
        if (motor.length() <= 0 || motor.length()> motor_wordsize) throw new RuntimeException("Illegal index");
        double prev=rows[Integer.parseInt(sensor, 2)].get(motor);
        rows[Integer.parseInt(sensor, 2)].put(motor, value+prev);
        Psensor.add(sensor,1.0);
        Pmotor.add(motor,1.0);
        N++;
    }

    // return A[i][j]
    public double get(String i, String j) {
        if (i.length()<=0 || i.length() > sensor_wordsize) throw new RuntimeException("Illegal index");
        if (j.length() <= 0 || j.length()> motor_wordsize) throw new RuntimeException("Illegal index");
        return rows[Integer.parseInt(i, 2)].get(j);
    }

    // return the number of nonzero entries (not the most efficient implementation)
    public int nnz() { 
        int sum = 0;
        for (int i = 0; i < motor_wordsize; i++)
            sum += rows[i].nnz();
        return sum;
    }
/*
    // return the matrix-vector product b = Ax
    public SparseVector times(SparseVector x) {
        SparseMatrix A = this;
        if (N != x.size()) throw new RuntimeException("Dimensions disagree");
        SparseVector b = new SparseVector(N);
        for (int i = 0; i < N; i++)
            b.put(i, A.rows[i].dot(x));
        return b;
    }

    // return C = A + B
    public SparseMatrix plus(SparseMatrix B) {
        SparseMatrix A = this;
        if (A.N != B.N) throw new RuntimeException("Dimensions disagree");
        SparseMatrix C = new SparseMatrix(N);
        for (int i = 0; i < N; i++)
            C.rows[i] = A.rows[i].plus(B.rows[i]);
        return C;
    }

*/
    // return a string representation
    public String toString() {
        String s = "N = " + motor_wordsize + ", nonzeros = " + nnz() + "\n";
        for (int i = 0; i < nrows; i++) {
            s += i + ": " + rows[i] + "\n";
        }
        return s;
    }
    public double computeJointEntropy(boolean biased,int base)
    {
        //reset the main variables
        double Iobs = 0;
        double Bstar = 0;
        for (int x = 0; x < nrows; x++) {
            //the number of occurencies for that word
            for (String y : rows[x].st) {
                //s += "(" + i + ", " + st.get(i) + ") ";
                double ni = rows[x].st.get(y);
                double pi = ni / N;
                if (ni > 0) {
                    Iobs -= pi * Math.log(pi);
                    Bstar++;
                }
            }
        }
        
       if(base>0) Iobs= Iobs / Math.log(base);
       return Iobs;
    }
    public double computeMutualInfo(boolean biased,int base) {
        //reset the main variables
        double Iobs = 0;
        double Bstar = 0;
        for (int x = 0; x < nrows; x++) {
            //the number of occurencies for that word
            for (String y : rows[x].st) {
                //s += "(" + i + ", " + st.get(i) + ") ";
                double ni = rows[x].st.get(y);
                double pi = ni / N;
                if (ni > 0) {
                    Iobs += pi * Math.log(pi);
                    Bstar++;
                }
            }
        }
        
       if(base>0) Iobs= Iobs / Math.log(base);
       if(!biased) Iobs+=(Psensor.getBstar()+Pmotor.getBstar()-Bstar-1)/(2*Psensor.getN());
       double Hx=getXEntropy(true, 2);
       double Hy=getYEntropy(true, 2);
        //System.out.println("H(X): " + Hx+ " B*: "+Xvector.getBstar());
        //System.out.println("H(Y): " + Hy+ " B*: "+Yvector.getBstar());
        //System.out.println("H(X,Y): " + Iobs+ " B*: "+Bstar);
       Iobs=Hx+Hy+Iobs;
       return Iobs;
    }

    public double computeChannelCapacity(String mode) {
        // It's a basic channel
        channel = new Channel();
        channel.kind = Channel.BASIC;
        int noOfInputs = nrows;
        int noOfOutputs = (int) Math.pow(2, sensor_wordsize);
        outputNames = new String[noOfOutputs];
        for (int sensor = 0; sensor < noOfOutputs; sensor++) {
            outputNames[sensor] = "s" + sensor;
        }
        inputNames = new String[noOfInputs];
        channelMatrix = new double[noOfInputs][noOfOutputs];

        for (int action = 0; action < noOfInputs; action++) {
            //the number of occurencies for that word
            inputNames[action] = "a" + action;
            for (int sensor = 0; sensor < noOfOutputs; sensor++) {
                //s += "(" + i + ", " + st.get(i) + ") ";
                double ni = rows[sensor].get(action);
                double pi = 0;
                
                if(mode.equals("p(action|sensor)") && ni!=0)
                    pi = ni/Psensor.get(sensor);
                else if(mode.equals("p(sensor|action)") && ni!=0)
                    pi = ni/Pmotor.get(action);
                //System.out.println("Pi: "+pi);
                channelMatrix[action][sensor] = pi;
            }
        }
        channel.setInputNames(inputNames);
        channel.setMatrix(channelMatrix);
        channel.setOutputNames(outputNames);
        BlahutArimoto ba = new BlahutArimoto(channel, acceptableError, noOfIterations);
        ba.calculateCapacity();
        return ba.getCapacity();
    }
    
    
    public Channel getChannel()
    {
    		return channel;
    }
    
    public void exportConditionedProbability(String filename,String mode) throws IOException
    {
        
        int yrows = (int)Math.pow(2, motor_wordsize);
        CsvWriter csvoutput = new CsvWriter(filename);
        final String[] record = new String[yrows+1];
        csvoutput.setDelimiter('|');
        record[0]="("+String.valueOf(nrows)+","+String.valueOf(yrows)+")";
        for (int sensor=0;sensor<yrows;sensor++) {
            record[sensor+1]="s"+String.valueOf(sensor);
        }
        csvoutput.writeRecord(record);
        for (int action= 0; action < nrows; action++) {
            //the number of occurencies for that word
            record[0]="a"+String.valueOf(action);
            for (int sensor=0;sensor<yrows;sensor++) {
                //s += "(" + i + ", " + st.get(i) + ") ";
                double ni = rows[action].get(sensor);
                double pi = 0;
                
                if(mode.equals("p(action|sensor)") && ni!=0)
                    pi = ni/Pmotor.get(sensor);
                else if(mode.equals("p(sensor|action)") && ni!=0)
                    pi = ni/Psensor.get(action);
                //System.out.println("Pi: "+pi);
                record[sensor+1]=String.valueOf(pi);
            }
            csvoutput.writeRecord(record);
        }  
        csvoutput.close();
    }
    // test client
    public static void main(String[] args) {
        SparseMatrix A = new SparseMatrix(1,1);
        SparseVector x = new SparseVector(1);
        A.add("0", "0", 1.0);
        A.add("0", "0", 1.0);
        A.add("1", "0", 1.0);
        A.add("1", "1", 1.0);
        A.add("1", "1", 1.0);
        A.add("0", "1", 1.0);
        //x.put(0, 0.75);
        //x.put(2, 0.11);
        //System.out.println("x     : " + x);
        //System.out.println("A     : " + A);
        //System.out.println("Hobs(X)     : " + A.getXEntropy(false, 2));
        //System.out.println("Hobs(X)     : " + A.getYEntropy(false, 2));
        System.out.println("Iobs(X,Y)     : " + A.computeMutualInfo(false, 2));
        System.out.println("Cmax     : " + A.computeChannelCapacity("p(action|sensor)"));
        try {
            //System.out.println("Ax    : " + A.times(x));
            //System.out.println("A + A : " + A.plus(A));
            A.exportConditionedProbability("/home/epokh/test.csv", "p(action|sensor)");
        } catch (IOException ex) {
            Logger.getLogger(SparseMatrix.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}



