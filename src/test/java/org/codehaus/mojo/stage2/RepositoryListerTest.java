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

import java.util.List;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.codehaus.plexus.PlexusTestCase;

/**
 *
 * @author mirko
 */
public class RepositoryListerTest extends PlexusTestCase {

    private ArtifactRepositoryCreator artifactRepositoryCreator;

    private DefaultRepositoryLister instance;

    private final String source = "foo::default::" + "file:" + getBasedir() + "/target/test-classes/staging-repository";

    private ArtifactRepository sourceRepository;

    @Override
    public void setUp() throws Exception {
        instance = (DefaultRepositoryLister) lookup(RepositoryLister.ROLE);
        artifactRepositoryCreator = (ArtifactRepositoryCreator) lookup(ArtifactRepositoryCreator.ROLE);
        sourceRepository = artifactRepositoryCreator.getRepository(source, "stage.repository");
    }

    /**
     * Test of list method, of class RepositoryLister.
     */
    public void testListAll() throws Exception {
        Gav gav = Gav.valueOf("org.apache.maven:*:2.0.6");
        final List<String> collectFiles = instance.collectFiles(sourceRepository, gav);
        assertEquals(513, collectFiles.size());
    }

    /**
     * Test of list method, of class RepositoryLister.
     */
    public void testListMaven() throws Exception {
        Gav gav = Gav.valueOf("org.apache.maven:maven:2.0.6");
        final List<String> collectFiles = instance.collectFiles(sourceRepository, gav);
        assertEquals(9, collectFiles.size());
        instance.list(sourceRepository, gav);
    }

    /**
     * Test of list method, of class RepositoryLister.
     */
    public void testListMavenPlugins() throws Exception {
        Gav gav = Gav.valueOf("org.apache.maven:maven-plugin-.*:2.0.6");
        final List<String> collectFiles = instance.collectFiles(sourceRepository, gav);
        assertEquals(108, collectFiles.size());
    }
}
