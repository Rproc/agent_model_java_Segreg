package periproject;
import uchicago.src.sim.space.Multi2DTorus;
import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.gui.*;
import uchicago.src.sim.util.Random;
import java.awt.*;
import java.util.ArrayList;
import java.awt.Color.*;



public class Agent implements Drawable {
	//agent parameter
	private int who;
	private Model model;
	private Color color;
	private float agentDirection;
	private boolean active;
	private char codeColor;
	private Conditions aCondition;
	private int x, y;
	private int age = 0;
	private boolean toOutskirts = false;
	private int cons;
	public int steps;
	//space
	public Multi2DTorus space;

	public Agent(Multi2DTorus space, int who, Model model) {
		this.who = who;
		this.space = space;
		this.model = model;
		aCondition = new Conditions();
	}
	////////////// WALK ///////////////
	public void walk() {
		int rd1, rd2, rd3;
		float walkDirection;
		//distance allows agent to walk in a different manner,
		int distance;
		distance = 1;
		//Change the direction of agent
		//option A: 20 degrees to the right, 20 degrees to the left
		rd1 = Random.uniform.nextIntFromTo(0,20);
		rd2 = -(Random.uniform.nextIntFromTo(0,20));
		this.setAgentDirection(rd1 + rd2 + this.agentDirection);
		//option B: set direction random 360
		rd3 = Random.uniform.nextIntFromTo(0,360);//test
		//this.setAgentDirection(rd3+ this.agentDirection);//teste 230 em vez de 20,20
		//this.setAgentDirection(rd3);
		this.forward(distance);
		//option C: not setting agentDirection!
		//walkDirection = this.agentDirection + rd1 + rd2;
		//this.forward(distance, walkDirection);
	}
	/////////////forward
	public void forward(float distance) {
		boolean freeOfContraints = true;
		int count = 0;
		//this method is a loop to avoid spatial constraints
		while (freeOfContraints){
			count++;
			int a, b;
			int newXtoInt;
			int newYtoInt;
			a = this.getX();
			b = this.getY();
			float agentDirectionlnRadians = toRadians(this.agentDirection);
			float newX = (float) (a + distance * Math.cos(agentDirectionlnRadians));
			float newY = (float) (b + distance * Math.sin(agentDirectionlnRadians));
			newXtoInt = Math.round(newX);
			newYtoInt = Math.round(newY);
			int normalizedX = space.xnorm(newXtoInt);
			int normalizedY = space.xnorm (newYtoInt);
			//if there is a spatial contraint at position change direction
			if (aCondition.isThereGrey(space, normalizedX, normalizedY)== true){
				//change direction to random ? — check what is best here!
				float oldDirection = this.getAgentDirection();
				//float rDirRight = Random, uniform. nextlntFromTo(0,25);
				//float rDirLeft = Random.uniform.nextlntFromTo(0, 25);
				//this.setAgentDirection(oldDirection + 180 + rDirRight - rDirLeft);
				float rDir01 = Random.uniform.nextIntFromTo(0, 90);
				float rDir02 = Random.uniform.nextIntFromTo(0, 90);
				this.setAgentDirection(oldDirection +180);
				this.setAgentDirection((this.getAgentDirection() - rDir01 + rDir02)% 360);
			}
			else {
				//set XY at position
				this.setXY(normalizedX, normalizedY);
				//set boolean to false and leave the loop!
				freeOfContraints = false;
			}
		}//while loop
		//to check if wrapping feature of torus is working:
		if (this.x > this.model.getSpaceSize()) 
			System.out.println("x maior que espaco");
		if (this.y > this.model.getSpaceSize()) 
			System.out.println("y maior que espaco");
	}
	///Conversion from degree to radians which is required by the Math.cos methods
	///The Math.toRadians method requires a double, that's why it is not been used here
	float toRadians (float agentDirection) {
		return (float) (agentDirection * Math.PI /180.0);
	}
	////////////FINDSPACEWALK
	public void findSpaceWalk(int numSteps) {
		int rd1, rd2, rd4;
		//Random.createUniform();
		rd1 = Random.uniform.nextIntFromTo(0,25);//40
		rd2 = Random.uniform.nextIntFromTo(0,25);
		rd4 = Random.uniform.nextIntFromTo(0,50);
		//this.setAgentDirection(this.agentDirection + rd1 - rd2);
		this.setAgentDirection(this.agentDirection + rd4);
		this.forward(numSteps);
	}
	/////////////////M
	//assenta o agente na posicao x,y
	public void setXY(int a, int b) {
		if (space.getObjectsAt(this.x, this.y).contains(this))
			space.removeObjectAt(this.x, this.y, this);
		this.setX(a);
		this.setY(b);
		space.putObjectAt(this.x, this.y, this);
	}
	//assenta o agente na posicao x,y
	public void moveToXY (int a, int b) {
		if (space.getObjectsAt(this.x, this.y).contains(this))
			space.removeObjectAt(this.x, this.y, this);
		this.setX(a);
		this.setY(b);
		space.putObjectAt(this.x, this.y, this);
	}


	public void findSpace(int findspaceSteps) {
		char activeAgColor;
		char inactiveAgColor;
		Agent inactiveAg;
		//int steps = 0;
//		System.out.println("agent: "+ this.who + " andando");
		int steps = findspaceSteps;
		while (this.active = true) {
			//defines numSteps either as equal to the parameter findspaceSteps
			// or as a random value between 1 and the parameter value (to avoid regularities)
			//Random Option: //numSteps = Random.uniform.nextlntFromTo(1, findspaceSteps);
			if (this.model.activateOutskirtsrule) {
			//if agent has abandoned a cell before, then it will have findSpaceSteps changed
				if (this.toOutskirts == true) {
					if (this.codeColor == 'r') {
						steps = this.model.findspaceSteps2;
					}
					if (this.codeColor == 'y') {
						steps = this.model.findspaceSteps3;
					}
				} // end if outskirts
			}
			//call findspaceWalk
			this.findSpaceWalk(steps);
			//module 2 -- choose size of steps according to density values
			//if density is bigger, then agent will do findspace again
			if (this.model.activateOutskirtsrule) {
				if (this.codeColor != 'b') {
					boolean n = true;
					while (n == true) {
						int densityHere = this.checkDensity();
						if (densityHere >= this.model.d) {
							if (this.codeColor == 'r') {
								steps = this.model.findspaceSteps2;
							}
							if (this.codeColor == 'y') {
								steps = this.model.findspaceSteps3;
							}
							this.findSpaceWalk(steps);
						} //if density
						else {
							n = false;
						} // else density
					} //if while
				} //if not blue
			}
			activeAgColor = this.getCodeColor();
			int inactivesHere = aCondition.getNumberlnactiveAgentAtList(space, this.x, this.y);
			int rand = Random.uniform.nextIntFromTo(0,8);
			int around = this.checkDensity2();

			if (this.model.occup) {
				if ((inactivesHere == 0)) {
					if (activeAgColor == 'r' || activeAgColor == 'y') {

						if (around <= rand) {
//							System.out.println("no loop, ag: " + this.getWho()+ " around: " + around+ " rand: " + rand);

							this.settleSimple();
//							this.setActive(false);
							break;
						}
					}
					if(activeAgColor == 'b') {
						this.settleSimple();
						break;
					}
				}
				else{
					inactiveAg = aCondition.getAnlnactiveAgentAtList(space, this.getX(), this.getY());
					inactiveAgColor = inactiveAg.getCodeColor();
					if ((activeAgColor == 'r') && (inactiveAgColor != 'r')) {
						if (around <= rand) {
							this.build(activeAgColor, inactiveAgColor, inactiveAg);
							break;
						}
					}
					if ((activeAgColor == 'y') && (inactiveAgColor == 'b')) {
						if (around <= rand) {
							this.build(activeAgColor, inactiveAgColor, inactiveAg);
							break;
						}
					}
					if ((activeAgColor == 'b') && (inactiveAgColor == 'g')) { 
						this.build(activeAgColor, inactiveAgColor, inactiveAg);
						break;
					}
				}//else
			}
			else if (inactivesHere == 0) {
				this.settleSimple();
//				this.setActive(false);
				break;
			}
			else{
				inactiveAg = aCondition.getAnlnactiveAgentAtList(space, this.getX(), this.getY());
				inactiveAgColor = inactiveAg.getCodeColor();
				if ((activeAgColor == 'r') && (inactiveAgColor != 'r')) {
					this.build(activeAgColor, inactiveAgColor, inactiveAg);
					break;
				}
				if ((activeAgColor == 'y') && (inactiveAgColor == 'b')) {
					this.build(activeAgColor, inactiveAgColor, inactiveAg);
					break;
				}
					
				if ((activeAgColor == 'b') && (inactiveAgColor == 'g')) { 
					this.build(activeAgColor, inactiveAgColor, inactiveAg);
					break;
				}
			}//else
		}//while
	}//findspace
	
	public void build (char activeAgColor, char inactiveAgColor, Agent inactiveAg) {
		
		if ((activeAgColor == 'r') && (inactiveAgColor == 'b')) {
			this.expelllnactiveAgent(inactiveAg);
			this.settleSimple();
		}
		if ((activeAgColor == 'r') && (inactiveAgColor == 'y')) {
			this.expelllnactiveAgent(inactiveAg);
			this.settleSimple();
		}
		if ((activeAgColor == 'y') && (inactiveAgColor == 'b')) {
			this.expelllnactiveAgent(inactiveAg);
			this.settleSimple();
		}
		if ((activeAgColor == 'b') && (inactiveAgColor == 'g')) {
			//System.out.println("blue on green");
			this.destroyGreenAgent(inactiveAg);
			this.settleSimple();
		}
	}


	///////////settle simple
	public void settleSimple() {
		if (this.codeColor == 'b'){
			if (this.model.activateConsolidationOld) {
				this.consolidate();
			}
		}
		int inactivesHere;
		inactivesHere = aCondition.getNumberlnactiveAgentAtList(space, this.x, this.y);
		if (inactivesHere == 0) {
			this.setActive(false);
			this.setAge(0);
//			System.out.println("agente#" + this.getWho() + " settled");
		}
	}
	
	//////////ex pelilnactiveAgent
	public void expelllnactiveAgent(Agent inactiveAg) {
		inactiveAg.setActive(true);
		inactiveAg.setAge(0); //added on 10 august
		space.removeObjectAt(inactiveAg.x, inactiveAg.y, inactiveAg);
		inactiveAg.findSpace(model.findspaceSteps);
	}

	public void destroyGreenAgent(Agent greenAg){
		space.removeObjectAt(greenAg.x, greenAg.y, greenAg);
		this.model.getAgentList().remove(greenAg);
	}
	///////////////////////////////////////^^^^
	//methods module 2
	
	public int checkDensity(){
		int densi = 0;
		int i;
		Agent ngbAg;
		ArrayList neighbours = space.getMooreNeighbors(this.x, this.y, false);
		for (i = 0; i < neighbours.size(); i++){
			ngbAg = (Agent) neighbours.get(i);
			if (ngbAg.active == false){ // only inactive neighbours!
				char ngbAgColour = ngbAg.getCodeColor();
				if (ngbAgColour == 'r'){
					densi++;
				}
			}
		}
		return densi;
	}

	public int checkDensity2(){
		int densi = 0;
		int i;
		Agent ngbAg;
		ArrayList neighbours = space.getMooreNeighbors(this.x, this.y, false);
		for (i = 0; i < neighbours.size(); i++){
			ngbAg = (Agent) neighbours.get(i);
			if (ngbAg.active == false){ // only inactive neighbours!
				char ngbAgColour = ngbAg.getCodeColor();
				if ( ngbAgColour == 'b')
					densi++;
				if (ngbAgColour == 'c')
					densi+=2;
				
			}
		}
		return densi;
	}
	public void selectSteps(){
		int densityHere = this.checkDensity();
		if (densityHere >= this.model.d) {
			if (this.codeColor == 'r') 
				steps = this.model.findspaceSteps2;
			if (this.codeColor == 'y') 
				steps = this.model.findspaceSteps3;
		}
		else {
			steps = this.model.findspaceSteps;
		}
	}


// consolidation rule
	public void consolidate(){
		this.setCons(cons + 1);
//		if (cons >= this.model.consLimit) {
//			this.setCodeColor('c');
//			this.setColor(Color.cyan);
//			this.setActive(false);
//		}
	}
	//methods required by drawable interface
	
	public int getX() {
		return this.x;
	}
	public int getY() {
		return this.y;
	}
	public void draw(SimGraphics g) {
		if (this.active == false) // only draw the inactive
			g.drawFastRoundRect(color);
	}
	///////other required methods
	public void setX(int newx) {
		this.x = newx;
	}
	public void setY(int newy) {
		this.y = newy;
	}
	public boolean getActive() {
		return this.active;
	}
	public void setActive(boolean bool) {
		this.active = bool;
	}
	public float getAgentDirection() {
		return this.agentDirection;
	}
	public void setAgentDirection (float value) {
		this.agentDirection = value;
	}
	public int getWho() {
		return this.who;
	}
	public Model getModel() {
		return model;
	}
	public void setCodeColor(char c) {
		this.codeColor = c;
	}
	public char getCodeColor() {
		return this.codeColor;
	}
	public void setColor(Color color) {
		this.color = color;
	}
	public Color getColor() {
		return color;
	}
	public void setAge (int a) {
		this.age = a;
	}
	public int getAge() {
		return this.age;
	}
	public void setToOutskirts (boolean b) {
		this.toOutskirts = b;
	}
	public boolean getToOutskirts() {
		return this.toOutskirts;
	}
	//consolidation Old version variable
	public void setCons (int a) {
		this.cons = a;
	}
	public int getCons() {
		return this.cons;
	}


//////////////////////»
/* //////generates a similar agent and includes it on AgentList
	public Agent generateASimilarAgent(int index) {
	//create new agent and set variables equal to 'this1.
		Agent newAgent = new Agent(space, index, model);
		newAgent.setActive(true);
		newAgent.setColor(this.getColorQ);
		newAgent.setCodeColor(this.getCodeColor());
		//set agentDirection as random
		int r = Random.uniform.nextintFromTo(0,360);
		newAgent.setAgentDirection(r);
		newAgent.setXY( (space.getSizeX() / 2), space.getSizeY() / 2); (0,0)
		this.model.addNumActiveAgents(newAgent);
		return newAgent;
	}
*/
}

