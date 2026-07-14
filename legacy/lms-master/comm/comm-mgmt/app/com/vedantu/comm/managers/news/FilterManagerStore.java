package com.vedantu.comm.managers.news;



public class FilterManagerStore {
	  private static final ThreadLocal<FilterManager> filterManagerSet = new ThreadLocal<FilterManager>();
	     
	  
	    public static FilterManager get( )
	    {
	        return filterManagerSet.get();
	    }
	    
	    public static void set( FilterManager filterManager ){
	    	filterManagerSet.set(filterManager);
	    }
	    
	 
}
