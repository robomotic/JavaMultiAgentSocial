// Channel.java
//
// This class represents an information theoretic channel
//
//  Tom Chothia           T.Chothia@cs.bham.ac.uk      16/10/2008  
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
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

public class Channel {

	// an array of the output action labels
	// outputNames[i] will be the output of the ith column of the matrix.
	String[] outputNames; 

	// an array of the input action labels. For single user matrixes
	// inputNames[i] will be the output of the ith roe of the matrix.
	// for multi-access channels the row label is found via inputsPerRow
	String[] inputNames;       

	// The channel matrix
	double[][] channelMatrix_W; 

	// inputsPerRow is an array of Vectors where inputPerRow[i] is a Vector
	// of the indexes of the inputs on row i of the channel matrix 
	@SuppressWarnings("unchecked")
	Vector[] inputsPerRow;

	// In Condition channels groupNames[i] is the group of the ith row
	String[] groupNames;

	// currently unused
	// This will be used later when I add multipule groups for a row
	@SuppressWarnings("unchecked")
	Vector[] groupsPerRow;

	// for row i: groupNames[groupForRow[i]] is its group label.
	int[] groupForRow;

	// rowsForGroup[i] is a Vector of the rows that are in group groupNames[i]
	@SuppressWarnings("unchecked")
	Vector[] rowsForGroup;

	// Is the channel single user, multi-access etc.
	int kind;

	public static final int BASIC = 1;
	public static final int MULTI = 2;
	public static final int COND = 3;
	public static final int MULTICOND = 4;
	public static final int NEWMULTICOND = 5;
	
	public Channel() {}

	public Channel(int kind, String[] inputNames, String[] outputNames, double[][] matrix)
	{
		this.kind = kind;
		this.inputNames = inputNames;
		this.outputNames = outputNames;
		this.channelMatrix_W = matrix;
	}

	public String[]   getOutputNames ()   { return outputNames; }
	public String[]   getInputNames ()    { return inputNames; } 
	public String[]   getGroupNames ()    { return groupNames; }
	public double[][] getMatrix ()        { return channelMatrix_W; } 
	@SuppressWarnings("unchecked")
	public Vector[]   getInputsPerRow ()  { return inputsPerRow; }
	@SuppressWarnings("unchecked")
	public Vector     getInputRow (int i) { return inputsPerRow[i]; }
	@SuppressWarnings("unchecked")
	public Vector[]   getGroupsPerRow ()  { return groupsPerRow; }
	@SuppressWarnings("unchecked")
	public Vector[]   getRowsForGroup ()  { return rowsForGroup; }
	public int        getKind ()          { return kind; }
	public int        noOfInputs()        { return inputNames.length; }
	public int        noOfRows()          { return channelMatrix_W.length; }
	public int        noOfOutputs()       { return outputNames.length; }
	public double     prob(int a,int o)   { return channelMatrix_W[a][o]; }

	public String getInputName(int i)  { return inputNames[i]; }
	public String getOutputName(int i) { return outputNames[i]; }

	public void setOutputNames (String[] arg)  { outputNames = arg; }
	public void setInputNames (String[] arg)   { inputNames = arg; }
	public void setGroupNames (String[] arg)   { groupNames = arg; }
	public void setMatrix (double[][] arg)     { channelMatrix_W = arg; } 
	@SuppressWarnings("unchecked")
	public void setInputsPerRow (Vector[] arg) { inputsPerRow = arg; }
	@SuppressWarnings("unchecked")
	public void setGroupsPerRow (Vector[] arg) { groupsPerRow = arg; }
	public void setGroupForRow (int[]arg)      { groupForRow = arg; }
	public void setKind (int arg)              { kind = arg; }
	@SuppressWarnings("unchecked")
	public void setRowsForGroup (Vector[] arg) { rowsForGroup = arg; }


	public String getRowLabel (int rowNo)
	{
		switch (kind) 
		{
			case BASIC: return (inputNames[rowNo]);
			case COND: return (inputNames[rowNo]+":"+groupNames[groupForRow[rowNo]]);
		}
		if (kind == BASIC || kind == COND)
			{ return (inputNames[rowNo]); }
		else {
			String result ="";
			if (inputsPerRow[rowNo].size() >0 )
			{
				result = inputNames[((Integer)inputsPerRow[rowNo].get(0)).intValue()];
				for (int i=1;i<inputsPerRow[rowNo].size();i++)
				{
					result = result +", "+inputNames[((Integer)inputsPerRow[rowNo].get(i)).intValue()];
				}	

			}
			return result;
		}	
	}

	public String[] inputsForRow (int rowNo)
	{ 
		if (kind == BASIC || kind == COND)
		{
			String[] result = {inputNames[rowNo]};
			return (result);
		}
		else
		{
			if (inputsPerRow[rowNo].size() >0 )
			{
				String[] result = new String[inputsPerRow[rowNo].size()];
				for (int i=0;i<inputsPerRow[rowNo].size();i++)
				{
					result[i] = inputNames[((Integer)inputsPerRow[rowNo].get(i)).intValue()];
				}	
				return result;
			}
			else
			{
				String[] result = {""};
				return result;
			}
		}	
	}

	//
	//Pretty Print functions
	//	

	// prints i+1 spaces
	public static void addspaces(int i)
	{
		for (int j = 0;j<=i;j++)
		{
			System.out.print(" ");
		}
	}

	// prints the string and pads it to length i
	public static void printToLength(String s, int i)
	{
		System.out.print(s);
		for (int j = s.length();j<=i;j++)
		{
			System.out.print(" ");
		}
	}

	public static void printSaveToLength(BufferedWriter writer, String s, int i)
	{
		System.out.print(s);
		try
		{
			writer.write(s);
			for (int j = s.length();j<=i;j++)
			{
				System.out.print(" ");
				writer.write(" ");
			}
		}
		catch (Exception e) {System.out.print(e);}
	}


	// Print the channel to standard out
	public void printChannel()
	{
		// Find maximum output length
		int maxOutLength = 0;
		for (int i=0;i< outputNames.length ;i++)
		{
			maxOutLength = Math.max(maxOutLength, outputNames[i].length());
		}
		//for (int i=0;i< channelMatrix_W.length ;i++)
		//{
		//	for (int j=0;j<channelMatrix_W[0].length;j++)
		//	{
		//		maxOutLength = Math.max(maxOutLength, ((new Double(channelMatrix_W[i][j]).toString()).length()));
		//	}
		//}
		//
		//Rounding probs to 4 d.p. therefore all probs have length 6 
		maxOutLength = Math.max(maxOutLength,6);
		// Find the Max Input row label length
		int maxInLength = 0;
		switch (kind)
		{
		case BASIC: 
			for (int i=0;i<inputNames.length;i++)
			{
				maxInLength = Math.max(maxInLength,inputNames[i].length());
			}
			maxInLength = maxInLength + 2;
			break;
		case MULTI: 
			for (int i=0;i<inputNames.length;i++)
			{
				maxInLength = maxInLength + inputNames[i].length();
			}
			maxInLength = maxInLength + inputNames.length;
			break;
		case COND: 
			for (int i=0;i<inputNames.length;i++)
			{
				maxInLength = Math.max(maxInLength,inputNames[i].length());
			}
			int maxGroupLength = 0;
			for (int i=0;i<groupNames.length;i++)
			{
				maxGroupLength = Math.max(maxGroupLength,groupNames[i].length());
			}
			maxInLength = maxInLength + maxGroupLength;
			break;
		}

		//Print the outputs names
		addspaces(maxInLength+2); 
		for (int i=0;i<outputNames.length;i++)
		{
			System.out.print("| ");
			printToLength(outputNames[i],maxOutLength);
		}
		System.out.println(""); 

		// Print each row
		for (int i=0;i<channelMatrix_W.length;i++)
		{ printRow (i,maxInLength, maxOutLength); }

	}

	public void printRow (int RowNo,int maxInLength, int maxOutLength)
	{
		System.out.print(" ");
		printToLength(getRowLabel(RowNo),maxInLength+1); 
		for (int j=0;j<outputNames.length;j++)
		{ 
			System.out.print("| ");
			printToLength(Double.toString(Stats.round(channelMatrix_W[RowNo][j],Math.max(4,maxOutLength-2))),maxOutLength);  
		}
		System.out.println(""); 
	}

	// Print the channel to standard out and write it to "fileName"
	public void printSaveChannel(String fileName)
	{
		// Find maximum output length
		int maxOutLength = 0;
		for (int i=0;i< outputNames.length ;i++)
		{
			maxOutLength = Math.max(maxOutLength, outputNames[i].length());
		}
		for (int i=0;i< channelMatrix_W.length ;i++)
		{
			for (int j=0;j<channelMatrix_W[0].length;j++)
			{
				maxOutLength = Math.max(maxOutLength, ((new Double(channelMatrix_W[i][j]).toString()).length()));
			}
		}

		// Find the Max Input row label length
		int maxInLength = 0;
		switch (kind)
		{
		case BASIC: 
			for (int i=0;i<inputNames.length;i++)
			{
				maxInLength = Math.max(maxInLength,inputNames[i].length());
			}
			maxInLength = maxInLength + 2;
			break;
		case MULTI: 
			for (int i=0;i<inputNames.length;i++)
			{
				maxInLength = maxInLength + inputNames[i].length();
			}
			maxInLength = maxInLength + inputNames.length;
			break;
		case COND: 
			for (int i=0;i<inputNames.length;i++)
			{
				maxInLength = Math.max(maxInLength,inputNames[i].length());
			}
			int maxGroupLength = 0;
			for (int i=0;i<groupNames.length;i++)
			{
				maxGroupLength = Math.max(maxGroupLength,groupNames[i].length());
			}
			maxInLength = maxInLength + maxGroupLength;
			break;
		}

		//Print the outputs names
		try 
		{ 
			BufferedWriter writer      =  new BufferedWriter(new FileWriter(fileName));

			//addspaces(maxInLength+2); 

			printSaveToLength(writer,"("+inputNames.length+","+outputNames.length+")",maxInLength+2);

			//for (int j = 0;j<=maxInLength+2;j++)
			//{
			//	 System.out.print(" ");
			//	 writer.write(" ");
			//}

			for (int i=0;i<outputNames.length;i++)
			{
				System.out.print("| ");
				writer.write("| ");
				printSaveToLength(writer,outputNames[i],maxOutLength);
			}
			System.out.println(""); 
			writer.write("\n");

			// Print each row
			for (int i=0;i<channelMatrix_W.length;i++)
			{ printSaveRow (writer,i,maxInLength, maxOutLength); }

			writer.close();
		} 
		catch (IOException e) { System.out.println("Error opening output files:"+fileName); }
	}	


	public void printSaveRow (BufferedWriter writer, int RowNo,int maxInLength, int maxOutLength)
	{
		System.out.print(" ");
		try
		{
			writer.write(" ");
			printSaveToLength(writer,getRowLabel(RowNo),maxInLength+1); 
			for (int j=0;j<outputNames.length;j++)
			{ 
				System.out.print("| ");
				writer.write("| ");
				printSaveToLength(writer,Double.toString(channelMatrix_W[RowNo][j]),maxOutLength);  
			}
			System.out.println(""); 
			writer.write("\n");
		}
		catch (Exception e) { System.out.print(e); }
	}
}
