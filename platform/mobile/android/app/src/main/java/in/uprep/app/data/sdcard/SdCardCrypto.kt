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
    // Bug found live via a real activated device + real encrypted file:
    // AEADBadTagException on every decrypt. Root cause — the server's
    // on-disk layout (lib/group-crypto.ts) is iv(12) + tag(16) + ciphertext,
    // written that way because Node's cipher.getAuthTag() is a separate call
    // from cipher.update()/final(), so the tag was placed right after the iv
    // for convenience. But javax.crypto's Cipher, on DECRYPT_MODE doFinal(),
    // expects the OPPOSITE order: ciphertext bytes followed by the trailing
    // tag (matching what its own ENCRYPT_MODE doFinal() would produce).
    //
    // Bug found live #2 via a real ~57MB book on the emulator's default heap
    // (192MB): the original implementation read the whole file into memory,
    // then built iv/tag/ciphertext copies AND a ciphertext+tag concatenation,
    // then cipher.doFinal() allocated the full plaintext on top of that —
    // four full-file-sized buffers alive at once, well past the heap limit
    // (OutOfMemoryError in Cipher.doFinal). Fixed by streaming.
    //
    // Bug found live #3, introduced by the streaming rewrite itself
    // (AEADBadTagException on the very next test): the tag sits at a FIXED
    // position right after the iv (see the file layout above) — it is NOT a
    // trailing tag at the end of the stream the way Java's own GCM output
    // would be. The first streaming attempt held back the *last*
    // TAG_LENGTH_BYTES of the stream on the (wrong) assumption it mirrored
    // Java's own encrypt format, which fed real ciphertext bytes into
    // doFinal() as if they were the tag and vice versa. Fix: read the tag
    // right after the iv (its real, known position), then every remaining
    // byte is pure ciphertext — stream all of it through cipher.update(),
    // and call doFinal(tag) once at EOF to produce the last plaintext bytes
    // and verify the tag, exactly matching what the whole-buffer version did
    // (reorder ciphertext-then-tag) without ever holding more than one
    // chunk in memory.
    private const val CHUNK_SIZE = 64 * 1024

    private fun readFully(input: InputStream, buf: ByteArray) {
        var off = 0
        while (off < buf.size) {
            val n = input.read(buf, off, buf.size - off)
            require(n >= 0) { "Encrypted file too short to contain iv+tag" }
            off += n
        }
    }

    fun decrypt(input: InputStream, keyBase64: String, output: OutputStream) {
        val keyBytes = Base64.decode(keyBase64, Base64.NO_WRAP)
        val iv = ByteArray(IV_LENGTH)
        readFully(input, iv)
        val tag = ByteArray(TAG_LENGTH_BYTES)
        readFully(input, tag)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(
            Cipher.DECRYPT_MODE,
            SecretKeySpec(keyBytes, "AES"),
            GCMParameterSpec(TAG_LENGTH_BITS, iv)
        )

        val chunk = ByteArray(CHUNK_SIZE)
        while (true) {
            val n = input.read(chunk)
            if (n <= 0) break
            val decrypted = cipher.update(chunk, 0, n)
            if (decrypted != null && decrypted.isNotEmpty()) output.write(decrypted)
        }
        val last = cipher.doFinal(tag)
        if (last != null && last.isNotEmpty()) output.write(last)
    }
}
