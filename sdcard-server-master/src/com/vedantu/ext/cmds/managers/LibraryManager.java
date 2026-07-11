package com.vedantu.ext.cmds.managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.vedantu.ext.cmds.db.datamanagers.FlashRecordDataManager;
import com.vedantu.ext.cmds.db.datamanagers.ImportedLibraryDataManager;
import com.vedantu.ext.cmds.db.datamanagers.ResourceDataManager;
import com.vedantu.ext.cmds.db.datamanagers.SDCardGroupDataManager;
import com.vedantu.ext.cmds.db.datamanagers.SyncInfoDataManager;
import com.vedantu.ext.cmds.db.models.ImportedLibrary;
import com.vedantu.ext.cmds.db.models.SyncInfo;
import com.vedantu.ext.cmds.enums.EntityType;
import com.vedantu.ext.cmds.pojo.responses.local.GetImportedLibrariesRes;

public class LibraryManager extends AbstractManager {

    public List<GetImportedLibrariesRes> getLibraries(int orgKeyId, String state) {

        List<ImportedLibrary> libraries = ImportedLibraryDataManager.INSTANCE.getSyncedLibraries(
                orgKeyId, state);

        List<GetImportedLibrariesRes> syncedLibraries = new ArrayList<GetImportedLibrariesRes>();

        List<String> targetIds = new ArrayList<String>();
        for (ImportedLibrary library : libraries) {
            targetIds.add(library.id);
        }
        // Map<String, Integer> resourceMap =
        // ResourceDataManager.INSTANCE.getResourceCount(targetIds,
        // EntityType.SECTION.name());
        // Map<String, Integer> flashMap = FlashRecordDataManager.INSTANCE.getFlashCounts(org._id,
        // new ArrayList<String>(), new ArrayList<String>(), targetIds,
        // FlashRecordInfo.FIELD_SECTION_ID);

        for (ImportedLibrary lib : libraries) {
            GetImportedLibrariesRes syncedLib = new GetImportedLibrariesRes();
            syncedLib.fromModel(lib);
            // TODO: add sync, sdcard info and content count
            syncedLibraries.add(syncedLib);
            int resourceCount = ResourceDataManager.INSTANCE.getResourceCount(lib.id,
                    EntityType.SECTION.name());

            syncedLib.setContentCount(resourceCount);

            int flashCount = FlashRecordDataManager.INSTANCE.getFlashCount(orgKeyId, null, null,
                    null, "orgKeyId");
            syncedLib.setFlashCount(flashCount);

            int sdCardGroupCount = SDCardGroupDataManager.INSTANCE.getSDCardGroupCount(orgKeyId,
                    lib.id, EntityType.SECTION.name());

            syncedLib.setNoOfCards(sdCardGroupCount);
            SyncInfo syncInfo = SyncInfoDataManager.INSTANCE.getSyncInfo(getSyncKey("library",
                    Arrays.asList(lib.id, EntityType.SECTION.name())));
            if (syncInfo != null) {
                syncedLib.setLastSynced(syncInfo.syncTime);
            }
            
        }

        return syncedLibraries;
    }
}
