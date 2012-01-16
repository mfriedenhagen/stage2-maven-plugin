/*
 * Copyright 2012 The Apache Software Foundation.
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
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;
import org.apache.maven.artifact.deployer.ArtifactDeployer;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.deploy.DeployFileMojo;
import org.apache.maven.wagon.WagonException;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.FileUtils;

/**
 *
 * @author Mirko Friedenhagen 
 *
 * @plexus.component
 */
class DefaultRepositoryUploader implements RepositoryUploader, LogEnabled {

    private Logger logger;

    private File basedir;
    
    /**
     * @component
     */
    private ArtifactDeployer deployer;

    /**
     * Component used to create an artifact.
     *
     * @component
     */
    protected ArtifactFactory artifactFactory;

    /**
     * @parameter default-value="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;

    /**
     * @inheritDoc
     */
    @Override
    public void upload(ArtifactRepository targetRepository, Gav gav) throws WagonException, IOException {
        basedir = new File( new File( System.getProperty( "java.io.tmpdir" ), "staging-plugin" ), gav.getEncodedPath() );
        if ( !basedir.exists() ) {
            throw new IllegalArgumentException("staging path " + basedir + " could not be found");
        }
        
        logger.info("Uploading from " + basedir + " to " + targetRepository.getUrl());
        final List<String> poms = FileUtils.getFileAndDirectoryNames(basedir, "**/*.pom", "", true, true, true, true);
        logger.info("poms=" + poms);
        final HashMap<String, Set<File>> mapOfArtifacts = new HashMap<String, Set<File>>();
        for (String pom : poms) {
            final File dirname = new File(FileUtils.dirname(pom));
            logger.info(pom);
            final List<File> listFiles = Arrays.asList(dirname.listFiles(new FilenameFilter() {
                                         @Override
                                         public boolean accept(File file, String name)
                                         {
                                             return !name.endsWith(".md5") && !name.endsWith(".sha1");
                                         }
                                     }));            
            final HashSet<File> hashSet = new HashSet<File>(listFiles.size());
            hashSet.addAll(listFiles);
            mapOfArtifacts.put(pom, hashSet);
        }
        logger.info(mapOfArtifacts.toString());
    }

    @Override
    public void enableLogging( Logger logger ) {
        this.logger = logger;
    }
}
