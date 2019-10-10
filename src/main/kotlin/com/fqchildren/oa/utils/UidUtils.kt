package com.fqchildren.oa.utils

import org.slf4j.LoggerFactory
import java.net.InetAddress
import java.net.UnknownHostException
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.time.LocalDate
import java.time.ZoneId
import java.util.regex.Pattern

/**
 * 53 bits unique id:
 *
 *
 * |--------|--------|--------|--------|--------|--------|--------|--------|
 * |00000000|00011111|11111111|11111111|11111111|11111111|11111111|11111111|
 * |--------|---xxxxx|xxxxxxxx|xxxxxxxx|xxxxxxxx|xxx-----|--------|--------|
 * |--------|--------|--------|--------|--------|---xxxxx|xxxxxxxx|xxx-----|
 * |--------|--------|--------|--------|--------|--------|--------|---xxxxx|
 *
 *
 * Maximum ID = 11111_11111111_11111111_11111111_11111111_11111111_11111111
 *
 *
 * Maximum TS = 11111_11111111_11111111_11111111_111
 *
 *
 * Maximum NT = ----- -------- -------- -------- ---11111_11111111_111 = 65535
 *
 *
 * Maximum SH = ----- -------- -------- -------- -------- -------- ---11111 = 31
 *
 *
 * It can generate 64k unique id per IP and up to 2106-02-07T06:28:15Z.
 */
object UidUtils {
    private val logger = LoggerFactory.getLogger(UidUtils::class.java)

    private val PATTERN_LONG_ID = Pattern.compile("^([0-9]{15})([0-9a-f]{32})([0-9a-f]{3})$")

    private val PATTERN_HOSTNAME = Pattern.compile("^.*\\D+([0-9]+)$")

    private val OFFSET = LocalDate.of(2000, 1, 1).atStartOfDay(ZoneId.of("Z")).toEpochSecond()

    private val MAX_NEXT = 0b11111_11111111_111L

    private val SHARD_ID = serverIdAsLong

    private var offset: Long = 0

    private var lastEpoch: Long = 0

    private val serverIdAsLong: Long
        get() {
            try {
                val hostname = InetAddress.getLocalHost().hostName
                val matcher = PATTERN_HOSTNAME.matcher(hostname)
                if (matcher.matches()) {
                    val n = java.lang.Long.parseLong(matcher.group(1))
                    if (n in 0..7) {
                        logger.info("detect server id from host name {}: {}.", hostname, n)
                        return n
                    }
                }
            } catch (e: UnknownHostException) {
                logger.warn("unable to get host name. set server id = 0.")
            }

            return 0
        }

    fun nextId(): Long {
        return nextId(System.currentTimeMillis() / 1000)
    }

    @Synchronized
    private fun nextId(second: Long): Long {
        var epochSecond = second
        if (epochSecond < lastEpoch) {
            // warning: clock is turn back:
            logger.warn("clock is back: $epochSecond from previous:$lastEpoch")
            epochSecond = lastEpoch
        }
        if (lastEpoch != epochSecond) {
            lastEpoch = epochSecond
            reset()
        }
        offset++
        val next = offset and MAX_NEXT
        if (next == 0L) {
            logger.warn("maximum id reached in 1 second in epoch: $epochSecond")
            return nextId(epochSecond + 1)
        }
        return generateId(epochSecond, next, SHARD_ID)
    }

    private fun reset() {
        offset = 0
    }

    private fun generateId(epochSecond: Long, next: Long, shardId: Long): Long {
        return epochSecond - OFFSET shl 21 or (next shl 5) or shardId
    }

    private fun sha1AsBytes(input: String): ByteArray {
        return sha1AsBytes(input.toByteArray(StandardCharsets.UTF_8))
    }

    /**
     * Generate SHA-1 as bytes.
     *
     * @param input Input as bytes.
     * @return Bytes.
     */
    private fun sha1AsBytes(input: ByteArray): ByteArray {
        var md: MessageDigest? = null
        try {
            md = MessageDigest.getInstance("SHA1")
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        }
        md!!.update(input)
        return md.digest()
    }
}
