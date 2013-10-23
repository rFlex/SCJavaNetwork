/////////////////////////////////////////////////
// Project : SCJavaNetwork
// Package : me.corsin.jnetwork.peer
// NetworkPeer.java
//
// Author : Simon CORSIN <simoncorsin@gmail.com>
// File created on Oct 23, 2013 at 11:37:35 AM
////////

package me.corsin.jnetwork.peer;

import java.net.InetSocketAddress;

import me.corsin.jnetwork.gate.NetworkGate;

public class NetworkPeer {

	////////////////////////
	// VARIABLES
	////////////////
	
	final private InetSocketAddress address;
	private NetworkGate gate;
	private INetworkPeerListener listener;
	
	////////////////////////
	// CONSTRUCTORS
	////////////////
	
	public NetworkPeer(String ip, int port) {
		this(ip, port, null);
	}
	
	public NetworkPeer(String ip, int port, NetworkGate gate) {
		this(new InetSocketAddress(ip, port), gate);
	}
	
	public NetworkPeer(InetSocketAddress address) {
		this(address, null);
	}
	
	public NetworkPeer(InetSocketAddress address, NetworkGate gate) {
		this.address = address;
		this.setGate(gate);
		
		if (this.address == null) {
			throw new NullPointerException("address");
		}
	}

	////////////////////////
	// METHODS
	////////////////
	
	public void send(Object packet) {
		if (this.gate == null) {
			throw new NetworkPeerException("Cannot send a packet to a NetworkPeer that is not registered to a NetworkGate");
		}
		this.gate.send(packet, this);
	}
	
	public static int computeHashCodeForAddress(InetSocketAddress address) {
		return computeHashCodeForAddress(address.getAddress().getAddress(), address.getPort());
	}
	
	public static int computeHashCodeForAddress(byte[] ip, int port) {
		long value = 0;
		
		if (ip != null) {
			for (int i = 0; i < ip.length; i++) {
				byte b = ip[i];
				value |= b << (i * 8);
			}
		}
		
		value |= ((long)port) << 32;
		
		return (int)(value ^ (value >>> 32));
	}
	
	public void signalReceived(Object packet, Exception exception) {
		if (this.listener != null) {
			if (exception == null) {
				this.listener.onReceived(this, packet);
			} else {
				this.listener.onFailedReceived(this, exception);
			}
		}
	}
	
	@Override
	public int hashCode() {
		return computeHashCodeForAddress(this.address);
	}

	////////////////////////
	// GETTERS/SETTERS
	////////////////

	public boolean isRegisterable() {
		return this.address.getAddress() != null;
	}
	
	public InetSocketAddress getAddress() {
		return address;
	}

	public NetworkGate getGate() {
		return gate;
	}

	public void setGate(NetworkGate gate) {
		this.gate = gate;
	}

	public INetworkPeerListener getListener() {
		return listener;
	}

	public void setListener(INetworkPeerListener listener) {
		this.listener = listener;
	}
	
	public String getIP() {
		return this.address.getHostName();
	}
	
	public int getPort() {
		return this.address.getPort();
	}
}
