package nachos.threads;

import java.util.ArrayList;
import java.util.List;

import nachos.machine.*;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {

	private WaitingThread waitingThread;

	/**
	 * Allocate a new Alarm. Set the machine's timer interrupt handler to this
	 * alarm's callback.
	 *
	 * <p><b>Note</b>: Nachos will not function correctly with more than one
	 * alarm.
	 */
	public Alarm() {
		Machine.timer().setInterruptHandler(new Runnable() {
			public void run() { timerInterrupt(); }
		});
		waitingThread = new WaitingThread();
	}

	/**
	 * The timer interrupt handler. This is called by the machine's timer
	 * periodically (approximately every 500 clock ticks). Causes the current
	 * thread to yield, forcing a context switch if there is another thread
	 * that should be run.
	 */
	public void timerInterrupt() {
		
		waitingThread.wakeUpNextThread();
		KThread.yield();
		//	System.out.println("timerinterupt on " + Machine.timer().getTime() + " ticks");
		//	System.out.println(System.currentTimeMillis());
	}

	/**
	 * Put the current thread to sleep for at least <i>x</i> ticks,
	 * waking it up in the timer interrupt handler. The thread must be
	 * woken up (placed in the scheduler ready set) during the first timer
	 * interrupt where
	 *
	 * <p><blockquote>
	 * (current time) >= (WaitUntil called time)+(x)
	 * </blockquote>
	 *
	 * @param	x	the minimum number of clock ticks to wait.
	 *
	 * @see	nachos.machine.Timer#getTime()
	 */
	public void waitUntil(long x) {
		// for now, cheat just to get something working (busy waiting is bad)
		
		//	while (wakeTime > Machine.timer().getTime())
		//	    KThread.yield();

		Machine.interrupt().disable();
		long wakeTime = Machine.timer().getTime() + x;
		waitingThread.addWaitingThread(KThread.currentThread(), wakeTime);
		KThread.sleep();

	}


	private class WaitingThread{
		
		private class Node{
			long wakeUpTime;
			KThread queuedThread;
			
			public Node(KThread queuedThread, long wakeUpTime){
				this.queuedThread = queuedThread;
				this.wakeUpTime = wakeUpTime;
			}
		}

		List<Node> waitingThreads;

//		long wakeUpTime;
//		KThread queuedThread;

		public WaitingThread(){
			waitingThreads = new ArrayList<Node>();
		}

		public void addWaitingThread(KThread queuedThread, long wakeUpTime){
//			this.queuedThread = queuedThread;
//			this.wakeUpTime = wakeUpTime;
			Node node = new Node(queuedThread, wakeUpTime);
			sortAndInsert(node);
		}

		private void sortAndInsert(Node toInsert){
//			waitingThreads.add(toInsert);
			if(waitingThreads.size()<1){
				waitingThreads.add(toInsert);
			}else{
				int i=0;
				while (toInsert.wakeUpTime>=waitingThreads.get(i).wakeUpTime) {
					i++;
					if(waitingThreads.size()>=i)break;
				}
				waitingThreads.add(i, toInsert);
			}
		}

		public void wakeUpNextThread(){
			if(waitingThreads.isEmpty())return;
			int index =0;
			while(waitingThreads.get(index).wakeUpTime<=Machine.timer().getTime()){
				if(!waitingThreads.get(index).queuedThread.checkIfStatusIsReady()){
					waitingThreads.remove(index).queuedThread.ready();
				}
				index++;
				if(waitingThreads.size()>=index || waitingThreads.isEmpty())return;
			}
//			for(int i=0;i<waitingThreads.size();i++){
//				if(waitingThreads.get(i).wakeUpTime<=Machine.timer().getTime()){
//					KThread tmp = waitingThreads.remove(i).queuedThread;
//					if(!tmp.checkIfStatusIsReady()){
//						tmp.ready();
//					}
//					i--;
//				}
//			}
		}

	}





}
