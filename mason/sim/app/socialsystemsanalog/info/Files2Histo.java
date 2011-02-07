
// File2Histo.java
//
// This class take a directory of observation files, calculates either:
//   - the mutual information with the uniform distribution on inputs
//   - or the maximising input distribution of the FIRST file with all 
//      the matrices 
// It then plots the distribution and calculates the mean and variance 
//
//  Tom Chothia           T.Chothia@cs.bham.ac.uk      6/11/2008  
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


public class Files2Histo {
	
	// The program looks for the files fileNamesRoot_0,fileNamesRoot_1, ... , fileNamesRoot_noOfTests
	//
	//static String fileNamesRoot = "C:\\Documents and Settings\\tpc\\workspace\\Data\\Dist\\100000";
	static String fileNamesRoot = "/home/scratch-staff/DCresults/nullCase/100000";
	static int noOfTests  = 99;
	static double[] Qhat;
	static double[] results = new double[noOfTests];

	static double min  = 0.095;
	static double max  = 0.12000;
	static int segments = 75;
	static double total = 0;
	static double correction ;

	// if useBA = true then Blahut-Arimoto is used to find the maximising distribution
	// of the first file, and then use that for all the other matrices
	// if useBA = false then the uniform distribution is used.
	static boolean useBA = true;
	
	static int inputs;
	static int outputs;
	static int sampleSize;
	
	public static void main(String[] args) {

		// Read the first file and set key values
		ReadFile obsReader = new ReadFile(fileNamesRoot+"_0");
		obsReader.readObservations();
		sampleSize = obsReader.getObservations().getNoOfTests();
		double[][] matrix = obsReader.getObservations().generateChannel().getMatrix();
		inputs = matrix.length;
		outputs = matrix[0].length;
		correction = (((double)inputs-1)*((double)outputs -1))/(2*(double)sampleSize) *IT.log2(Math.E);
		
		
		// find the input distribution to be used with each matrix.
		double mi;
		 if (useBA) {
		   BlahutArimoto	ba = new BlahutArimoto(obsReader.getObservations().generateChannel(),0.00001,5000);
		   mi = ba.calculateCapacity();
		   System.out.println("\\hat Q(W_n) = ");
		   Qhat = ba.getMaxInputDist();
		   IT.printPMF(Qhat);
	 	}
	 	else
	 	{
		  Qhat = IT.unifromDist(inputs);
		  mi = IT.mutualInformation(Qhat,matrix);
		  System.out.println("Using uniform input"); IT.printPMF(Qhat);
		}
	
		// Print the first matrix
		System.out.println("\n");
		obsReader.getObservations().generateChannel().printChannel();
		System.out.println("\n");
		
		//System.out.println("cap for "+fileNamesRoot+"_"+0+" is "+mi);
		System.out.print(mi);
		results[0] = mi;
		total = mi;
		
		for (int testIndex = 1;testIndex < noOfTests;testIndex++) {
			results[testIndex] = mi;
			obsReader = new ReadFile(fileNamesRoot+"_"+testIndex);
			obsReader.readObservations();
		
			mi = IT.mutualInformation2(Qhat, obsReader.getObservations().generateChannel().getMatrix());
			//System.out.println("cap for "+fileNamesRoot+"_"+testIndex+" is "+mi);
			if ((testIndex)%3==0) {System.out.print("\n");} else {System.out.print("     ");}
			System.out.print(mi);
			
			total = total+mi;
		}  
		
		
		// calculate the high of each bar of the histogram
		int[] histoGraph = new int[segments];
		int maxHigh = 0;
		double segwidth = (max -min)/(double)segments;
	
		System.out.println("\n");
		
		for (int seg=0;seg<segments;seg++) {
			for (int i=0;i<noOfTests;i++) {
				if( results[i] > (min+seg*segwidth) && results[i] < (min+(seg+1)*segwidth) ) {
					histoGraph[seg]++;
				}
			}
			//System.out.println("seg"+seg+": "+histoGraph[seg]);
			maxHigh = Math.max(maxHigh, histoGraph[seg]);
		}
		
		// calculate mean and variance
		double mean = total/(double)noOfTests;
		double diffSqSum = 0;
		for (int i=0;i<noOfTests;i++){
			double diff = results[i] - mean;
			double diffSq = diff*diff;
			diffSqSum = diffSqSum + diffSq;
		}
		double var = diffSqSum/noOfTests;
			
		// Print the histograph
		for (int row = maxHigh;row > 0;row--) {
			System.out.print("|");
			for (int col = 0; col<segments;col++) {
				if (histoGraph[col]>=row) {System.out.print("*");} else {System.out.print(" ");}
			}
			System.out.print("\n");
		}
		for (int seg = 0; seg<segments;seg++) {
			if ( (mean > (min+seg*segwidth) && mean < (min+(seg+1)*segwidth))
					||  (correction > (min+seg*segwidth) && correction < (min+(seg+1)*segwidth))) {
						System.out.print("|");
			} else { System.out.print("_");}		
		}
		System.out.print("\n");
		for (int seg = 0; seg<segments;seg++) {
			if ( (mean > (min+seg*segwidth) && mean < (min+(seg+1)*segwidth))
					||  (correction > (min+seg*segwidth) && correction < (min+(seg+1)*segwidth))) {
						System.out.print("|");
			} 	else { System.out.print(" ");}
		}
		System.out.print("\n");
		for (int seg = 0; seg<segments;seg++) {
			if ( (mean > (min+seg*segwidth) && mean < (min+(seg+1)*segwidth))
					&&  !(correction > (min+seg*segwidth) && correction < (min+(seg+1)*segwidth))) {
						System.out.print("M");
			} else if ( !(mean > (min+seg*segwidth) && mean < (min+(seg+1)*segwidth))
					&&  (correction > (min+seg*segwidth) && correction < (min+(seg+1)*segwidth))) {
				System.out.print("C");
			} else if ( (mean > (min+seg*segwidth) && mean < (min+(seg+1)*segwidth))
					&&  (correction > (min+seg*segwidth) && correction < (min+(seg+1)*segwidth))) {
						System.out.print("MC");
			} else System.out.print(" ");
					
		}
		
		// Print the results
		System.out.println("\n");
		System.out.println(inputs+" inputs, "+outputs+" outputs "+" and "+sampleSize+" samples\n");
		
		
		System.out.println("observed mean = "+mean);
		System.out.println("observed variance = "+var);
		
		System.out.println("\ncorrection =  log2(e).(inputs-1)(outputs-1)/2.sampleSize= "+correction);

		System.out.println("\nIf the value is null the the distribution should be chi-squared with:");
		System.out.println("    mean = correction*log2(e) =" + correction);
		System.out.println("    variance = log2(e)^2.(inputs-1)(outputs-1)/2.(sampleSize^2) =" + 
				(((double)inputs-1)*((double)outputs -1))/((double)2*Math.pow(sampleSize,2)) *Math.pow(IT.log2(Math.E),2));
		
		System.out.println("\nIn the value is non-null the the distribution should be normal with:");
		System.out.println("    mean = trueValue+correction");
		System.out.println("    variance = " + IT.variance(sampleSize,Qhat, matrix));	
	}
}
