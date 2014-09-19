package com.tradingfun.fix.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cameronsystems.fix.exception.CryptoException;
import com.cameronsystems.fix.message.FIXMessageAsIndexedByteArray;
import com.cameronsystems.fix.message.IFIXMessage;
import com.cameronsystems.fix.message.IMessageTransformer;
import com.cameronsystems.fix.message.IToMessageTransformer;
import com.cameronsystems.fix.message.StandardFixTransformer;
import com.cameronsystems.fix.socketadapter.SocketMessage;
import com.cameronsystems.util.data.BytesSegment;

public class SocketAdaptor {

	private static final Logger _logger = LoggerFactory.getLogger(SocketAdaptor.class);

	private InputStream inputStream_;
	private OutputStream outStream_;
	private Socket socket_;
	private String hostname_;
	private int port_;
	private boolean ack_ = false;

	private IToMessageTransformer transformer_;
	private IMessageTransformer msgTranformer;

	private LinkedBlockingQueue<IFIXMessage> blockingQueue = null;

	private volatile boolean isRunning = true;
	
	private Set<ISocketAdaptorListener> listenerSet = new CopyOnWriteArraySet<ISocketAdaptorListener>();
	private Thread msgThread;
	private Thread connectionThread;
	
	
	public SocketAdaptor(String hostname, int port) {
		this.hostname_ = hostname;
		this.port_ = port;
	}

	
	public synchronized void initialize() {
		_logger.info("Initializing of SocketAdaptor");
		
		isRunning = true;
		transformer_ = new StandardFixTransformer();
		msgTranformer = new StandardFixTransformer();
		
		blockingQueue = new LinkedBlockingQueue<IFIXMessage>();
		
		//start connection handler thread
		connectionThread = new Thread(new ConnectionController());
		connectionThread.setDaemon(true);
		connectionThread.start();
		
		//start message processing thread
		msgThread = new Thread(new MsgProcessor());
		msgThread.setDaemon(true);
		msgThread.start();

	}
	
	public synchronized void dispose() {
		_logger.info("Disposing of SocketAdaptor");
		isRunning = false;
		
		connectionThread.interrupt();
		try {
			connectionThread.join();
		} catch (InterruptedException e) {
		}
		
		msgThread.interrupt();
		
		try {
			msgThread.join();
		} catch (InterruptedException e) {
		}
		
		disconnect();
		
		//notify the disposal
		for (ISocketAdaptorListener listener : listenerSet) {
			listener.onDisposed();
		}
		
		_logger.info("SocketAdaptor disconnected");
	}

	/**
	 * Connect to the socket adapter.
	 * 
	 * @throws Exception
	 *             if there is a problem connecting to the socket adapter.
	 */
	private void connect() throws Exception {
		String _logger_method = "connect";
		if (_logger.isDebugEnabled()) {
			_logger.debug("> " + _logger_method);
		}

		if (socket_ != null) {
			try {
				socket_.close();
			} catch (Exception e) {
				// ignore
			}
		}
		
		socket_ = new Socket(hostname_, port_);
		socket_.setTcpNoDelay(true);
		outStream_ = new BufferedOutputStream(socket_.getOutputStream());
		inputStream_ = new BufferedInputStream(socket_.getInputStream());
		
		_logger.info("SocketAdaptor: Finish Connect method:");

		if (_logger.isDebugEnabled()) {
			_logger.debug("< " + _logger_method);
		}
	}

	/**
	 * Disconnect from the socket adapter.
	 */
	private void disconnect() {
		String _logger_method = "disconnect";
		if (_logger.isDebugEnabled()) {
			_logger.debug("> " + _logger_method);
		}

		try {

			if (socket_ != null) {
				socket_.close();
			}

			try {
				if (inputStream_ != null)
					inputStream_.close();
			} catch (IOException ioe) {
			}

			try {
				if (outStream_ != null)
					outStream_.close();
			} catch (IOException ioe) {
			}

		} catch (Exception f) {
			_logger.warn("Error closing socket.", f);
		}
		socket_ = null;

		if (_logger.isDebugEnabled()) {
			_logger.debug("< " + _logger_method);
		}
	}

	/**
	 * Find out if the client is connected to the server.
	 * 
	 * @return true if the client is connected to the server, false otherwise.
	 */
	public boolean isConnected() {
		return (socket_ != null && socket_.isConnected());
	}
	
	
	private class ConnectionController implements Runnable {

		/**
		 * Run the thread loop that reconnects the client to the socket adapter
		 * and reads messages from it.
		 */
		@Override
		public void run() {
			String _logger_method = "run";
			if (_logger.isDebugEnabled()) {
				_logger.debug("> " + _logger_method);
			}

			while (isRunning) {
				if (!isConnected()) {
					try {
						
						//notify the connection status
						for (ISocketAdaptorListener listener : listenerSet) {
							listener.onConnectionDown();
						}
						
						connect();
						
						//notify the connection status
						for (ISocketAdaptorListener listener : listenerSet) {
							listener.onConnectionUp();
						}
						
						_logger.info("Client connected to: " + hostname_ + ":" + port_);
					} catch (Exception ex) {
						_logger.error("Error attempting to connect.", ex);
						try {
							Thread.sleep(1000);
						} catch (InterruptedException ie) {
							// ignore
						}
					}
				} else {
					try {
						SocketMessage msg = SocketMessage.readMessage(inputStream_);

						// Process the message
						switch (msg.type_) {

						case SocketMessage.RECV_MESSAGE:
							// _logger.debug("Message received: " +
							// msg.getAsString());
							IFIXMessage ifixmessage = new FIXMessageAsIndexedByteArray();
							ifixmessage = transformer_.toMessage(msg.getBytesSegment(), ifixmessage, true);
							blockingQueue.offer(ifixmessage);
							// MsgProcessorThread mpt = new MsgProcessorThread(
							// blockingQueue.take() );
							// mpt.start();
							if (ack_) {
								msg.type_ = SocketMessage.SEND_ACK;
								msg.writeMessage(outStream_);
							}
							break;

						case SocketMessage.RECV_ACK:
							break;

						case SocketMessage.RECV_NACK:
							_logger.info("Received nack. id: " + msg.id_ + " - " + msg.getAsString());
							break;

						case SocketMessage.RECV_EXCEPTION:
							String exceptionMsg = new String(msg.getAsString());
							_logger.info("Received exception: " + exceptionMsg);
							break;

						case SocketMessage.RECV_RESET_MSG:
							_logger.info("Received reset: " + msg.getAsString());
							break;

						case SocketMessage.RECV_CONNECTION_STATUS:
							_logger.info("Received connection status: " + msg.getAsString());
							break;

						default:
							throw new Exception("Received message of unknown type. message: " + msg);
						}
					} catch (Exception e) {
						_logger.error("Error processing messages. " + e);
						disconnect();
					}
				}
			}

		}
	}

	/**
	 * Send a message a given FIX session.
	 * 
	 * @param target
	 *            the name of the destination session.
	 * @throws Exception
	 *             if there is a problem sending the message.
	 */
	public void sendMessage(IFIXMessage message) throws Exception {
		String _logger_method = "sendMessage";
		if (_logger.isDebugEnabled()) {
			_logger.debug("> " + _logger_method);
		}

		try {
			BytesSegment data = (BytesSegment) msgTranformer.toObject(message, null);
			SocketMessage sm = new SocketMessage(SocketMessage.SEND_MESSAGE, data);
			sm.writeMessage(outStream_);

		} catch (IOException e) {
			_logger.warn("IOException", e);
			disconnect();
		} catch (IllegalArgumentException e) {
			_logger.warn("IllegalArgumentException", e);
		} catch (UnsupportedOperationException e) {
			_logger.warn("UnsupportedOperationException", e);
		} catch (CryptoException e) {
			_logger.warn("CryptoException", e);
		}

	}

	/**
	 * Thread used to process incoming messages
	 * 
	 */
	private class MsgProcessor implements Runnable {
		

		@Override
		public void run() {

			String _logger_method = "MsgProcessorThread::run()";
			if (_logger.isDebugEnabled()) {
				_logger.debug("> " + _logger_method);
			}

			while (isRunning) {

				try {

					IFIXMessage message = blockingQueue.take();

					if (message != null) {

						for (ISocketAdaptorListener listener : listenerSet) {
							listener.onMessage(message);
						}
					}

				} catch (InterruptedException e) {
					_logger.warn("MsgProcessor is interrupted", e);

				} catch (Exception ex) {
					_logger.error("MsgProcessor encountered exception while processing an incomming message: ", ex);
				}

			}
			if (_logger.isDebugEnabled()) {
				_logger.debug("< " + _logger_method);
			}
		}
	}
	
	
	public void registerListener(ISocketAdaptorListener listener ) {
		
		if (listener == null) 
			throw new IllegalArgumentException("the listener is null");
		
		listenerSet.add(listener);
		
	}

	
	public void deRegisterListener(ISocketAdaptorListener listener ) {
		
		if (listener == null) 
			throw new IllegalArgumentException("the listener is null");
		
		listenerSet.remove(listener);
		
	}


}
