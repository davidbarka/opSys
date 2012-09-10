package nachos.threads;

import java.util.LinkedList;
import nachos.machine.*;

/**
 * An implementation of condition variables that disables interrupt()s for
 * synchronization.
 *
 * <p>
 * You must implement this.
 *
 * @see	nachos.threads.Condition
 */
public class Condition2 {
	/**
	 * Allocate a new condition variable.
	 *
	 * @param	conditionLock	the lock associated with this condition
	 *				variable. The current thread must hold this
	 *				lock whenever it uses <tt>sleep()</tt>,
	 *				<tt>wake()</tt>, or <tt>wakeAll()</tt>.
	 */
	public Condition2(Lock conditionLock) {
		this.conditionLock = conditionLock;
		
		// initializers
		awaitingThreads = new LinkedList<KThread>();
		interruptState = false; 
	}

	/**
	 * Atomically release the associated lock and go to sleep on this condition
	 * variable until another thread wakes it using <tt>wake()</tt>. The
	 * current thread must hold the associated lock. The thread will
	 * automatically reacquire the lock before <tt>sleep()</tt> returns.
	 */
	public void sleep() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());

		KThread current = KThread.currentThread();
		awaitingThreads.add(current); // add the current thread to a list of waiters

		conditionLock.release();
		
		interruptState = Machine.interrupt().disable();
		
		KThread.sleep();
		
		Machine.interrupt().restore(interruptState);
		
		// when the thread is woken up, it will try to re-acquire the lock
		conditionLock.acquire();
	}

	/**
	 * Wake up at most one thread sleeping on this condition variable. The
	 * current thread must hold the associated lock.
	 */
	public void wake() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());
		if(!awaitingThreads.isEmpty()) {
			interruptState = Machine.interrupt().disable();
			
			// remove the first thread from the waiter queue and set it to ready
			awaitingThreads.removeFirst().ready(); 
			
			Machine.interrupt().restore(interruptState);
		}
		
	}

	/**
	 * Wake up all threads sleeping on this condition variable. The current
	 * thread must hold the associated lock.
	 */
	public void wakeAll() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());

		if(!awaitingThreads.isEmpty()) {
			interruptState = Machine.interrupt().disable();
			
			// remove all threads from the waiter queue and set them to ready
			for(int i=0;i<awaitingThreads.size();i++) {
				awaitingThreads.remove(i).ready();
			}
			
			Machine.interrupt().restore(interruptState);
		}

	}

	private Lock conditionLock;
	private boolean interruptState;
	private LinkedList<KThread> awaitingThreads;
}
