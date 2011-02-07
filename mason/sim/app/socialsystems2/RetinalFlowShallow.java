/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sim.app.socialsystems2;
import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.GrayPaintScale;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.data.DomainOrder;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.xy.XYZDataset;

import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
/**
 *
 * @author epokh
 */
public class RetinalFlowShallow extends ApplicationFrame{
    private static boolean computeC=false;
    private int empower_order=1;
    private int max_word_size=32;
    private int min_word_size=1;


    public int timebreak=200000;
    public String base;
    public String datasimfile;
    int stage;
    EntropyString retinal_flow[];
    EntropyString mioreflex;
    
    public RetinalFlowShallow(String title)
    {

        super(title);
        JPanel chartPanel = createDemoPanel();
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        setContentPane(chartPanel);
          
          base="/home/epokh/DataSim/shallow/";
          datasimfile="predictive/datasim.dat";

        try {

            for(int order=4;order<=6;order++)
            {
                stage=order;
                computeRetinalFlow(base+datasimfile,order,4,225);
                saveRetinal2CSV(base+"retinal"+order+".dat");
                computeRetinalFlow(base+datasimfile,order,4,225);
                saveReflex2CSV(base+"reflex"+order+".dat");
            }


        } catch (FileNotFoundException ex) {
            Logger.getLogger(RetinalFlowShallow.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(RetinalFlowShallow.class.getName()).log(Level.SEVERE, null, ex);
        }

    }


    public void computeRetinalFlow(String inputfile,int order,int toskip,int retinalsize) throws FileNotFoundException
    {
        CsvReader datalog=new CsvReader(inputfile);
        try {

            /*
            datalog.setDelimiter('\t');
            for(int k=1;k<=toskip;k++)
            {
                datalog.skipLine();
                //datalog.readRecord();
            }
            datalog.readHeaders();
             */


            mioreflex=new EntropyString(0,true,"unbiased");
            mioreflex.setBase(7,2,order,1);

            retinal_flow = new EntropyString[retinalsize];

            for(int k=0;k<retinalsize;k=k+1)
            {
                retinal_flow[k]=new EntropyString(0,true,"unbiased");
                retinal_flow[k].setBase(7,2,order,1);

            }


            datalog.setDelimiter('\t');
            while (datalog.readRecord()) {
                String[] record = datalog.getValues();
                if (record.length >= (3+retinalsize)) {
                    int timestamp = Integer.valueOf(record[0]);
                    int reflex = Integer.valueOf(record[1]);
                    int motor = Integer.valueOf(record[2]);

                    mioreflex.addEmpoweredSymbol(motor, reflex);

                    for (int k = 0; k < retinalsize; k = k + 1) {
                        int pixel = Integer.valueOf(record[3+k]);
                        retinal_flow[k].addEmpoweredSymbol(motor, pixel);
                    }
                }

            }
            datalog.close();

        } catch (IOException ex) {
            Logger.getLogger(RetinalFlowShallow.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Creates a sample dataset.
     */
    private static XYZDataset createDataset() {
        return new XYZDataset() {
            public int getSeriesCount() {
                return 1;
            }
            public int getItemCount(int series) {
                return 10000;
            }
            public Number getX(int series, int item) {
                return new Double(getXValue(series, item));
            }
            public double getXValue(int series, int item) {
                return item / 100 - 50;
            }
            public Number getY(int series, int item) {
                return new Double(getYValue(series, item));
            }
            public double getYValue(int series, int item) {
                return item - (item / 100) * 100 - 50;
            }
            public Number getZ(int series, int item) {
                return new Double(getZValue(series, item));
            }
            public double getZValue(int series, int item) {
                double x = getXValue(series, item);
                double y = getYValue(series, item);
                return Math.sin(Math.sqrt(x * x + y * y) / 5.0);
            }
            public void addChangeListener(DatasetChangeListener listener) {
                // ignore - this dataset never changes
            }
            public void removeChangeListener(DatasetChangeListener listener) {
                // ignore
            }
            public DatasetGroup getGroup() {
                return null;
            }
            public void setGroup(DatasetGroup group) {
                // ignore
            }
            public Comparable getSeriesKey(int series) {
                return "sin(sqrt(x + y))";
            }
            public int indexOf(Comparable seriesKey) {
                return 0;
            }
            public DomainOrder getDomainOrder() {
                return DomainOrder.ASCENDING;
            }
        };
    }
    
    /**
     * Creates a panel for the demo.
     *
     * @return A panel.
     */
    public static JPanel createDemoPanel() {
        return new ChartPanel(createChart(createDataset()));
    }
    
     private static JFreeChart createChart(XYZDataset dataset) {
        NumberAxis xAxis = new NumberAxis("X");
        xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        xAxis.setLowerMargin(0.0);
        xAxis.setUpperMargin(0.0);
        NumberAxis yAxis = new NumberAxis("Y");
        yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        yAxis.setLowerMargin(0.0);
        yAxis.setUpperMargin(0.0);
        XYBlockRenderer renderer = new XYBlockRenderer();
        PaintScale scale = new GrayPaintScale(-2.0, 1.0);
        //renderer.setPaintScale(scale);
        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinePaint(Color.white);
        JFreeChart chart = new JFreeChart("XYBlockChartDemo1", plot);
        chart.removeLegend();
        chart.setBackgroundPaint(Color.white);
        return chart;
    }



 public void saveReflex2CSV(final String filename)
 {

        //generate a Csv writer for every time series
        CsvWriter csvoutput = new CsvWriter(filename);

            try {
                //export every time series to a file
                // the first record is the header of the file
                final String[] record=new String[2];

                record[0]=String.valueOf(0);
                record[1]=String.valueOf(mioreflex.getMutualInformation(false,2));
                csvoutput.writeRecord(record);
                csvoutput.flush();
            } catch (IOException ex) {
                Logger.getLogger(RetinalFlowShallow.class.getName()).log(Level.SEVERE, null, ex);
            }
        csvoutput.close();
}


    public void saveRetinal2CSV(final String filename) {

        //generate a Csv writer for every time series
        CsvWriter csvoutput = new CsvWriter(filename);
        //export every time series to a file
        // the first record is the header of the file
        final String[] record = new String[225];
        for (int k = 0; k < 225; k++) {
            record[k] = String.valueOf(retinal_flow[k].getMutualInformation(false, 2));
        }
        try {
            csvoutput.writeRecord(record);
            csvoutput.flush();
        } catch (IOException ex) {
            Logger.getLogger(RetinalFlowShallow.class.getName()).log(Level.SEVERE, null, ex);
        }
        csvoutput.close();
    }


    public static void main(String[] args){

        RetinalFlowShallow demo = new RetinalFlowShallow("Block Chart Demo");
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);



    }
    
}