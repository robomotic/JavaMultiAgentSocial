
// TestAnony.java
//
// This is the main Class of the "Anonymity Engine": a suite of tools to
// Calculate the anonymity of a system.
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
import java.util.Iterator;
import java.io.*;
import java.util.regex.*;

public class TestAnony {

	// Default options that can be override by the commandline.

	static boolean readFromChanFile = false; 
	static boolean readFromObsFile = false;
	static boolean miUniform = false;
	static int verbose = 5;
	static double acceptableError = 0.000000000001;
	static int noOfIterations = 5000;

	static Observations obs;
	static Channel channel;
	static double[] inputDist;

	static int sampleSize =0;
	// Apple
	// static String fileLocation = "/Users/tom/Documents/AE/";
	// eee
	//static String fileLocation = "/home/user/Projects/ae/code/";
	// bham 
	static String fileLocation = "/home/staff/tpc/AE/workspace/Data/";
	//static String fileLocation = System.getProperty("user.dir")+"/";
	// Win	
	// static String fileLocation = "C:\\Documents and Settings\\tpc\\workspace\\Data\\";


	// A noisy channel with non-overlapping outputs
	// static String fileName = fileLocation+"Channels/NoisyNonOverlap.txt";
	// Cap = 1

	// A binary symmetric channel
	// static String fileName = fileLocation+"Channels/home/staff/tpc/AE/workspace/Data/Channels//BinSymChannel.txt";
	//
	static double p = 0.25;
	static double[][] defaultChannelMatrix_W = {{1-p,p},{p,1-p}};
	static String[] defaultOutputNames = {"o1","o2"};
	static String[] defaultInputNames = {"a1","a2"};
	//static double[] defaultInputPMF_Q= {0.75,0.25}; 
	// Cap = 1-H(p)   i.e. 0.18872187554086717 for p = 0.25

	// A binary erasure channel
	//static String fileName = fileLocation+"Channels/BinErasureChannel.txt";
	// Cap = 1-a

	// Noisy Typewriter with 6 keys
	//static String fileName = fileLocation+"Channels/TextBookInfoTheory/NoisyTypeW6.txt";
	// Cap = log2(3)

	// Noisy Typewriter with 26 keys
	//static String fileName = fileLocation+"Channels/NoisyTypeW26.txt";
	//Cap = log2(13)

	// static String fileName = fileLocation+"Channels/NonSymChannel.txt";
	//static String fileName = fileLocation+"Channels/MultiChannel1.txt";
	//static String fileName = fileLocation+"Channels/multiChannel.txt";

	// Motivating example for D.C.
	//static String fileName = fileLocation+"Channels/DiningCryptos/dc4fair.txt";
	//static String fileName = fileLocation+"Channels/DiningCryptos/dc4allbais.txt";
	//static String fileName = fileLocation+"Channels/DiningCryptos/dc4twoBiased.txt";

	// Motivating example for multi-sender channels.
	//static String fileName = fileLocation+"Channels/MultiSender/MultiAsSingleChannel.txt";
	//static String fileName = fileLocation+"Channels/MultiSender/MultiChannelExample.txt";
	//static String fileName = fileLocation+"Channels/MultiSender/MultiChannel1.txt";

	// Examples of obs files
	//static String fileName = fileLocation+"Tests/dc4cpu1linux40000.txt";
 
	//static String fileName = fileLocation+"Tests/dc4cpu1apple40000.txt";
	//static String fileName = "/home/staff/tpc/AE/workspace/Data/Tests/dc4cpu2apple40000.txt";
	 //static String fileName = "/home/staff/tpc/AE/workspace/Data/Channels/DiningCryptos/dc3allbias4.txt";
	// static String fileName = "/home/staff/tpc/AE/workspace/Data/Tests/dc4cpu8win100000.txt";
//	 static String fileName = "/home/staff/tpc/DCtests/DCtest.txt";
			//"/home/staff/tpc/AE/workspace/Data/Channels/DiningCryptos/dc4allbias.txt";
	// static String fileName = "C:\\Documents and Settings\\tpc\\workspace\\Data\\Channels\\DiningCryptos\\dc3allbias4.txt";
	//static String fileName = "/home/user/apps/mixs/data2/test.txt";   

	//static String fileName = "/home/user/data2/matrix0904.txt";   
 
	
	//static String fileName = "/home/user/DCtests/SSLcover2800000.txt";   
	//static String fileName = "/home/user/DCtest.t)xt";   
	
	//static String fileName = "/home/user/DCtests/SSLlocalDC.txt";
	//static String fileName = fileLocation+"Tests/dc4cpu2apple100000.txt";
	
	//static String fileName = fileLocation+"Tests/dc4cpu1apple200000.txt";
	//static String fileName = fileLocation+"Tests/test1.txt";
	//static String fileName = fileLocation+"Channels/multiChannel.txt";

	//
	// Conditional channels
	//
	//static String fileName = fileLocation+"Channels/DiningCryptos/dc3nopay.txt";

	//
	//Mixes
	//

	// Theshold Mixes
	//static String fileName = fileLocation+"Channels/Mixes/Thesholds3r3n2.txt";
	//static String fileName = fileLocation+"Channels/Mixes/Thesholds3r3n2Conditional.txt";
	//static String fileName = fileLocation+"Channels/Mixes/Thesholds3r3n2condSender";

	//Pool Mixes
	//static String fileName = fileLocation+"Channels/Mixes/Pools3r3n2p1Once.txt";
	//static String fileName = fileLocation+"Channels/Mixes/Pools3r3n2p1OnceCondSender.txt";

	//static String fileName = "/home/staff/tpc/AE/workspace/Data/Tests/dc4cpu2apple40000.txt";
	//static String fileName ="/home/staff/tpc/AE/workspace/Data/Tests/dc4cpu1apple40000.txt";
	//static String fileName ="/home/staff/tpc/50000_0";

	static String fileName = "unset"; // this is set by the commandline
	public static void main(String[] args) {

		//
		//  Read the commandline arguments
		//
		if ((args.length==0)|| (args.length > 0 && (args[0].equals("help") || args[0].equals("h") || args[0].equals("-h") || args[0].equals("-help"))))
		{                                                                                                          
			System.out.println("Anonymity Engine\n  This is a program to calculate the anonymity of a system from\n  either a matrix relating the inputs and outputs or a list of\n  observations of the systems.");
			System.out.println("To run type: java -jar ae.jar <fileName> <options>");
			System.out.println("    where <fileName> is a text file of observations or a channel matrix");
			System.out.println("Options:\n   -h,help print this message \n   -f <fileName> or file <fileName>, read input from file <fileName>"); 
			System.out.println("   -v set the level of information shown (0 to 4) e.g. -v 4"); 
			System.out.println("   -o process an observations file"); 
			System.out.println("   -c process a channel file"); 
			System.out.println("   -e set the acceptable error level e.g. -e 0.0000001");
			System.out.println("   -i set the maximum number of interations e.g. -i 500");
			System.out.println("   -mi calculate mutual information with uniform distribution");	
			System.out.println("        (rather than capacity)");	
			System.exit(0);
		}
		int clc = 0;

		while (clc < args.length)
		{
			if (args[clc].equalsIgnoreCase("-f") || args[clc].equalsIgnoreCase("file"))
			{
				if (clc < args.length+1) { fileName = args[clc+1]; }
				clc = clc +2;
			} else if (args[clc].equalsIgnoreCase("-v")) {
				verbose = Integer.parseInt(args[clc+1]);;
				clc = clc + 2;
			} else if (args[clc].equalsIgnoreCase("-o")) { 
				readFromObsFile = true;
				clc = clc + 1;
			} else if (args[clc].equalsIgnoreCase("-e")) {
				acceptableError = Double.parseDouble(args[clc+1]);
				clc = clc + 2;
			} else if (args[clc].equalsIgnoreCase("-i")) {
				noOfIterations = Integer.parseInt(args[clc+1]);
				clc = clc + 2;
			} else if (args[clc].equalsIgnoreCase("-c")) {
				readFromChanFile = true; 
				clc = clc + 1;	
			} else if (args[clc].equalsIgnoreCase("-mi")) {
				miUniform = true; 
				clc = clc + 1;
			} else  if (fileName.equals("unset")) {
				fileName = args[clc];
				clc = clc + 1;
			} else {
				System.out.println("Unrecognised commandline option: "+args[clc]);
				System.out.println("  Skipping it. Use -h or help for a list of options");
				clc = clc + 1;
			}
		}

		// If it hasn't been specified on the command line
		// find out if the file is a matrix or observations.
		if (!readFromChanFile && !readFromObsFile) {
			try {
				BufferedReader reader =  new BufferedReader(new FileReader(fileName));
				String line = reader.readLine();
				while ( (line.trim()).equalsIgnoreCase("") || (line.trim()).startsWith("//"))
					{ line = reader.readLine(); }

				Pattern patternObs = Pattern.compile("\\([\\s]*([\\w:]+)[\\s]*,[\\s]*([\\w:]+)[\\s]*\\)[\\s]*$");
				Matcher matcherObs = patternObs.matcher(line.trim());;	
				if (matcherObs.find()) {
					if (verbose>0) { System.out.println("Reading file: " +fileName+" as an observation file\n"); }
					readFromObsFile = true;
				} else {
					if (verbose>0) { System.out.println("Reading file: " +fileName+" as a matrix file"); }
					readFromChanFile = true;
				}
				reader.close();
			} catch (IOException e){ System.out.println(e+ " while trying to read file: "+fileName);System.exit(1); } 
		}



		//
		// Read in the channel file
		//

		// reading the channel from an observations file
		if (readFromObsFile)
		{
			ReadFile obsReader = new ReadFile(fileName);
			obsReader.readObservations();
			obs = obsReader.getObservations();
			channel = obs.generateChannel();
			sampleSize = obs.noOfTests;
			if (verbose >2) {
				System.out.println("These observations lead to the following channel matrix, to 4 decimal places:");
				channel.printChannel();
			}
			//channel.printSaveChannel("/home/user/matrix.txt");
			if (verbose >3) {
				System.out.println("\nThe largest "+ obs.certainty*100+"% confidence interval for any entry, to 4 decimal places, is "+Stats.round((2*obs.largestInterval()),4));
				//System.out.println("\n With certainty: "+ obs.totalCertainty()+" the largest confidence interval is: "+(2*obs.largestInterval()));
				//System.out.println("\n Max error ratio is: "+ obs.maxErrorRatio()+" the min error ratio is :"+ obs.minErrorRatio());
			}
		}

		// reading the channel from a channel file
		if (readFromChanFile)
		{  	
			// Read the channel matrix using the ReadChannelFile Class
			ReadFile channelFileReader = new ReadFile(fileName);
			channelFileReader.readChannel();
			channel = channelFileReader.getChannel();
		}

		//
		// Calculate the Capacity
		//

		// Switch on the kind of channel and calculate capacity accordingly.
		// Channels can be basic, conditional or multi-user. 
		BlahutArimoto ba = new BlahutArimoto(channel,acceptableError,noOfIterations);
		double result = 0.0;
		switch (channel.kind)
		{
		case (Channel.BASIC):
			// Find capacity or mutual information of a basic channel
			if (miUniform==true) {
				result = IT.MIuniformInput(channel.getMatrix());
				inputDist = IT.unifromDist(channel.noOfInputs());
			} else {
				ba.calculateCapacity();
				result = ba.getCapacity();
				inputDist = ba.getMaxInputDist();
			}
		// Print the output, depending on the value of verbose
		// the higher "verbose" the more details given.
		if (readFromObsFile)
		{
			//if (verbose > 2) {
			//	System.out.print("  Maximising Input distribution estimated to be:\n    ");
			//	IT.printPMF(ba.inputNames,ba.inputPMF_Q);
			//	System.out.print("\n");
			//}

			if (verbose>4) {
				System.out.print("\nWith certainty: "+ Stats.round(obs.totalCertainty(),4)+" the attacker learns between ");		
				System.out.print(Stats.round(Math.max(0,(obs.minErrorRatio() * result) + IT.log2( obs.minErrorRatio() / obs.maxErrorRatio())),4));
				System.out.print(" and ");
				System.out.print(Stats.round((obs.maxErrorRatio() * result) + IT.log2( obs.maxErrorRatio() / obs.minErrorRatio()),4));
				System.out.println(" bits about the users");
			}
			double correction = (double)(channel.noOfInputs()-1)*(double)(channel.noOfOutputs()-1)/(double)(2*sampleSize)*IT.log2(Math.E); 
			if (verbose>2) {
				System.out.println("\n"+channel.noOfInputs()+" inputs, "+channel.noOfOutputs()+" outputs and "+sampleSize+" samples");
				System.out.println("  estimate result = "+Stats.round(result,4));
			    System.out.println("  correction = log_2(e).(noOfInputs-1)(noOfOutputs-1)/2.sampleSize = "+ Stats.round(correction,4));
				System.out.println("   The results are no more accurate that the correction value,\n    increase the sample size to decrease the correction.\n    Calculations are to 95% confidence.");
			}
			if (verbose > 5) {
				System.out.print("Maximising Input distribution estimated to be:\n    ");
				IT.printPMF(ba.inputNames,ba.inputPMF_Q);
				System.out.print("\n");
			}
			if (miUniform == false) {
				System.out.println("  The estimated capacity is "+Stats.round(result,4)+" - "+Stats.round(correction, 4)+" = "+Stats.round(result-correction,4));
			} else {	
				System.out.println("  The mutual information is "+Stats.round(result,4)+" - "+Stats.round(correction, 4)+" = "+Stats.round(result-correction,4));
			}
			double zeroUpperBound = Stats.upperBoundForZero(  (channel.noOfInputs()-1)*(channel.noOfOutputs()-1),sampleSize ); 
			System.out.println("  The 95% confidence interval for zero information leakage is :"+Stats.round(zeroUpperBound,4));

			double mean = result-correction;
			double variance = Stats.nonZeroVariance(inputDist, channel.getMatrix(), sampleSize);
			
			if (result<=zeroUpperBound) {
				System.out.println("  This is consistent with the chi^2 distribution for zero leakage.");
				System.out.println("Capacity is between 0 and "+ Math.max(0,Stats.round(Stats.upperBoundNormal95(mean, variance),4)));
				System.out.println("");
			} else {
				System.out.println("  This is not consistent with the chi^2 distribution for zero leakage.");
				System.out.printf("  The estimated leakage is has mean "+ Stats.round(mean,4)+" and variance %1$6.5g",variance);
				System.out.println("\nCapacity is between "+Math.max(0,Stats.round(Stats.lowerBoundNormal95(mean, variance),4))+" and "+ Math.max(0,Stats.round(Stats.upperBoundNormal95(mean, variance),4)));
				
				System.out.println("");
			}
			//if (result == 1) 
			//{ System.out.println("\nThe attacker learns 1 bit about the users"); }
			//else
			//{ System.out.println("\nThe attacker learns "+result+" bits about the users"); }
		} else { // Print output for a processed matrix, e.g. no error info.
			if (miUniform == false) {
				if (verbose > 1) {
					if (ba.possibleError == 0) { 
						System.out.println("  Complete, after "+ba.iteration+" iterations");
					} else if (ba.possibleError <= ba.acceptableError) { 
						System.out.println("  Capacity calculated to within acceptable error, in "+ba.iteration+" iterations");
					} else if (ba.possibleError > ba.acceptableError) {
						System.out.println("  NOT COMPLETE: Performed the maximum number of iterations: "+ba.iteration+"\n"+
								" and still not with acceptable error rate\n " +
								" increase the maximum number of iterations (with flag -i <int>)\n" +
						" or increase the acceptable error (with flag -e <double>");
					}

					System.out.printf("  The Channel Capacity is: %1$6.5g +/- %2$6.5g",(result+(ba.possibleError/2)),(ba.possibleError/2));
					System.out.printf("  and there are "+channel.noOfInputs()+" possible input events\n");		
				}

				if (verbose > 2) {
					System.out.print("  Maximising Input distribution estimated to be:\n    ");
					IT.printPMF(ba.inputNames,ba.inputPMF_Q);
					System.out.print("\n\n");
				}
			} else { // then using MI not capacity.
				System.out.println("Calculating the mutual information with the uniform distribution");
			}

			if (result == 1) 
			{ System.out.println("The attacker learns 1 bit"); }
			else
			{ System.out.printf("The attacker learns %6.5g bits",result);}

			System.out.printf(", out of a possible %6.5g bits, about the input events.",IT.log2(channel.noOfInputs()));
			System.out.print("\n");
			}
		break;

		// The channel has multiple users that can send at the same time
		// so we use network information theory to find the worst case
		case (Channel.MULTI):
			// find the capacity for each of the inputs in turn.
			double[] capResults = new double[channel.noOfInputs()];
		boolean allAccurate = true;
		for (int inputIndex=0;inputIndex<channel.noOfInputs();inputIndex++)
		{
			boolean accurateResults = true; 
			double maxCapSoFar = -1;
			BlahutArimoto maxResultsSoFar = new BlahutArimoto();

			for (int rowCounter =0; rowCounter<channel.noOfRows();rowCounter++)
			{
				if (( channel.getInputRow(rowCounter)).contains(new Integer(inputIndex)))
				{
					// row rowCounter contains an input we are currently looking at 
					// so find the matching row without that input and make a matrix
					Vector matchingRow = (Vector)(channel.getInputRow(rowCounter)).clone();
					matchingRow.remove(new Integer(inputIndex));

					int matchingIndex = 0;
					boolean found = false;
					while (matchingIndex< channel.noOfRows() && !found)
					{  
						Vector inputRowInfor = channel.getInputRow(matchingIndex);
						if (matchingRow.containsAll(inputRowInfor) && inputRowInfor.containsAll(matchingRow))
						{
							found = true; 
						}
						else 
						{
							matchingIndex++; 
						}
					}
					if (!found) 
					{
						System.out.println("Syntax error in multi channel file \n all input combinations must be present");
						System.exit(0);
					}

					// Make a new channel object for just this test
					double[][] subChannelMatrix = new double[2][channel.noOfOutputs()];
					for (int collNo=0;collNo<channel.noOfOutputs();collNo++)
					{
						subChannelMatrix[0][collNo] = channel.prob(rowCounter,collNo);
					}

					for (int collNo=0;collNo<channel.noOfOutputs();collNo++)
					{
						subChannelMatrix[1][collNo] = channel.prob(matchingIndex,collNo);
					}

					System.out.println(" \n\n For inputIndex "+inputIndex+" rows "+rowCounter+" and "+matchingIndex+" match \n");

					String[] subInputNames = new String[2];
					subInputNames[0] = channel.getRowLabel(rowCounter);
					subInputNames[1] = channel.getRowLabel(matchingIndex);

					Channel subChannel = new Channel(Channel.BASIC,subInputNames,channel.getOutputNames(),subChannelMatrix);
					subChannel.printChannel();

					//double[] subInputPMF_Q ={0.5, 0.5};
					ba = new BlahutArimoto(subChannel,acceptableError,noOfIterations);

					if (ba.calculateCapacity() > maxCapSoFar) 
					{ 
						maxCapSoFar = ba.getCapacity(); 
						maxResultsSoFar = ba; 
						if (ba.getpossibleError()> acceptableError)
						{
							accurateResults = false;
							System.out.println("This result is not within the acceptable error level");
						}
					}
					System.out.println(" \n has cap: "+ba.getCapacity()); 
				}
			}
			System.out.println("The max cap. for "+channel.getInputName(inputIndex)+" is "+maxResultsSoFar.getCapacity());
			if (!accurateResults) 
			{ 
				System.out.println("BUT this result is not within the acceptable error level.");
				System.out.println("Please increase the number of iterations or raise the acceptable error level"); 
				allAccurate = false;
			}
			capResults[inputIndex] = maxResultsSoFar.getCapacity();
		}


		// Print the results for each input.
		System.out.println("\n\n");
		for (int i = 0;i<capResults.length;i++)
		{
			if (capResults[i] == 1)
			{
				System.out.println("The attacker learns 1 bit about user "+channel.getInputName(i));
			}
			else
			{
				System.out.println("The attacker learns "+capResults[i]+" bits about user "+channel.getInputName(i));
			}
		}
		if (!allAccurate) { System.out.println("HOWEVER these results are not with the acceptable error level");}
		break;

		case(Channel.COND):
			System.out.println("It's a cond channel with:");

		String[] groupNames = channel.getGroupNames();

		double[] capForGroup = new double[groupNames.length];
		double overallCap = 0;

		for (int i=0;i<groupNames.length;i++)
		{
			System.out.println("\nInput group "+groupNames[i]+" has the submatrix:" );
			// For the vector of rows in group i
			Vector keyRows = channel.getRowsForGroup()[i];	
			// Create the submatrix for groupName[i]
			String [] subInputNames= new String[keyRows.size()];
			double[][] subChannelMatrix = new double[keyRows.size()][channel.noOfOutputs()];

			for(int j=0;j<keyRows.size();j++)
			{
				subInputNames[j] = channel.getInputName(((Integer)keyRows.get(j)).intValue());
				for (int cols=0;cols<channel.noOfOutputs();cols++)
				{
					subChannelMatrix[j][cols] = channel.prob(((Integer)keyRows.get(j)).intValue(), cols);
				}
			}

			double[] subInputPMF_Q = new double[keyRows.size()];
			for (int input=0;input<keyRows.size();input++)
			{
				subInputPMF_Q[input] = (1.0/(double)keyRows.size());
			}

			Channel subChannel = new Channel(Channel.BASIC,subInputNames,channel.getOutputNames(),subChannelMatrix);
			subChannel.printChannel();

			ba = new BlahutArimoto(subChannel,acceptableError,noOfIterations);
			System.out.println("\n The capacity for group "+groupNames[i]+" is:");
			capForGroup[i] = ba.calculateCapacity();
			overallCap = Math.max(overallCap, capForGroup[i]);
		}
		System.out.println("\n\n\n\n\nThe relative loss of anonymity is: "+overallCap);
		result = overallCap;
		break;
		}
		//return result;
	}

	// Take a file name and calculate the mutual information
	// for a uniform input distribution.
	public static double obsToMIuniformInput(String fileName) {
		ReadFile obsReader = new ReadFile(fileName);
		obsReader.readObservations();
		return IT.MIuniformInput(obsReader.getObservations().generateChannel().getMatrix());
	}

	// Take a file name and calculate the point estimate of capacity.
	public static double obsToCapacity(String fileName) {
		return(obsToCapacity(fileName,0.00001,5000)); 
	}


	// Take a file name and calculate the point estimate of capacity
	// to with some BA error or maximum number of iterations.
	public static double obsToCapacity(String fileName, double acceptableError, int noOfIterations) {
		ReadFile obsReader = new ReadFile(fileName);
		obsReader.readObservations();
		BlahutArimoto	ba = new BlahutArimoto(obsReader.getObservations().generateChannel(),acceptableError,noOfIterations);
		ba.calculateCapacity();
		obsReader.getObservations().generateChannel().printSaveChannel("/home/staff/tpc/matrix.txt");
		return (ba.getCapacity());
	}

	@SuppressWarnings("unchecked")
	public static Vector removeIndex (int index, Vector combinations)
	{
		int i = 0;
		while(i<combinations.size())
		{
			if ( ((Vector)combinations.get(i)).contains(new Integer(index)) )
			{ combinations.remove(i); }		
			else 
			{ i++; }
		}
		return combinations;
	}


	@SuppressWarnings("unchecked")
	public static Vector<Vector<Integer>> allCombinations (int fromIndex, int noOfInputs)
	{
		Vector<Vector<Integer>> results = new Vector<Vector<Integer>>();
		if (fromIndex == noOfInputs-1)
		{
			Vector<Integer> trueCase = new Vector<Integer>();
			trueCase.add(new Integer(fromIndex));
			Vector<Integer> falseCase = new Vector<Integer>();
			results.add(trueCase);
			results.add(falseCase);
		}
		if (fromIndex < noOfInputs-1)
		{
			Vector<Vector<Integer>> resultsSoFar = allCombinations (fromIndex+1, noOfInputs);
			for (Iterator<Vector<Integer>> it = resultsSoFar.iterator(); it.hasNext(); )
			{
				Vector<Integer> result = it.next();	
				results.add((Vector<Integer>)result.clone());
				result.add(new Integer(fromIndex));
				results.add((Vector<Integer>)result.clone());
			}
		}
		return results;
	}
}