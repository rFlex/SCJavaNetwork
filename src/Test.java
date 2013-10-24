import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;

import sun.awt.event.IgnorePaintEvent;

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

	////////////////////////
	// METHODS
	////////////////
	
	public static void main(String[] args) {
		try {
			UDPGate serverGate = new UDPGate(new TextProtocol());
			serverGate.setListener(new INetworkGateListener() {
				@Override
				public void onSent(NetworkPeer peer, Object packet) {
					
				}
				
				@Override
				public void onReceived(NetworkPeer peer, Object packet) {
					System.out.println("[SERVER] " + packet);
				}
				
				@Override
				public void onFailedSend(NetworkPeer peer, Exception exception) {
					System.out.println("Failed send: " + exception.getMessage());
				}
				
				@Override
				public void onFailedReceive(NetworkPeer peer, Exception exception) {
					System.out.println("Failed receive: " + exception.getMessage());
				}
			});
			System.out.println("Listening to " + serverGate.getPort());
			
			UDPGate clientGate = new UDPGate(new TextProtocol());
			NetworkPeer serverPeer = new NetworkPeer("127.0.0.1", serverGate.getPort());
			serverPeer.setListener(new INetworkPeerListener() {
				
				@Override
				public void onSent(NetworkPeer peer, Object packet) {
					System.out.println("SENT " + packet);
				}
				
				@Override
				public void onReceived(NetworkPeer peer, Object packet) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onFailedSend(NetworkPeer peer, Object packet,
						Exception exception) {
					System.out.println("FAILED SEND " + packet);
				}
				
				@Override
				public void onFailedReceived(NetworkPeer peer, Exception exception) {
				}
			});
			clientGate.register(serverPeer);
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			while (true) {
				String str = reader.readLine();
				
				if (str.equals("quit")) {
					clientGate.close();
					serverGate.close();
					return;
				} else {
					serverPeer.send(str);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	////////////////////////
	// GETTERS/SETTERS
	////////////////
}
