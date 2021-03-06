/**
 * This class and the technique in which it is used in this project 
 * comes from an online lecture which can be found at:
 * http://www.youtube.com/watch?v=7JvmIYjyYYE
 * 
 * Implementation (if not changed from initial commit on 4/16) comes from Chris's 555 hw2
 */

package com.yippee.db.util;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.persist.EntityStore;

public class DbShutdownHook extends Thread {
	
	private Environment env;
	private EntityStore store;
	private String envName;
	
	public DbShutdownHook(Environment e, EntityStore s){
		env = e;
		envName = e.getConfig().getNodeName();
		store = s;
	}
	
	@Override
	public void run(){
		if(env != null){
			try{				
				store.close();
				if(!env.getConfig().getReadOnly()) env.cleanLog();
				env.close();
				System.out.println("Environment closed: " + envName);
			}catch (DatabaseException dbe){
				System.out.println("Environment not shutdown properly: " + envName);
			}
		}
	}
}