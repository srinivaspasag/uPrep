package `in`.uprep.app.data.sdcard

// Mirrors the manifest.json written by platform/web's
// app/api/cmds/tools/seller/groups/[id]/package/route.ts exactly. Two
// packaging modes share this shape: "plain" items carry fileName (open
// directly, no decryption), "encrypted" items carry encryptedFileName
// (needs the AES-256-GCM key from /api/seller/verify — see SdCardCrypto).
data class SdCardManifest(
    val groupId: String?,
    val groupName: String?,
    val packagingMode: String?,
    val programName: String?,
    val courseGroups: List<SdCardCourseGroup>?,
    // Real chapter/session tree below the Course level, derived fresh from
    // each item's own folderId at package time — not stored on the group,
    // so it's always in sync with items[] even for manually-picked groups.
    // Absent (null) on cards packaged before this field existed; readers
    // must fall back to the old flat-per-course view in that case.
    val folders: List<SdCardFolder>?,
    val items: List<SdCardManifestItem>?
)

data class SdCardCourseGroup(
    val courseId: String?,
    val courseName: String?,
    val itemIds: List<String>?
)

// Mirrors lib/courses.ts's FolderNode — a course is a folder with
// parentId == null; chapters/sessions are just deeper nodes of the same
// tree, no schema distinction between them.
data class SdCardFolder(
    val id: String?,
    val name: String?,
    val parentId: String?,
    val order: Int?
)

data class SdCardManifestItem(
    val id: String?,
    val name: String?,
    val type: String?,
    val includedAsFile: Boolean?,
    // Which folder (chapter/session) node this item is filed directly
    // under — matches an id in SdCardManifest.folders, or null if this
    // item was resolved with no folderId (e.g. a TEST/QUESTION_SET, or a
    // manifest from before this field existed).
    val folderId: String?,
    val reason: String?,
    val fileName: String?,
    val encryptedFileName: String?
)
