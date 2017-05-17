package com.lenis0012.bukkit.loginsecurity.test;

import com.lenis0012.bukkit.loginsecurity.hashing.Algorithm;
import org.junit.Assert;
import org.junit.Test;

/**
 * Checks if all hashing algorithms work properly.
 */
public class HashTest {

    @Test
    public void testActiveAlgorithms() {
        String rightPassword = "MojangWasHere";
        String wrongPassword = "mojangWasHere";

        for(Algorithm algorithm : Algorithm.values()) {
            if(algorithm.isDeprecated()) continue;
            String hashed = algorithm.hash(rightPassword);
            Assert.assertNotEquals("Algorithm " + algorithm.toString() + " did nothing", rightPassword, hashed);
            Assert.assertTrue("Algorithm " + algorithm.toString() + " didn't match hash", algorithm.check(rightPassword, hashed));
            Assert.assertFalse("Algorithm " + algorithm.toString() + " matched hash with differing casing", algorithm.check(wrongPassword, hashed));
        }
    }
}
