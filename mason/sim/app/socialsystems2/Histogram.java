/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sim.app.socialsystems2;

/**
 *
 * @author epokh
 */
public class Histogram {
int [] bins = null;
  int nBins;
  float xLow,xHigh;
  float delBin;
  int T=0;
  int overFlows=0,underFlows=0;
  String dataString=null;
  private int bin_index;
  //This constructor assumes the data must be "binned"
  Histogram (int nBins, float xLow, float xHigh){
  
   this.nBins = nBins;
   this.xLow  = xLow;
   this.xHigh = xHigh;

   bins = new int[nBins];
   delBin = (xHigh-xLow)/(float)nBins;

   reset();
  }

  //This constructor assumes the input data is not yet "binned"
  Histogram (int nBins){
  
   this.nBins = nBins;
   bins = new int[nBins];
   delBin = 0;
   reset();
  }
  
  //----------------------------------------------------------------
  // Extra constructor to allow for double values
  Histogram (int nBins, double xLow, double xHigh){
    this(nBins, (float) xLow, (float) xHigh);
  }

  //----------------------------------------------------------------
  void setData(double data){
    setData((float)data);
  }
  //set a non normalized data
  int setData(float data){
   T++;
   if( data < xLow)
   {  
       underFlows++;
       return -1;
   }
   else if ( data >= xHigh) 
   {
       overFlows++;
       return nBins+1;
   }
   else{
     bin_index = (int)((data-xLow)/delBin);
     if(bin_index >=0 && bin_index < nBins) bins[bin_index]++;
     return bin_index;
   }
  }    
  public int getLastBin()
  {
      return bin_index;
  }
  public String getLastBinString()
  {
      return Integer.toString(bin_index);
  }
   //set normalized data
  void setData(int index){
   T++;
   if( index < 0)
     underFlows++;
   else if ( index>nBins) 
     overFlows++;
   else{
     bins[index]++;
   }
  }   

  //----------------------------------------------------------------
  // To display the histogram in a chart, we need to pass the data
  // as a string. 
  public String printIt(){
    dataString = "";
    for (int i=0; i<nBins; i++){
      dataString += bins[i] + " ";
    }
    return dataString;
  }
  
  //----------------------------------------------------------------
  public void reset(){
    dataString = "";
    for (int i=0; i<nBins; i++){
      bins[i]=0;
      dataString = dataString + bins[i] + " ";
    }
  }

}
