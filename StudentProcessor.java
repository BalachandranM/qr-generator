package com;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

public class StudentProcessor implements Processor {
	Student stu;
	CountDownLatch latch;
	Semaphore sem;

	@Override
	public void run() {
		if (stu != null) {
			System.out.println("processing student: " + stu.name);
//			try {
//				Thread.sleep(10000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			if (latch != null) {
				System.out
						.println("latching down the count from " + latch.getCount() + " to " + (latch.getCount() - 1));
				latch.countDown();
			}
			if (sem != null) {
				try {
					sem.acquire();
					System.out.println("aquired semaphore lock for student " + stu.id);
					Thread.sleep(1000);
					sem.release();
					System.out.println("released semaphore lock for student " + stu.id);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

}
