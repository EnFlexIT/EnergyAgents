package de.enflexit.ea.lib.powerFlowEstimation;

//A Java program for Dijkstra's single source shortest path algorithm.
//The program is for adjacency matrix representation of the graph
import java.util.*;

public class Clustering {
	// A utility function to find the vertex with minimum distance value,
	// from the set of vertices not yet included in shortest path tree
	// static final int V=9;
	int nNumNodes;
	ArrayList<List<Integer>> autarkicGridList = new ArrayList<List<Integer>>();
	ArrayList<Integer> autarkicGrid = new ArrayList<Integer>();
	ArrayList<Integer> VisitedNodes = new ArrayList<Integer>();

	public void dijkstraManager(double graph[][]) {
		this.nNumNodes = graph.length;
		for (int i = 0; i < this.nNumNodes; i++) {
			// Is StartNode part of an autark Area ? No --> Start Searching
			if (this.VisitedNodes.contains(i + 1) == false) { // contains(i+1),
																// weil 0tes
																// Element =
																// Knoten 1
				this.dijkstra(graph, i);
				ArrayList<Integer> CloneNetzbezirke = new ArrayList<Integer>();
				// DeepCopy of ArrayList
				for (int v = 0; v < this.autarkicGrid.size(); v++) {
					CloneNetzbezirke.add(v, autarkicGrid.get(v));
				}
				// DeepCopy = Netzbezirke add to NetzbezirkeListe 
				this.autarkicGridList.add(CloneNetzbezirke);
			}
			// clear Elements in Netzbezirke
			autarkicGrid.clear();
		}
	}

	public ArrayList<List<Integer>> getAutarkicGridList() {
		return autarkicGridList;
	}

	public void setNetzbezirkeListe(ArrayList<List<Integer>> netzbezirkeListe) {
		autarkicGridList = netzbezirkeListe;
	}

	double minDistance(double dist[], Boolean sptSet[]) {
		// Initialize min value
		double min = Integer.MAX_VALUE, min_index = -1;
		for (int v = 0; v < this.nNumNodes; v++)
			if (sptSet[v] == false && dist[v] <= min) {
				min = dist[v];
				min_index = v;
			}
		return min_index;
	}

	// Funtion that implements Dijkstra's single source shortest path
	// algorithm for a graph represented using adjacency matrix
	// representation
	void dijkstra(double graph[][], int src) {
		double dist[] = new double[this.nNumNodes]; // The output array. dist[i]
													// will hold
		// the shortest distance from src to i

		// sptSet[i] will true if vertex i is included in shortest
		// path tree or shortest distance from src to i is finalized
		Boolean sptSet[] = new Boolean[this.nNumNodes];

		// Initialize all distances as INFINITE and stpSet[] as false
		for (int i = 0; i < this.nNumNodes; i++) {
			dist[i] = Integer.MAX_VALUE;
			sptSet[i] = false;
		}
		// Distance of source vertex from itself is always 0
		dist[src] = 0;
		// Find shortest path for all vertices
		for (int count = 0; count < this.nNumNodes - 1; count++) {
			// Pick the minimum distance vertex from the set of vertices
			// not yet processed. u is always equal to src in first
			// iteration.
			double u = minDistance(dist, sptSet);

			// Mark the picked vertex as processed
			sptSet[(int) u] = true;

			// Update dist value of the adjacent vertices of the
			// picked vertex.
			for (int v = 0; v < this.nNumNodes; v++)
				// Update dist[v] only if is not in sptSet, there is an
				// edge from u to v, and total weight of path from src to
				// v through u is smaller than current value of dist[v]
				if (!sptSet[v] && graph[(int) u][v] != 0 && dist[(int) u] != Integer.MAX_VALUE
						&& dist[(int) u] + graph[(int) u][v] < dist[v])
					dist[v] = dist[(int) u] + graph[(int) u][v];
		}

		// print the constructed distance array
		// printSolution(dist, V);
		// List<Integer> Netzbezirke = new ArrayList<Integer>();
		for (int i = 0; i < this.nNumNodes; i++) {
			if (dist[i] < Integer.MAX_VALUE) {
				this.autarkicGrid.add(i + 1);
				this.VisitedNodes.add(i + 1);
			}
		}
	}
}
