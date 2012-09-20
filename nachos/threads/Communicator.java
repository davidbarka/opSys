package nachos.threads;

import nachos.machine.*;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */
public class Communicator {
	
	private Condition2 canSpeak, canListen, canLeave;
	private Lock lock;
	private int buffer;
	private boolean full;
	
    /**
     * Allocate a new communicator.
     */
    public Communicator() {
    	lock = new Lock();
    	
    	canSpeak = new Condition2(lock);
    	canListen = new Condition2(lock);
    	canLeave = new Condition2(lock);
    	
    	full = false;
    }

    /**
     * Wait for a thread to listen through this communicator, and then transfer
     * <i>word</i> to the listener.
     *
     * <p>
     * Does not return until this thread is paired up with a listening thread.
     * Exactly one listener should receive <i>word</i>.
     *
     * @param	word	the integer to transfer.
     */
    public void speak(int word) {
		// wait until the lock is free and then acquire it
    	lock.acquire();
    	
    	// if a thread has already spoken, no thread may speak again until 
    	// some other thread listens for the spoken word
    	while(full) {
    		canSpeak.sleep();
    	}
    	
    	buffer = word;
    	full = true;
    	
    	System.out.println(KThread.currentThread().getName() + " speaks word " + word);
    	
    	// the buffer now contains a spoken word
    	// it's time to wake up listener threads
    	canListen.wakeAll();
    	canLeave.sleep();
    	
    	// set the lock to free
    	lock.release();
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */    
    public int listen() {
		int result;

		// wait until the lock is free and then acquire it
		lock.acquire();
    	
		// if no thread have spoken, ergo no word in the buffer
		// the listener threads will have to wait until a speaker thread has spoken
		while(!full) {
			canSpeak.wakeAll();
			canListen.sleep();
		}
		
		result = buffer;
		full = false;
		
		System.out.println(KThread.currentThread().getName() + " listens to word " + result);
		
		// the result now contains the spoken word from the buffer
		// speaker threads may now speak again
		canLeave.wakeAll();
		canSpeak.wakeAll();
		
		// set the lock to free
		lock.release();
		
		return result;
    }
}
