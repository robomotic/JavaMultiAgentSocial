// ReadFile.java
//
// This file contains the methods needed to read a conditional probability
// matrix or a list of observations from a text file.
//
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


package sim.app.socialsystems2;
import java.io.*;
import java.util.regex.*;
import java.util.Vector;

public class ReadFile {

	int noOfInputs;
	String[] outputNames; 
    String[] inputNames;       
    double[][] channelMatrix; 
    Observations obs;
    int noOfGroups;
    String[] groupNames;
    int[] groupForRow;
	@SuppressWarnings("unchecked")
	Vector[] rowsForGroup;
    // boolean multiSender;
    // inputsPerRow is a vector of vectors. 
    // inputPerRow.get(i) are the indexes of the inputs 
    // on row i of the channel matrix 
	Vector<Vector<Integer>> inputsPerRow = new Vector<Vector<Integer>>();
    Channel channel;
    
    String fileName;
    
    public ReadFile (String channelFile) {fileName = channelFile; }

    public Channel getChannel()
    {
    		return channel;
    }
    @SuppressWarnings("unchecked")
	public Vector[] getInputPerRow()
    {
    		return vectorVectorToArrayVector(inputsPerRow);
    }
    //public boolean multiSender()
    //{
    //		return multiSender;
    //}
    public Observations getObservations()
    {
    		return obs;
    }
    public String[] getOutputName()
    {
    		return outputNames;
    }
    public String[] getInputName ()
    {
    		return inputNames;
    }
    public double[][] getChannelMatrix ()
    {
    		return channelMatrix;
    }
    
    
    

    @SuppressWarnings("unchecked")
	public void readChannel ()
	{
	    try {
	      BufferedReader reader =  new BufferedReader(new FileReader(fileName));
	      try {
	    	  
	        String line = reader.readLine();	
	        while ( (line.trim()).equalsIgnoreCase("") || (line.trim()).startsWith("//"))
	        	{ line = reader.readLine(); }

	        channel = new Channel();
	        
	        String[] terms = line.split("\\|");   
	        Pattern pattern = Pattern.compile("\\([\\s]*([\\d]+)[\\s]*,[\\s]*([\\d]+)[\\s]*\\)[\\s]*:[\\s]*([\\d]+)*[\\s]*");	
	        Matcher matcher = pattern.matcher(terms[0].trim());

	        if (matcher.find()) 
		      {
  			  	// It's a conditional channel.
  			  	channel.kind = Channel.COND;
  			  
  			  	int noOfInputs = Integer.parseInt(matcher.group(1));
  			  	int noOfOutputs = Integer.parseInt(matcher.group(2));
  			  	int noOfGroups = Integer.parseInt(matcher.group(3));
    	     
  			  	outputNames = new String[noOfOutputs];
  			  	inputNames = new String[noOfInputs];           	 
  			  	groupNames = new String[noOfGroups];
  			    
  			  	channelMatrix = new double[noOfInputs][noOfOutputs];
  			  	groupForRow = new int[noOfInputs];
	        	
  			  	rowsForGroup = new Vector[noOfGroups];
  			  	for(int i=0;i<noOfGroups;i++)
  			  	{
  			  		rowsForGroup[i] = new Vector();
  			  	}
  			  	
  			  	// Read the output names from along the top of the matrix
  			  	for (int i= 1;i<terms.length;i++)
  			  	{
  			  		outputNames[i-1] = terms[i].trim();
  			  	}
  			  	channel.setOutputNames(outputNames);
	        	
  			  	// Read the rest of the matrix one line at a time
  			  	int linecounter = 0;
  			  	while (( line = reader.readLine()) != null)
  			  	{
  			  		if (!(line.trim()).equalsIgnoreCase("") && !(line.trim()).startsWith("//") )
  			  		{	        			
  			  			terms = line.split("\\|");
  			  			String[] rowlabel = terms[0].split(":");
  			  			inputNames[linecounter] = rowlabel[0].trim();

  			  			int groupIndex = addifnew(rowlabel[1].trim(),groupNames);
  			  			
  			  			groupForRow[linecounter] = groupIndex;
	        			
  			  		    rowsForGroup[groupIndex].add(new Integer(linecounter));
  			  			
  			  			for (int i=1;i<terms.length;i++)
  			  			{
  			  				channelMatrix[linecounter][i-1] = Double.parseDouble(terms[i].trim());
  			  			}
  			  			linecounter++;
  			  		}
  			  	}
  			  	channel.setRowsForGroup(rowsForGroup);
  			  	channel.setGroupForRow(groupForRow);
  			  	channel.setInputNames(inputNames);
  			  	channel.setMatrix(channelMatrix);
  			  	channel.setOutputNames(outputNames);
  			  	channel.setGroupNames(groupNames);			  
		    }
	        else
	        {
	        	pattern = Pattern.compile("\\([\\s]*([\\d]+)[\\s]*,[\\s]*([\\d]+)[\\s]*\\)");
	        	matcher = pattern.matcher(terms[0].trim());
	        
	        	if (matcher.find()) 
	        	{
	        		// It's a basic channel
	        		channel.kind = Channel.BASIC;
	        	
	        		int noOfInputs = Integer.parseInt(matcher.group(1));
	        		int noOfOutputs = Integer.parseInt(matcher.group(2));
	     
	        		outputNames = new String[noOfOutputs];
	        		inputNames = new String[noOfInputs];           	        
	        		channelMatrix = new double[noOfInputs][noOfOutputs];
	        
	        		// Read the output names from along the top of the matrix
	        		for (int i= 1;i<terms.length;i++)
	        		{
	        			outputNames[i-1] = terms[i].trim();
	        		}
	        		channel.setOutputNames(outputNames);
	        	
	        		// Read the rest of the matrix one line at a time
	        		int linecounter = 0;
	        		while (( line = reader.readLine()) != null)
	        		{
	        			if (!(line.trim()).equalsIgnoreCase("") && !(line.trim()).startsWith("//") )
	        			{	        			
	        				terms = line.split("\\|");
	        				inputNames[linecounter] = terms[0].trim();
	        				for (int i=1;i<terms.length;i++)
	        				{
	        					channelMatrix[linecounter][i-1] = Double.parseDouble(terms[i].trim());
	        				}
	        				linecounter++;
	        			}
	        		}
	        		channel.setInputNames(inputNames);
	        		channel.setMatrix(channelMatrix);
	        	}
	        	else
	        	{
	        		pattern = Pattern.compile("\\([\\s]*([\\d]+)[\\s]*,[\\s]*([\\d]+)[\\s]*,[\\s]*([\\d]+)[\\s]*\\)*");	
	        		matcher = pattern.matcher(terms[0].trim());
	        		if (matcher.find()) 
	        		{
	        			// Channel is a multi-access channel
	        			channel.kind = Channel.MULTI;
	        		
	        			String[] arrayOfInputs;
	        			outputNames = new String[Integer.parseInt(matcher.group(3))];
	        			inputNames = new String[Integer.parseInt(matcher.group(1))];
	        			channelMatrix = new double[Integer.parseInt(matcher.group(2))][Integer.parseInt(matcher.group(3))];
	      	   
	        			// Read the output names from along the top of the matrix
	        			for (int i= 1;i<terms.length;i++)
	      	        		{ outputNames[i-1] = terms[i].trim(); }
	        			channel.setOutputNames(outputNames);
	      	   
	        			// Read the rest of the matrix one line at a time
	        			int rowCounter = 0;
	        			while (( line = reader.readLine()) != null)
	        			{
	        				if (!(line.trim()).equalsIgnoreCase("") && !(line.trim()).startsWith("//") )
	        				{
	        					terms = line.split("\\|");
	        					arrayOfInputs = terms[0].split(",");    		
	        					// change all the strings to their index's inNames
	        					//  add a new entry to inNames if needed
	        					//  then add the indexes to inputsPerRow[linecounter]
	        					Vector<Integer> inputIndexRowVector = new Vector<Integer>();
	        					if (!arrayOfInputs[0].trim().equals(""))
	        					{
	        						for (int ic=0;ic<arrayOfInputs.length;ic++)
	        						{
	        							// look up the index of arrayOfInputs[ic]
	        							int inputIndex = 0;
	        							while (inputNames[inputIndex] != null && !(inputNames[inputIndex].equals(arrayOfInputs[ic].trim())))
	      	        						{ inputIndex++; }
	        							if (inputNames[inputIndex] == null)
	      	        						{ inputNames[inputIndex] = arrayOfInputs[ic].trim(); }
	        							// inputIndex is the index of arrayOfInputs[ic]
	        							inputIndexRowVector.add(new Integer(inputIndex));
	        						}
	        					}      		
	        					inputsPerRow.add(inputIndexRowVector);
	        					for (int i=1;i<terms.length;i++)
	      	        				{ channelMatrix[rowCounter][i-1] = Double.parseDouble(terms[i].trim()); }
	        					rowCounter++;
	        				}
	        			}
	      	        
	        			channel.setInputNames(inputNames);
	        			channel.setInputsPerRow(vectorVectorToArrayVector(inputsPerRow));
	        			channel.setMatrix(channelMatrix);
	      	      	}
	        		else
	        		{
	        			  // The file has the wrong format
	        			  System.out.println("Syntax error while reading line: "+line); 
	        			  System.out.println(" ... file should start with a term of the form (noOfInputs,noOfOutputs)"); 
	        			  System.out.println("           (noOfInputs,noOfOutputs):noOfGroups or (noOfInputs,noOfRows,noOfOutputs)"); 
	        			  System.exit(0);
	        		  }
	        	  }
	        }
	      }
	      finally {
	        reader.close();
	      }
	   }  
	   catch (FileNotFoundException ex){
		     System.out.println("File not found");
		     System.out.println("I was looking for "+fileName);
		     System.out.println("             ... but couldn't find it");
		     System.exit(0);
		     }
	   catch (IOException ex){
	     ex.printStackTrace();
	     System.exit(0);
	   }
	}
		
	
    int addifnew(String str, String[] strs)
    {
    	int i;
    	for(i = 0;i<strs.length;i++)
    	{
    		if (strs[i] == null) { strs[i]=str;break;}
    		if (strs[i].equals(str)) {break;}
    	}
    	return i;
    }
    
    @SuppressWarnings("unchecked")
	public static Vector[] vectorVectorToArrayVector (Vector<Vector<Integer>> v)
    {
    	
    	Vector[] result = new Vector[v.size()]; 
    	for (int i=0;i<v.size();i++)
    	{
    		result[i] = (v.get(i));
    	}
    	return result;
    
    }
    
    
//	public void readMultiChannel ()
//	{
//		try {
//	      BufferedReader reader =  new BufferedReader(new FileReader(fileName));
//
//	      try {
//	        String line = reader.readLine();	       
//	        String[] terms = line.split("\\|");
//	        String[] arrayOfInputs;
//	        Pattern pattern = Pattern.compile("\\([\\s]*([\\d]+)[\\s]*,[\\s]*([\\d]+)[\\s]*,[\\s]*([\\d]+)[\\s]*\\)");
//	        Matcher matcher = pattern.matcher(terms[0].trim());
//	        if (!matcher.find()) 
//	        {
//	        		System.out.println("Syntax error while reading line: "+line); 
//	        		System.out.println(" ... wants a term of the form (noOfInputs,noOfRows,noOfOutputs)"); 
//	        		System.exit(0);
//	        	}
//	        outputNames = new String[Integer.parseInt(matcher.group(3))];
//	        inputNames = new String[Integer.parseInt(matcher.group(1))];           	        
//	        channelMatrix = new double[Integer.parseInt(matcher.group(2))][Integer.parseInt(matcher.group(3))];
//	        for (int i= 1;i<terms.length;i++)
//	        		{ outputNames[i-1] = terms[i].trim(); }
//	        int rowCounter = 0;
//	        while (( line = reader.readLine()) != null)
//	        {
//	        		terms = line.split("\\|");
//	        		arrayOfInputs = terms[0].split(",");    		
//	        		// change all the strings to their index's inNames
//	        		//  add a new entry to inNames if needed
//	        		//  then add the indexes to inputsPerRow[linecounter]
//	        		Vector inputIndexRowVector = new Vector();
//	        		if (!arrayOfInputs[0].trim().equals(""))
//	        		{
//	        			for (int ic=0;ic<arrayOfInputs.length;ic++)
//	        			{
//	        				// look up the index of arrayOfInputs[ic]
//	        				int inputIndex = 0;
//	        				while (inputNames[inputIndex] != null && !(inputNames[inputIndex].equals(arrayOfInputs[ic].trim())))
//	        					{ inputIndex++; }
//	        				if (inputNames[inputIndex] == null)
//	        					{ inputNames[inputIndex] = arrayOfInputs[ic].trim(); }
//	        				// inputIndex is the index of arrayOfInputs[ic]
//	        				inputIndexRowVector.add(new Integer(inputIndex));
//	        			}
//	        		}      		
//	        		inputsPerRow.add(inputIndexRowVector);
//	        		for (int i=1;i<terms.length;i++)
//	        			{ channelMatrix[rowCounter][i-1] = Double.parseDouble(terms[i].trim()); }
//	        		rowCounter++;
//	        	}
//	      }
//	      finally { reader.close(); }
//	    }
//	    catch (IOException ex){ ex.printStackTrace(); }
//	  }

	

	
	
	public void readObservations ()
	{
		obs = new Observations();
		try 
		{
			BufferedReader input =  new BufferedReader(new FileReader(fileName));
			try
			{ 
				 String line = input.readLine();	
				 while ( (line.trim()).equalsIgnoreCase("") || (line.trim()).startsWith("//"))
		        	{ line = input.readLine(); }
				
				
				Pattern pattern = Pattern.compile("\\([\\s]*([\\w:]+)[\\s]*,[\\s]*([\\w:]+)[\\s]*\\)");
				Matcher matcher;
				
				
				while ( (line.trim()).equalsIgnoreCase("") || (line.trim()).startsWith("//"))
		        	{ line = input.readLine(); }
				 
				while ( line != null)
				{
					matcher = pattern.matcher(line.trim());
					if (!matcher.find()) {System.out.println("Syntax error while reading line: "+line);}
					obs.addObservation(matcher.group(1),matcher.group(2));
					line = input.readLine();
				}
			}
			finally 
			{
				input.close();
			}
	    }
		 catch (FileNotFoundException ex){
		     System.out.println("File not found");
		     System.out.println("I was looking for "+fileName);
		     System.out.println("             ... but couldn't find it");
		     System.exit(0);
		     }
	   catch (IOException ex){
	     ex.printStackTrace();
	     System.exit(0);
	   }
	}	
}
	
	
	
	
