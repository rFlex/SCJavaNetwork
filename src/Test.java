import me.corsin.javatools.task.TaskQueue;
import me.corsin.jnetwork.gate.INetworkGateListener;
import me.corsin.jnetwork.gate.UDPGate;
import me.corsin.jnetwork.peer.INetworkPeerListener;
import me.corsin.jnetwork.peer.NetworkPeer;
import me.corsin.jnetwork.protocol.TextProtocol;

/////////////////////////////////////////////////
// Project : SCJavaNetwork
// Package : 
// Test.java
//
// Author : Simon CORSIN <simoncorsin@gmail.com>
// File created on Oct 23, 2013 at 4:44:01 PM
////////

public class Test {

	////////////////////////
	// VARIABLES
	////////////////

	////////////////////////
	// CONSTRUCTORS
	////////////////
	
	public static class ChatPeer extends NetworkPeer {
		
		private int iteration;
		private String login;

		public ChatPeer(NetworkPeer networkPeer) {
			super(networkPeer);
			this.setLogin("Player");
		}

		public int getIteration() {
			return iteration;
		}

		public void setIteration(int iteration) {
			this.iteration = iteration;
		}

		public String getLogin() {
			return login;
		}

		public void setLogin(String login) {
			this.login = login;
		}
		
	}

	////////////////////////
	// METHODS
	////////////////
	
	public static void main(String[] args) {
		try {
			final UDPGate gate = new UDPGate(new TextProtocol());
			gate.setListener(new INetworkGateListener() {
				
				@Override
				public void onSent(NetworkPeer peer, Object packet) {
					
				}
				
				@Override
				public void onReceived(NetworkPeer peer, Object packet) {
					if (!gate.isRegistered(peer)) {
						peer = new ChatPeer(peer);
						gate.register(peer);
					}
					
					ChatPeer chatPeer = (ChatPeer)peer;
					
					String str = (String)packet;
					
					if (str.startsWith("\\")) {
						String cmd = str.substring(1);
						
						if (cmd.startsWith("nickname ")) {
							String newNickName = cmd.split(" " )[1];
							
							if (newNickName.endsWith("\n")) {
								newNickName = newNickName.substring(0, newNickName.length() - 1);
							}
							gate.sendToAllRegisteredPeer(chatPeer.getLogin() + " changed login to " + newNickName + "\n");
							chatPeer.setLogin(newNickName);
							
						} else if (cmd.startsWith("exit")) {
							gate.unregister(peer);
						}
					} else {
						gate.sendToAllRegisteredPeer(chatPeer.getLogin() + ": " + str);
						
					}

				}
				
				@Override
				public void onFailedSend(NetworkPeer peer, Object packet, Exception exception) {
					
				}
				
				@Override
				public void onFailedReceive(NetworkPeer peer, Exception exception) {
					
				}
			});
			
			System.out.println("Port:" + gate.getPort());
			
			TaskQueue taskQueue = new TaskQueue();
			
			gate.setCallBackTaskQueue(taskQueue);
			
			while (true) {
				taskQueue.flushTasks();
				Thread.sleep(10);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	////////////////////////
	// GETTERS/SETTERS
	////////////////
}
