// This file calculates the mean and variance of a set of multiple test
//  It is used to should the convergence of the mean and variance.

package info;

import java.util.Vector;

public class GroupAnalysis {

	//static String fileNames[] = {"200","400","800","1000","1200","1400","1600","1800","2000","2200","2400"};
	static String fileNames[] = {"100000"};
	static int noOfTests  = 50;
	static String logRoot = "/home/scratch-staff/DCresults/nullCase/";
	static Vector<Double> means = new Vector<Double>();
	static Vector<Double> variance = new Vector<Double>();
	static boolean useBA = false;
	
	
	public static void main (String[] args) {
		for (int fileNameIndex = 0;fileNameIndex < fileNames.length;fileNameIndex++) {
			Vector<Double> capResults = new Vector<Double>();
			double total=0;
			
			for (int testIndex = 0;testIndex < noOfTests;testIndex++) {
	
				double result;
				if (useBA) {
			      result = TestAnony.obsToCapacity( logRoot+fileNames[fileNameIndex]+"_"+testIndex);
				}
				else
				{
					ReadFile obsReader = new ReadFile(logRoot+fileNames[fileNameIndex]+"_"+testIndex);
					obsReader.readObservations();
					result = IT.MIuniformInput(obsReader.getObservations().generateChannel().getMatrix());
					obsReader.getObservations().generateChannel().printChannel();
				}
				capResults.add(new Double(result));
				total = total + result;
			
			}  // end of for loop for the 30 tests
			
			// Calculate mean and variance
			double mean = (total/noOfTests);
			means.add(new Double(mean));
			
			double diffSqSum = 0;
			for (int i=0;i<noOfTests;i++){
				double diff = capResults.get(i) - mean;
				double diffSq = diff*diff;
				diffSqSum = diffSqSum + diffSq;
			}
			variance.add(new Double(diffSqSum/noOfTests));
		} // end of for loop for root file name	

		for (int i = 0;i<fileNames.length;i++) {
			System.out.println("\n"+ fileNames[i]+"  has mean  "+means.get(i)+" var   "+variance.get(i) );
		}
		System.out.println("Correction = (8-1)(4-1)/2.100000 = 0.000105 = 1.05E-4");
	}
}