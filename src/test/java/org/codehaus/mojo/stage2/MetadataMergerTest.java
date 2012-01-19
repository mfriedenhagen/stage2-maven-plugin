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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.codehaus.plexus.util.FileUtils;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import static org.junit.matchers.JUnitMatchers.containsString;
import org.junit.runner.RunWith;

/**
 *
 * @author Mirko Friedenhagen
 */
@RunWith(JUnitParamsRunner.class)
public class MetadataMergerTest
{

    final static Class<?> TKLASS = MetadataMergerTest.class;

    /**
     * Test of writeNewMetadata method, of class MetadataMerger.
     */
    @Test
    @Parameters({
        "pom-pom.xml, 2.1.1, 2.1.3",
        "jar-pom.xml, 2.1.1, 2.1.3",
        "maven-plugin-pom.xml, 1.0-alpha-2, 1.1"
    })
    public void testWriteNewMetadata(String pom, String previous, String latest) throws Exception {
        final File pomFile = resource(pom);
        final File metadata = new File(pomFile.getParentFile(), "maven-metadata-test.xml");
        MetadataMerger instance = new MetadataMerger(metadata);
        instance.writeNewMetadata(pomFile);
        final String metadataContent = FileUtils.fileRead(metadata, "utf-8");
        commonChecks(metadataContent, latest, metadata);
    }

    /**
     * Test of mergeMetadata method, of class MetadataMerger.
     */
    @Test
    @Parameters({
        "maven-metadata-parent.xml, pom-pom.xml, 2.1.1, 2.1.3",
        "maven-metadata-tljunit.xml, jar-pom.xml, 2.1.1, 2.1.3",
        "maven-metadata-stage.xml, maven-plugin-pom.xml, 1.0-alpha-2, 1.1"
    })
    public void testMergeMetadata(String existingMeta, String pom, String previous, String latest) throws Exception {
        final File pomFile = resource(pom);
        final File metadata = resource(existingMeta);
        MetadataMerger instance = new MetadataMerger(metadata);
        instance.mergeMetadata(pomFile);
        final String metadataContent = FileUtils.fileRead(metadata, "utf-8");
        commonChecks(metadataContent, latest, metadata);
        assertThat(metadataContent, containsString("<version>" + previous + "</version>"));

    }

    @Test(expected=IOException.class)
    public void testCorruptMetadata() throws URISyntaxException, IOException {
        final File resource = resource("maven-metadata-corrupt.xml");
        MetadataMerger instance = new MetadataMerger(resource);
        instance.mergeMetadata(resource);
    }

    @Test(expected=IOException.class)
    public void testCorruptPom() throws URISyntaxException, IOException {
        final File pomFile = resource("corrupt-pom.xml");
        final File metadata = new File(pomFile.getParentFile(), "maven-metadata-corrupt-pom.xml");
        MetadataMerger instance = new MetadataMerger(metadata);
        instance.mergeMetadata(pomFile);
    }

    @Test
    public void testInvalidPom() throws URISyntaxException {
        final File pomFile = resource("invalid-pom.xml");
        final File metadata = new File(pomFile.getParentFile(), "maven-metadata-invalid-pom.xml");
        MetadataMerger instance = new MetadataMerger(metadata);
        try {
            instance.mergeMetadata(pomFile);
        } catch (IOException e) {
            assertEquals("[0]  'version' is missing.", e.getMessage().trim());
        }
    }

    void commonChecks(final String metadataContent, String latest, final File metadata) {
        assertThat(metadataContent, not(containsString("<latest>" + latest + "</latest>")));
        assertThat(metadataContent, not(containsString("<release>" + latest + "</release>")));
        assertThat(metadataContent, containsString("<version>" + latest + "</version>"));
        assertTrue(new File(metadata.getAbsolutePath() + "." + Constants.MD5).canRead());
        assertTrue(new File(metadata.getAbsolutePath() + "." + Constants.SHA1).canRead());
    }

    File resource(String pom) throws URISyntaxException {
        return new File(TKLASS.getResource(pom).toURI());
    }

}
