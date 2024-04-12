package periproject;
import uchicago.src.sim .space.*;
import java.util.List;
import java.awt.*;
public class Conditions {
	public Conditions() {
	
	}
	//public Multi2DTorus space;
	////checks if the list of objects at a cell contains a GREY agent
	public boolean isThereGrey(Multi2DTorus space, int a, int b) {
		Agent ag;
		List listObjectAtPosition = space.getObjectsAt(a, b);
		int size = listObjectAtPosition.size();
		for (int i = 0; i < size; i++) {
			ag = (Agent) listObjectAtPosition.get(i);
			if ( (ag.getColor() == Color.darkGray)){
				return true;
			}//if
		}//for
		return false;
	}
	////checks if the list of objects at a cell contains a red inactive agent
	public boolean isThereRedlnactive(Multi2DTorus space, int a, int b) {
		Agent ag;
		List listObjectAtPosition = space.getObjectsAt(a, b);
		int size = listObjectAtPosition.size();
		for (int i = 0; i < size; i++) {
			ag = (Agent) listObjectAtPosition.get(i);
			if ( (ag.getColor() == Color.red) && !ag.getActive())
				return true;
			}
		return false;
	}

	////checks if the list of objects at a cell contains a yellow inactive agent
	public boolean isThereYellowlnactive(Multi2DTorus space, int a, int b) {
		Agent ag;
		List listObjectAtPosition = space.getObjectsAt(a, b);
		int size = listObjectAtPosition.size();
		for (int i = 0; i < size; i++) {
			ag = (Agent) listObjectAtPosition.get(i);
			if ( (ag.getColor() == Color.yellow) && !ag.getActive())
				return true;
			}
		return false;
	}
	////checks if the list of objects at a cell contains a blue inactive agent
	public boolean isThereBluelnactive(Multi2DTorus space, int a, int b) {
		Agent ag;
		List listObjectAtPosition = space.getObjectsAt(a, b);
		int size = listObjectAtPosition.size();
		for (int i = 0; i < size; i++) {
			ag = (Agent) listObjectAtPosition.get(i);
			if ( (ag.getColor() == Color.blue) && !ag.getActive())
				return true;
			}
		return false;
	}
	////checks if the list of objects at a cell contains a cyan inactive agent
	public boolean isThereCyanlnactive(Multi2DTorus space, int a, int b) {
		Agent ag;
		List listObjectAtPosition = space.getObjectsAt(a, b);
		int size = listObjectAtPosition.size();
		for (int i = 0; i < size; i++) {
			ag = (Agent) listObjectAtPosition.get(i);
			if ( (ag.getColor() == Color.cyan) && !ag.getActive())
				return true;
			}
		return false;
	}
	////checks if the list of objects at a cell contains a green inactive agent
	public boolean isThereGreenlnactive(Multi2DTorus space, int a, int b) {
		Agent ag;
		List listObjectAtPosition = space.getObjectsAt(a, b);
		int size = listObjectAtPosition.size();
		for (int i = 0; i < size; i++) {
			ag = (Agent) listObjectAtPosition.get(i);
			if ( (ag.getColor() == Color.green) && !ag.getActive()){
				return true;
			}
		}
		return false;
	}
	////checks if the list of objects at a cell contains an inactive agent
	public boolean isThereAnlnactiveAgentAtList(Multi2DTorus space, int a, int b) {
		Agent ag;
		List listObjectAtPosition = space.getObjectsAt(a, b);
		int size = listObjectAtPosition.size();
		for (int i = 0; i < size; i++) {
			ag = (Agent) listObjectAtPosition.get(i);
			if (!ag.getActive()) {
				return true;
			}
		}
		return false;
	}
	////checks if the list of objects at a cell contains an inactive agent and returns this agents
	public Agent getAnlnactiveAgentAtList(Multi2DTorus space, int a, int b) {
		Agent ag;
		List listObjectAtPosition = space.getObjectsAt(a, b);
		for (int i = 0; i < listObjectAtPosition.size(); i++) {
			ag = (Agent) listObjectAtPosition.get(i);
			if (ag.getActive() == false) {
				return ag;
			}
		}
		return null;
	}
	////checks if the list of objects at a cell contains an inactive agent and returns this agents
	public int getNumberlnactiveAgentAtList(Multi2DTorus space, int a, int b) {
		Agent ag;
		int count;
		count = 0;
		List listObjectAtPosition = space.getObjectsAt(a, b);
		for (int i = 0; i < listObjectAtPosition.size(); i++) {
			ag = (Agent) listObjectAtPosition.get(i);
			if (ag.getActive() == false) {
				count++;
			}
		}
		return count;
	}
}