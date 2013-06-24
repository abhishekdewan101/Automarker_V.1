package test;

import java.util.Random;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class scatterPlot extends JFrame {

	public scatterPlot(){
		XYDataset dataset = createDataSet();
		JFreeChart chart = createChart(dataset,"title");
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(800,800));
	    setContentPane(chartPanel);
	}

	private JFreeChart createChart(XYDataset dataset, String string) {
		JFreeChart chart = ChartFactory.createScatterPlot(string, "X", "Y", dataset, PlotOrientation.VERTICAL, true, true, false);
		return chart;
	}

	private XYDataset createDataSet() {
	   final Random r = new Random();
	   XYSeriesCollection result = new XYSeriesCollection();
	   XYSeries series = new XYSeries("Random");
	   for(int i=0;i<=100;i++){
		   double x = r.nextDouble();
		   double y = r.nextDouble();
		   series.add(x,y);
	   }
	   result.addSeries(series);
		return result;
	}
	public static void main(String[] args){
		scatterPlot sp = new scatterPlot();
		sp.pack();
		sp.setVisible(true);
	}
}
