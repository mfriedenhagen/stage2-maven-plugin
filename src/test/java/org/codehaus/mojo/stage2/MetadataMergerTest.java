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
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.maven.model.Model;
import org.codehaus.plexus.util.FileUtils;
import org.hamcrest.CoreMatchers;
import org.junit.*;
import static org.junit.Assert.*;
import org.junit.matchers.JUnitMatchers;
import org.junit.runner.RunWith;

/**
 *
 * @author Mirko Friedenhagen
 */
@RunWith(JUnitParamsRunner.class)
public class MetadataMergerTest
{

    /**
     * Test of writeNewMetadata method, of class MetadataMerger.
     */
    @Test
    public void testWriteNewMetadata() throws Exception
    {
        System.out.println( "writeNewMetadata" );
        File pomFile = null;
        MetadataMerger instance = null;
        instance.writeNewMetadata( pomFile );
        // TODO review the generated test code and remove the default call to fail.
        fail( "The test case is a prototype." );
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
    public void testMergeMetadata(String existingMeta, String pom, String previous, String latest) throws Exception
    {
        final File pomFile = new File(MetadataMergerTest.class.getResource(pom).toURI());
        final File metadata = new File(MetadataMergerTest.class.getResource(existingMeta).toURI());
        MetadataMerger instance = new MetadataMerger(metadata);
        instance.mergeMetadata(pomFile);
        final String metadataContent = FileUtils.fileRead(metadata, "utf-8");
        assertThat(metadataContent, JUnitMatchers.containsString("<latest>" + latest + "</latest>"));
        assertThat(metadataContent, JUnitMatchers.containsString("<release>" + latest + "</release>"));
        assertThat(metadataContent, JUnitMatchers.containsString("<version>" + latest + "</version>"));
        assertThat(metadataContent, JUnitMatchers.containsString("<version>" + previous + "</version>"));
        assertTrue(new File(metadata.getAbsolutePath() + "." + Constants.MD5).canRead());
        assertTrue(new File(metadata.getAbsolutePath() + "." + Constants.SHA1).canRead());
        
    }

    /**
     * Test of generateModel method, of class MetadataMerger.
     */
    @Test
    public void testGenerateModel()
    {
        System.out.println( "generateModel" );
        String groupId = "";
        String artifactId = "";
        String version = "";
        String packaging = "";
        MetadataMerger instance = null;
        Model expResult = null;
        Model result = instance.generateModel( groupId, artifactId, version, packaging );
        assertEquals( expResult, result );
        // TODO review the generated test code and remove the default call to fail.
        fail( "The test case is a prototype." );
    }

    /**
     * Test of encode method, of class MetadataMerger.
     */
    @Test
    public void testEncode()
    {
        System.out.println( "encode" );
        byte[] binaryData = null;
        MetadataMerger instance = null;
        String expResult = "";
        String result = instance.encode( binaryData );
        assertEquals( expResult, result );
        // TODO review the generated test code and remove the default call to fail.
        fail( "The test case is a prototype." );
    }
}
