package com.circuits.circuitsmod.common;

public class Log {
	//TODO: for all of these, also include a current timestamp!
	public static void dev(String msg) {
		//TODO: Write out to a log.dev.txt file as well!
		System.out.println("Circuits DEV: " + msg);
	}
	public static void internalError(String msg) {
		//TODO: Write out to a log.internal.txt file as well!
		System.out.println("Circuits Internal ERROR: " + msg);
	}
	
	public static void userError(String msg) {
		//TODO: Write out to a log.user.txt file as well!
		System.out.println("Circuits User ERROR: " + msg);
	}
	
	public static void fatal(String msg) {
		//TODO: Write out to a log.fatal.txt file as well!
		System.out.println("Circuits FATAL: " + msg);
	}
	public static void info(String msg) {
		//TODO: Write out to a log.info.txt file as well!
		System.out.println("Circuits INFO: " + msg);
	}
}
