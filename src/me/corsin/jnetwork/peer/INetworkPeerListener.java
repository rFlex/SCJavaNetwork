/////////////////////////////////////////////////
// Project : SCJavaNetwork
// Package : me.corsin.jnetwork.peer
// INetworkPeerListener.java
//
// Author : Simon CORSIN <simoncorsin@gmail.com>
// File created on Oct 23, 2013 at 2:15:43 PM
////////

package me.corsin.jnetwork.peer;

public interface INetworkPeerListener {

	void onReceived(NetworkPeer peer, Object packet);
	void onFailedReceived(NetworkPeer peer, Exception exception);
	
}
