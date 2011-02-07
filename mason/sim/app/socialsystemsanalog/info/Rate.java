// Rate.java
//
// This file contains the methods needed to calculate worst case
// anonymity bounds.
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
public class Rate {

	
	// find r such that r.p(o|a) >= p(o|a') forall o,a,a;
	public static double findWorstCaseBoundCPP (double[][] matrix)
	{
		double bound = 0;
		//  check for each output in turn
		for (int y = 0;y<matrix[0].length;y++)
		{
			for (int x = 0;x<matrix.length;x++)
			{
				for (int x2 = x;x2<matrix.length;x2++)
				{
					if (matrix[x][y] == 0 || matrix[x2][y] == 0)
						{ System.exit(1); }
					else
						{ bound = Math.max(bound,(matrix[x][y]/matrix[x2][y]));}
				}
			}
		}
		return bound;
	}


	// find r lowest such that "p(o|a') < r.Sigma_a p(o|a)" forall o,a';
	public static double findWorstCaseBound (double[][] matrix)
	{
		double bound = 0;
		//  check for each output in turn
		for (int y = 0;y<matrix[0].length;y++)
		{
			double probSum = 0;
			for (int i=0;i<matrix.length;i++)
				{ probSum = probSum + matrix[i][y]; }
			for (int x = 0;x<matrix.length;x++)
			{
				if (matrix[x][y] != 0)
				{
					bound = Math.max(bound,(matrix[x][y]/probSum));}
				}
			}
		return bound;
	}
}
