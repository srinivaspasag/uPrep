package com.vedantu.commons.fs.exception;

import java.util.HashMap;
import java.util.Map;

import play.Play;

import com.vedantu.commons.fs.handlers.FileSystemFactory;
import com.vedantu.commons.fs.handlers.IFileSystemHandler;
import com.vedantu.commons.fs.handlers.LocalFileSystemHandler;

public class FileHandlerFactory {

	public enum HandlerType{
		TEMP, FILE_SYSTEM
		
	};
	
	private static Map<String,IFileSystemHandler> handlerMap = new HashMap<String, IFileSystemHandler>();
	
	public static IFileSystemHandler get( HandlerType type )
	{
		switch( type ){
			case TEMP : {
				if( handlerMap.get(type.name() ) == null ){
					synchronized( handlerMap ){
						if( handlerMap.get(type.name() ) == null ){
							handlerMap.put( type.name(), new LocalFileSystemHandler(false,Play.application().configuration().getString("util.temp_dir")));
				 		}
					}
				}
				return handlerMap.get(type.name());
			}
			case FILE_SYSTEM :
			{
				if( handlerMap.get(type.name() ) == null ){
					synchronized( handlerMap ){
						if( handlerMap.get(type.name() ) == null ){
							handlerMap.put( type.name(),FileSystemFactory.INSTANCE.getFS());
				 		}
					}
				}
				return handlerMap.get(type.name());
			}
			default: return null;
		}
	}
}
