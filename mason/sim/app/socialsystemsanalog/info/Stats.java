package info;

// This class should contain all the stats methods 
// Currently the mean, variance, distributions are
// calculated in the other classes where ever need

public class Stats {
	
	// chiSquValues95[i] is the 95% value for the chi-squared distribution with i+1 degrees of freedom
	public static double[] chiSquValues95 = {3.84,5.99,7.82,9.49,11.07,12.59,14.07,15.51,16.92,18.31,19.68,21.03,22.36,23.69,25.00,26.30,27.59,28.87,30.14,31.41,32.67,33.92,35.17,36.42,37.65,38.89,40.11,41.34,42.56,43.77,44.99,46.19,47.40,48.60,49.80,51.00,52.19,53.38,54.57,55.76,56.94,58.12,59.30,60.48,61.66,62.83,64.00,65.17,66.34,67.51,68.67,69.83,70.99,72.15,73.31,74.47,75.62,76.78,77.93,79.08,80.23,81.38,82.53,83.68,84.82,85.97,87.11,88.25,89.39,90.53,91.67,92.81,93.95,95.08,96.22,97.35,98.49,99.62,100.75,101.88,103.01,104.14,105.27,106.40,107.52,108.65,109.77,110.90,112.02,113.15,114.27,115.39,116.51,117.63,118.75,119.87,120.99,122.11,123.23,124.34}; 
	
	// rounds the double n to dp decimal places
	public static double round(double n,int dp) {
		return (Math.rint(n*Math.pow(10,dp))/Math.pow(10,dp));
	}
	
	// chiSqu95Interval(n) returns the upper bound for the 
	// 95% confidence interval for the chi-squared distribution 
	// with n degrees of freedom.
	public static double chiSqu95Interval(int freedom) {
		if (freedom<101){
			return chiSquValues95[freedom-1];
		} else {
			// approximate chi-squared distribution with > 100 degrees of freedom as normal
			// chi ~ N(freedom,2freesom)
			// 95% Z value ~ 1.65
			
			// Z = (? - freedom)/Math.sqrt(2.freedom)
			// ? = Z.Math.sqrt(2.freedom)+freedom
			return (1.65*Math.sqrt(2*freedom)+freedom);
		}
	}
	
	// This method calculates the upper bound for a 95% confidence interval
	// for a normal distribution with given mean and variance
	// i.e., the value below which 97.5% of sample take
	public static double upperBoundNormal95(double mean,double variance) {
		// x = mean+Z.Math.sqrt(variance) 
		return mean+1.96*Math.sqrt(variance);
	}
	
	// This method calculates the upper bound for a 95% confidence interval
	// for a normal distribution with given mean and variance
	// i.e., the value below which 97.5% of sample take
	public static double lowerBoundNormal95(double mean,double variance) { 
		// x = mean-Z.Math.sqrt(variance)
		return mean-1.96*Math.sqrt(variance);
	}
	
	
	public static double upperBoundForZero(int freedom,int sampleSize) {
		return Math.pow(IT.log2(Math.E),2)*chiSqu95Interval(freedom)/(2*sampleSize);
	}
	
	// This method calculates the variance for an estimated non-zero capacity estimate.

	public static double nonZeroVariance(double[] Q, double[][] W, int sampleSize) {
		// variance equals 1/N.Sigma_i Q(i).(
		//      (   ( \Sigma_j W(j|i). (log( (Q(i).W(j|i)) / Sigma_{i'} Q(i').W(j|i')))^2)
        //        - (  (\Sigma_j W(j|i).log( (Q(i).W(j|i)) / Sigma_{i'} Q(i').W(j|i')))^2) ) )
		double result = 0;
		for (int x =0;x<W.length;x++) //Sigma_i
		{
			double firstPart = 0; // = ( \Sigma_j W(j|i). (log( (Q(i).W(j|i)) / Sigma_{i'} Q(i').W(j|i')))^2)
			double secondPart = 0; // =(\Sigma_j W(j|i).log( (Q(i).W(j|i)) / Sigma_{i'} Q(i').W(j|i')))^2
			
			for(int y=0;y<(W[0]).length;y++) //Sigma_j
			{
				firstPart = firstPart + W[x][y]*Math.pow(IT.log2( (Q[x]*W[x][y]) / IT.QW(y,Q,W)),2);
			}
			
			for(int y=0;y<(W[0]).length;y++) //Sigma_j
			{
				secondPart = secondPart + W[x][y]*IT.log2( (Q[x]*W[x][y]) / IT.QW(y,Q,W));
			}
			secondPart = Math.pow(secondPart, 2);
			
			result = result + Q[x]*(firstPart-secondPart);
		}
		return ((1/(double)sampleSize)*result)*Math.pow(IT.log2(Math.E),2);
	}
	
}
