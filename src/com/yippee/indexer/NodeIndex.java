package com.yippee.indexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

import com.yippee.db.crawler.model.DocAug;
import com.yippee.db.indexer.model.Hit;
import com.yippee.util.Configuration;

public class NodeIndex {
	 /**
     * Create logger in the Log4j hierarchy named by by software component
     */
    static Logger logger = Logger.getLogger(NodeIndex.class);
    
	volatile HashMap<String, ArrayList<Hit>> wordIndex;
	private int capacity = 2;
	private HashMap<String,String> stopWords;
	private int docCount = 0;
	private long startTime = 0;
//	private ArrayList<DocAug> docArchive;
//	private DocArchiveManager dam;
//	private boolean archiveMode = false;
	
	
	public NodeIndex() {
		wordIndex = new HashMap<String, ArrayList<Hit>>();
	//	globalIndex = new HashMap<String, ArrayList<Hit>>();
		startTime = System.currentTimeMillis();
//		docArchive = new ArrayList<DocAug>();
//		dam = new DocArchiveManager();
	}
	
//	public void setArchiveMode(boolean mode) {
//		archiveMode = mode;
//		if(mode){
//			docArchive = dam.getAllDocs(); 
//		}
//	}
	
//	public boolean isArchiveMode(){
//		return archiveMode;
//	}
	
//	public synchronized DocAug poll(){
//		if(!docArchive.isEmpty()) {return docArchive.remove(0);}
//		else {return null;}
//	}
	
	
	/**
	 * adds all hits from a document into wordIndex
	 * if word isnt there, make new entry. otherwise append the Hit
	 * 
	 * @param hitMap
	 */
	public synchronized void addAllHits(HashMap<String, ArrayList<Hit>> hitMap) {
		docCount++;
		Set<String> keys = hitMap.keySet();
		Iterator<String> iter = keys.iterator();
		
		while(iter.hasNext()) {
			
			String word = iter.next();

			ArrayList<Hit> hitList = hitMap.get(word); 
			ArrayList<Hit> list;			
			if(wordIndex.containsKey(word)){
				list = wordIndex.get(word);
			} else {
				list = new ArrayList<Hit>();
			}						
			list.addAll(hitList);
			wordIndex.put(word, list);
		
		}
		if(docCount%10==0){
			sendGoodWordsToRing();
//			logger.info("WE'VE INDEXED "+docCount+" DOCS IN "+(System.currentTimeMillis()-startTime)+"ms");
			//System.out.println("WE'VE INDEXED "+docCount+" DOCS IN "+(System.currentTimeMillis()-startTime)+"ms");
		}
	//	if (wordIndex.size() > capacity) {
			//logger.info("REACHED CAPACITY, SENDING TO RING");
		//640 DOCS IN 359s
		//1500 DOCS IN 9756s
		//	printIndex();
	//	}		
	}
	
	public synchronized ArrayList<Hit> getHitList(String word){
		return wordIndex.get(word);
	}

//	public synchronized void printIndex(){		
//		Set<String> keys = wordIndex.keySet();
//		Iterator<String> iter = keys.iterator();
//		
//		while(iter.hasNext()) {
//			String word = iter.next();
////			System.out.println("[" + word + "=" + wordIndex.get(word).size() + "]");
//			logger.info("[" + word + /*lexiconMap.get(word) +*/ "=" + wordIndex.get(word).size() + "]");
//		}
//	}
	
	public synchronized void sendGoodWordsToRing() {
		// HASH keys
		Set<String> keys = wordIndex.keySet();
		Iterator<String> iter = keys.iterator();
		ArrayList<String> deleteKeys = new ArrayList<String>();
		
		while(iter.hasNext()) {
			String word = iter.next();
			ArrayList<Hit> list = wordIndex.get(word);
			if(list.size()>capacity){
				Configuration.getInstance().getPastryEngine().sendList(word,list);
//				logger.info("[" + word + /*lexiconMap.get(word) +*/ "=" + wordIndex.get(word).size() + "]");
				deleteKeys.add(word);
			}
		}
	//	System.out.println("SENT OUT "+deleteKeys.size()+" out of "+wordIndex.size());
	/*	for(int i=0; i<deleteKeys.size();i++){
			String k = deleteKeys.get(i);
			wordIndex.remove(k);
		}*/
		wordIndex = new HashMap<String, ArrayList<Hit>>();
		//	Configuration.getInstance().getPastryEngine().sendList(word,list);

		
			
	}
	
//	public synchronized void printAll(){
//		
//		Set<String> keys = wordIndex.keySet();
//		Iterator<String> iter = keys.iterator();
//		
//		while(iter.hasNext()) {
//			String word = iter.next();
//			//System.out.println("[" + word + "=" + wordIndex.get(word).size() + "]");
//		}
//		
//	}
	
}