package ru.cocsosoft;

import android.content.Context;


public class CacheVoteBox {
	private String boxId;
	private String nameImg1;
	private String nameImg2;
	private String cocsBoxId;
	
	
	public CacheVoteBox setBoxId(String id) {
		boxId = id; 
		return this;
	}
	
	public void setPathImg(String pathImg, int numImg) {
		if(numImg==1){
			nameImg1 = pathImg;	
		}
		else{
			nameImg2 = pathImg;	
		}			
		//return this;
	}
	
	public String getBoxId() {
		return boxId; 
	}
	
	public String getCocsBoxId() {
		return cocsBoxId; 
	}
	
	public CacheVoteBox setCocsBoxId(String id) {
		cocsBoxId = id; 
		return this;
	}
	
	public String getPathImg(int numImg, Context ctx) {		
		if(numImg==1){			
			return LocalFile.getFullPath(nameImg1, ctx);	
		}
		else{
			return LocalFile.getFullPath(nameImg2, ctx);	
		}				 
	}
	
}
