package periproject;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.space.Multi2DTorus;
import uchicago.src.sim.gui.MultiObject2DDisplay;
import uchicago.src.sim.analysis.*;
import uchicago.src.sim.util.Random;
import uchicago.src.sim.util.SimUtilities;
import uchicago.src.sim.engine.*;
import uchicago.src.sim.util.Random;

import java.awt.*;
import java.lang.*;
import java.util.List;

import org.apache.tools.ant.types.CommandlineJava.SysProperties;
import org.apache.velocity.runtime.directive.Foreach;

import java.awt.Color.*;
import java.util.Hashtable;
import java.util.ArrayList;
import java.io.*;
import java.util.*;

public class Model extends SimModelImpl {
	private Schedule schedule;
	private ArrayList agentList = new ArrayList();
	private Multi2DTorus space;
	///Model parameters:
	private int spaceSize = 51;
	private int numAgents = 100;
	private int percentageBlue = 50;
	private int percentageYellow = 40;
	private int percentagered = 10;
	public int findspaceSteps = 2;
	public int findspaceSteps2 = 2;
	public int findspaceSteps3 = 2;
	public int decayStartPoint = 1000;
	public int decayrandom = 40;
	public int consolidationLimit = 500;
	public int consolidationrandom = 50;
	public int d = 4;
	public int consLimit = 4;
	//Mudan�a para automatiza��o (18/01/2021)
	public int log = 0;
	public int archive = 0;
	public int fim = 0;
	public String outpath;

	//Activate / Desactivate inner city dynamics features
	boolean activateDecay = true;
	boolean activateConsolidation = true;
	boolean activateConsolidationOld = true; //true
	boolean activateOutskirtsrule = true; //false

//	public static String path2 = "D:\\Linux\\input\\";
	List<String> lista = new ArrayList<String>();

	boolean neighCons = true; // true  facilitar a consolida��o levando em considera��o os vizinhos
	boolean occup = true; // ocupar celulas sem vizinhos pobres/consolidados (dobro do peso para celulas consolidadas)
	boolean prejudice = true; // ap�s morar, se tiver vizinhos ruins, vou tender a sair
	public int pred = 7;
	public int predrandom = 25;

	boolean favSeed = false;


//	String[] oiArray = getInitParam();



	//Activate / Desactivate graphs
	boolean showLogLogPlot = false;
	boolean showDensityPlot = false;
	boolean showlnactAgVsTime = false;
	boolean showActAgVsTime = false;
	//initial conditions
	// one single seed at the centre
	boolean centralSeed = true;
	// 2 fixed seeds
	boolean twoSeeds = false;
	// 2 random seeds
	boolean tworandomSeeds = false;
	// 4 random seeds
	boolean fourrandomSeeds = false;
	// 4 fixed seeds
	boolean fourSeeds = false;
	//grid colonial city
	boolean gridSeeds = false;
	// path
	boolean pathSeeds = false;
	//initial conditions with spatial constraints:
	boolean spatialConstraints = false;
	boolean spatialConstraintsPoa = false;
	//Activate shuffle
	boolean activateShuffle = true;
	// The surface on which the agents and their environment are displayed
	private DisplaySurface dsurf;
	//GrAPH
	// A graph that plots sequence
	private OpenSequenceGraph actAgVsTime;

	private OpenSequenceGraph inactAgVsTime;
	private Plot logTimeVsLoglnacAg;
	private Plot densityPlot;
	private OpenSequenceGraph derivVsTime;
	//DATA rECOrDErS
	private DataRecorder recNumActAg;
	private DataRecorder recNumlnactAg;
	private DataRecorder recLog;
	private DataRecorder recDerivative;
	static DataOutputStream outputDensity;
	static DataOutputStream output;
	static DataOutputStream outputASCIIGRID;
	//Activate / Desactivate output data files
	boolean activaterecNumActAg = true;
	boolean activaterecNumlnactAg = true;
	boolean activaterecLog = true;
	boolean activaterecDerivative = true;
	boolean activateOutputDensity = true;
	boolean OutputMatrices = false;
	boolean outputASCIIfile = true;
	//Activate / desctivate snapshots
	boolean activateSnapshots = false;
	boolean activateSnapshotsByNumInactive = false;//takes snapshots every 1000 patches
	private Conditions aCondition;
	public Agent newAgent;
//	Hashtable<Color, String> colorTable = new Hashtable<Color, String>();
	Hashtable colorTable = new Hashtable();
	public int ttt = 1;




	public Model() { }


	//calculate the number of agent per group from the percentage given by parameters
	int numBlue = (numAgents * percentageBlue /100);
	int numYellow = (numAgents * percentageYellow /100);
	int numRed = (numAgents * percentagered /100);


	private void buildModel() {
		space = new Multi2DTorus(spaceSize, spaceSize, true);
		aCondition = new Conditions();


		int a, b;
		// create the agents
		if (spatialConstraintsPoa){
			a = 15;
			b = 33;
		}
		else{
			a = (spaceSize / 2);
			b = (spaceSize / 2);
		}
		int index = 0;
		for (int i = 0; i < numBlue; i++) {
			index = agentList.size()+ 1;
			Random.createUniform();
			int r = Random.uniform.nextIntFromTo(0,360);
			Agent blue = new Agent(space, index, this);
			blue.setXY(a, b);
			blue.setActive(true);
			blue.setColor(Color.blue);
			blue.setAgentDirection(r);
			blue.setCodeColor('b');
			blue.setToOutskirts(false);
			agentList.add(blue);
		}
		for (int i = 0; i < numYellow; i++) {
			index = agentList.size()+ 1;
			Random.createUniform();
			int r = Random.uniform.nextIntFromTo(0, 360);
			Agent yellow = new Agent(space, index, this);
			yellow.setXY(a, b);
			yellow.setActive(true);
			yellow.setColor(Color.yellow);
			yellow.setAgentDirection(r);
			yellow.setCodeColor('y');
			yellow.setToOutskirts(false);
			agentList.add(yellow);
		}
		for (int i = 0; i < numRed; i++) {
			index = agentList.size()+ 1;
			Agent red = new Agent(space, index, this);
			Random.createUniform();
			int r = Random.uniform.nextIntFromTo(0, 360);
			red.setXY(a, b);
			red.setActive(true);
			red.setColor(Color.red);
			red.setCodeColor('r');
			red.setAgentDirection(r);
//			System.out.println("ag dir: " + red.getAgentDirection());
			red.setToOutskirts(false);
			agentList.add(red);
		}

		//initial conditions
		if (centralSeed) {
			Agent meio = new Agent(space, index, this);
			meio.setXY(a, b);
			meio.setColor(Color.red);
			meio.setActive(false);
			meio.setCodeColor('r');
			meio.setToOutskirts(false);
			agentList.add(meio);
		}


		if (tworandomSeeds) {
			for (int i = 0; i < 2; i++) {
				int rx = Random.uniform.nextIntFromTo(0, spaceSize);
				int ry = Random.uniform.nextIntFromTo(0, spaceSize);
				Agent meio = new Agent(space, getAgentList().size() + 1, this);
				meio.setXY(rx, ry);
				meio.setColor(Color.red);
				meio.setActive(false);
				meio.setCodeColor('r');
				meio.setToOutskirts(false);
				agentList.add(meio);
			}
		}
		if (twoSeeds) {
		//seed one
			Agent firstAg = new Agent(space, getAgentList().size() + 1, this);
			//firstAg.setXY(35, 35);
			firstAg.setXY(35, 50);
			firstAg.setColor(Color.red);
			firstAg.setActive(false);
			firstAg.setCodeColor('r');
			firstAg.setToOutskirts(false);
			agentList.add(firstAg);
			//seed two
			Agent secondAg = new Agent(space, getAgentList().size() + 1, this);
			//secondAg.setXY(65, 65);
			secondAg.setXY(65, 50);
			secondAg.setColor(Color.red);
			secondAg.setActive(false);
			secondAg.setCodeColor('r');
			secondAg.setToOutskirts(false);
			agentList.add(secondAg);
		}
		if (fourrandomSeeds) {
			for (int i = 0; i < 4; i++) {
				int rx = Random.uniform.nextIntFromTo(0, spaceSize);
				int ry = Random.uniform.nextIntFromTo(0, spaceSize);
				Agent meio = new Agent(space, getAgentList().size() + 1, this);
				meio.setXY(rx, ry);
				meio.setColor(Color.red);
				meio.setActive(false);
				meio.setCodeColor('r');
				meio.setToOutskirts(false);
				agentList.add(meio);
			}
		}
		if (fourSeeds) {
			//seed one
			Agent firstAg = new Agent(space, getAgentList().size() + 1, this);
			//firstAg.setXY(40, 40);
			firstAg.setXY(50, 35);
			firstAg.setColor(Color.red);
			firstAg.setActive(false);
			firstAg.setCodeColor('r');
			firstAg.setToOutskirts(false);
			agentList.add(firstAg);
			//seed two
			Agent secondAg = new Agent(space, getAgentList().size() + 1, this);
			//secondAg.setXY(70, 50);
			secondAg.setXY(35, 50);
			secondAg.setColor(Color.red);
			secondAg.setActive(false);
			secondAg.setCodeColor('r');
			secondAg.setToOutskirts(false);
			agentList.add(secondAg);
			//seed one
			Agent thirdAg = new Agent(space, getAgentList().size() + 1, this);
			//thirdAg.setXY(60,80);
			thirdAg.setXY(65, 50);
			thirdAg.setColor(Color.red);
			thirdAg.setActive(false);
			thirdAg.setCodeColor('r');
			thirdAg.setToOutskirts(false);
			agentList.add(thirdAg);
			//seed four
			Agent fourthAg = new Agent(space, getAgentList().size() + 1, this);
			//fourthAg.setXY(45, 70);
			fourthAg.setXY(50, 65);
			fourthAg.setColor(Color.red);
			fourthAg.setActive(false);
			fourthAg.setCodeColor('r');
			fourthAg.setToOutskirts(false);
			agentList.add(fourthAg);
		}
		if (gridSeeds){
			for (int sx = 46; sx < 54; sx++){
				for (int sy = 48; sy < 52; sy++){
					if (sx == 46 || sx == 48 || sx == 50 || sx ==52 || sx == 54) {
						if (sy == 48 || sy == 50 || sy == 52) {
							Agent meio = new Agent(space, getAgentList().size() + 1, this);
							meio.setXY(sx, sy);
							meio.setColor(Color.red);
							meio.setActive(false);
							meio.setCodeColor('r');
							meio.setToOutskirts(false);
							agentList.add(meio);
						}
					}
				}
			}
		}//grid seeds
		if (pathSeeds){
			for (int i = 46; i < 55; i++){
				Agent meio = new Agent(space, getAgentList().size() + 1, this);
				meio.setXY(i, i);
				meio.setColor(Color.red);
				meio.setActive(false);
				meio.setCodeColor('r');
				meio.setToOutskirts(false);
				agentList.add(meio);
			}
		}

		if (favSeed) {

			for (int sx = 20; sx < 25; sx++){
				for (int sy = 30; sy < 38; sy++){
					Agent meio2 = new Agent(space, getAgentList().size() + 1, this);
					meio2.setXY(sx, sy);
					meio2.setColor(Color.cyan);
					meio2.setActive(false);
					meio2.setCodeColor('c');
					meio2.setToOutskirts(false);
					agentList.add(meio2);
				}
			}
		}

		if (spatialConstraints){
		//spatial constraints POA exercise
		//spatial contraint spatialconstraint02:
		//readMatrix("spatialconstraint02.txt");
		//spatial contraint spatialconstraint04:
		//readMatrix(“spatialconstraint04.txt");
			readMatrix("spatialconstraint-poa4.txt");

		}

		lista.add("base");
		lista.add("10-50-40");
		lista.add("30-50-20");
		lista.add("steps=4");
		lista.add("steps=8");
		lista.add("steps2=4");
		lista.add("steps2=8");
		lista.add("steps3=4");
		lista.add("steps3=8");
		lista.add("density=2");
		lista.add("density=8");
		lista.add("decay=100");
		lista.add("consolidation=100");
		lista.add("pred=4");
		lista.add("pred=6");
		lista.add("pred=8");
		lista.add("predrandom=50");
		lista.add("predrandom=100");
		// create the agents
//		outpath = path2 + lista.get(archive);
//
//		System.out.println(lista.get(archive));
//		System.out.println(outpath);
//		System.out.println("log " + log);

		System.out.println("Criei o modelo");
		System.out.println("space: " + this.getSpaceSize());
//		for (int i = 0; i < agentList.size(); i++) {
//			Agent ag = (Agent) agentList.get(i);
//			if (ag.getActive() == false)
//				System.out.println("ag id: " + ag.getWho());
// 		}
//
//		System.out.println("has: " + agentList.size() + " elements");
	}

	private void buildDisplay() {
//		MultiObject2DDisplay display = new MultiObject2DDisplay(space);


		//in case of spaceSize >101: display.reSize(505,505);
		colorTable.put(Color.blue, "b");//low-income
		colorTable.put(Color.red, "r");// high-income
		colorTable.put(Color.yellow, "y");// middle-income
		colorTable.put(Color.cyan, "c");//consolidated cells
		colorTable.put(Color.green, "g"); // abandoned central cells
		colorTable.put(Color.darkGray, "d");//spatial constraints
//		display.setObjectList(agentList);
//		dsurf.addDisplayableProbeable(display, "space");
		//set name of snapshot
		String snapname;
		if (log < 10) {
			System.out.println("log menor que 10, " + String.valueOf(log));
//			snapname = outpath + "_snapshot_0" +  String.valueOf(log) + "_";
			snapname = "snapshot_0" +  String.valueOf(log) + "_";

		}
		else {
//			snapname = outpath + "_snapshot_" +  String.valueOf(log) + "_";
			snapname = "snapshot_" +  String.valueOf(log) + "_";

		}
		dsurf.setSnapshotFileName(snapname);

		//GrAPHS
		//Active agents x time graph
		if (showActAgVsTime){
			actAgVsTime.addSequence("total active agents", new CountNumActiveAgents(),
			Color.BLACK, OpenSequenceGraph.FILLED_CIRCLE);

			actAgVsTime.addSequence("red active agents", new CountNumActiveRedAgents(),
			Color.red, OpenSequenceGraph.FILLED_CIRCLE);

			actAgVsTime.addSequence("yellow active agents",
			new CountNumActiveYellowAgents(),Color.yellow,
			OpenSequenceGraph.FILLED_CIRCLE);

			actAgVsTime.addSequence("blue active agents",
			new CountNumActiveBlueAgents(), Color.blue,
			OpenSequenceGraph.FILLED_CIRCLE);

			actAgVsTime.setAxisTitles("time", "active agents");
			actAgVsTime.setXRange(0,2000);
			actAgVsTime.setYRange(0,200);
		}
		//Inactive agents x time graph
		if (showlnactAgVsTime){
			inactAgVsTime.addSequence("total inactive agents",
			new countNumInactiveAgents(), Color.BLACK,
			OpenSequenceGraph.FILLED_CIRCLE);

			inactAgVsTime.addSequence("red inactive agents",
			new CountNumInactiveRedAgents(), Color.red,
			OpenSequenceGraph.FILLED_CIRCLE);

			inactAgVsTime.addSequence("yellow inactive agents",
			new CountNumlnactiveYellowAgents(),
			Color.yellow, OpenSequenceGraph.FILLED_CIRCLE);

			inactAgVsTime.addSequence("blue inactive agents", new CountNumlnactiveBlueAgents(), Color.blue,
			OpenSequenceGraph.FILLED_CIRCLE);
			inactAgVsTime.setAxisTitles("time", "inactive agents");
			inactAgVsTime.setXRange(0,2000);
			inactAgVsTime.setYRange(0,200);
		}
		//-------------------------------------- end GrAPH
		////rECOrDErS
		//recorder Number of Active Agents
		String actag;
		if (log < 10) {
//			actag = outpath  + "_activeag_0" + String.valueOf(log) + ".txt";
			actag = "activeag_0" + String.valueOf(log) + ".txt";

		}
		else {
//			actag = outpath  + "_activeag_" + String.valueOf(log) + ".txt";
			actag = "activeag_" + String.valueOf(log) + ".txt";

		}
//		String actag = path2 + "activeag_" + log + ".txt";
		recNumActAg = new DataRecorder (actag, this, "recorder Number of active agents");
		recNumActAg.addNumericDataSource("total active agents", new NumActiveAgentsSource());
		recNumActAg.addNumericDataSource("red active agents", new
		NumActiveRedAgentsSource());
		recNumActAg.addNumericDataSource("yellow active agents", new
		NumActiveYellowAgentsSource());
		recNumActAg.addNumericDataSource("blue active agents", new
		NumActiveBlueAgentsSource());

		//recorder Number of Inactive Agents
//		String inactag = path2 + "inactiveag_" + log + ".txt";
		String inactag;
		if (log < 10) {
//			inactag = outpath  + "_inactiveag_0" + String.valueOf(log) + ".txt";
			inactag = "inactiveag_0" + String.valueOf(log) + ".txt";

		}
		else {
//			inactag = outpath  + "_inactiveag_" + String.valueOf(log) + ".txt";
			inactag = "inactiveag_" + String.valueOf(log) + ".txt";

		}
		recNumlnactAg = new DataRecorder (inactag, this, "recorder Number of inactive agents");
		recNumlnactAg.addNumericDataSource("total inactive agents", new
		NumInactiveAgentsSource());
		recNumlnactAg.addNumericDataSource("red inactive agents", new
		NumInactiveRedAgentsSource());
		recNumlnactAg.addNumericDataSource("yellow inactive agents", new
		NumInactiveYellowAgentsSource());
		recNumlnactAg.addNumericDataSource("blue inactive agents", new
		NumInactiveBlueAgentsSource());
		recNumlnactAg.addNumericDataSource("cyan inactive agents", new
		NumInactiveCyanAgentsSource());
		recNumlnactAg.addNumericDataSource("green inactive agents", new
		NumInactiveGreenAgentsSource());

		//recorder Number of Log time vs Log inactive agents
		String pathLog;
		if (log < 10) {
//			pathLog = outpath  + "_log_0" + String.valueOf(log) + ".txt";
			pathLog = "log_0" + String.valueOf(log) + ".txt";

		}
		else {
//			pathLog = outpath  + "_log_" + String.valueOf(log) + ".txt";
			pathLog = "log_" + String.valueOf(log) + ".txt";

		}
//		String pathLog = path2 + "log_" + log +".txt";
		recLog = new DataRecorder (pathLog, this, "recorder log-log");
		recLog.addNumericDataSource("log Inactive Agents", new LogTotalInactiveAgentsSource());

		//calculateMassPerTime
		//recorder Derivative
//		String pathDerivate = path2 + "log_" + log + ".txt";
		String pathDerivate;
		if (log < 10) {
//			pathDerivate = outpath  + "_Derivative_0" + String.valueOf(log) + ".txt";
			pathDerivate = "Derivative_0" + String.valueOf(log) + ".txt";

		}
		else {
//			pathDerivate = outpath  + "_Derivative_" + String.valueOf(log) + ".txt";
			pathDerivate = "Derivative_" + String.valueOf(log) + ".txt";

		}
		recDerivative = new DataRecorder (pathDerivate, this, "recorder derivative");
		recDerivative.createNumericDataSource("derivative", this, "calculateMassPerTime");
//		addSimEventListener(dsurf);
//		dsurf.display();
	}

	private void buildRecord() {

		String actag;
		if (log < 10) {
//			actag = outpath  + "_activeag_0" + String.valueOf(log) + ".txt";
			actag = "activeag_0" + String.valueOf(log) + ".txt";

		}
		else {
//			actag = outpath  + "_activeag_" + String.valueOf(log) + ".txt";
			actag = "activeag_" + String.valueOf(log) + ".txt";

		}
//		String actag = path2 + "activeag_" + log + ".txt";
		recNumActAg = new DataRecorder (actag, this, "recorder Number of active agents");
		recNumActAg.addNumericDataSource("total active agents", new NumActiveAgentsSource());
		recNumActAg.addNumericDataSource("red active agents", new
		NumActiveRedAgentsSource());
		recNumActAg.addNumericDataSource("yellow active agents", new
		NumActiveYellowAgentsSource());
		recNumActAg.addNumericDataSource("blue active agents", new
		NumActiveBlueAgentsSource());

		//recorder Number of Inactive Agents
//		String inactag = path2 + "inactiveag_" + log + ".txt";
		String inactag;
		if (log < 10) {
//			inactag = outpath  + "_inactiveag_0" + String.valueOf(log) + ".txt";
			inactag = "inactiveag_0" + String.valueOf(log) + ".txt";

		}
		else {
//			inactag = outpath  + "_inactiveag_" + String.valueOf(log) + ".txt";
			inactag = "inactiveag_" + String.valueOf(log) + ".txt";

		}
		recNumlnactAg = new DataRecorder (inactag, this, "recorder Number of inactive agents");
		recNumlnactAg.addNumericDataSource("total inactive agents", new
		NumInactiveAgentsSource());
		recNumlnactAg.addNumericDataSource("red inactive agents", new
		NumInactiveRedAgentsSource());
		recNumlnactAg.addNumericDataSource("yellow inactive agents", new
		NumInactiveYellowAgentsSource());
		recNumlnactAg.addNumericDataSource("blue inactive agents", new
		NumInactiveBlueAgentsSource());
		recNumlnactAg.addNumericDataSource("cyan inactive agents", new
		NumInactiveCyanAgentsSource());
		recNumlnactAg.addNumericDataSource("green inactive agents", new
		NumInactiveGreenAgentsSource());

		//recorder Number of Log time vs Log inactive agents
		String pathLog;
		if (log < 10) {
//			pathLog = outpath  + "_log_0" + String.valueOf(log) + ".txt";
			pathLog = "log_0" + String.valueOf(log) + ".txt";

		}
		else {
//			pathLog = outpath  + "_log_" + String.valueOf(log) + ".txt";
			pathLog = "log_" + String.valueOf(log) + ".txt";

		}
//		String pathLog = path2 + "log_" + log +".txt";
		recLog = new DataRecorder (pathLog, this, "recorder log-log");
		recLog.addNumericDataSource("log Inactive Agents", new LogTotalInactiveAgentsSource());

		//calculateMassPerTime
		//recorder Derivative
//		String pathDerivate = path2 + "log_" + log + ".txt";
		String pathDerivate;
		if (log < 10) {
//			pathDerivate = outpath  + "_Derivative_0" + String.valueOf(log) + ".txt";
			pathDerivate = "Derivative_0" + String.valueOf(log) + ".txt";

		}
		else {
//			pathDerivate = outpath  + "_Derivative_" + String.valueOf(log) + ".txt";
			pathDerivate = "Derivative_" + String.valueOf(log) + ".txt";

		}
		recDerivative = new DataRecorder (pathDerivate, this, "recorder derivative");
		recDerivative.createNumericDataSource("derivative", this, "calculateMassPerTime");

	}

	private void buildSchedule() {
		class Modelrunner extends BasicAction {
			Agent ag;
			public void execute() {
				// call the shuffleAgents method on this model
				if (activateShuffle)
					shuffleAgents();
				// on each Agent in the agentList
				for (int i = 0; i < agentList.size(); i++) {
					ag = (Agent) agentList.get(i);
					if (!ag.getActive()){
						int ran = Random.uniform.nextIntFromTo(0,100);
						char agColor = ag.getCodeColor();
						//all agents age
						int agAge = ag.getAge();
						ag.setAge(agAge + 1);
						if (activateDecay) {
							if (agColor == 'r'){
								//if age value is higher than parameter i, decay rule is activated (also random):
								if ( (agAge >= decayStartPoint) && (ran <= decayrandom)){
									//if age and density are higher than parameters i and j, then red agent leaves the location
									int densityHere = ag.checkDensity();
									if (densityHere >= d) {
										System.out.println(ag.getWho() + " leaving from centre");
										ag.setActive(true);
										ag.setAge(0);// added 10 august
										ag.setToOutskirts(true);
										createGreenAgentHere(ag.getX(), ag.getY());
									}
								}
							}
						}//end of activate decay
						if (prejudice) {
							if (agColor == 'r' || agColor == 'y') {

								int low = ag.checkDensity2();
								if ((low >= pred) && (ran <= predrandom)) {
									System.out.println(ag.getWho() + " leaving cell do to prejudice");
									ag.setActive(true);
									ag.setAge(0);// added 10 august
									ag.setToOutskirts(true);
									agentList.add(ag);
//									createGreenAgentHere(ag.getX(), ag.getY());
								}
							}
						}
						if (agColor == 'b') {
							//if consolidation boolean is true
							if (activateConsolidation){
								int ran2 = Random.uniform.nextIntFromTo(0,100);

								if (neighCons) {
//									System.out.println("Entrei nova regra, ag: " + ag.getWho());
									//if age of agent is higher than consolidationLimit, consolidate agent!
									int neigh = 0;
									ArrayList neighbours = space.getMooreNeighbors(ag.getX(), ag.getY(), false);
									for (int j = 0; j < neighbours.size(); j++){
										Agent ngbAg = (Agent) neighbours.get(j);
										if (ngbAg.getActive() == false){ // only inactive neighbours!
											char ngbAgColour = ngbAg.getCodeColor();
											if (ngbAgColour == 'c' || ngbAgColour == 'b'){
												neigh++;
											}
										}
									}
									if (neighbours.size() > 0) {
										int indicador = 50*(neigh/neighbours.size());
										int ran3 = (int) ran2/50;

//										System.out.println("indicador: " + indicador);
										if ((agAge >= consolidationLimit) && ((indicador + ran3) >= consolidationrandom)) {
//											System.out.println("Consolidei, regra nova");
											ag.setCodeColor('c');
											ag.setColor(Color.cyan);
											//ag.setActive(false);
										}
									}
									else if ((agAge >= consolidationLimit) && (ran2 <= consolidationrandom)) {
										ag.setCodeColor('c');
										ag.setColor(Color.cyan);
										//ag.setActive(false);
									}
								}
								//if age of agent is higher than consolidationLimit, consolidate agent!
								else if ((agAge >= consolidationLimit) && (ran2 <= consolidationrandom)) {
									ag.setCodeColor('c');
									ag.setColor(Color.cyan);
									//ag.setActive(false);
								}
							}
						}
					}// end - if inactive
					// call the walk method on all active agents
					boolean s = ag.getActive();
					if (s == true) {
						ag.walk();
//						System.out.println("ag #" + ag.getWho() + " andou");
						if (aCondition.isThereRedlnactive(space, ag.getX(), ag.getY())) {
//							System.out.println("ag #" + ag.getWho() + " procurando local para morar");
							ag.findSpace(findspaceSteps);
						}
					}//if

				}//for

				ttt+=1;

				// updates the number of active agents
				double numActBlue = countNumActiveBlueAgents();
				double numActYell = countNumActiveYellowAgents();
				double numActred = CountNumActiveRedAgents();

				if (numActBlue < numBlue) {
					double numAgToCreate = numBlue - numActBlue;
					int i;
					for (i = 0; i < numAgToCreate; i++) {
						createNewAgent('b');
					}
				}
				if (numActYell < numYellow) {
					double numAgToCreate = numYellow - numActYell;
					int i;
					for (i = 0; i < numAgToCreate; i++) {
						createNewAgent('y');
					}
				}
				if (numActred < numRed) {
					double numAgToCreate = numRed - numActred;
					int i;
					for (i = 0; i < numAgToCreate; i++) {
						createNewAgent('r');
					}
				}
//				dsurf.updateDisplay(); //display every tick
				//update graphs
				if (showActAgVsTime)
					actAgVsTime.step();

				if (showlnactAgVsTime)
					inactAgVsTime.step();

				if (showLogLogPlot)
					plotLogLog();
				//stops the simulation automatically if reaches maximum number of occupied cells
				//it still has to be tested and final values corrected
				if (spatialConstraints){
					int totalOccupiedCells = countNumInactiveAgents();
					if (totalOccupiedCells >= 9500)
						stop();//9000 for POA!
				}
				else{
					if (spaceSize == 51) {
						int totalOccupiedCells = countNumInactiveAgents();
						if (totalOccupiedCells >= 2000)
							stop();
					}
					if (spaceSize == 101) {
						int totalOccupiedCells = countNumInactiveAgents();
						if (totalOccupiedCells >= 8000)
							stop();
					//if western city then total occupied cells = 7000:
					//if (totalOccupiedCells >= 7000) stop();
					}
					if (spaceSize == 201) {
						int totalOccupiedCells = countNumInactiveAgents();
						if (totalOccupiedCells >= 30500)
							stop();
					}
					if (spaceSize == 301) {
						int totalOccupiedCells = countNumInactiveAgents();
						if (totalOccupiedCells >= 88500)
							stop();
					}
				}//else
				double timeEnd = getTickCount();

				if (spaceSize == 51 && timeEnd >= 5000) {
					setFim();
					exportASCIIfile();
//					dsurf.takeSnapshot();
					stop();
				}

				//take snapshots every 1000 patches
				if (activateSnapshotsByNumInactive ){
					int numInactiveNow = countNumInactiveAgents();
					double timeNow = getTickCount();
					if ((numInactiveNow >= 1000) & (numInactiveNow <=1100)) {
						dsurf.takeSnapshot();
						System.out.println(numInactiveNow +" time =" + timeNow);
					}
					if ((numInactiveNow >= 2000) & (numInactiveNow <=2100)) {
						dsurf.takeSnapshot();
						System.out.println(numInactiveNow +" time =" + timeNow);
					}
					if ((numInactiveNow >= 3000) & (numInactiveNow <=3100)) {
						dsurf.takeSnapshot();
						System.out.println(numInactiveNow +" time =" + timeNow);
					}
					if ((numInactiveNow >= 4000) & (numInactiveNow <=4100)) {
						dsurf.takeSnapshot();
						System.out.println(numInactiveNow +" time =" + timeNow);
					}
					if ((numInactiveNow >= 5000) & (numInactiveNow <=5100)) {
						dsurf.takeSnapshot();
						System.out.println(numInactiveNow +" time =" + timeNow);
					}
					if ((numInactiveNow >= 6000) & (numInactiveNow <=6100)) {
						dsurf.takeSnapshot();
						System.out.println(numInactiveNow +" time =" + timeNow);
					}
					if ((numInactiveNow >= 7000) & (numInactiveNow <=7100)) {
						dsurf.takeSnapshot();
						System.out.println(numInactiveNow +" time =" + timeNow);
					}
					if ((numInactiveNow >= 8000) & (numInactiveNow <=8100)) {
						dsurf.takeSnapshot();
						System.out.println(numInactiveNow +" time =" + timeNow);
					}
					if ((numInactiveNow >= 9000) & (numInactiveNow <=9100)){
						dsurf.takeSnapshot();
						System.out.println(numInactiveNow +" time =" + timeNow);
					}
					if ((numInactiveNow >= 10000) & (numInactiveNow <=10100)){
						dsurf.takeSnapshot();
						System.out.println(numInactiveNow +" time =" + timeNow);
					}
					if ((numInactiveNow >= 11000) & (numInactiveNow <=11100)) {
						dsurf.takeSnapshot();
						System.out.println(numInactiveNow +" time =" + timeNow);
					}
					if ((numInactiveNow >= 12000) & (numInactiveNow <=12100)) {
						dsurf.takeSnapshot();
						System.out.println(numInactiveNow +" time =" + timeNow);
					}
				}
			} //execute
//			private int countNumInactiveAgents() {
//				// TODO Auto-generated method stub
//				return 0;
//			}
		} //basic action
		// the schedule has been created in setup()
		schedule.scheduleActionBeginning(0, new Modelrunner());
//		System.out.println("passei " + ttt + " vezes por aqui");
		//display every 10 ticks - (need to take off the dsurf.updatedisplay method above)
//		schedule.scheduleActionAtInterval(10, dsurf, "updateDisplay");//display every 10 ticks
//		dsurf.updateDisplay(); //display every tick

		if (showDensityPlot)
			schedule.scheduleActionAtEnd(this, "calculateDensity");
//		if (OutputMatrices)
//			schedule.scheduleActionAtEnd(this, "exportMatricesClusterlndentification");
		//export files for analysis every 1000 ticks
//		schedule.scheduleActionAtInterval(1000, this,"exportMatricesClusterlndentification");
		if (outputASCIIfile) {
			schedule.scheduleActionAtEnd(this, "setFim");
			schedule.scheduleActionAtEnd(this, "exportASCIIfile");
		}
		//export files for analysis every 1000 ticks
		schedule.scheduleActionAtInterval(50, this,"exportASCIIfile");
		//call recorders
		schedule.scheduleActionBeginning(0, new BasicAction(){
			public void execute(){
				recNumActAg.record();
				recNumlnactAg.record();
				recDerivative.record();
				recLog.record();
			}
		});
			//compute density and write to file if boolean is true (kind of recorder)
		if (activateOutputDensity){
			schedule.scheduleActionAtEnd(this, "computeDensityTotal");
		}
		//write the recorder information to file at the end of simulation if the boolean is true
		if (activaterecNumActAg)
			schedule.scheduleActionAtEnd(recNumActAg, "writeToFile");
		if (activaterecNumlnactAg)
			schedule.scheduleActionAtEnd(recNumlnactAg, "writeToFile");
		if (activaterecDerivative)
			schedule.scheduleActionAtEnd(recDerivative, "writeToFile");
		if (activaterecLog)
			schedule.scheduleActionAtEnd(recLog, "writeToFile");
		//set snapshots every 250 ticks and at end of simulation
		if (activateSnapshots){
//			schedule.scheduleActionAtInterval(25, dsurf, "takeSnapshot");
			schedule.scheduleActionAtEnd(dsurf, "takeSnapshot");
			schedule.scheduleActionAt(0, dsurf,"takeSnapshot");
			//schedule.scheduieActionBeginning(0, dsurf, "takeSnapshot");
		}
		if (activateSnapshotsByNumInactive){
//			schedule.scheduleActionAtEnd(dsurf, "takeSnapshot");
//			schedule.scheduleActionAt(0, dsurf,"takeSnapshot");
		}
	} //build schedule

	// randomize the order of the object (the SugarAgents) in the agentList
	public void shuffleAgents() {
		SimUtilities.shuffle(agentList);
	}

	public void begin() {
		buildModel();
//		buildDisplay();
		buildRecord();
		buildSchedule();
//		dsurf.display();
		//.------------------------ GrAPHS
//		if (showActAgVsTime)
//			actAgVsTime.display();
//		if (showlnactAgVsTime)
//			inactAgVsTime.display();
//		if (showLogLogPlot)
//			logTimeVsLoglnacAg.display();
	}

	public void setup() {
		schedule = null;
		agentList = new ArrayList();
		space = null;
		if (dsurf != null)
			dsurf.dispose();
		dsurf = null;
		//------------GrAPHS
		if (showActAgVsTime){
			if (actAgVsTime != null)
				actAgVsTime.dispose();

			actAgVsTime = null;
		}
		if (showlnactAgVsTime){
			if (inactAgVsTime != null)
				inactAgVsTime.dispose();

			inactAgVsTime = null;
		}

		if (showLogLogPlot) {
			if (logTimeVsLoglnacAg != null)
				logTimeVsLoglnacAg.dispose();

			logTimeVsLoglnacAg = null;
		}
		if (densityPlot != null)
			densityPlot.dispose();
		densityPlot = null;

		System.gc();
		// create a schedule with an interval of one.
		schedule = new Schedule(1);
//		dsurf = new DisplaySurface(this, "DisplaySurface");
//		registerDisplaySurface("Display", dsurf);
		//. GrAPHS
		if (showActAgVsTime)
			actAgVsTime = new OpenSequenceGraph("active agents vs time", this);
		if (showlnactAgVsTime)
			inactAgVsTime = new OpenSequenceGraph("inactive agents vs time", this);
		if (showLogLogPlot)
			logTimeVsLoglnacAg = new Plot("log time vs log total inactive agents");
		if (showDensityPlot)
			densityPlot = new Plot ("Density from the centre");
		derivVsTime = new OpenSequenceGraph("derivative (mass) vs time", this);
	}

	// GETTErS AND SETTErS
	public int getNumAgents() {
		return numAgents;
	}
	public void setNumAgents(int numAgents) {
		this.numAgents = numAgents;
	}
	public int getSpaceSize() {
		return spaceSize;
	}
	public void setSpaceSize(int spaceSize) {
		this.spaceSize = spaceSize;
	}
	public Multi2DTorus getSpace() {
		return space;
	}
	public int getPercentageBlue() {
		return percentageBlue;
	}
	public void setPercentageBlue(int perc) {
		this.percentageBlue = perc;
	}
	public int getPercentageYellow() {
		return percentageYellow;
	}
	public void setPercentageYellow(int perc) {
		this.percentageYellow = perc;
	}
	public int getPercentagered() {
		return percentagered;
	}
	public void setPercentagered(int perc) {
		this.percentagered = perc;
	}
	public ArrayList getAgentList() {
		return agentList;
	}
	public int getFindspaceSteps() {
		return findspaceSteps;
	}
	public void setFindspaceSteps(int s) {
		this.findspaceSteps = s;
	}
	//parameters modules 2 and 3
	public int getFindspaceSteps2() {
		return findspaceSteps2;
	}
	public void setFindspaceSteps2(int s) {
		this.findspaceSteps2 = s;
	}
	public int getFindspaceSteps3() {
		return findspaceSteps3;
	}
	public void setFindspaceSteps3(int s) {
		this.findspaceSteps3 = s;
	}
	public int getDecayStartPoint() {
		return decayStartPoint;
	}
	public void setDecayStartPoint(int s) {
		this.decayStartPoint = s;
	}
	public int getConsolidationLimit() {
		return consolidationLimit;
	}
	public void setConsolidationLimit(int s) {
		this.consolidationLimit = s;
	}
	public int getConsolidationrandom() {
		return consolidationrandom;
	}
	public void setConsolidationrandom(int s) {
		this.consolidationrandom = s;
	}
	public int getDecayrandom() {
		return decayrandom;
	}
	public void setDecayrandom(int s) {
		this.decayrandom = s;
	}
	public int getD() {
		return d;
	}
	public void setD(int s) {
		this.d = s;
	}
	public int getConsLimit() {
		return consLimit;
	}
	public void setConsLimit(int s) {
		this.consLimit = s;
	}
	public int getPred() {
		return pred;
	}
	public void setPred(int s) {
		this.pred = s;
	}
	public int getPredrandom() {
		return predrandom;
	}
	public void setPredrandom(int s) {
		this.predrandom = s;
	}
//	public String[] getInitParam() {
//		// TODO Auto-generated method stub
//		return null;
//	}
	public int getLog() {
		return log;
	}
	public void setLog(int log) {
		this.log = log;
	}
	public int getArchive() {
		return archive;
	}
	public void setArchive(int archive) {
		this.archive = archive;
	}
	public int getFim() {
		return fim;
	}
	public void setFim() {
		this.fim = 1;
	}

	public String[] getInitParam() {
		String[] params = {
		"numAgents", "SpaceSize", "percentageBlue", "percentageYellow", "consLimit",
		"percentagered", "findspaceSteps", "findspaceSteps2", "findspaceSteps3",
		"decayStartPoint", "consolidationLimit", "d", "consolidationrandom",
		"decayrandom", "predrandom", "pred", "log", "archive"};

//		System.out.println(Arrays.toString(params));
		return params;
	}

//	//////////END OF GETTErS AND SETTErS
//	public void addNumActiveAgents(Agent ag) {
//		int count = 0;
//		Agent ag1 ;
//		for (int i = 0; i < agentList.size(); i++) {
//			ag1 = (Agent) agentList.get(i);
//			if (ag1.getActive())
//				count++;
//		}
//	}

	public void createNewAgent (char c) {
		int a, b;
		//create the agents
		if (spatialConstraintsPoa){
			a = 15;
			b = 33;
		}
		else{
			a = (spaceSize / 2);
			b = (spaceSize / 2);
		}
		int index = getAgentList().size() + 1;
		int r = Random.uniform.nextIntFromTo(0, 360);
		Agent extraAg = new Agent (space, index, this);
		extraAg.setXY(a, b);
		extraAg.setActive(true);
		extraAg.setAgentDirection(r);
		extraAg.setToOutskirts(false);
		if (c == 'r'){
			extraAg.setCodeColor('r');
			extraAg.setColor(Color.red);
		}
		if (c == 'y' ) {
			extraAg.setCodeColor('y');
			extraAg.setColor(Color.yellow);
		}
		if (c == 'b') {
			extraAg.setCodeColor('b');
			extraAg.setColor(Color.blue);
		}
		agentList.add(extraAg);
//		System.out.println("criei novo agente, cor: " + extraAg.getCodeColor());
	}

	public void createGreenAgentHere(int a, int b){
		int index = agentList.size()+ 1;
		Agent greenAg = new Agent (space, index, this);
		greenAg.setXY(a, b);
		greenAg.setActive(false);
		greenAg.setColor(Color.green);
		greenAg.setCodeColor('g');
		agentList.add(greenAg);
		System.out.println("green agent created at "+ a +", "+ b );
	}
	////MEASUrEMENTS — GrAPHS
	///rECOrDErS
	class NumActiveAgentsSource implements NumericDataSource {
		public double execute() {
			double count = 0;
			Agent ag1;
			for (int i = 0; i < agentList.size(); i++) {
				ag1 = (Agent) agentList.get(i);
				if (ag1.getActive())
					count++;
			}
			return count;
		}
	}
	//number of red active agents
	class NumActiveRedAgentsSource implements NumericDataSource {
		public double execute() {
			double count = 0;
			Agent ag1;
			for (int i = 0; i < agentList.size(); i++) {
				ag1 = (Agent) agentList.get(i);
				if (ag1.getActive() && (ag1.getCodeColor()== 'r'))
					count++;
			}
			//System.out.printlnf number of active agents:"+ count);
			return count;
		}
	}
	//number of yellow active agents
	class NumActiveYellowAgentsSource implements NumericDataSource {
		public double execute() {
			double count = 0;
			Agent ag1;
			for (int i = 0; i < agentList.size(); i++) {
				ag1 = (Agent) agentList.get(i);
				if (ag1.getActive() && (ag1.getCodeColor() == 'y'))
					count++;
				}
			return count;
		}
	}
	//number of blue active agents
	class NumActiveBlueAgentsSource implements NumericDataSource {
		public double execute() {
			double count = 0;
			Agent ag1;
			for (int i = 0; i < agentList.size(); i++) {
				ag1 = (Agent) agentList.get(i);
				if (ag1.getActive() && (ag1.getCodeColor() == 'b'))
					count++;
			}
			return count;
		}
	}
	//number of inactive agents
	class NumInactiveAgentsSource implements NumericDataSource {
		public double execute() {
			double count = 0;
			Agent ag1;
			for (int i = 0; i < agentList.size(); i++) {
				ag1 = (Agent) agentList.get(i);
				if (ag1.getActive() == false)
					count++;
			}
			return count;
		}
	}
	//number of red inactive agents
	class NumInactiveRedAgentsSource implements NumericDataSource {
		public double execute() {
			double count = 0;
			Agent ag1;
			for (int i = 0; i < agentList.size(); i++) {
				ag1 = (Agent) agentList.get(i);
				if ( (ag1.getActive() == false) && (ag1.getCodeColor() == 'r'))
					count++;
			}
			return count;
		}
	}
	//number of yellow inactive agents
	class NumInactiveYellowAgentsSource implements NumericDataSource {
		public double execute() {
			double count = 0;
			Agent ag1;
			for (int i = 0; i < agentList.size(); i++) {
				ag1 = (Agent) agentList.get(i);
				if ( (ag1.getActive() == false) && (ag1.getCodeColor() == 'y'))
					count++;
			}
			return count;
		}
	}
	//number of blue inactive agents
	class NumInactiveBlueAgentsSource implements NumericDataSource {
		public double execute() {
			double count = 0;
			Agent ag1;
			for (int i = 0; i < agentList.size(); i++) {
				ag1 = (Agent) agentList.get(i);
				if ( (ag1.getActive() == false) && (ag1.getCodeColor() == 'b'))
					count++;
			}
			return count;
		}
	}
	//number of cyan inactive agents
	class NumInactiveCyanAgentsSource implements NumericDataSource {
		public double execute() {
			double count = 0;
			Agent ag1;
			for (int i = 0; i < agentList.size(); i++) {
				ag1 = (Agent) agentList.get(i);
				if ( (ag1.getActive() == false) && (ag1.getCodeColor() == 'c'))
					count++;
			}
			return count;
		}
	}
	//number of green inactive agents
	class NumInactiveGreenAgentsSource implements NumericDataSource {
		public double execute() {
			double count = 0;
			Agent ag1;
			for (int i = 0; i < agentList.size(); i++) {
				ag1 = (Agent) agentList.get(i);
				if ( (ag1.getActive() == false) && (ag1.getCodeColor() == 'g'))
					count++;
			}
			return count;
		}
	}

	class LogTimeSource implements NumericDataSource {
		private Model myModel;
		public double execute() {
			myModel = new Model();
			double logTime;
			double time;
			time = getTickCount();
			logTime = Math.log(time);
			return logTime;
		}
	}

	class LogTotalInactiveAgentsSource implements NumericDataSource {
		public double execute() {
			double loglnactiveAgents;
			loglnactiveAgents = Math.log(countNumInactiveAgents());
			return loglnactiveAgents;
		}
	}
	/////////END rECOrDErS

	class CountNumActiveAgents implements Sequence {
		public double getSValue() {
			double count = 0;
			Agent ag1;
			for (int i = 0; i < agentList.size(); i++) {
				ag1 = (Agent) agentList.get(i);
				if (ag1.getActive())
					count++;
			}
			return count;
		}
	}
	//number of red active agents
	class CountNumActiveRedAgents implements Sequence {
		public double getSValue() {
			double count = 0;
			Agent ag1;
			for (int i = 0; i < agentList.size(); i++) {
				ag1 = (Agent) agentList.get(i);
				if (ag1.getActive() && (ag1.getCodeColor()== 'r'))
					count++;
			}
			return count;
		}
	}
	//number of yellow active agents
	class CountNumActiveYellowAgents implements Sequence {
		public double getSValue() {
			double count = 0;
			Agent ag1;
			for (int i = 0; i < agentList.size(); i++) {
				ag1 = (Agent) agentList.get(i);
				if (ag1.getActive() && (ag1.getCodeColor() == 'y'))
					count++;
			}
			return count;
		}
	}
	//number of blue active agents
	class CountNumActiveBlueAgents implements Sequence {
		public double getSValue() {
			double count = 0;
			Agent ag1;
			for (int i = 0; i < agentList.size(); i++) {
				ag1 = (Agent) agentList.get(i);
				if (ag1.getActive() && (ag1.getCodeColor() == 'b'))
					count++;
			}
			return count;
		}
	}
	//number of inactive agents
	class countNumInactiveAgents implements Sequence {
		public double getSValue() {
			double count = 0;
			Agent ag1;
			for (int i = 0; i < agentList.size(); i++) {
				ag1 = (Agent) agentList.get(i);
				if (ag1.getActive() == false)
					count++;
			}
			return count;
		}
	}
	//number of red inactive agents
	class CountNumInactiveRedAgents implements Sequence {
		public double getSValue() {
			double count = 0;
			Agent ag1;
			for (int i = 0; i < agentList.size(); i++) {
				ag1 = (Agent) agentList.get(i);
				if ( (ag1.getActive() == false) && (ag1.getCodeColor() == 'r'))
					count++;
			}
			return count;
		}
	}
	//number of yellow inactive agents
	class CountNumlnactiveYellowAgents implements Sequence {
		public double getSValue() {
			double count = 0;
			Agent ag1;
			for (int i = 0; i < agentList.size(); i++) {
				ag1 = (Agent) agentList.get(i);
				if ( (ag1.getActive() == false) && (ag1.getCodeColor() == 'y'))
					count++;
			}
			return count;
		}
	}
	//number of blue inactive agents
	class CountNumlnactiveBlueAgents implements Sequence {
		public double getSValue() {
			double count = 0;
			Agent ag1;
			for (int i = 0; i < agentList.size(); i++) {
				ag1 = (Agent) agentList.get(i);
				if ( (ag1.getActive() == false) && (ag1.getCodeColor() == 'b'))
					count++;
			}
			return count;
		}
	}

	class LogTotalActiveAgents implements Sequence {
		public double getSValue() {
			double logActiveAgents;
			logActiveAgents = Math.log(countNumActiveAgents());
			return logActiveAgents;
		}
	}

	class LogTime implements Sequence {
		private Model myModel;
		public double getSValue() {
			myModel = new Model();
			double logTime;
			double time;
			time = myModel.getTickCount();
			logTime = Math.log(time);
			return logTime;
		}
	}

	class LogTotallnactiveAgents implements Sequence {
		public double getSValue() {
			double loglnactiveAgents;
			loglnactiveAgents = Math.log(countNumInactiveAgents());
			return loglnactiveAgents;
		}
	}
	//number of active agents
	public int countNumActiveAgents() {
		int count = 0;
		Agent ag1;
		for (int i = 0; i < agentList.size(); i++) {
			ag1 = (Agent) agentList.get(i);
			if (ag1.getActive())
				count++;
		}
		return count;
	}
	//number of inactive agents
	public int countNumInactiveAgents() {
		int count = 0;
		Agent ag1;
		for (int i = 0; i < agentList.size(); i++) {
			ag1 = (Agent) agentList.get(i);
			if (ag1.getActive()== false)
				count++;
		}
		return count;
	}
	//number of inactive red agents
	public int CountNumInactiveRedAgents() {
		int count = 0;
		Agent ag1;
		for (int i = 0; i < agentList.size(); i++) {
			ag1 = (Agent) agentList.get(i);
			if ((ag1.getActive()== false) && (ag1.getCodeColor()== 'r'))
				count++;
		}
		return count;
	}
		//method to update log-log graph
	private void plotLogLog (){
		double logActiveAgents = Math.log(countNumActiveAgents());
		double time = this.getTickCount();
		double alogTime = Math.log(time);
		logTimeVsLoglnacAg.setConnected(true);
		logTimeVsLoglnacAg.setXRange(0,100);
		logTimeVsLoglnacAg.setYRange(0,100);
		logTimeVsLoglnacAg.setAxisTitles("log time","log inactive agents");
		logTimeVsLoglnacAg.plotPoint(alogTime, logActiveAgents, 0);
		logTimeVsLoglnacAg.updateGraph();
		logTimeVsLoglnacAg.fillPlot();
	}
	//method to calculate density from the centre
	public void calculateDensity() {
		int a, b, i;
		double r, dx, dy;
		int[] massArray, massArrayRed, massArrayYellow, massArrayBlue;
		massArray = new int[50];
		massArrayRed = new int[50];
		massArrayYellow = new int[50];
		massArrayBlue = new int[50];
		densityPlot.setConnected(true);
		densityPlot.setXRange(0,100);

		densityPlot.setYRange(0,100);
		for (i = 0; i < 50; i++){
			massArray [i] = 0;
			massArrayRed[i] = 0;
			massArrayYellow[i] = 0;
			massArrayBlue[i] = 0;
		}


		//density of total inactive agents (patches)- dataset 0
		for (i = 0; i < 50; i++){
			for (a = 0; a < spaceSize; a++){
				for (b = 0; b < spaceSize; b++){
					if (aCondition.isThereAnlnactiveAgentAtList(space, a, b)){
						dx = Math.pow((a - 50), 2);
						dy = Math.pow((b - 50), 2);
						r = Math.sqrt(dx + dy);
						if (r <= i){
							massArray[i]++;
						}
					}//if aCondition
				}//for y
			}//for x
			//plot points densityTotal
			densityPlot.plotPoint(i, massArray[i], 0);
		}//for i
		//density of red inactive agents (patches) - dataset 1
		for (i = 0; i < 50; i++){
			for(a = 0; a < 101;a++){
				for (b = 0; b < 101; b++){
					if (aCondition.isThereRedlnactive(space, a, b)){
						dx = Math.pow((a - 50), 2);
						dy = Math.pow((b - 50), 2);
						r = Math.sqrt(dx + dy);
						if (r <= i){
							massArrayRed[i]++;
						}
					}//if aCondition
				}//for y
			}//for x
			//plot points densityTotal
			densityPlot.plotPoint(i, massArrayRed[i], 1);
		}//for i
		//density of yellow inactive agents (patches) - dataset 2
		for (i = 0; i < 50; i++){
			for (a = 0; a < 101; a++){
				for (b = 0; b < 101; b++){
					if (aCondition.isThereYellowlnactive(space, a, b)){
						dx = Math.pow((a - 50), 2);
						dy = Math.pow((b - 50), 2);
						r = Math.sqrt(dx + dy);
						if (r <= i){
							massArrayYellow[i]++;
						}
					}//if aCondition
				}//for y
			}//for x
			//plot points densityYellow
			densityPlot.plotPoint(i, massArrayYellow[i], 2);
		}//for i
		//density of blue inactive agents (patches) - dataset 3
		for (i = 0; i < 50; i++){
			for(a = 0;a< 101;a++){
				for (b = 0; b < 101; b++){
					if (aCondition.isThereBluelnactive(space, a, b)){
						dx = Math.pow((a - 50), 2);
						dy = Math.pow((b - 50), 2);
						r = Math.sqrt(dx + dy);
						if (r <= i){
							massArrayBlue[i]++;
						}
					}//if aCondition
				}//for y
			}//for x
			//plot points densityYellow
			densityPlot.plotPoint(i, massArrayBlue[i], 3);
		}//for i
		// plot display:
//		densityPlot.setAxisTitles("radius", "mass inactive agents");
//		densityPlot.addLegend(0, "Total inactive", Color.black, Plot.FILLED_CIRCLE);
//		densityPlot.addLegend(1, "Red inactive", Color.red, Plot.FILLED_CIRCLE);
//		densityPlot.addLegend(2, "Yellow inactive", Color.yellow, Plot.FILLED_CIRCLE);
//		densityPlot.addLegend(3, "Blue inactive", Color.blue, Plot.FILLED_CIRCLE);
//		densityPlot.display();
//		densityPlot.updateGraph();
//		densityPlot.fillPlot();

//		String FileName=path2 + "density2_"+ log + ".txt";
		String FileName;
		if (log < 10) {
//			FileName = outpath  + "_density2_0" + String.valueOf(log) + ".txt";
			FileName = "density2_0" + String.valueOf(log) + ".txt";

		}
		else {
//			FileName = outpath  + "_density2_" + String.valueOf(log) + ".txt";
			FileName = "density2_" + String.valueOf(log) + ".txt";

		}
		DataOutputStream outputDensity2 = null;
		try {
			outputDensity2 = new DataOutputStream(new FileOutputStream(FileName));
		}
		catch ( IOException e) {
			System.err.println("Error during output\n");
			System.exit(1);
		}
		try {

			outputDensity2.writeBytes("i, massa total dos inativos, red, yellow, blue \n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (i = 0; i < 50; i++) {
			try{
				outputDensity2.writeBytes(i + ","+ massArray[i] + "," + massArrayRed[i] + ","+
						massArrayYellow[i] + "," + massArrayBlue[i]  +"\n"); //aqui ele ta gravando no arquivo
			}
			catch(IOException ioException){
				System.out.println("Cannot save result");
			}
		}
		try{
			outputDensity2.close(); //este comando fecha o arquivo
		}
		catch(IOException ioException){
			System.out.println("Cannot close file");
		}
//		densityPlot.setSnapshotFileName("density");
//		densityPlot.takeSnapshot();
	} //method

	public void openFile(){
//		String FileName= path2 + "density_"+ log +".txt";
		String FileName;
		if (log < 10) {
//			FileName = "density_0" + String.valueOf(log) + ".txt";
			FileName = "density_0" + String.valueOf(log) + ".txt";

		}
		else {
//			FileName = outpath  + "_density_" + String.valueOf(log) + ".txt";
			FileName = "density_" + String.valueOf(log) + ".txt";

		}
		try {
			outputDensity = new DataOutputStream(new FileOutputStream(FileName));
		}
		catch ( IOException e) {
			System.err.println("Error during output\n");
			System.exit(1);
		}
	}
	// pag 265(280/299)

	//method to calculate density from the centre
	public void computeDensityTotal() {
		int a, b, i;
		double r, dx, dy;
		int halfSpaceSize = spaceSize/2;
		int radiusSize = (spaceSize/2) + 1;
		int[] massArrayTotal;
		massArrayTotal = new int[radiusSize];
		int[] massArrayRed;
		massArrayRed = new int[radiusSize];
		int[] massArrayYellow;
		massArrayYellow = new int[radiusSize];
		int[]  massArrayBlue;
		massArrayBlue = new int[radiusSize];
		int[] massArrayCyan;
		massArrayCyan = new int[radiusSize];
		int[] massArrayGreen;
		massArrayGreen = new int[radiusSize];
		openFile();
		for (i = 0; i < radiusSize; i++) {
			massArrayTotal[i] = 0;
			massArrayRed[i] = 0;
			massArrayYellow[i] = 0;
			massArrayBlue[i] = 0;
		}
		//density of total inactive agents (patches)
		for (i = 0; i < radiusSize; i++) {
			for (a = 0; a < spaceSize; a++) {
				for (b = 0; b < spaceSize; b++) {
					if (aCondition.isThereAnlnactiveAgentAtList(space, a, b)) {
						dx = Math.pow( (a - (halfSpaceSize)), 2);
						dy = Math.pow( (b - (halfSpaceSize)), 2);
						r = Math.sqrt(dx + dy);
						if (r <= i)
							massArrayTotal[i]++;
					} //if aCondition
				} //for y
			} //for x
		}// end for 50 (i)
		//density of red inactive agents (patches)
		for (i = 0; i < radiusSize; i++){
			for (a = 0; a < spaceSize; a++){
				for (b = 0; b < spaceSize; b++){
					if (aCondition.isThereRedlnactive(space, a, b)){
						dx = Math.pow((a - halfSpaceSize), 2);
						dy = Math.pow((b - halfSpaceSize), 2);
						r = Math.sqrt(dx + dy);
						if (r <= i){
							massArrayRed[i]++;
						}
					}//if aCondition
				}//for y
			}//for x
		}//for i
		//density of yellow inactive agents (patches)
		for (i = 0; i < radiusSize; i++){
			for (a = 0; a < spaceSize; a++){
				for (b = 0; b < spaceSize; b++){
					if (aCondition.isThereYellowlnactive(space, a, b)){
						dx = Math.pow((a - halfSpaceSize), 2);
						dy = Math.pow((b - halfSpaceSize), 2);
						r = Math.sqrt(dx + dy);
						if (r <= i){
							massArrayYellow[i]++;
						}
					}//if aCondition
				}//for y
			}//for x
		}//for i
		//density of blue inactive agents (patches)
		for (i = 0; i < radiusSize; i++){
			for (a = 0; a < spaceSize; a++){
				for (b = 0; b < spaceSize; b++){
					if (aCondition.isThereBluelnactive(space, a, b)){
						dx = Math.pow((a - halfSpaceSize), 2);
						dy = Math.pow((b - halfSpaceSize), 2);
						r = Math.sqrt(dx + dy);
						if (r <= i){
							massArrayBlue[i]++;
						}
					}//if aCondition
				}//for y
			}//for x
		}//for i
		//density of cyan inactive agents (patches)
		for (i = 0; i < radiusSize; i++){
			for (a = 0; a < spaceSize; a++){
				for (b = 0; b < spaceSize; b++){
					if (aCondition.isThereCyanlnactive(space, a, b)){
						dx = Math.pow((a - halfSpaceSize), 2);
						dy = Math.pow((b - halfSpaceSize), 2);
						r = Math.sqrt(dx + dy);
						if (r <= i){
							massArrayCyan[i]++;
						}
					}//if aCondition
				}//for y
			}//for x
		}//for i
		//density of cyan inactive agents (patches)
		for (i = 0; i < radiusSize; i++){
			for (a = 0; a < spaceSize; a++){
				for (b = 0; b < spaceSize; b++){
					if (aCondition.isThereGreenlnactive(space, a, b)){
						dx = Math.pow((a - halfSpaceSize), 2);
						dy = Math.pow((b - halfSpaceSize), 2);
						r = Math.sqrt(dx + dy);
						if (r <= i){
							massArrayGreen[i]++;
						}
					}//if aCondition
				}//for y
			}//for x
		}//for i
		//print results in a file
		try {
			outputDensity.writeBytes("i, massa total dos inativos, red, yellow, blue, cyan, green \n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (i = 0; i < radiusSize; i++) {
			try{
				outputDensity.writeBytes(i + ","+ massArrayTotal[i] + "," + massArrayRed[i] + ","+
						massArrayYellow[i] + "," + massArrayBlue[i] + "," + massArrayCyan[i] + ","+
				massArrayGreen[i] +"\n"); //aqui ele ta gravando no arquivo
			}
			catch(IOException ioException){
				System.out.println("Cannot save result");
			}
		}
		try{
			outputDensity.close(); //este comando fecha o arquivo
		}
		catch(IOException ioException){
			System.out.println("Cannot close file");
		}
	}// end method

	public double calculateMassPerTime(){
		int i;
		double derivative;
		int[] massPerTime = new int[200000];
		i = (int) this.getTickCount();
		massPerTime[i] = this.countNumInactiveAgents();
		derivative = massPerTime[i] - massPerTime[i -1];
		return derivative;
	}
	//number of red active agents
	public double CountNumActiveRedAgents () {
		double count = 0;
		Agent ag1;
		for (int i = 0; i < agentList.size(); i++) {
			ag1 = (Agent) agentList.get(i);
			if (ag1.getActive() && (ag1.getCodeColor()== 'r'))
				count++;
		}
		return count;
	}
	//number of yellow active agents
	public double countNumActiveYellowAgents () {
		double count = 0;
		Agent ag1;
		for (int i = 0; i < agentList.size(); i++) {
			ag1 = (Agent) agentList.get(i);
			if (ag1.getActive() && (ag1.getCodeColor() == 'y'))
				count++;
		}
		return count;
	}
	//number of blue active agents
	public double countNumActiveBlueAgents (){
		double count = 0;
		Agent ag1;
		for (int i = 0; i < agentList.size(); i++) {
			ag1 = (Agent) agentList.get(i);
			if (ag1.getActive() && (ag1.getCodeColor() == 'b'))
				count++;
		}
		return count;
	}

	////////////////SPATIAL CONSTRAINTS
//	@SuppressWarnings("deprecation")
	public void readMatrix(String filename) {
		int matrixSize = spaceSize;
		DataInputStream input;
		String inputLine, element;
		StringTokenizer tokenizedLine;
		int line = 0;
		int column = 0;
		int[][] matrix = new int[matrixSize][matrixSize];
		try {
			input = new DataInputStream(new FileInputStream(filename));
			while ( (inputLine = input.readLine()) != null) {
				tokenizedLine = new StringTokenizer(inputLine);
				while (column < matrixSize){
					element = tokenizedLine.nextToken();
					matrix [column][line] = Integer.parseInt(element);
					column++;
				}
				line++;
				column = 0;
			}
			//create grey inactive agents at each point of matrix = 1
			for (int i = 0; i < matrixSize ; i++){
				for (int j = 0; j< matrixSize; j++){
					//System.out.print(matrix[i][j]+"");
					if (matrix[i][j] == 1){
						createGreyAgentAt(i, j);
					}
					if (matrix[i][j] == 2){
						createRedAgentAt(i, j);
					}
				}
			//System .out.println();
			}
		}
		catch (IOException e) {
			System.err.println("Cannot read matrix\n" + e.toString());
		}
	}

	public void createGreyAgentAt (int a, int b){
		int index = agentList.size()+ 1;
		Agent greyAgent = new Agent (space, index, this);
		//System.out.printlnfgrey agent created at “+ a + + b );
		greyAgent.setXY(a, b);
		greyAgent.setActive(false);
		greyAgent.setColor(Color.darkGray);
		greyAgent.setCodeColor('d');
		agentList.add(greyAgent);
		//System.out.println(greyAgent.getWho() +" cinza inativo");
	}

	public void createRedAgentAt (int a, int b){
		int index = agentList.size()+ 1;
		Agent redAgent = new Agent (space, index, this);
		//System.out.printlnfgrey agent created at"+ a + + b );
		redAgent.setXY(a, b);
		redAgent.setActive(false);
		redAgent.setColor(Color.red);
		redAgent.setCodeColor('r');
		agentList.add(redAgent);
	//System.out.println(greyAgent.getWho() +" cinza inativo");
	}
	//////END SPATIAL CONSTRAINTS
	////EXPORT MATRIX ASCIIGRID
	public void exportASCIIfile(){
		int[][] fragstatsMatrix = new int[spaceSize][spaceSize];
		for (int x = 0; x < spaceSize; x++) {
			for (int y = 0; y < spaceSize; y++) {
				//create fragstatsMatrix:
				if (aCondition.isThereAnlnactiveAgentAtList(space, x, y) == true){
					Agent inactiveHere = aCondition.getAnlnactiveAgentAtList(space, x, y);
					char colorHere = inactiveHere.getCodeColor();
					if (colorHere == 'r') fragstatsMatrix[x][y] = 1;
					if (colorHere == 'y') fragstatsMatrix[x][y] = 2;
					if (colorHere == 'b') fragstatsMatrix[x][y] = 3;
					if (colorHere == 'c') fragstatsMatrix[x][y] = 4;
					if (colorHere == 'g') fragstatsMatrix[x][y] = 5;
					//if cell is grey (spatial constraint), treat it as background:
					if (colorHere == 'd') fragstatsMatrix[x][y] = 9;
				}
				else {
					fragstatsMatrix[x][y] = 9;
				}
			} //for x
		}//for y
		//open file
		double time = this.getTickCount();

//		String namem = path2  + log + "_"+ "fragstats_"+time+".txt";
		String namem;
//		outpath
//		int log2 = Integer.parseInt(log);
		if (getFim() == 1) {
			String path2 = System.getProperty("user.dir");
			if (log < 10) {
//				namem = path2 + "\\" + lista.get(archive) + "\\"  + lista.get(archive) + "_0" + String.valueOf(log) + ".txt";
				namem = path2 + "/output/" + "matrix_0" + String.valueOf(log) + ".txt";
//				namem = "matrix_0" + String.valueOf(log) + ".txt";

			}
			else {
//				namem = path2 + "\\" + lista.get(archive) + "\\"  + lista.get(archive) + "_" + String.valueOf(log) + ".txt";
				namem = path2 + "/output/" + "matrix_" + String.valueOf(log) + ".txt";
//				namem = "matrix_" + String.valueOf(log) + ".txt";

			}
		}
		else {
			if (log < 10) {
//				namem = outpath  + "_fragstats_" + time+ "_0" + String.valueOf(log) + ".txt";
				namem = "fragstats_" + time+ "_0" + String.valueOf(log) + ".txt";

			}
			else {
//				namem = outpath + "_fragstats_" +time +"_" + String.valueOf(log) + ".txt";
				namem = "fragstats_" + time+ "_" + String.valueOf(log) + ".txt";

			}
		}
		try {
			outputASCIIGRID = new DataOutputStream(new
//			FileOutputStream("/src/results/fragstats.txt"));

			FileOutputStream(namem));

		}
		catch ( IOException e) {
			System.err.println("Error during output\n");
			System.exit(1);
		}
		//print matrix to
		for (int i = 0; i < spaceSize; i++){
			for (int j = 0; j < spaceSize; j++) {
				try {
					outputASCIIGRID.writeBytes(fragstatsMatrix[i][j] + " ");
				}
				catch (IOException ioException) {
					System.out.println("Cannot save result");
				}
			} //for j
			//new line after each
			try {
				outputASCIIGRID.writeBytes("\n");
			}
			catch (IOException ioException) {
				System.out.println("Cannot save result");
			}
		}//for y
	}// export ASCIIfile method


	// a required method
	public Schedule getSchedule() {
		return schedule;
	}
	// a required method - displayed on the Controller toolbar
	public String getName() {
		return "Model";
	}

	public static void main(String[] args) {
		String arg=args[2];
		String t=args[1];
		System.out.println(arg);
		SimInit init = new SimInit();
		Model model = new Model();
		init.loadModel(model,arg,true);

	}


}
