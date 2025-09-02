Artificial-Intelligence-with-graph-search

Java code for Optimized Route Detection between two cities using A* and Greedy search, with a back-routing approach to display the result.

Uses a priority queue fringe (PriorityQueue in Java) to store nodes for expansion.

Supports A* (default), Greedy Best-First (with --algo greedy), and Uniform-Cost Search (when no heuristic file is provided).

Tracks counters: Nodes Popped, Nodes Expanded, Nodes Generated.

Outputs the total distance and the full route in step-by-step format.

Run Instructions
# Compile
javac FindRoute.java

# Run with A* (requires heuristic file)
java FindRoute Sample_Input_File.txt NewYork SanFrancisco --heuristic Sample_Heuristics_File.txt --algo astar

# Run with Greedy Best-First
java FindRoute Sample_Input_File.txt NewYork SanFrancisco --heuristic Sample_Heuristics_File.txt --algo greedy

# Run with Uniform-Cost Search (no heuristic file)
java FindRoute Sample_Input_File.txt NewYork SanFrancisco

Input Format

Graph Input File:

CityA CityB Distance
...
END OF INPUT


Heuristics File (optional):

City HeuristicValue
...
END OF INPUT
