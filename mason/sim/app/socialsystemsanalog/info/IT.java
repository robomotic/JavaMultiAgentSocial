// IT.java 
//
// This is a library of useful Information Theory definitions. Most are 
// complete standard, see for example "Elements of Information Theory" 
// by Cover and Thomas.
//  
// Tom Chothia           T.Chothia@cwi.nl      21/05/2008  
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

//Probability Mass Functions are represented by an array of doubles
// p(element i) = pmf[i]

public class IT {
	
	// This method calculates the entropy of a PMF	
	public static double entropy (double[] pmf)
	{
		double result = 0; 
		for (int i= 0;i < pmf.length;i++)
		{
			// Entropy calculation requires that 0.log(0) = 0
			if (pmf[i] != 0) {result = result + pmf[i] * log2(pmf[i]);}
		}
		return (-result);
		
	}
	public static double H (double[] pmf)
	{
		return entropy(pmf);
	}

	// N.B. the inputs to this function are a PMF and a Channel Matrix
	// that links that PMF with the other PMF  i.e., X and the channel to Y
	// The inputs are NOT two pmfs i.e., H(Q,W) not H(X|Y)
	// W[input][output] = W(output|input) 
	public static double conditionalEntropy (double[] pmf, double[][] matrix_W)
	{
		// This method returns H(X|Y) where X is given by pmf and matrix_W is the channel from X to Y  
		//  i.e. value returned equals: - Sigma_x Q(x).Sigma_y W(y|x).log ( Q(x).W(y|x)/(QW)(y) )
		//                            = - Sigma_x.Sigma_y Q(x).W(y|x).log ( Q(x).W(y|x)/(QW)(y) )
		//  QW(y) = 0 => Q(x).W(y|x) = 0 therefore 
		//        if Q(x).W(y|x) = 0 we take W(y|x).log ( Q(x).W(y|x)/(QW)(y) ) = 0MIuniformInput
       
		double result = 0; 
		//Sigma_x
		for (int i= 0;i < pmf.length;i++)
		{
			for (int j = 0; j<matrix_W[0].length;j++) 
			{
				if (pmf[i] != 0 && matrix_W[i][j] != 0) 
				{
					result = result + pmf[i] * matrix_W[i][j] 
					                    * log2( (pmf[i] * matrix_W[i][j] ) / QW(j,pmf,matrix_W));
				}
			}
		}
		return (-result);
	}
	public static double H (double[] Q, double[][] W)
	{
		return H(Q,W);
	}
	
	// N.B. the inputs to this function are a PMF and a Channel Matrix
	// that links that PMF with the other PMF  i.e., X and the channel to Y
	// The inputs are NOT two pmfs i.e., I(Q,W) not I(X;Y)
	
	// This method returns I(Q,W) = I (X;Y) = H(X) - H(X|Y)
	//    produces the same result as mutualInformation_I2
	public static double mutualInformation (double[] Q, double[][] W)
	{
		return ( H(Q) - conditionalEntropy(Q,W));
	}
	
	public static double I (double[] Q, double[][] W)
	{
		return mutualInformation (Q,W);
	}

	// This method returns I(Q,W) = I (X;Y) = Sigma_x Sigma_y Q(x).W(y|x)log(W(y|x)/QW(y))
	//    produces the same result as mutualInformation,I but it might be faster
	public static double mutualInformation2 (double[] Q, double[][] W)
	{
		double result = 0;
		for (int x =0;x<W.length;x++)
		{
			for(int y=0;y<(W[0]).length;y++)
			{
				result = result + Q[x]*W[x][y]*log2(W[x][y] / QW(y,Q,W));
			}
		}
		return result;
	}

	
	public static double variance (int sampleSize, double[] Q, double[][] W)
	{
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
				firstPart = firstPart + W[x][y]*Math.pow(log2( (Q[x]*W[x][y]) / QW(y,Q,W)),2);
			}
			
			for(int y=0;y<(W[0]).length;y++) //Sigma_j
			{
				secondPart = secondPart + W[x][y]*log2( (Q[x]*W[x][y]) / QW(y,Q,W));
			}
			secondPart = Math.pow(secondPart, 2);
			
			result = result + Q[x]*(firstPart-secondPart);
		}
		return ((1/(double)sampleSize)*result);
	}
	
	public static double MIuniformInput (double[][] W)
	{
		return ( mutualInformation(unifromDist(W.length),W));
	}
	
	
	
	// this method calculate the joint entropy of X and Y
	// where p(x,y) = p[x_index][y_index]
	// H( (X,Y) ) = - Sigma_x Sigma_y p(x,y).log (p(x,y)) 
	public static double jointEntropy (double[][] p)
	{
		double result = 0;
		for (int x=0;x<p.length;x++)
		{
			for (int y=0;y<p[0].length;y++)
			{
				if (p[x][y] != 0) 
					{ result = result + p[x][y]*log2( p[x][y]); }
			}
		}
		return -result;
	}

	// this method calculates the relative entropy of two PMFs p and q
	// D(p||q) = Sigma_x p(x) log(p(x)/q(x))
	// 0.log(0/q) = 0   and 
	// p.log(p/0) = inf, (we throw an exception rather that return inf)
	public static double relativeEntropy (double[] p, double[] q) throws ArithmeticException
	{
		double result = 0;
		for (int x=0;x<p.length;x++)
		{
			if (p[x]!=0)
			{
				if (q[x]==0)
				{
					throw new ArithmeticException ("The Relative Entropy equals infinite");
				}
				else 
				{
					result = result + p[x] * log2(p[x]/q[x]);
				}
			}
		}
		return result;
	}
	public static double D (double[] p, double[] q) throws ArithmeticException
	{ 
		return relativeEntropy (p,q);
	}
	public static double KullbackLeibler (double[] p, double[] q) throws ArithmeticException
	{ 
		return relativeEntropy (p,q);
	}
	
	
	// this method finds the change of output elementIndex
	//   using P_Y(y) = R(y) = QW(y) = Sigma_x W(y|x)Q(x)
	public static double outputProb (int outputIndex, double[] Q, double[][] W )
	{
		double result = 0;	
		for (int i=0;i<Q.length;i++)
		{
			result = result + W[i][outputIndex] * Q[i];
		}
		return result;
	}
	public static double R (int outputIndex, double[] inputProbs_Q, double[][] matrix_W )
	{
		return  outputProb (outputIndex, inputProbs_Q, matrix_W);
	}
	public static double QW (int outputIndex, double[] inputProbs_Q, double[][] matrix_W )
	{
		return  outputProb (outputIndex, inputProbs_Q, matrix_W);
	}

	
	// Returns a uniform distribution of length "noOfElements"
	public static double[] unifromDist (int noOfElements)
	{
		double[] dist = new double[noOfElements];
		for (int i=0;i<noOfElements;i++)
		{
			dist[i] = (1.0/(double)noOfElements);
		}
		return dist;
	}
	

	public static double log2 (double x)
	{
	    if (x == 0) {//System.out.println("Taking a log of 0 to be 0"); 
	    	return 0;} 
		else 
			{ return Math.log(x)/Math.log(2);}
	}
	// prints the probability distribution probs with where
	// element a_i has prob[i], to 4 decimal places
	public static void printPMF(double[] probs)
	{
		// Require that names.length = probs.length > 1
		
		System.out.printf(", a0:%6.5f",probs[0]);
		for (int i = 1;i < probs.length;i++)
		{
			System.out.printf(", a"+i+":%6.5f",probs[i]);
		}
	}
	

	// prints the probability distribution probs with where
	// element names[i] has prob[i]
	public static void printPMF(String[] names, double[] probs)
	{
		// Require that names.length = probs.length > 1
		
		System.out.printf(names[0]+":%6.3f",probs[0]);
		for (int i = 1;i < names.length;i++)
		{
			System.out.printf(", "+names[i]+":%6.4f",probs[i]);
		}
	}
	
}
