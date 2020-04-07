package de.enflexit.energyAgent.core;

import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;

/**
 * Thread for starting new agents. If an agent with the same name already exists, the thread will for that agent to terminate.
 *
 * @author Nils Loose - DAWIS - ICB - University of Duisburg-Essen
 */
public class AgentStarterThread extends Thread {
	
	private static final int WAIT_MS = 200;				// Wait that long before trying again
	private static final int MAX_WAIT_MS = 10000;		// Give up after waiting that long
	
	private String agentLocalName;
	private String agentClassName;
	
	private AgentContainer agentContainer;

	/**
	 * Instantiates a new agent starter thread.
	 *
	 * @param agentLocalName the agent local name
	 * @param agentClassName the agent class name
	 * @param agentContainer the agent container
	 */
	public AgentStarterThread(String agentLocalName, String agentClassName, AgentContainer agentContainer) {
		super();
		this.agentLocalName = agentLocalName;
		this.agentClassName = agentClassName;
		this.agentContainer = agentContainer;
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		
		// --- Wait for the original agent to terminate
		int millisWaiting = 0;			// Current waiting time
		boolean success = false;
		do {
			
			try {
				// --- Try to get an AgentController instance for the original agent --------
				@SuppressWarnings("unused")
				AgentController agentControllerOriginalAgent = this.agentContainer.getAgent(this.agentLocalName);
				
				// --- If successful, the agent is not terminated -> wait --------
				Thread.sleep(WAIT_MS);
				millisWaiting += WAIT_MS;
			
			} catch (ControllerException e1) {
				
				// --- Getting the AgentController failed -> the original agent terminated
				try {
					
					// --- Start the proxy agent ------------------
					AgentController agentControllerProxyAgent = this.agentContainer.createNewAgent(this.agentLocalName, this.agentClassName, null);
					agentControllerProxyAgent.start();
					// --- Job done, leave the surrounding loop ----
					success = true;
					
				} catch (StaleProxyException spEx) {
					// --- Starting the agent failed --------------
					System.err.println("Unknown error when trying to start proxy agent for " + this.agentLocalName);
					spEx.printStackTrace();
					
				}
				
			} catch (InterruptedException intEx) {
				intEx.printStackTrace();
			}
			
		} while (millisWaiting<=MAX_WAIT_MS && success==false);
		
		if (millisWaiting > MAX_WAIT_MS && success == false) {
			System.err.println("Error starting agent '" + this.agentLocalName + "': Original agent did not terminate");
		}
		
	}
	

}
