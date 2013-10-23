/////////////////////////////////////////////////
// Project : SCJavaNetwork
// Package : me.corsin.jnetwork.gate
// NetworkMessage.java
//
// Author : Simon CORSIN <simoncorsin@gmail.com>
// File created on Oct 23, 2013 at 2:09:15 PM
////////

package me.corsin.jnetwork.gate;

import me.corsin.jnetwork.peer.NetworkPeer;

public class SendPacket {

	////////////////////////
	// VARIABLES
	////////////////

	final private NetworkPeer peer;
	final private Object packet;
	
	////////////////////////
	// CONSTRUCTORS
	////////////////

	public SendPacket(NetworkPeer peer, Object packet) {
		this.peer = peer;
		this.packet = packet;
	}

	////////////////////////
	// METHODS
	////////////////

	////////////////////////
	// GETTERS/SETTERS
	////////////////

	public NetworkPeer getPeer() {
		return peer;
	}

	public Object getPacket() {
		return packet;
	}

}
