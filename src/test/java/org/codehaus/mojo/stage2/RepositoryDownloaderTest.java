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
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.codehaus.plexus.PlexusTestCase;
import org.junit.Test;

/**
 *
 * @author mirko
 */
public class RepositoryDownloaderTest extends PlexusTestCase {
    private ArtifactRepositoryCreator artifactRepositoryCreator;

    private DefaultRepositoryDownloader instance;

    private final String source = "foo::default::" + "file:" + getBasedir() + "/src/test/staging-repository";

    private ArtifactRepository sourceRepository;

    public void setUp() throws Exception {
        System.setProperty("java.io.tmpdir", getBasedir() + "/target/download-directory");
        instance = (DefaultRepositoryDownloader) lookup(RepositoryDownloader.ROLE);
        artifactRepositoryCreator = (ArtifactRepositoryCreator) lookup(ArtifactRepositoryCreator.ROLE);
        sourceRepository = artifactRepositoryCreator.getRepository(source, "stage.repository");
    }


    /**
     * Test of download method, of class DefaultRepositoryDownloader.
     */
    @Test
    public void testDownload() throws Exception {
        System.out.println("download");
        Gav gav = Gav.valueOf("org.apache.maven:maven:2.0.6");
        instance.download(sourceRepository, gav);
        final File pomFile = new File(getBasedir() + "/target/download-directory/staging-plugin/org.apache.maven%3Amaven%3A2.0.6/org/apache/maven/maven/2.0.6/");
        assertTrue("Can not read " + pomFile, pomFile.canRead());
    }
}