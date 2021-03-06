/////////////////////////////////////////////////
// Project : SCJavaNetwork
// Package : me.corsin.jnetwork.gate
// IGateListener.java
//
// Author : Simon CORSIN <simoncorsin@gmail.com>
// File created on Oct 23, 2013 at 11:20:51 AM
////////

package me.corsin.jnetwork.gate;

import me.corsin.jnetwork.peer.NetworkPeer;

public interface INetworkGateListener {

	void onReceived(NetworkPeer peer, Object packet);
	void onSent(NetworkPeer peer, Object packet);
	void onFailedSend(NetworkPeer peer, Object packet, Exception exception);
	void onFailedReceive(NetworkPeer peer, Exception exception);
	
}
