package gg.cartograph.plugin.common;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Produces salted SHA-256 hashes of IP addresses for privacy-preserving telemetry.
 *
 * <p>The salt is read from config and persisted across restarts, so the same
 * IP always produces the same hash on the same server — enabling session
 * correlation without storing raw IPs.</p>
 */
public class IpHasher
{

    private final String salt;

    public IpHasher(String salt)
    {
        this.salt = salt;
    }

    public String hash(String ip)
    {
        if (ip == null) {
            return null;
        }
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt.getBytes(StandardCharsets.UTF_8));
            digest.update(ip.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
