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
import junitparams.Parameters;
import org.junit.*;
import static org.junit.Assert.*;
import static junitparams.JUnitParamsRunner.$;
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
    @Parameters(method="matchesResults")
    public void testMatches(String versionString, String matching, String nonMatching) {
        Gav instance = Gav.valueOf(versionString);
        assertTrue(matching + " shpuld match!", instance.matches(matching));
        assertFalse(nonMatching + " shpuld NOT match!", instance.matches(nonMatching));
    }

    private Object[] matchesResults() {
        return $
        (
            $("org.codahaus.mojo:stage2-maven-plugin:1.1", "org/codahaus/mojo/stage2-maven-plugin/1.1/foo", "org/codahaus/mojo/stage2-maven-plugin/1.2/foo"),
            $("org.codahaus.mojo:*:1.1", "org/codahaus/mojo/stage2-maven-plugin/1.1/foo", "org/codahaus/mojo/stage2-maven-plugin/1.2/foo")
        );
    }

    /**
     * Test of toString method, of class Gav.
     */
    @Test
    @Parameters(method="toStringResults")
    public void testToString(String versionString, String expResult)
    {
        Gav instance = Gav.valueOf(versionString);
        String result = instance.toString();
        assertEquals( expResult, result );
    }

    private Object[] toStringResults() {
        return $
        (
            $("org.codahaus.mojo:stage2-maven-plugin:1.1", "gav=org/codahaus/mojo/stage2-maven-plugin/1.1"),
            $("org.codahaus.mojo:*:1.1", "gav=org/codahaus/mojo/*/1.1")
        );
    }

    /**
     * Test of getEncodedPath method, of class Gav.
     */
    @Test
    @Parameters(method="encodedPaths")
    public void testGetEncodedPath(String versionString, String encodedPath)
    {
        Gav instance = Gav.valueOf(versionString);
        String result = instance.getEncodedPath();
        assertEquals(encodedPath, result);
    }

    private Object[] encodedPaths() {
        return $
        (
            $("org.codahaus.mojo:stage2-maven-plugin:1.1", "org.codahaus.mojo%3Astage2-maven-plugin%3A1.1"),
            $("org.codahaus.mojo:*:1.1", "org.codahaus.mojo%3AALL_ARTIFACTS%3A1.1")
        );
    }

    /**
     * Test of valueOf method, of class Gav.
     */
    @Test
    @Parameters({
        "org.codahaus.mojo:stage2-maven-plugin:1.1, org.codahaus.mojo, stage2-maven-plugin, 1.1",
        "org.codahaus.mojo:*:1.1, org.codahaus.mojo, *, 1.1"
    })
    public void testValueOf(String versionString, String groupId, String artifactId, String version)
    {
        Gav result = Gav.valueOf(versionString);
        assertEquals(groupId, result.groupId);
        assertEquals(artifactId, result.artifactId);
        assertEquals(version, result.version);
    }
}
