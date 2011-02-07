// Observations.java
//
// This file contains the methods needed to turn a list of observations
// in a conditional probability matrix and calculate the possible error.
//
// Tom Chothia           T.Chothia@cwi.nl      June/2008  


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
import java.util.Vector;

public class Observations {

	//double Z = 1.96;  // The confidence in a single probability value 2.34 => 99.01 certainty
	//double certainty = 0.975;
	
	double Z = 2.34;  // The confidence in a single probability value 2.34 => 99.01 certainty
	double certainty = 0.999;
	
	public int noOfInputs = 0;
	public Vector<String> inputNames = new Vector<String>();
	public int noOfOutputs = 0;
	public Vector<String> outputNames = new Vector<String>();
	public int noOfTests =0;
	
	
	// (pairsSeen.get(i)).get(j) is the number of times (inputNames.get(i),outputNmes.get(j)) has been seen
	public Vector<Vector<Integer>> pairsSeen  = new Vector<Vector<Integer>>();
	
	
	// noOftestPerInput.get(i) is the number of times that inputNames.get(i) has been tested
	public Vector<Integer> noOftestPerInput = new Vector<Integer>();

	// Test i used the input inputNames.get(testResultsInputs.get(i)) 
	//      and returned result outputNames.get(testResultsOutputs.get(i))
	
	//public Vector testResultsInputs = new Vector();
    //public Vector testResultsOutputs = new Vector();
    
	public double[][] channelMatrix;
	
	public Observations () {}
	
	public void addObservation(String input,String output)
	{
		noOfTests++;
		
		int inputIndex = 0;
		boolean foundInput = false;
		while (inputIndex<inputNames.size() && !foundInput)
		{
			if ( ((String) inputNames.get(inputIndex)).equals(input) )
			{
				foundInput = true;
			}
			else
			{
				inputIndex++;
			}
		}

		// If it hasn't been seen before add it to //the input names,
		// add 1 to the number of inputs
		// add a cell for the number of times it's been seen
		// add a vector for the number of pairs seen.  
		if (!foundInput)
		{
			inputNames.add(input);
			noOfInputs =  noOfInputs + 1;
			noOftestPerInput.add(new Integer(1));
			Vector<Integer> newInputVector = new Vector<Integer>();
			for (int i = 0;i<noOfOutputs;i++)
			{
				newInputVector.add(new Integer(0));
			}
			pairsSeen.add(newInputVector);
		}
		else
		{
			noOftestPerInput.set(inputIndex, new Integer(noOftestPerInput.get(inputIndex).intValue() + 1) );
		}
		
		
		// Check to see if the output has an index
		int outputIndex = 0;
		boolean foundOutput = false;
		while (outputIndex<outputNames.size() && !foundOutput)
		{
			if ( ((String) outputNames.get(outputIndex)).equals(output) )
			{
				foundOutput = true;
			}
			else
			{
				outputIndex++;
			}
		}
		
		// If it hasn't been seen before add it to the output names,
		// add 1 to the number of outputs
		// add a cell to each of the input vectors for the number of pairs seen.  
		if (!foundOutput)
		{
			outputNames.add(output);
			noOfOutputs =  noOfOutputs + 1;
			for (int i=0;i<pairsSeen.size();i++)
			{
				pairsSeen.get(i).add(new Integer(0));
			}
		}
		
		// record the observation
		pairsSeen.get(inputIndex).set(outputIndex, new Integer( pairsSeen.get(inputIndex).get(outputIndex).intValue() +1));
	}
	
	public double[][] getChannelMatrix()
	{
		return channelMatrix;
	}
	
	public int getNoOfTests()
	{
		return noOfTests;
	}
	
	
	public String[] getInputNames()
	{
		return ((String[]) inputNames.toArray(new String[inputNames.size()]));
	}
	
	public String[] getOutputNames()
	{
		return ((String[]) outputNames.toArray(new String[outputNames.size()]));
	}

	// Returns the size/2 of the confidence interval of channelMatrix[x][y]
	//   with a certainty 99%
	public double calculateConfidence(int x,int y)
	{
		// We use the theory of "Confidence Intervals for Population Proportion"
		// i.e. true mean = p +/- Z.sqrt( (p(1-p))/n)
		// where p is the point estimate, n is the sample size
		//   and Z is the Z-value for the require confidence
		//   possible values include z = 2.34 for a confidence of.9901
		//                        or z = 3.49 for a confidence of .9998
		double n = ((Integer)noOftestPerInput.get(x)).doubleValue();
		double p = channelMatrix[x][y];
		double possError =  Z*  Math.sqrt( (p * (1-p))/n );
		//double possError =  Z*  Math.sqrt( (p * (1-p))/500000000 );
		
		//System.out.println("The 99% confidence interval of ("+x+","+y+") is ("+(p-possError)+","+(p+possError)+")");
		
		return ( possError );
	}
	
	// Find the largest confidence interval
	public double largestInterval()
	{
		double maxInterval = 0;
		for (int x = 0; x< inputNames.size();x++)
		{
			for (int y=0;y<outputNames.size();y++)
			{
				maxInterval = Math.max(maxInterval,(double)calculateConfidence(x,y));
			}
		}
		return ( maxInterval );
	}
	
	public double maxErrorRatio()
	{
		double maxErrorRatio = 0;
		for (int x = 0; x< inputNames.size();x++)
		{
			for (int y=0;y<outputNames.size();y++)
			{
				if (channelMatrix[x][y] != 0)
				{
					//System.out.println("Entry "+x+","+y+" has interval "+calculateConfidence(x,y)+" and value "+channelMatrix[x][y] );
					maxErrorRatio = Math.max(maxErrorRatio,  ( calculateConfidence(x,y) /channelMatrix[x][y] )+1 );
				}
			}
		}
		return ( maxErrorRatio );
	}
	
	public double minErrorRatio()
	{
		double minErrorRatio = 1;
		for (int x = 0; x< inputNames.size();x++)
		{
			for (int y=0;y<outputNames.size();y++)
			{
				if (channelMatrix[x][y] != 0)
				{
					//System.out.println("Looking at: "+x+","+y+": "+channelMatrix[x][y] +" with int. "+ calculateConfidence(x,y));
					//System.out.println(" ratio is:"+(1 - calculateConfidence(x,y) /channelMatrix[x][y] ));
					minErrorRatio = Math.min(minErrorRatio,  (1 - calculateConfidence(x,y) /channelMatrix[x][y] ) );
				}
			}
		}
		return ( minErrorRatio );
	}
	
	
	public double totalCertainty () 
	{
		return Math.pow(certainty, (inputNames.size() * (outputNames.size()-1)));
	}
	
	// Generate the most probably channel matrix from the observation seen so far
	public void generateMatrix ()
	{
		
		//System.out.println( "PS: size" + pairsSeen.size() + "PS element size "+pairsSeen.get(0).size());
		//System.out.println( "inputs  " + noOfInputs + "outputs: "+noOfOutputs);
		
		
		channelMatrix = new double[noOfInputs][noOfOutputs];
		//for (int inputIndex=0;inputIndex<noOfInputs;inputIndex++)
		//{
		//	for (int outputIndex=0;outputIndex<noOfOutputs;outputIndex++)
		//	{
		//		System.out.print(" "+pairsSeen.get(inputIndex).get(outputIndex).doubleValue()+",");
		//	}
		//	System.out.println("");
		//}
		
		for (int inputIndex=0;inputIndex<noOfInputs;inputIndex++)
		{
			for (int outputIndex=0;outputIndex<noOfOutputs;outputIndex++)
			{
				//if ( noOftestPerInput.get(inputIndex).doubleValue() != 0 )
				//{			
					channelMatrix[inputIndex][outputIndex] = pairsSeen.get(inputIndex).get(outputIndex).doubleValue() /  noOftestPerInput.get(inputIndex).doubleValue();
				
					//System.out.println("seen "+inputNames.get(inputIndex)+","+outputNames.get(outputIndex)+" "+pairsSeen.get(inputIndex).get(outputIndex).doubleValue()+"times");
					//System.out.println(" prob is: "+channelMatrix[inputIndex][outputIndex]);
					//}
				//else
				//{
				//	channelMatrix[inputIndex][outputIndex] = 0;
				//}
			}
		}
	} 
	
	
	public Channel generateChannel ()
	{
		generateMatrix ();
		return new Channel(Channel.BASIC,getInputNames(),getOutputNames(),channelMatrix);
	}
	
}
