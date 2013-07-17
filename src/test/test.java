package test;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.util.HashMap;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.Layer;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RefineryUtilities;
import org.jfree.ui.TextAnchor;

/**
 * This demo shows a simple bar chart created using the {@link XYSeriesCollection} dataset.
 *
 */
public class test extends ApplicationFrame {

    /**
     * Creates a new demo instance.
     *
     * @param title  the frame title.
     */
    public test(final String title,HashMap<Double, Double> dataSet2) {
        super(title);
        IntervalXYDataset dataset = createDataset(dataSet2);
        JFreeChart chart = createChart(dataset);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        setContentPane(chartPanel);
    }
    
    /**
     * Creates a sample dataset.
     * 
     * @return A sample dataset.
     */
    private IntervalXYDataset createDataset(HashMap<Double, Double> dataSet2) {
        final XYSeries series = new XYSeries("Random Data");
        for(Double key: dataSet2.keySet()){
        	series.add(key,dataSet2.get(key));
        }
        final XYSeriesCollection dataset = new XYSeriesCollection(series);
        return dataset;
    }

    /**
     * Creates a sample chart.
     * 
     * @param dataset  the dataset.
     * 
     * @return A sample chart.
     */
    private JFreeChart createChart(IntervalXYDataset dataset) {
        final JFreeChart chart = ChartFactory.createXYBarChart(
            "XY Series Demo",
            "X", 
            false,
            "Y", 
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );
        XYPlot plot = (XYPlot) chart.getPlot();
        final IntervalMarker target = new IntervalMarker(400.0, 700.0);
        target.setLabel("Target Range");
        target.setLabelFont(new Font("SansSerif", Font.ITALIC, 11));
        target.setLabelAnchor(RectangleAnchor.LEFT);
        target.setLabelTextAnchor(TextAnchor.CENTER_LEFT);
        target.setPaint(new Color(222, 222, 255, 128));
        plot.addRangeMarker(target, Layer.BACKGROUND);
        return chart;    
    }
    
    // ****************************************************************************
    // * JFREECHART DEVELOPER GUIDE                                               *
    // * The JFreeChart Developer Guide, written by David Gilbert, is available   *
    // * to purchase from Object Refinery Limited:                                *
    // *                                                                          *
    // * http://www.object-refinery.com/jfreechart/guide.html                     *
    // *                                                                          *
    // * Sales are used to provide funding for the JFreeChart project - please    * 
    // * support us so that we can continue developing free software.             *
    // ****************************************************************************
    
    /**
     * Starting point for the demonstration application.
     *
     * @param args  ignored.
     */
    public static void main(final String[] args) {
    	File[] theseFiles = new File("textFiles").listFiles();
    	Double[] histogram = new Double[100];
    	Double[] p = new Double[100];
    	Double[] cdf = new Double[100];
    	Double[] normal = new Double[100];
    	for(int i=0;i<histogram.length;i++){
    		histogram[i] =0.0;
    		p[i] =0.0;
    		cdf[i] =0.0;
    		normal[i] =0.0;
    	}
    	
    	for(int i=0;i<theseFiles.length;i++){
    		String tmp[] = theseFiles[i].getName().split("_");
    		int mark = Integer.parseInt(tmp[1].substring(0,2));
    		histogram[mark] += 1;
    	}
    	
    	double total =0.0;
    	
    	double maxHist = 0.0;
    	for(int i=0;i<histogram.length;i++){
    		if(maxHist<histogram[i]){
    			maxHist = histogram[i];
    		}
    		total += histogram[i];
    	}
    	
    	for(int i=0;i<p.length;i++){
    		p[i] = histogram[i]/theseFiles.length;
    	}
    	
    	for(int i=0;i<cdf.length;i++){
    	//	System.out.println(i+"    "+p[i]);
    		for(int j=0;j<=i;j++){
    			cdf[i] += histogram[j];
    		}
    	}
    	double min = Double.MAX_VALUE;
    	double max =0;
    	for(int i =0;i<cdf.length;i++){
    		if(cdf[i]!=0){
    		if(cdf[i]>max){
    			max = cdf[i];
    		}
    		
    		if(cdf[i]<min){
    			min = cdf[i];
    		}
    		}
    	}
    	
    	
    	System.out.println(min+"	"+max);
    	for(int i=0;i<normal.length;i++){
    		//System.out.println(i+"  "+(double) Math.round(((cdf[i]-min)/(theseFiles.length-min))*(100-1)));
    		normal[i] = (double) Math.round(((cdf[i]-min)/(theseFiles.length-min))*(100-1));
    	}
    	
    	HashMap<Double,Double> dataSet = new HashMap<Double,Double>();
    	for(int i=0;i<normal.length;i++){
    		
    		dataSet.put((double) i,normal[i]);
    		
    	}
        final test demo = new test("XY Series Demo 3",dataSet);
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);

    }

}

