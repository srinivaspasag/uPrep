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
    val items: List<SdCardManifestItem>?
)

data class SdCardCourseGroup(
    val courseId: String?,
    val courseName: String?,
    val itemIds: List<String>?
)

data class SdCardManifestItem(
    val id: String?,
    val name: String?,
    val type: String?,
    val includedAsFile: Boolean?,
    val reason: String?,
    val fileName: String?,
    val encryptedFileName: String?
)
