package betel.alw3d;

import java.util.Iterator;
import java.util.Set;

import android.util.Log;
import betel.alw3d.renderer.Node;

public class Alw3dSimulator {

	private Alw3dSimulation simulation;

	private Set<Node> nodes;

	private long time;
	private long timeAccumulator = 0;
	
	// Compensate for paused time
	private long timeOffset = 0;
	private long timeOfPauseStart = 0;
	

	// Time passed to the simulation. Doesn't increase while the sim is paused.
	private long simTime = 0;
	private long tick = 0;

	private SimState simState = SimState.STOP;

	public void setSimulation(Alw3dSimulation simulation) {
		this.simulation = simulation;
	}

	public enum SimState {
		STOP, RUN, PAUSE, EXIT;
	}
	
	private Alw3dOnSimulationListener onSimulationListener = null;

	/*Thread simulatorThread;
	private Runnable simulatorRunnable = new Runnable() {
		@Override
		public void run() {

			while (simState != SimState.EXIT) {

				while (simState == SimState.STOP)
					Thread.yield();

				steps();

				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};*/

	public Alw3dSimulator(Set<Node> nodes) {
		this.nodes = nodes;
		//simulatorThread = new Thread(simulatorRunnable, "simulatorThread");
		//simulatorThread.start();
	}

	public void start() {
		time = System.nanoTime();
		simState = SimState.RUN;
	}
	
	public void pause() {
		Log.d(Alw3d.LOG_TAG, "Pausing Simulator.");
		timeOfPauseStart = System.nanoTime();
		simState = SimState.PAUSE;
	}
	
	public void resume() {
		if(simState == SimState.PAUSE) {
			timeOffset += System.nanoTime() - timeOfPauseStart;
			Log.d(Alw3d.LOG_TAG, "Resuming Simulator; timeOffset = " + timeOffset);
			simState = SimState.RUN;
		}
	}

	public void exit() {
		simState = SimState.EXIT;
	}

	public Alw3dSimulation getSimulation() {
		return simulation;
	}

	public void steps() {
		if (simulation != null && simState == SimState.RUN) {
			long newTime = System.nanoTime() - timeOffset;
			timeAccumulator += newTime - time;
			time = newTime;
			
			// TODO: check if the sim runs slow
			long timeStep = simulation.getTimeStep();
			while (timeAccumulator >= timeStep) {
				timeAccumulator -= timeStep;
								
				simulation.setTime(simTime);
				simulation.setRealTime(time - timeAccumulator + timeOffset); // Add back timeOffset to give View real time.

				if(onSimulationListener != null)
					onSimulationListener.onSimulationTick(time - timeAccumulator);
				simulation.beforeProcessingNodes();

				for(Node node : nodes) {
					// TODO: Give array of nodes instead so that the nodes have to be iterated over just once.
					// TODO: Parallel instead of serial. Provide hook up.
					simulation.preProcessNode(node);
					simulation.processNode(node);
				}
				
				simTime += timeStep;
			}
		}
	}

	public Alw3dOnSimulationListener getOnSimulationListener() {
		return onSimulationListener;
	}

	public void setOnSimulationListener(Alw3dOnSimulationListener onSimulationListener) {
		this.onSimulationListener = onSimulationListener;
	}
	
}
