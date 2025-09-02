Code Details (Java Version)

The programming language used is Java.

The code uses HashMap and PriorityQueue for storing graph data and managing the fringe.

A Node class holds information about each city (name, cumulative cost, heuristic, parent, etc.).

Functions are organized into methods for:

Loading graph edges from the input file.

Loading heuristics (if provided).

Performing search using A* (default) or Greedy Best-First (if --algo greedy flag is passed).

Reconstructing the route and printing the result.

The search tracks counters like nodesPopped, nodesExpanded, and nodesGenerated to mirror your original C++ outputs.

To run the code:

Compile with

javac FindRoute.java


Run with

java FindRoute Sample_Input_File.txt NewYork SanFrancisco --heuristic Sample_Heuristics_File.txt --algo astar


Or for Greedy:

java FindRoute Sample_Input_File.txt NewYork SanFrancisco --heuristic Sample_Heuristics_File.txt --algo greedy


Or Uniform-Cost (no heuristics):

java FindRoute Sample_Input_File.txt NewYork SanFrancisco


Java compiler version: works on Java 8+ (tested with OpenJDK 17).
