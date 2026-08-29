package `in`.uprep.app.data.sdcard

import android.util.Base64
import java.io.InputStream
import java.io.OutputStream
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

// Raw AES-256-GCM decrypt matching lib/group-crypto.ts's per-file layout
// exactly: iv(12 bytes) || authTag(16 bytes) || ciphertext. Deliberately NOT
// androidx.security's EncryptedFile — that only accepts a Keystore-resident
// MasterKey (no raw external key bytes) and writes a completely different
// on-disk format (Tink's chunked AES256_GCM_HKDF_4KB), so it can't read
// this file at all. This is plain javax.crypto, already on the platform SDK.
private const val IV_LENGTH = 12
private const val TAG_LENGTH_BYTES = 16
private const val TAG_LENGTH_BITS = TAG_LENGTH_BYTES * 8

object SdCardCrypto {
    // Whole-file buffering, not streaming: a true single-pass stream decrypt
    // would need to hold back the final TAG_LENGTH_BYTES until EOF anyway
    // (see the reorder below), so buffering the whole file trades peak
    // memory (~2x file size, input+output) for simplicity. Acceptable for
    // lecture-length video on a modern tablet; revisit with real streaming
    // if very large files cause OOM in practice.
    //
    // Bug found live via a real activated device + real encrypted file:
    // AEADBadTagException on every decrypt. Root cause — the server's
    // on-disk layout (lib/group-crypto.ts) is iv(12) + tag(16) + ciphertext,
    // written that way because Node's cipher.getAuthTag() is a separate call
    // from cipher.update()/final(), so the tag was placed right after the iv
    // for convenience. But javax.crypto's Cipher, on DECRYPT_MODE doFinal(),
    // expects the OPPOSITE order: ciphertext bytes followed by the trailing
    // tag (matching what its own ENCRYPT_MODE doFinal() would produce). The
    // first version of this function fed the file's tag-then-ciphertext
    // bytes straight through unreordered — always the wrong order for Java's
    // API, hence every decrypt failing its tag check. Fix: read the pieces
    // out by their real position in the file, then feed Java ciphertext+tag.
    fun decrypt(input: InputStream, keyBase64: String, output: OutputStream) {
        val keyBytes = Base64.decode(keyBase64, Base64.NO_WRAP)
        val all = input.readBytes()
        require(all.size > IV_LENGTH + TAG_LENGTH_BYTES) { "Encrypted file too short to contain iv+tag" }
        val iv = all.copyOfRange(0, IV_LENGTH)
        val tag = all.copyOfRange(IV_LENGTH, IV_LENGTH + TAG_LENGTH_BYTES)
        val ciphertext = all.copyOfRange(IV_LENGTH + TAG_LENGTH_BYTES, all.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(
            Cipher.DECRYPT_MODE,
            SecretKeySpec(keyBytes, "AES"),
            GCMParameterSpec(TAG_LENGTH_BITS, iv)
        )
        output.write(cipher.doFinal(ciphertext + tag))
    }
}
