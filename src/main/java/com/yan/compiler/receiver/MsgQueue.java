package com.yan.compiler.receiver;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import com.yan.compiler.Log;
import com.yan.compiler.compiler.CompilerManagement;

public class MsgQueue {
	private static MsgQueue obj = null;

	public static MsgQueue factory() {
		if (null == obj) {
			obj = new MsgQueue();
		}
		return obj;
	}

	/**
	 * udp msg queue.
	 */
	private LinkedList<String> queue = null;
	/**
	 * The worker package queue.
	 */
	private Map<String, LinkedList<BasePackage>> workQueue;

	private MsgQueue() {
		queue = new LinkedList<String>();
		workQueue = new HashMap<String, LinkedList<BasePackage>>();
	}

	/**
	 * Add massage to massage queue. and this method will notify all threads
	 * waiting on {@link #queue}
	 * 
	 * @param msg
	 */
	public void addMsg(String msg) {
		synchronized (queue) {
			queue.addLast(msg);
			queue.notifyAll();
			Log.record(Log.INFO, "Receive", msg);
		}
	}

	/**
	 * Get massage from massage queue. If message is empty, the thread want to
	 * get message will wait on {@link #queue} until a message string has been
	 * add to queue.
	 * 
	 * @return A json string.
	 */
	public String getMsg() {
		String msg = null;
		synchronized (queue) {
			if (queue.size() <= 0) {
				try {
					queue.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			msg = queue.removeFirst();
			Log.record(Log.INFO, "Get Massage", msg);
		}
		return msg;
	}

	/**
	 * Add a {@linkplain BasePackage} to {@link #workQueue}. This method will
	 * notify all threads waiting on a work queue.
	 * 
	 * @param bp
	 */
	public void addPackage(BasePackage bp) {
		String project = bp.getProject();

		synchronized (workQueue) {
			if (!workQueue.containsKey(project)) {
				LinkedList<BasePackage> list = new LinkedList<BasePackage>();
				Log.record(Log.INFO, "Create BasePackage List", project);
				workQueue.put(project, list);
				CompilerManagement manage = CompilerManagement.factory();
				manage.createWorker(project);
			}
		}
		LinkedList<BasePackage> list = workQueue.get(project);
		synchronized (list) {
			list.addLast(bp);
			list.notifyAll();
			Log.record(Log.INFO, "Add Package", bp.toString());
		}
	}

	/**
	 * Get a {@linkplain BasePackage} from work queue. If a queue is empty, the
	 * thread witch will wait until a package has been add to the queue.
	 * 
	 * @param project
	 *            The project the thread wants to get.
	 * @return {@linkplain BasePackage}
	 */
	public BasePackage getPackage(String project) {
		LinkedList<BasePackage> list;
		BasePackage bp = null;
		synchronized (workQueue) {
			list = workQueue.get(project);
		}
		if (null != list) {
			synchronized (list) {
				if (list.size() <= 0) {
					try {
						list.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				}
				bp = list.removeFirst();
				Log.record(Log.INFO, "Get BasePackage", bp.toString());
			}
		}
		return bp;
	}

}
