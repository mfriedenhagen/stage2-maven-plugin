/*
 * Copyright 2012 Codehaus.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.mojo.stage2;

import junitparams.JUnitParamsRunner;
import static junitparams.JUnitParamsRunner.$;
import junitparams.Parameters;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Mirko Friedenhagen
 */
@RunWith(JUnitParamsRunner.class)
public class GavTest {

    /**
     * Test of matches method, of class Gav.
     */
    @Test
    @Parameters(method = "matchesResults")
    public void testMatches(String versionString, String matching, String nonMatching) {
        Gav sut = Gav.valueOf(versionString);
        assertTrue(matching + " should match!", sut.matches(matching));
        assertFalse(nonMatching + " shpuld NOT match!", sut.matches(nonMatching));
    }

    private Object[] matchesResults() {
        return $(
                $("org.codahaus.mojo:stage2-maven-plugin:1.1", "org/codahaus/mojo/stage2-maven-plugin/1.1/foo", "org/codahaus/mojo/stage2-maven-plugin/1.2/foo"),
                $("org.codahaus.mojo:*:1.1", "org/codahaus/mojo/stage2-maven-plugin/1.1/foo", "org/codahaus/mojo/stage2-maven-plugin/1.2/foo"));
    }

    /**
     * Test of toString method, of class Gav.
     */
    @Test
    @Parameters(method = "toStringResults")
    public void testToString(String versionString, String expResult) {
        Gav sut = Gav.valueOf(versionString);
        String result = sut.toString();
        assertEquals(expResult, result);
    }

    private Object[] toStringResults() {
        return $(
                $("org.codahaus.mojo:stage2-maven-plugin:1.1", "gav=org/codahaus/mojo/stage2-maven-plugin/1.1"),
                $("org.codahaus.mojo:*:1.1", "gav=org/codahaus/mojo/*/1.1"));
    }

    /**
     * Test of getEncodedPath method, of class Gav.
     */
    @Test
    @Parameters(method = "encodedPaths")
    public void testGetEncodedPath(String versionString, String encodedPath) {
        Gav sut = Gav.valueOf(versionString);
        String result = sut.getEncodedPath();
        assertEquals(encodedPath, result);
    }

    private Object[] encodedPaths() {
        return $(
                $("org.codahaus.mojo:stage2-maven-plugin:1.1", "org.codahaus.mojo%3Astage2-maven-plugin%3A1.1"),
                $("org.codahaus.mojo:*:1.1", "org.codahaus.mojo%3AALL_ARTIFACTS%3A1.1"));
    }

    /**
     * Test of valueOf method, of class Gav.
     */
    @Test
    @Parameters({
        "org.codahaus.mojo:stage2-maven-plugin:1.1, org.codahaus.mojo, stage2-maven-plugin, 1.1",
        "org.codahaus.mojo:*:1.1, org.codahaus.mojo, *, 1.1"
    })
    public void testValueOf(String versionString, String groupId, String artifactId, String version) {
        Gav result = Gav.valueOf(versionString);
        assertEquals(groupId, result.getGroupId());
        assertEquals(artifactId, result.getArtifactId());
        assertEquals(version, result.getVersion());
    }
}
