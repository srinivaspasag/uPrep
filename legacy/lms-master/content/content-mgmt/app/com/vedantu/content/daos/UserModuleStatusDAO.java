package com.vedantu.content.daos;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.VedantuException;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.content.models.Module;
import com.vedantu.content.models.ModuleEntry;
import com.vedantu.content.models.UserModuleStatus;
import com.vedantu.content.pojos.responses.ModuleAccessStatus;
import com.vedantu.content.pojos.responses.ModuleContentAccessStatus;
import com.vedantu.content.pojos.responses.ModuleInfo;
import com.vedantu.mongo.VedantuBasicDAO;

public class UserModuleStatusDAO extends VedantuBasicDAO<UserModuleStatus, ObjectId> {
    private static final ALogger LOGGER   = Logger.of(UserModuleStatusDAO.class);

    public static final UserModuleStatusDAO  INSTANCE = new UserModuleStatusDAO();
  

    private UserModuleStatusDAO() {

        super(UserModuleStatus.class);
        // TODO Auto-generated constructor stub
    }
    
    public boolean update(String userId, String moduleId, ModuleEntry moduleEntry) {

        LOGGER.debug(".......Inside update function.......");
        UserModuleStatus userModuleStatus = getDS().find(UserModuleStatus.class)
                .filter("userId", userId).filter("moduleId", moduleId).get();

        if (userModuleStatus == null) {
            LOGGER.debug("....... user module status is null.......");
            userModuleStatus = new UserModuleStatus();
            List<ModuleEntry> moduleEnteries = new ArrayList<ModuleEntry>();
            moduleEnteries.add(moduleEntry);
             userModuleStatus.children = moduleEnteries;
             userModuleStatus.moduleId = moduleId;
             userModuleStatus.userId = userId;
             
        } 
        else if(userModuleStatus.children==null) {
            userModuleStatus = new UserModuleStatus();
            List<ModuleEntry> moduleEnteries = new ArrayList<ModuleEntry>();
            moduleEnteries.add(moduleEntry);
             userModuleStatus.children = moduleEnteries;
        }
        else {
            userModuleStatus.children.add(moduleEntry);
        }
        
        save(userModuleStatus);
        return true;
    }

    
    public List<ModuleContentAccessStatus> get(String userId, String moduleId) throws VedantuException {

        LOGGER.debug(".......Inside get function.......");
        UserModuleStatus userModuleStatus = getDS().find(UserModuleStatus.class)
                .filter("userId", userId)
                .filter("moduleId", moduleId)
                .get();

        List<ModuleEntry> children = ModuleDAO.INSTANCE.get(moduleId);
        LOGGER.debug(".......After ModuleDAO.......");
        List<ModuleContentAccessStatus> contentsAccessStatus = new ArrayList<ModuleContentAccessStatus>();
        if(!CollectionUtils.isEmpty(children)) {
        for (ModuleEntry child : children) {
            LOGGER.debug(".......Inside for loop.......");
            ModuleContentAccessStatus contentAccessStatus = new ModuleContentAccessStatus();
            contentAccessStatus.moduleEntry = child;
            LOGGER.debug(".......Before Accessed is called.......");
            contentAccessStatus.accessed = (userModuleStatus!=null && !CollectionUtils.isEmpty(userModuleStatus.children)) && userModuleStatus.children.contains(child);
            LOGGER.debug(".......After Accessed is called.......");
            contentsAccessStatus.add(contentAccessStatus);
        }
      }
        return contentsAccessStatus;
    }
    
    public List<ModuleInfo> getModules(String orgId) throws VedantuException {

        LOGGER.debug(".......Inside getModules function.......");
        List<Module> modules = getDS().find(Module.class)
                .filter("contentSrc.type", EntityType.ORGANIZATION ) 
                .filter("contentSrc.id", orgId)
                .asList();
        LOGGER.debug(".......Inside getModules function......." + modules.size());
        List<ModuleInfo> infos = new ArrayList<ModuleInfo>();
       if(!CollectionUtils.isEmpty(modules)){
           for(Module module : modules){
               ModuleInfo info = new ModuleInfo();
               info.name = module.name;
               info.id = module._getStringId();
               infos.add(info);
           }
       }
       LOGGER.debug(".......Exiting getModules function......." + infos.size());
        return infos;
    }
    
    public List<ModuleAccessStatus> getModulesStatus(String userId, List<String> moduleIds) throws VedantuException {

//        LOGGER.debug(".......Inside get function.......");
//        UserModuleStatus userModuleStatus = getDS().find(UserModuleStatus.class)
//                //.filter("userId", userId)
//                //.filter("id", new ObjectId(moduleId))
//                .get();
//
//        for(String moduleId : moduleIds){
//        
//        Set<ModuleEntry> children = ModuleDAO.INSTANCE.get(moduleId);
//        LOGGER.debug(".......After ModuleDAO......."+ userModuleStatus.children);
//        List<ModuleAccessStatus> modulesAccessStatus = new ArrayList<ModuleAccessStatus>();
//        if(!CollectionUtils.isEmpty(children)) {
//        for (ModuleEntry child : children) {
//            LOGGER.debug(".......Inside for loop.......");
//            ModuleContentAccessStatus contentAccessStatus = new ModuleContentAccessStatus();
//            contentAccessStatus.moduleEntry = child;
//            contentAccessStatus.accessed = isAccessed(userModuleStatus, child);
//            contentsAccessStatus.add(contentAccessStatus);
//            
//            if(isAccessed(userModuleStatus, child) == true){
//                accessedContent++;
//            }
//            
//        }
//        
//        }
//        return contentsAccessStatus;
        return null;
    }
    
}
