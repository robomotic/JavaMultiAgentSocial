// BlahutArimoto.java
// 
// This class calculates the capacity of a channel using the Blahut-Arimoto Algorithm.
// The description for this algorithm is largely taken from the paper: "A Generalized
// Blahut-Arimoto Algorithm" by Pascal Vontobel", N.B. this paper uses natural logs and
// e where as I am using log2 and 2, to give the capacity in bit.
//
//  Tom Chothia           T.Chothia@cwi.nl      16/05/2008  
//
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.
//
// Copyright 2008 Tom Chothia

     
package info;
import java.util.Arrays;

public class BlahutArimoto {
	
	boolean verbose = false;
	boolean displayIts = false;
	Integer noOfIntOutputs;

	int noOfOutputs;
	int noOfInputs;
	String[] inputNames;
	String[] outputNames;
	double[][] channelMatrix_W;
	double acceptableError;
	int noOfiterations;
	double[] inputPMF_Q;
	Channel channel;
	double error;
	
	double capacity;
	double possibleError;
	int iteration = 1;
	
	public String[] getInputNames () { return inputNames; }
	public double getCapacity () { return capacity; }
	public double getpossibleError () { return possibleError; }
	public double[] getMaxInputDist () { return inputPMF_Q; }
	public void setInputNames (String[] inputs) { inputNames=inputs; noOfInputs=inputs.length;}
	public void setOutputNames (String[] inputs) { outputNames=inputs; noOfOutputs=inputs.length;}
	public void setChannelMatrix (double[][] matrix) { channelMatrix_W=matrix;}
	public void setVerbose (boolean b) { verbose = b;}
	       
	public BlahutArimoto() {};
	
	public BlahutArimoto(Channel channel,double[] inputPMF_Q,double acceptableError,int noOfiterations)
	{
		this.inputNames = channel.inputNames;
		this.outputNames = channel.outputNames;
		this.channelMatrix_W = channel.channelMatrix_W;
		this.inputPMF_Q = inputPMF_Q;
		this.acceptableError = acceptableError;
		this.noOfiterations = noOfiterations;
		noOfInputs = inputNames.length; 
		noOfOutputs = outputNames.length;
		this.channel = channel;
	}
	
	public BlahutArimoto(Channel channel,double acceptableError,int noOfiterations)
	{
		this.inputNames = channel.inputNames;
		this.outputNames = channel.outputNames;
		this.channelMatrix_W = channel.channelMatrix_W;
		this.inputPMF_Q = IT.unifromDist(inputNames.length);
		this.acceptableError = acceptableError;
		this.noOfiterations = noOfiterations;
		noOfInputs = inputNames.length; 
		noOfOutputs = outputNames.length;
		this.channel = channel;
	}
	
	public BlahutArimoto( String[] inputNames,
									String[] outputNames,
									double[][] channelMatrix_W,
									double[] inputPMF_Q,
									double acceptableError,
									int noOfiterations )
	{
		this.inputNames = inputNames;
		this.outputNames = outputNames;
		this.channelMatrix_W = channelMatrix_W;
		this.inputPMF_Q = inputPMF_Q;
		this.acceptableError = acceptableError;
		this.noOfiterations = noOfiterations;
		noOfInputs = inputNames.length; 
		noOfOutputs = outputNames.length;
	}
		
public double calculateCapacity() {
	
		boolean finished = false;

		// We require that channelMatrix_W.length =  noInputs
		// and that channelMatrix_W[i].length  =  noOnputs for all i
		// Start off with a random inputPMF which will become 
		// closer to the real value with each iteration.
		// We require that inputPMF_Q.length = noInputs
		// inputPMF_Q = defaultInputPMF_Q; 
		
		double[] newInputPMF = new double[noOfInputs];	
			
		if (verbose && displayIts) 
		{ 
			System.out.println("\n Channel Matix is: \n"); 
			channel.printChannel();
		}
	
		while  (iteration < noOfiterations && finished == false)
		{
			if (verbose  && displayIts) 
			{ 
				System.out.println("\n \nIteration "+iteration); 
				System.out.print("  Trying inputPMF_Q =    "); 
				IT.printPMF(inputNames,inputPMF_Q); 
			}
	//		System.out.print("\n  This inputPMF makes the probs of the outputs: "); 
				
	//		for (int i=0;i<noOfOutputs;i++)
	//		{
	//			System.out.print(outputNames[i]+":"+ calculateOutputProb_R_QW(i,inputPMF_Q,channelMatrix_W)+", ");
	//		}
		
			possibleError = calculateError(inputPMF_Q,channelMatrix_W);
			if (verbose && displayIts) {  System.out.print("\n  Maximum possible error = "+ possibleError); }
			//System.out.print("\n  Calculating next inputPMF:    ");
			//	
			for (int i=0;i<noOfInputs;i++)
			{
				newInputPMF[i] =   inc_2powerT(i, inputPMF_Q, channelMatrix_W) / sumOfTValues(inputPMF_Q, channelMatrix_W) ;	
				//System.out.print(inputNames[i]+":"+ newInputPMF[i]+", ");
			}
			
			capacity = IT.mutualInformation(inputPMF_Q,channelMatrix_W);
			if (verbose  && displayIts) { System.out.println("\n  This input PMF give a Channel Capacity "+capacity); }
			// System.out.println(" Entropy of new input pmf: "+ entropy_HX(newInputPMF));
			
			if  ( Arrays.equals(inputPMF_Q,newInputPMF) || possibleError <= acceptableError) 
			{ 
				finished = true;

			}
			else 
			{
				System.arraycopy(newInputPMF, 0, inputPMF_Q, 0, noOfInputs);
				iteration++;
			}		
		}

		// zero error might appear non-zero due to rounding error in Java doubles
		if (finished && Arrays.equals(inputPMF_Q,newInputPMF)) { possibleError = 0;}
		
		if (verbose) { 
			if ( finished && (Arrays.equals(inputPMF_Q,newInputPMF) || possibleError==0) )
			{
				System.out.println("\n\nComplete, after "+iteration+" iterations");
				//	System.out.println("  The Channel Capacity is: "+ IT.mutualInformation(newInputPMF,channelMatrix_W));
				System.out.println("  The attacker learns: "+ capacity  +" bit of information about the users");
				//System.out.println("  I.e. they learn the user's ID with probility:  "+(capacity / IT.log2(noOfInputs)));
				System.out.println("  Capacity/log2(inputs)} is "+ (capacity / IT.log2(noOfInputs))+" out of 1");
			} 
			else 
			{
				if (possibleError <= acceptableError)
				{
					System.out.println("\n\nCapacity calculated to within acceptable error, in "+iteration+" iterations");
				}
				else 
				{ 
					System.out.println("\n\nNOT COMPLETE\nPerformed the maximum number of iterations: "+iteration+"\n  The current results are:");
				}
				
				System.out.printf("  The Channel Capacity is: %1$6.5g +/- %2$6.5g\n",(capacity+(possibleError/2)),(possibleError/2));
				//System.out.println("  I.e. they learn the user's ID with around probability:  "+ (capacity / log2(noOfInputs)));
				System.out.println("  Capacity/2^{inputs} is "+ (capacity / IT.log2(noOfInputs))+" out of 1");
			}
		System.out.print("  Input distribution: ");
		IT.printPMF(inputNames,inputPMF_Q);
		}
		
		return capacity;
	}
	
	//
	// methods to calculate useful values
	//

	public double inc_2powerT  (int inputElement, double[] inputProbs_Q,double[][] matrix_W ){
		
		// This method returns 2^{Sigma_y W(y|x).log ( Q(x).W(y|x)/(QW)(y) )
        //      with the special cases that 0.log(0) = 0        (as x.log(x) -> 0 as x -> 0) 
		//      and log (0/0) = 0                               (as x/x -> 1 as x -> 0 and log(1) = 0) 
		//      and e^(...+ n.log 0 + ... ) = 0 went n != 0     (as n.log(x) -> -inf as x -> x)

		// We need to avoid taking the log of 0
		boolean minusinf = false; // set to true if we make n.log(0) at when summing T
		double sum = 0;
		double logtop,logbottom,W;
		int loopcounter = 0;
		
		while (loopcounter < noOfOutputs && !minusinf)
		{
            W = matrix_W[inputElement][loopcounter];
            logtop = (inputProbs_Q[inputElement] * matrix_W[inputElement][loopcounter]);
            logbottom =  IT.QW(loopcounter,inputProbs_Q,matrix_W);
  
            if (W!=0 && logtop != 0)
            {	// N.B. bottom == 0 => top == 0, so we never divide by zero
            	sum = sum + W * IT.log2 ( logtop/logbottom);
            }
            else { if (W!=0 && logtop == 0 && logbottom !=0 )
            		{
            			// we are trying to calculate 2^(-inf) so set flag and stop the calculation, 
            			minusinf = true;
            		}
            }
            // The other cases have no effect on the result
            //    W == 0  => terms is 0.log(0) = 0
            //    W != 0 && logtop == 0 && logbottom == 0 => term is n.log(0/0) = 0
            //    W != 0 && logtop != 0 && logbottom == 0 can't happen as  logbottom == 0 => logtop != 0
		
            loopcounter = loopcounter + 1;
		}
		
		if (minusinf) {return 0; }
			else {return Math.pow(2,sum);}
	}
	
	
	public  double calculateError(double[] inputProbs_Q, double[][] matrix_W )
	{
		// this method finds the maximum possible error for a input PMF using:
		// I(Q,W) <= true Cap <= max_x[T(x) -log(Q(x))]
		// i.e. max possible err is max_x[T(x) -log(Q(x))] - I(Q,W) 
	
		// Find max_x[T(x) -log(Q(x))]
		//     T(x) =  Sigma_y W(y|x).log(Q(x).W(y|x) / (QW)(y))
		//     we take log (0/0) = 0  (as x/x -> 1 as x -> 0 and log(1) = 0) 
		//     and n.log 0 = -inf     (as n.log(x) -> -inf as x -> x)
		
		double T;
		double maxTminuslogQ = 0;
		boolean maxTminuslogQSet = false;
		for (int u=0;u<noOfInputs;u++)
		{
			if (inputProbs_Q[u] != 0 )
			{
				// Find T(x)	
				T = 0;
				for (int y=0;y<noOfOutputs;y++)
				{
					if (matrix_W[u][y]!=0)
					{	
					    // Q(x)!=0=!W  =>  W,logtop,logbottom != 0  (so we're not dividing by 0)
						T = T + matrix_W[u][y] * IT.log2 ( (inputProbs_Q[u] * matrix_W[u][y])
						                                /IT.QW(y,inputProbs_Q,matrix_W));  
					}
					// W == 0 case results in W(y|x).log(Q(x).W(y|x) / (QW)(y)) = 0
					// Q(x) != means that minusinf case can't happen.
		         }
				if (maxTminuslogQSet)
				{
					maxTminuslogQ = Math.max(maxTminuslogQ, (T-IT.log2(inputProbs_Q[u])) );	
				}
				else
				{
					maxTminuslogQ = T - IT.log2(inputProbs_Q[u]);
					maxTminuslogQSet = true;
				}
			}
		}		
		return maxTminuslogQ - IT.mutualInformation(inputProbs_Q,matrix_W);
	}
	
	
    //  This method is not used because the T values can be -infinite and may also require division by 0
	//      use inc_2powerT  method instead 
	// 	
	public double calculateValues_T(int inputElement, double[] inputProbs_Q, double[][] matrix_W )
	{
		// this method calculates the Values function T(x) 
		// using T(x) =  Sigma_y W(y|x).log V(x|y)\
		//            =  Sigma_y W(y|x).log(Q(x).W(y|x) / (QW)(y))
		
		double result = 0;
		double W,top,bottom = 0;
		
		for (int i=0;i<noOfOutputs;i++)
		{
            W = matrix_W[inputElement][i];
            top = (inputProbs_Q[inputElement] * matrix_W[inputElement][i]);
            bottom =  IT.QW(i,inputProbs_Q,matrix_W);
			result = result + W * IT.log2 ( top/bottom);
		}		
		return result;
	}
	
	public double sumOfTValues(double[] inputProbs_Q, double[][] matrix_W )
	{
		// this method calculates Sigma_x e^{T(x)}
		
		double result = 0;
		
		for (int i=0;i<noOfInputs;i++)
		{
			// if p(x) = 0 then 2^{T(x)} should equal 0 (???)
			if (inputProbs_Q[i] != 0)
			{ 
				result = result + inc_2powerT(i, inputProbs_Q, matrix_W );
			}
		}
		return result;
	}
	
	
	
	
	
}
