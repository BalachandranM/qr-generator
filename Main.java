package com;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.Semaphore;

public class Main {

	public static void main(String[] args) {

		System.out.println("This is a sample!");

		byte id = 50;
		short id1 = 50;
		int id2 = 1;
		long val = 1000000;
		char gender = 'M';
		float height = 160.1f;
		double height1 = 160.1;
		boolean differentlyAbled = false;

		System.out.println(String.valueOf(id) + ":::" + String.valueOf(id1) + ":::" + String.valueOf(id2) + ":::"
				+ String.valueOf(val) + ":::" + String.valueOf(gender) + ":::" + String.valueOf(height) + ":::"
				+ String.valueOf(height1) + ":::" + String.valueOf(differentlyAbled));

		GenderMin g1 = GenderMin.M;
		String gender1 = g1.toString();

		System.out.println(g1);
		System.out.println(gender1);

		Gender g2 = Gender.M;
		String gender2 = g2.toString();

		System.out.println(g2);
		System.out.println(g2.getName());
		System.out.println(gender2);

		System.out.println(id1 + id2);
		System.out.println(id1 - id2);
		System.out.println(id1 * id2);
		System.out.println(id1 / id2);
		System.out.println(id1 % id2);
		System.out.println(id1 + id2);

		int[] intArr = new int[8];
		intArr[0] = 1;
		intArr[1] = 2;
		intArr[2] = 3;
		intArr[3] = 4;
		intArr[4] = 5;
		intArr[5] = 6;
		intArr[6] = 7;
		intArr[7] = 8;
//		intArr[8] = 9;
		System.out.println(intArr); // this line will not be executed due to exception

		int x = 0;
		while (x < 5) {
			System.out.println("while" + intArr[x]);
			x++; // show infinite loop
		}

		int y = 0;
		do {
			System.out.println("do while" + intArr[y]);
			y++; // show infinite loop
		} while (y < 5);

		for (int i = 0; i < 5; i++) {
			System.out.println("for" + intArr[i]);
		}

		for (int i : intArr) {
			System.out.println("enhanced for" + i);
		}

		System.out.println("if::::::::::::::::::::");
		for (int i : intArr) {
			if (i == 7) {
				System.out.println("7 is present");
			} else if (i == 8) {
				System.out.println("8 is present");
			} else {
				System.out.println("no match");
			}
		}
		System.out.println("::::::::::::::::::::");
		System.out.println("switch::::::::::::::::::::");
		for (int i : intArr) {
			switch (i) {
			case 7:
				System.out.println("7 is present");
				break;
			case 8:
				System.out.println("8 is present");
				break;
			default:
				System.out.println("no match");
				break;
			}
		}
		System.out.println("::::::::::::::::::::");

		System.out.println("class::::::::::::::::::::");
		User u = new User();
		u.id = 1;
		u.name = "hero";
		u.gender = 'M';
//		u.className = "1";
		u.alpha = false;
		System.out.println(u);
		System.out.println(u.id);
		System.out.println("::::::::::::::::::::");

		System.out.println("inh::::::::::::::::::::");
		Student s = new Student();
		s.id = 1;
		s.name = "hero";
		s.gender = 'M';
		s.className = "1";
		s.alpha = false;
		System.out.println(s);
		System.out.println(s.id);

		Teacher t = new Teacher();
		t.id = 1;
		t.name = "hero";
		t.gender = 'M';
		t.alpha = false;
		t.experienceYrs = 5;
		t.subject = "ENG";
		System.out.println(t);
		System.out.println(t.id);
		System.out.println("::::::::::::::::::::");

		System.out.println("Intf::::::::::::::::::::");
		IUser iUser = t;
		System.out.println(iUser);
		System.out.println("::::::::::::::::::::");

		System.out.println("ML::::::::::::::::::::");
		Student s1 = new Student();
		s1.id = 1;
		s1.name = "a";
		Student s2 = new Student();
		s2.id = 2;
		s2.name = "b";
		Student s3 = new Student();
		s3.id = 3;
		s3.name = "c";

		Teacher t1 = new Teacher();
		t1.id = 1;
		t1.name = "a";
		Teacher t2 = new Teacher();
		t2.id = 2;
		t2.name = "b";
		Teacher t3 = new Teacher();
		t3.id = 3;
		t3.name = "c";

//		CountDownLatch latch = new CountDownLatch(6);
		Semaphore sem = new Semaphore(1);
		StudentProcessor sp1 = new StudentProcessor();
		sp1.stu = s1;
//		sp1.latch = latch;
		sp1.sem = sem;
		StudentProcessor sp2 = new StudentProcessor();
		sp2.stu = s2;
//		sp2.latch = latch;
		sp2.sem = sem;
		StudentProcessor sp3 = new StudentProcessor();
		sp3.stu = s3;
//		sp3.latch = latch;
		sp3.sem = sem;
		TeacherProcessor tp1 = new TeacherProcessor();
		tp1.tchr = t1;
//		tp1.latch = latch;
		tp1.sem = sem;
		TeacherProcessor tp2 = new TeacherProcessor();
		tp2.tchr = t2;
//		tp2.latch = latch;
		tp2.sem = sem;
		TeacherProcessor tp3 = new TeacherProcessor();
		tp3.tchr = t3;
//		tp3.latch = latch;
		tp3.sem = sem;

		Processor[] prcrs = new Processor[] { sp1, sp2, sp3, tp1, tp2, tp3 };
		int thrdCt = prcrs.length;
		List<Thread> thrds = new ArrayList<>();
		for (int i = 0; i < thrdCt; i++) {
			Thread trd = new Thread(prcrs[i]);
			trd.start();
			thrds.add(trd);
		}

		for (Thread trd : thrds) {
			try {
				trd.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

//		try {
//			Thread.sleep(10000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

//		try {
//			latch.await();
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		System.out.println("::::::::::::::::::::");

		System.out.println("CLC::::::::::::::::::::");
		Vector<User> vec = new Vector<>();
		vec.add(s1);
		vec.add(s2);
		vec.add(s2);
		vec.add(s3);
		System.out.println(vec);
		System.out.println(thrds);
		Set<User> set = new HashSet<>();
		set.add(s1);
		set.add(s2);
		set.add(s2);
		set.add(s3);
		System.out.println(set);
		System.out.println("::::::::::::::::::::");

		System.out.println("SRLN::::::::::::::::::::");

		Student s11 = new Student();
		s11.id = 1;
		s11.name = "a";
		String fileName = "stu.txt";

		FileOutputStream file;
		try {
			file = new FileOutputStream(fileName);
			ObjectOutputStream out = new ObjectOutputStream(file);
			out.writeObject(s11); // show transient
			out.close();
			file.close();
			System.out.println("Object has been serialized");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Student s111 = null;
		try {
			FileInputStream rFile = new FileInputStream(fileName);
			ObjectInputStream in = new ObjectInputStream(rFile);
			s111 = (Student) in.readObject();
			in.close();
			rFile.close();
			System.out.println("Object has been deserialized ");
			System.out.println(s111.id);
			System.out.println(s111.name);
		}

		catch (IOException | ClassNotFoundException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}

		System.out.println("::::::::::::::::::::");
	}

}
