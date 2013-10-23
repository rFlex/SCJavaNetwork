/////////////////////////////////////////////////
// Project : SCJavaNetwork
// Package : me.corsin.jnetwork.gate
// Gate.java
//
// Author : Simon CORSIN <simoncorsin@gmail.com>
// File created on Oct 23, 2013 at 11:18:41 AM
////////

package me.corsin.jnetwork.gate;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import me.corsin.javatools.task.MultiThreadedTaskQueue;
import me.corsin.javatools.task.TaskQueue;
import me.corsin.jnetwork.peer.NetworkPeer;
import me.corsin.jnetwork.protocol.INetworkProtocol;
import me.corsin.jnetwork.protocol.VoidProtocol;

public abstract class NetworkGate implements Closeable {

	////////////////////////
	// VARIABLES
	////////////////
	
	final private Queue<SendPacket> pendingPacketsToSend;
	final private Map<Integer, NetworkPeer> peers;
	final private TaskQueue queues;
	private INetworkProtocol protocol;
	private boolean closed;
	private TaskQueue callBackTaskQueue;
	private INetworkGateListener listener;
	
	////////////////////////
	// CONSTRUCTORS
	////////////////
	
	public NetworkGate() {
		this(null);
	}
	
	public NetworkGate(INetworkProtocol protocol) {
		this.pendingPacketsToSend = new ArrayDeque<SendPacket>();
		this.peers = new HashMap<Integer, NetworkPeer>();
		this.queues = new MultiThreadedTaskQueue(2);
		this.setProtocol(protocol);
		
		this.queues.executeAsync(new Runnable() {
			public void run() {
				beginRead();
			}
		});
		this.queues.executeAsync(new Runnable() {
			public void run() {
				beginWrite();
			}
		});
	}

	////////////////////////
	// METHODS
	////////////////

	protected abstract ReadPacket readNextPacket() throws IOException;
	protected abstract void sendPacket(InputStream inputStream, InetSocketAddress socketAddress) throws IOException;
	
	private void beginRead() {
		while (!this.closed) {
			Exception exception = null;
			Object deserializedPacket = null;
			NetworkPeer peer = null;
			try {
				final ReadPacket packet = this.readNextPacket();
				if (packet == null) {
					throw new NetworkGateException("The NetworkGate did not return a packet");
				}
				
				peer = this.getPeerForAddress(packet.getSocketAddress());
				deserializedPacket = this.getProtocol().deserialize(packet.getInputStream());
				if (deserializedPacket == null) {
					throw new NetworkGateException("The protocol did not deserialize the packet");
				}
				
			} catch (Exception e) {
				exception = e;
			}
			
			if (this.closed) {
				break;
			}
			
			final Exception thrownException = exception;
			final Object object = deserializedPacket;
			final NetworkPeer thePeer = peer;
			
			if (thePeer != null) {
				this.executeOnAskedQueue(new Runnable() {
					public void run() {
						thePeer.signalReceived(object, thrownException);
					}
				});
			}
			this.executeOnAskedQueue(new Runnable() {
				public void run() {
					if (listener != null) {
						if (thrownException == null) {
							listener.onReceived(thePeer, object);
						} else {
							listener.onFailedReceive(thePeer, thrownException);
						}
					}
				}
			});
		}
	}
	
	private void beginWrite() {
		while (!this.isClosed()) {
			SendPacket packet = null;
			synchronized (this.pendingPacketsToSend) {
				if (this.pendingPacketsToSend.isEmpty()) {
					try {
						this.pendingPacketsToSend.wait();
					} catch (InterruptedException e) {
					}
				}
				if (!this.pendingPacketsToSend.isEmpty()) {
					packet = this.pendingPacketsToSend.poll();
				}
			}
			
			if (packet != null) {
				InputStream inputStream = this.getProtocol().serialize(packet.getPacket());
				
				final SendPacket thePacket = packet;
				try {
					if (inputStream != null) {
						this.sendPacket(inputStream, packet.getPeer().getAddress());
						
						this.executeOnAskedQueue(new Runnable() {
							public void run() {
								if (listener != null) {
									listener.onSent(thePacket.getPeer(), thePacket.getPacket());
								}
							}
						});
					} else {
						throw new NetworkGateException("The protocol did not serialize the packet");
					}
				} catch (final Exception e) {
					this.executeOnAskedQueue(new Runnable() {
						public void run() {
							if (listener != null) {
								listener.onFailedSend(thePacket.getPeer(), e);
							}
						}
					});
				}
			}
		}
	}
	
	private void executeOnAskedQueue(Runnable runnable) {
		if (this.callBackTaskQueue != null) {
			this.callBackTaskQueue.executeAsync(runnable);
		} else {
			runnable.run();
		}
	}
	
	public void send(Object packet, String ip, int port) {
		this.send(packet, InetSocketAddress.createUnresolved(ip, port));
	}
	
	public void send(Object packet, InetSocketAddress remoteAddress) {
		this.send(packet, this.getPeerForAddress(remoteAddress));
	}
	
	public void send(Object packet, NetworkPeer peer) {
		synchronized (this.pendingPacketsToSend) {
			this.pendingPacketsToSend.add(new SendPacket(peer, packet));
			this.pendingPacketsToSend.notifyAll();
		}
	}
	
	/**
	 * Unregister the NetworkPeer from the gate. Every new packet that comes from the IP and port represented by this peer
	 * will result in a new NetworkPeer being allocated
	 * @param peer
	 * @return
	 */
	public boolean unregister(NetworkPeer peer) {
		peer.setGate(null);
		return this.peers.remove(peer.hashCode()) != null;
	}
	
	/**
	 * Register the NetworkPeer to the gate. If another packet comes from the IP and port represented by this peer, this peer object will be reused
	 * instead of allocating a new one
	 * @param peer
	 */
	public void register(NetworkPeer peer) {
		if (!peer.isRegisterable()) {
			throw new NetworkGateException("The NetworkPeer is currently not registerable.");
		}
		this.peers.put(peer.hashCode(), peer);
		peer.setGate(this);
	}
	
	@Override
	public void close() {
		this.closed = true;
		this.queues.dispose();
		synchronized (this.pendingPacketsToSend) {
			this.pendingPacketsToSend.clear();
			this.pendingPacketsToSend.notifyAll();
		}
	}
	
	public NetworkPeer getPeerForAddress(InetSocketAddress socketAddress) {
		NetworkPeer peer = this.peers.get(NetworkPeer.computeHashCodeForAddress(socketAddress));
		
		if (peer == null) {
			peer = new NetworkPeer(socketAddress, this);
		}
		
		return peer;
	}
	
	////////////////////////
	// GETTERS/SETTERS
	////////////////

	public INetworkProtocol getProtocol() {
		return protocol;
	}

	public void setProtocol(INetworkProtocol protocol) {
		if (protocol == null) {
			protocol = new VoidProtocol();
		}
		
		this.protocol = protocol;
	}

	public boolean isClosed() {
		return closed;
	}

	public TaskQueue getCallBackTaskQueue() {
		return callBackTaskQueue;
	}

	public void setCallBackTaskQueue(TaskQueue callBackTaskQueue) {
		this.callBackTaskQueue = callBackTaskQueue;
	}

	public INetworkGateListener getListener() {
		return listener;
	}

	public void setListener(INetworkGateListener listener) {
		this.listener = listener;
	}
}
