/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sim.app.socialsystems2;
import java.awt.Color;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import sim.util.Bag;
/**
 *
 * @author epokh
 */
public class InputAnalysis {
    private static boolean computeC=false;
    private int empower_order=1;
    private int max_word_size=32;
    private int min_word_size=1;

    public XYSeries bias_series;
    public XYSeries sigma_series;
    public XYSeries series_class;
    public XYSeries mi_series_1;
    public XYSeries mi_series_2;
    public XYSeries mi_series_3;
    public XYSeries mi_series_4;
    
    public XYSeries weight_series_before;
    public XYSeries weight_series_after;
    public int timebreak=200000;
    String outputdirectory;;
    String[] datasimfile;
    public InputAnalysis()
    {
          mi_series_3 = new XYSeries("Before");
          mi_series_4 = new XYSeries("After");  
          series_class = new XYSeries("Class");  
          mi_series_1=new XYSeries("Before");
          mi_series_2=new XYSeries("After");
          weight_series_before=new XYSeries("Weight before");
          weight_series_after=new XYSeries("Weight after");
          
          outputdirectory="/home/epokh/DataSim/";
          datasimfile=new String[5];
          datasimfile[0]=outputdirectory+"sim_attraction_food.csv";
          datasimfile[2]=outputdirectory+"sim_attraction_agent.csv";
          datasimfile[1]=outputdirectory+"simulation_class.csv";
          datasimfile[3]=outputdirectory+"sim_avoidance.csv";
          datasimfile[4]=outputdirectory+"sim_output.csv";
    }
    public static void main(String[] args){
        InputAnalysis entropy=new InputAnalysis();
        try {
            //entropy.timeEntropy(entropy.datasimfile[0], "HX_comparison.csv","unbiased");
            //entropy.readSimulationClassFile();
           // entropy.readSimulationDataAgentFile(entropy.datasimfile[0],"H(X,Y)_all_food.csv");
            //entropy.readSimulationSingleAgent(entropy.datasimfile[3],"H(X,Y)_avoidance.csv","biased",0);
            //entropy.readSimulationSingleAgent(entropy.datasimfile[0],"H(X,Y)_all_food.csv","biased",0);
            if(computeC)
            {
                for (int k = 1; k <= 4; k++) {
                    String suffix=String.valueOf(k);
                    suffix="_n"+suffix+".csv";
                    entropy.empower_order=k;
                    //entropy.computeCapacity(entropy.datasimfile[3], entropy.datasimfile[2], entropy.datasimfile[0], entropy.datasimfile[4], 0, "Cmax_avoid"+suffix);
                    //entropy.computeCapacity(entropy.datasimfile[3], entropy.datasimfile[2], entropy.datasimfile[0], entropy.datasimfile[4], 1, "Cmax_agent"+suffix);
                    entropy.computeCapacity(entropy.datasimfile[3], entropy.datasimfile[2], entropy.datasimfile[0], entropy.datasimfile[4], 2, "Cmax_food"+suffix);
                }
            }
            else{
                for (int k = 1; k <= 4; k++) {
                    String suffix = String.valueOf(k);
                    suffix = "_n" + suffix + ".csv";
                    entropy.empower_order = k;
                    entropy.computeIOMI(entropy.datasimfile[3], entropy.datasimfile[2], entropy.datasimfile[0], entropy.datasimfile[4],
                            0, "unbiased", "MI_avoid" + suffix);
                    entropy.computeIOMI(entropy.datasimfile[3], entropy.datasimfile[2], entropy.datasimfile[0], entropy.datasimfile[4],
                            1, "unbiased", "MI_agent" + suffix);
                    entropy.computeIOMI(entropy.datasimfile[3], entropy.datasimfile[2], entropy.datasimfile[0], entropy.datasimfile[4],
                            2, "unbiased", "MI_food" + suffix);
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(InputAnalysis.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(InputAnalysis.class.getName()).log(Level.SEVERE, null, ex);
        }

        // create subplot 1...
        XYSeriesCollection xyDataset = new XYSeriesCollection();
        xyDataset.addSeries(entropy.mi_series_1);
        xyDataset.addSeries(entropy.mi_series_2);
        final XYItemRenderer renderer1 = new StandardXYItemRenderer();
        final NumberAxis rangeAxis1 = new NumberAxis("Entropy [Bits]");
        rangeAxis1.setAutoRange(true);
        rangeAxis1.setAutoTickUnitSelection(true);
        //rangeAxis1.setRange(3.5, 4.5);
        final XYPlot subplot1 = new XYPlot(xyDataset, null, rangeAxis1, renderer1);
        subplot1.setRangeAxisLocation(AxisLocation.TOP_OR_LEFT);
        renderer1.setBasePaint(Color.black);
        //create subplot 2...
        XYSeriesCollection xyDataEntropy2 = new XYSeriesCollection();
        xyDataEntropy2.addSeries(entropy.mi_series_3);
        xyDataEntropy2.addSeries(entropy.mi_series_4);
        xyDataEntropy2.addSeries(entropy.series_class);
        final XYItemRenderer renderer2 = new StandardXYItemRenderer();
        renderer2.setBasePaint(Color.black);
        final NumberAxis rangeAxis2 = new NumberAxis("Entropy [Bits]");
        rangeAxis2.setAutoRange(true);
        final XYPlot subplot2 = new XYPlot(xyDataEntropy2, null, rangeAxis2, renderer2);
        subplot2.setRangeAxisLocation(AxisLocation.TOP_OR_LEFT);   
        // parent plot...
        final CombinedDomainXYPlot plot = new CombinedDomainXYPlot(new NumberAxis("Domain"));
        plot.setGap(10.0);
        // add the subplots...
        plot.add(subplot1, 1);
        plot.add(subplot2, 1);
        plot.setOrientation(PlotOrientation.VERTICAL);
        JFreeChart chart=new JFreeChart("Summary entropy",JFreeChart.DEFAULT_TITLE_FONT, plot, true);
         ChartFrame frame1=new ChartFrame("Entropy comparison",chart);
         frame1.setVisible(true);
         frame1.setSize(600,400);
    }
    //input: log file in CSV for distal, reflex for every behaviour
    //output: mutual information of the closed loop for every behaviour
    public void computeIOMI(String file_avoid, String file_agent, String file_food, String file_motor, 
            int type, String method,String filename) throws FileNotFoundException, IOException {
        //first empty the previous series
        mi_series_1.clear();
        mi_series_2.clear();
        mi_series_3.clear();
        mi_series_4.clear();
        weight_series_before.clear();
        weight_series_after.clear();
        //then open all the log files from the simulations
        CsvReader csvimport_avoid = new CsvReader(file_avoid);
        csvimport_avoid.readHeaders();
        CsvReader csvimport_agent = new CsvReader(file_agent);
        csvimport_agent.readHeaders();
        CsvReader csvimport_food = new CsvReader(file_food);
        csvimport_food.readHeaders();
        CsvReader csvimport_motor = new CsvReader(file_motor);
        csvimport_motor.readHeaders();
        int col_avoid = csvimport_avoid.getHeaderCount();
        int col_agent = csvimport_agent.getHeaderCount();
        int col_food = csvimport_food.getHeaderCount();
        int col_motor = csvimport_motor.getHeaderCount();
        if (col_avoid == col_agent && col_agent == col_food) {
            System.out.println("Import done");
            int nagents=(col_avoid-1)/3;
            EntropyString mi_reflexmotor_before[] = new EntropyString[nagents];
            EntropyString mi_distalmotor_before[] = new EntropyString[nagents];
            EntropyString mi_reflexmotor_after[] = new EntropyString[nagents];
            EntropyString mi_distalmotor_after[] = new EntropyString[nagents];
            
            for(int k=0;k<nagents;k=k+1)
            {
                mi_distalmotor_before[k] = new EntropyString(0, true, method);
                mi_reflexmotor_before[k] = new EntropyString(0, true, method);    
                //empowerment measure of order 3:
                // 3 motor words followed by 1 sensor word: 
                //the motor action is encoded in 3 bits because only 8 directions available
                mi_distalmotor_before[k].setBase(3,2,empower_order,1);
                mi_reflexmotor_before[k].setBase(3,2,empower_order,1);
                mi_reflexmotor_after[k] =new EntropyString(0, true, method);
                mi_distalmotor_after[k]=new EntropyString(0, true, method);
                mi_reflexmotor_after[k].setBase(3,2,empower_order,1);
                mi_distalmotor_after[k].setBase(3,2,empower_order,1);
            }

            while (csvimport_motor.readRecord()) {
                //csvimport_motor.readRecord(); this should be not relevant
                String[] record_sensor = null;
                String[] record_motor = csvimport_motor.getValues();
                switch (type) {
                    case 0:
                        csvimport_avoid.readRecord();
                        record_sensor = csvimport_avoid.getValues();
                        break;
                    case 1:
                        csvimport_agent.readRecord();
                        record_sensor = csvimport_agent.getValues();
                        break;
                    case 2:
                        csvimport_food.readRecord();
                        record_sensor = csvimport_food.getValues();
                        break;
                }
                //exit the while loop if some record at the end are not full
                if (record_motor.length < col_motor) 
                    break;
                if(record_sensor.length<col_avoid)
                    break;
                double time = (Double.valueOf(record_motor[0]));
                for (int k = 0; k < nagents; k = k + 1) {
                 //first calculate entropy for the input time series and different word size
                //that's what the for loop is about
                int reflex = Integer.valueOf(record_sensor[3 * k + 1]);
                int distal = Integer.valueOf(record_sensor[3 * k + 2]);
                int motor=0;
                if(record_motor[k+ 1].length()>0)
                {    try{
                    motor = Integer.valueOf(record_motor[k+ 1]);
                    }
                    catch(NumberFormatException nfe)
                    {
                        motor=0;
                    }
                }
                else break;
                    //the agent here is only reactive
                    if (time < 200000) {
                        mi_reflexmotor_before[k].addEmpoweredSymbol(motor, reflex);
                        mi_distalmotor_before[k].addEmpoweredSymbol(motor, distal);
                        if(time==100000)
                            weight_series_before.add(k+1,Double.valueOf(record_sensor[3 * k + 3]));
                    //the agent at this point is pro-active (I hope so!)
                    } else if (time >= 200000 && time <= 400000) {
                        mi_reflexmotor_after[k].addEmpoweredSymbol(motor, reflex);
                        mi_distalmotor_after[k].addEmpoweredSymbol(motor, distal);
                         if(time==200000)
                            weight_series_after.add(k+1,Double.valueOf(record_sensor[3 * k + 3]));
                    }
                }
            }//parsing of the log file finished
            for (int k = 0; k < mi_reflexmotor_before.length; k++) {
                mi_series_1.add(k + 1, mi_reflexmotor_before[k].getMutualInformation(false,2));
                mi_series_2.add(k + 1, mi_distalmotor_before[k].getMutualInformation(false,2));
                mi_series_3.add(k + 1, mi_reflexmotor_after[k].getMutualInformation(false,2));
                mi_series_4.add(k + 1, mi_distalmotor_after[k].getMutualInformation(false,2));

            }
            XYSeries[] allseries = new XYSeries[6];
            allseries[0] =  mi_series_1;
            allseries[1] =  mi_series_2;
            allseries[2] =  weight_series_before;
            allseries[3] =  mi_series_3;
            allseries[4] =  mi_series_4;
            allseries[5] =  weight_series_after;
            String[] labels = {"Agent", "I(X,M) before","I(Y,M) before","W","I(X,M) after","I(Y,M) after","W"};
            save2CSVonlyY(filename,allseries, labels);
        } else {
                  System.out.println("Error: incorrect dimension");  
        }
        csvimport_agent.close();
        csvimport_food.close();
        csvimport_avoid.close();
        csvimport_motor.close();
    }
    
     //input: log file in CSV for distal, reflex for every behaviour
    //output: mutual information of the closed loop for every behaviour
    public void computeCapacity(String file_avoid, String file_agent, String file_food, String file_motor, 
            int type, String filename) throws FileNotFoundException, IOException {
        //first empty the previous series
        mi_series_1.clear();
        mi_series_2.clear();
        mi_series_3.clear();
        mi_series_4.clear();
        weight_series_before.clear();
        weight_series_after.clear();
        //then open all the log files from the simulations
        CsvReader csvimport_avoid = new CsvReader(file_avoid);
        csvimport_avoid.readHeaders();
        CsvReader csvimport_agent = new CsvReader(file_agent);
        csvimport_agent.readHeaders();
        CsvReader csvimport_food = new CsvReader(file_food);
        csvimport_food.readHeaders();
        CsvReader csvimport_motor = new CsvReader(file_motor);
        csvimport_motor.readHeaders();
        int col_avoid = csvimport_avoid.getHeaderCount();
        int col_agent = csvimport_agent.getHeaderCount();
        int col_food = csvimport_food.getHeaderCount();
        int col_motor = csvimport_motor.getHeaderCount();
        if (col_avoid == col_agent && col_agent == col_food) {
            int nagents=(col_avoid-1)/3;
            EntropyString entropy_before[] = new EntropyString[nagents];
            EntropyString entropy_after[] = new EntropyString[nagents];
            EntropyString mi_reflex_before[] = new EntropyString[nagents];
            EntropyString mi_reflex_after[] = new EntropyString[nagents];
            
            for(int k=0;k<nagents;k=k+1)
            {
                entropy_before[k] = new EntropyString(0, true, "unbiased");
                entropy_after[k] = new EntropyString(0, true, "unbiased");    
                //empowerment measure of order 3:
                // 3 motor words followed by 1 sensor word: 
                //the motor action is encoded in 3 bits because only 8 directions available
                entropy_before[k].setBase(3,2,empower_order,1);
                entropy_after[k].setBase(3,2,empower_order,1);
                mi_reflex_before[k] =new EntropyString(0, true,"unbiased");
                mi_reflex_after[k]=new EntropyString(0, true, "unbiased");
                mi_reflex_after[k].setBase(3,2,empower_order,1);
                mi_reflex_before[k].setBase(3,2,empower_order,1);
            }

            while (csvimport_motor.readRecord()) {
                //csvimport_motor.readRecord(); this should be not relevant
                String[] record_sensor = null;
                String[] record_motor = csvimport_motor.getValues();
                switch (type) {
                    case 0:
                        csvimport_avoid.readRecord();
                        record_sensor = csvimport_avoid.getValues();
                        break;
                    case 1:
                        csvimport_agent.readRecord();
                        record_sensor = csvimport_agent.getValues();
                        break;
                    case 2:
                        csvimport_food.readRecord();
                        record_sensor = csvimport_food.getValues();
                        break;
                }
                //exit the while loop if some record at the end are not full
                if (record_motor.length < col_motor) 
                    break;
                if(record_sensor.length<col_avoid)
                    break;
                double time = (Double.valueOf(record_motor[0]));
                for (int k = 0; k < nagents; k = k + 1) {
                 //first calculate entropy for the input time series and different word size
                //that's what the for loop is about
                int reflex = Integer.valueOf(record_sensor[3 * k + 1]);
                int distal = Integer.valueOf(record_sensor[3 * k + 2]);
                int motor = Integer.valueOf(record_motor[k+ 1]);
                    //the agent here is only reactive
                    if (time <= 200000) {
                        entropy_before[k].addEmpoweredSymbol(motor, reflex);
                        mi_reflex_before[k].addEmpoweredSymbol(motor, distal);
                        if(time==10)
                        weight_series_before.add(k+1,Double.valueOf(record_sensor[3 * k + 3]));
                    //the agent at this point is pro-active (I hope so!)
                    } 
                }
            }//parsing of the log file finished
            for (int k = 0; k < entropy_after.length; k++) {
                mi_series_1.add(k + 1, entropy_before[k].getChannelCapacity());
                //entropy_series_learned.add(k + 1, entropy_after[k].getChannelCapacity());
                mi_series_3.add(k + 1, mi_reflex_before[k].getChannelCapacity());
                //series_after.add(k + 1, mi_reflex_after[k].getChannelCapacity());

            }
            XYSeries[] allseries = new XYSeries[3];
            allseries[0] =  mi_series_1;
            allseries[1] =  mi_series_3;
            allseries[2] =  weight_series_before;
            String[] labels = {"Agent", "Cpre(X,M)","Cpre(Y,M)","W"};
            save2CSVonlyY(filename,allseries, labels);
        } else {
                  System.out.println("Error: incorrect dimension");  
        }
        csvimport_agent.close();
        csvimport_food.close();
        csvimport_avoid.close();
        csvimport_motor.close();
    }
    
public boolean save2CSVonlyY(String filename,XYSeries[] series,final String[] labels)
 {
        //some columns may have more records than others, so I pick the minimum between every column
        int common_length = 0;
        for (int k = 0; k < series.length - 1; k++) {
            common_length = Math.min(series[k].getItemCount(), series[k + 1].getItemCount());
        }
        //generate a Csv writer for every time series
        CsvWriter csvoutput = new CsvWriter(outputdirectory + filename);
        if(common_length==0)
        {
           System.out.println("Error in data format");
           return false;
        }    
        for (int k = 0; k < common_length; k++) {
            try {
                //export every time series to a file
                // the first record is the header of the file
                final String[] record = new String[labels.length];
                if (k == 0) {
                    csvoutput.writeRecord(labels);
                }
                record[0]=String.valueOf(series[0].getX(k));
                for (int j = 0; j < series.length; j++) {
                    record[j+1] = String.valueOf(series[j].getY(k));
                }
                csvoutput.writeRecord(record);
            } catch (IOException ex) {
                Logger.getLogger(InputAnalysis.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        System.out.println(outputdirectory+filename+" generated.");
        csvoutput.close();
        return true;
}
    
    //input: class log generated by maven in CSV format
    public void readSimulationClassFile() throws FileNotFoundException, IOException {
        CsvReader csvimport_class = new CsvReader(datasimfile[1]);
        csvimport_class.readHeaders();
        int columns = csvimport_class.getHeaderCount();
        int nagents = columns - 1;
        System.out.println("Imported with " + nagents + " agents");
        Bag entropylist_class = new Bag(nagents);
        for (int k = 0; k < nagents; k++) {
            EntropyString temp = new EntropyString(1, true, "unbiased");
            temp.setBase(1, 1,k+1, k + 1);
            entropylist_class.add(temp);
        }
        while (csvimport_class.readRecord()) {
            csvimport_class.readRecord();
            String[] record_class = csvimport_class.getValues();
            if (record_class.length < columns) {
                break;
            }
            for (int k = 0; k < entropylist_class.numObjs; k++) {
                if (record_class[k].length() > 0) {
                    int classval = Integer.valueOf(record_class[k]);
                    ((EntropyString) entropylist_class.get(k)).lastMotorWord(classval);
                }
            }
        }
        System.out.println("Parsed...computing entropies");
        String[] record = new String[3];
        EntropyString current = null;
        CsvWriter csvout = new CsvWriter(datasimfile[1] + "out.csv");
        for (int k = 0; k < entropylist_class.numObjs; k++) {
            current = (EntropyString) entropylist_class.get(k);
            series_class.add(k + 1, current.getXEntropy(false,2));
            record[0] = String.valueOf(k);
            record[1] = String.valueOf(current.getXEntropy(false,2));
            csvout.writeRecord(record);
        }
        csvout.close();
    }
            
    public void readSimulationDataAgentFile(String inputfile,String outputname) throws FileNotFoundException
    {
        CsvReader csvimport_input=new CsvReader(inputfile);
        try {
            csvimport_input.readHeaders();
            int columns=csvimport_input.getHeaderCount();
            //the header is composed by:
            // 1 timestamp: when the data was samples
            //triplet: reflex,distal and weight
            //so the number of agents is 
            int nagents=(columns-1)/3;
            System.out.println("Imported with "+nagents+" agents");
             Bag entropylist_before=new Bag(nagents);
             Bag entropylist_after=new Bag(nagents);
             for(int k=0;k<nagents;k++)
             {
                    EntropyString temp1=new EntropyString(6,true,"unbiased");
                    entropylist_before.add(temp1);
                    EntropyString temp2=new EntropyString(6,true,"unbiased");
                    entropylist_after.add(temp2);
             }
            while(csvimport_input.readRecord())
            {
                String[] record=csvimport_input.getValues();
                float time=Float.valueOf(record[0]);
                if(record.length<columns)
                    break;
                 for(int k=0;k<entropylist_before.numObjs;k++)
                 {
                    int reflex=Integer.valueOf(record[3*k+1]);
                    int distal=Integer.valueOf(record[3*k+2]);
                     if(time<100000)
                     {
                         ((EntropyString) entropylist_before.get(k)).addXYSymbol(reflex,distal);
                     }
                     else if(time>100000 && time<200000)
                     {
                         ((EntropyString) entropylist_after.get(k)).addXYSymbol(reflex,distal); 
                     }    

                 }
            }
                System.out.println("Computing entropies");
                EntropyString current=null;
                for (int k = 0; k < entropylist_before.numObjs; k++) {
                     current=(EntropyString) entropylist_before.get(k);
                     mi_series_1.add(k+1, current.getConditionedEntropy(2));
                     mi_series_3.add(k+1,current.getXEntropy(false,2));
                     current=(EntropyString) entropylist_after.get(k);
                     mi_series_2.add(k+1, current.getConditionedEntropy(2));
                     mi_series_4.add(k+1,current.getXEntropy(false,2));
                 } 
        } catch (IOException ex) {
            Logger.getLogger(InputAnalysis.class.getName()).log(Level.SEVERE, null, ex);
        }
      //now save the results on a csv file later on matlab can plot it
      XYSeries[] allseries=new XYSeries[2];
      allseries[0]=mi_series_2;
      allseries[1]=mi_series_1;
      String[] labels={"Agent index","H(X,Y) before learning","Agent Index","H(X,Y) after learning"};
     save2CSV(allseries,outputname,labels);
    }
    
      /*
   *  Input: a single agent data log from Maven
   *  Output: compute entropy before and after learning and update the time series
 */
    public void readSimulationSingleAgent(String filename, String outputname, String method, int agentindex) throws FileNotFoundException, IOException {
        EntropyString entropy_before[] = new EntropyString[1 + max_word_size - min_word_size];
        EntropyString entropy_after[] = new EntropyString[1 + max_word_size - min_word_size];
        double w0 = 0.0;
        double wend = 0.0;
        //create entropy estimators of different word size
        for (int k = 0; k < entropy_before.length; k++) {
            entropy_before[k] = new EntropyString(k + min_word_size, true, method);
            entropy_after[k] = new EntropyString(k + min_word_size, true, method);
        }
        CsvReader csvimport_class = new CsvReader(filename);
        csvimport_class.readHeaders();
        int columns = csvimport_class.getHeaderCount();
        System.out.println("Imported file with " + columns);

        while (csvimport_class.readRecord()) {
            csvimport_class.readRecord();
            String[] record = csvimport_class.getValues();
            if (record.length < columns) {
                break;
            //first calculate entropy for the input time series and different word size
            //that's what the for loop is about
            }
            double time = (Double.valueOf(record[0]));
            for (int k = 0; k < entropy_before.length; k++) {
                int reflex = Integer.valueOf(record[3 * agentindex + 1]);
                int distal = Integer.valueOf(record[3 * agentindex + 2]);

                if (time < 100000) {
                    entropy_before[k].addXYSymbol(reflex, distal);
                    if (k == 1) {
                        w0 = Double.valueOf(record[3 * agentindex + 3]);
                    }
                } else if (time >= 100000 && time <= 200000) {
                    entropy_after[k].addXYSymbol(reflex, distal);
                    if (time == 200000) {
                        wend = Double.valueOf(record[3 * agentindex + 3]);
                    }
                }
            }
        }//end while

        //put the data into the time series data structure
        // k is basically the word size in bits
        for (int k = 0; k < entropy_after.length; k++) {
            mi_series_1.add(k + min_word_size, (entropy_before[k].getMutualInformation(false,2)));
            mi_series_2.add(k + min_word_size, (entropy_after[k].getMutualInformation(false,2)));
            System.out.println("Initial weight " + w0 + " final weight " + wend);
            mi_series_3.add(k + min_word_size, w0);
            mi_series_4.add(k + min_word_size, wend);
        }
        //
        System.out.println("Initial weight " + w0 + " final weight " + wend);
        //now save the results on a csv file later on matlab can plot it
        XYSeries[] allseries = new XYSeries[2];
        allseries[0] = mi_series_2;
        allseries[1] = mi_series_1;
        String[] labels = {"Word Size", "Entropy (Bits) before learning", "Word Size", "Entropy (Bits) after learning"};
        save2CSV(allseries, outputname, labels);
    }
  
  /*
   *  Input: a single agent data log from Maven
   *  Output: compute entropy before and after learning and update the time series
 */
   public void timeEntropy(String filename,String outputname,String method) throws FileNotFoundException, IOException
{ 
             EntropyString entropylist[]=new EntropyString[1+max_word_size-min_word_size];
             //create entropy estimators of different word size
             for(int k=0;k<entropylist.length;k++)
             {
                    entropylist[k]=new EntropyString(k+min_word_size,true,method);
             }
            CsvReader csvimport_class=new CsvReader(filename);        
            csvimport_class.setDelimiter(',');
            csvimport_class.readHeaders();
            int columns=csvimport_class.getHeaderCount();
            System.out.println("Imported file with "+columns);
           
            while(csvimport_class.readRecord())
            {
                csvimport_class.readRecord();
                String[] record_class=csvimport_class.getValues();
                if(record_class.length<columns)
                    break;
                  //first calculate entropy for the input time series and different word size
                  //that's what the for loop is about
                double time=(Double.valueOf(record_class[0]));
                 for(int k=0;k<entropylist.length;k++)
                 {

                     if(time<100000)
                     entropylist[k].addSensorSymbol(Integer.valueOf(record_class[1]));
                     else if(time>100000 && time<=200000)
                     entropylist[k].lastMotorWord(Integer.valueOf(record_class[1]));    
                 }
         }//end while
 
     //put the data into the time series data structure
     // k is basically the word size in bits
     for(int k=0;k<entropylist.length;k++)
     {
         mi_series_2.add(k+min_word_size,  (entropylist[k].getYEntropy(false,2)));
         mi_series_1.add(k+min_word_size,  (entropylist[k].getXEntropy(false,2)));
     }
      //now save the results on a csv file later on matlab can plot it
      XYSeries[] allseries=new XYSeries[2];
      allseries[0]=mi_series_2;
      allseries[1]=mi_series_1;
      String[] labels={"Word Size","Entropy (Bits) before learning","Word Size","Entropy (Bits) after learning"};
     save2CSV(allseries,outputname,labels);
}
public void save2CSV(XYSeries[] series,final String filename,final String[] labels)
 {
        //some columns may have more records than others, so I pick the minimum between every column
        int common_length = 0;
        for (int k = 0; k < series.length - 1; k++) {
            common_length = Math.min(series[k].getItemCount(), series[k + 1].getItemCount());
        }
        //generate a Csv writer for every time series
        CsvWriter csvoutput = new CsvWriter(outputdirectory + filename);
        for (int k = 0; k < common_length; k++) {
            try {
                //export every time series to a file
                // the first record is the header of the file
                final String[] record = new String[2 * series.length];
                if (k == 0) {
                    csvoutput.writeRecord(labels);
                }
                for (int j = 0; j < series.length; j++) {
                    record[2 * j] = String.valueOf(series[j].getX(k));
                    record[2 * j + 1] = String.valueOf(series[j].getY(k));
                }
                csvoutput.writeRecord(record);
            } catch (IOException ex) {
                Logger.getLogger(InputAnalysis.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        csvoutput.close();
        System.out.println("Entropy file generated!\n");
}

public void listFiles(String directory)
{
    File dir = new File(directory);
    String[] children = dir.list();
    if (children == null) {
        // Either dir does not exist or is not a directory
    } else {
        for (int i=0; i<children.length; i++) {
            // Get filename of file or directory
            String filename = children[i];
        }
    }
    
    // only list the files generated from simulations with .out extension
    FilenameFilter filter = new FilenameFilter() {
        public boolean accept(File dir, String name) {
            return name.endsWith(".out");
        }
    };
    children = dir.list(filter);
    
    // The list of files can also be retrieved as File objects
    File[] files = dir.listFiles();
    
    // This filter only returns directories
    FileFilter fileFilter = new FileFilter() {
        public boolean accept(File file) {
            return file.isDirectory();
        }
    };
    files = dir.listFiles(fileFilter);

}
private HashMap<Integer,AgentInfoValues> summary;
public  class AgentInfoValues{
    private int reflex_motor = 0;
    private int distal_motor = 1;
    private double[] MIbefore;
    private double[] MIafter;
    private double[] Cbefore;
    private double[] Cafter;
 public AgentInfoValues()
 {
     MIbefore=new double[2];
     MIafter=new double[2];
     Cbefore=new double[2];
     Cafter=new double[2];
 }
 public void setMIreflexBefore(double val)
 {
     MIbefore[reflex_motor]=val;
 }
  public void setMIdistalBefore(double val)
 {
     MIbefore[distal_motor]=val;
 }
    }
}
