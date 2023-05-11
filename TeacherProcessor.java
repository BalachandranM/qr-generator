package com;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

public class TeacherProcessor implements Processor {
	Teacher tchr;
	CountDownLatch latch;
	Semaphore sem;
	
	@Override
	public void run() {
		if (tchr != null) {
			System.out.println("processing Teacher: " + tchr.name);
//			try {
//				Thread.sleep(10000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			if (latch != null)
				latch.countDown();
			
			if (sem != null) {
				try {
					sem.acquire();
					System.out.println("aquired semaphore lock for teacher " + tchr.id);
					Thread.sleep(1000);
					sem.release();
					System.out.println("released semaphore lock for teacher " + tchr.id);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

}
