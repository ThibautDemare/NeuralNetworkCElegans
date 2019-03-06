import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.graphstream.algorithm.Toolkit;
import org.graphstream.algorithm.community.Leung;
import org.graphstream.algorithm.measure.ChartMeasure.PlotException;
import org.graphstream.algorithm.measure.ChartMeasure.PlotParameters;
import org.graphstream.algorithm.measure.ChartSeries1DMeasure;
import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;

public class GenerateAdjacencyMatrix {

	int[][] matrix;
	Graph network;
	ArrayList<String> neuronNames;
	
	
	public GenerateAdjacencyMatrix() {
		matrix = new int[280][280];
		neuronNames = new ArrayList<String>();
		network = new SingleGraph("Neural Network of C. Elegans");
		network.setStrict(false);
		network.addAttribute("ui.stylesheet", "node {"
				+ "size-mode:fit;"
				+ "padding:8px;"
				+ "shape:rounded-box;"
				+ "fill-color:white;"
				+ "stroke-mode:plain;"
				+ "stroke-width:1px;"
				+ "stroke-color:red;}");
		network.display(true);
	}

	public void readCSV() {
		System.out.println(System.getProperty("user.dir")+"\\NeuronConnect.csv");
		String csvFile = System.getProperty("user.dir")+"\\NeuronConnect.csv";
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";

		try {
			ArrayList<String> ans= new ArrayList<String>();
			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {
				// use comma as separator
				String[] edge = line.split(cvsSplitBy);
	
				if(!neuronNames.contains(edge[1]) && !edge[1].contains("NMJ")) {
					neuronNames.add(edge[1]);
					network.addNode(edge[1]).addAttribute("ui.label", edge[1]);
				}
				else if(!neuronNames.contains(edge[0]) && edge[1].contains("NMJ")){
					neuronNames.add(edge[0]);
					network.addNode(edge[0]).addAttribute("ui.label", edge[0]);
				}

				ans.add(line);
			}

			// Read again
			for (String result: ans) {
				// use comma as separator
				String[] edge = result.split(cvsSplitBy);
	
				if(edge[2].contains("Sp") ||  edge[2].contains("S") || edge[2].contains("EJ")) {
					matrix[neuronNames.indexOf(edge[0])][neuronNames.indexOf(edge[1])] = 1;
					network.addEdge(edge[0]+"_"+edge[1], edge[0], edge[1], true);
				}
				else if(edge[2].contains("R") || edge[2].contains("Rp")) {
					matrix[neuronNames.indexOf(edge[1])][neuronNames.indexOf(edge[0])] = 1;
					network.addEdge(edge[1]+"_"+edge[0], edge[1], edge[0], true);
				}
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void communityDetection() {
		// TODO
		Leung leung = new Leung(network, "cm");
		leung.compute();
		System.out.println(network.getNode(0).getNumber("cm"));
	}
	
	public String printAdjacencyMatrix() {
		String s = "";
		s += "name,";
		for(int i = 0; i < matrix.length; i++) {
			s+= neuronNames.get(i) + ",";
		}
		s += "\n";
		for(int i = 0; i < matrix.length; i++) {
			s += neuronNames.get(i) + ",";
			for(int j = 0; j < matrix[0].length; j++) {
				s += matrix[i][j];
				if(j < matrix[0].length - 1)
					s += ",";
			}
			s += "\n";
		}
		return s;
	}

	public void statistics() {
		System.out.println("Number of nodes : "+network.getNodeCount());
		network.addAttribute("NodeNumber", network.getNodeCount());

		System.out.println("Number of edges : "+network.getEdgeCount());
		network.addAttribute("EdgeNumber", network.getEdgeCount());

		double ade = Toolkit.averageDegree(network);
		System.out.println("Average degree : " + ade);
		network.addAttribute("AverageDegree", ade);

		double de = Toolkit.density(network);
		System.out.println("Density : " + de);
		network.addAttribute("Density", de);

		double di = Toolkit.diameter(network);
		System.out.println("Diameter : " + di);
		network.addAttribute("Diameter", di);

		double acc = Toolkit.averageClusteringCoefficient(network);
		System.out.println("Average clustering coefficients : " + acc);
		network.addAttribute("AverageClusteringCoefficients", acc);

		double dad = Toolkit.degreeAverageDeviation(network);
		System.out.println("Degree average deviation : " + dad);
		network.addAttribute("DegreeAverageDeviation", dad);

		System.out.println("Compute degree distribution...");
		network.addAttribute("DegreeDistribution", Toolkit.degreeDistribution(network));
		int[] degreeDistribution = (int[])network.getAttribute("DegreeDistribution");
		ChartSeries1DMeasure m = new ChartSeries1DMeasure("Degree distribution");
		m.setWindowSize(150);
		PlotParameters pp = new PlotParameters();
		pp.xAxisLabel = "degree";
		pp.yAxisLabel = "Number of nodes";
		pp.title = "Degree distribution";
		for (int i = 0; i < degreeDistribution.length; i++) {
			m.addValue(degreeDistribution[i]);
			System.out.println(i + " -> "+degreeDistribution[i]);
		}
		try {
			m.plot(pp);
		} catch (PlotException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String args[]) throws IOException {		
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		GenerateAdjacencyMatrix gam = new GenerateAdjacencyMatrix();
		gam.readCSV();
		BufferedWriter writer = new BufferedWriter(new FileWriter(System.getProperty("user.dir")+"\\AdjacencyMatrix.csv"));
		writer.write(gam.printAdjacencyMatrix());
		writer.close();
		gam.statistics();
	}
}
