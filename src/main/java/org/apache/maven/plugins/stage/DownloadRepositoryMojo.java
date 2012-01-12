package org.apache.maven.plugins.stage;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.plugin.AbstractMojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.wagon.WagonException;
import org.apache.maven.wagon.repository.Repository;

/**
 * Copies artifacts from one repository to another repository.
 * 
 * @author Mirko Friedenhagen
 *
 * @requiresProject false
 * @goal download
 */
public class DownloadRepositoryMojo extends AbstractMojo {
    
    private static final Pattern ALT_REPO_SYNTAX_PATTERN = Pattern.compile( "(.+)::(.+)::(.+)" );

    /**
     * @component
     */

    private RepositoryDownloader repositoryDownloader;
    
    /**
     * Component used to create a repository.
     *
     * @component
     */
    ArtifactRepositoryFactory repositoryFactory;

    /**
     * Map that contains the layouts.
     *
     * @component role="org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout"
     */
    private Map repositoryLayouts;

 
   /**
     * Specifies an alternative repository to which the project artifacts should be deployed ( other
     * than those specified in &lt;distributionManagement&gt; ).
     * 
     * <br/>
     * 
     * Format: id::layout::url
     * 
     * @parameter expression="${stage.sourceRepository}"
     * @required
     */
    private String sourceRepository;
    
    /**
     * The GAV coordinates of the artifact that is to be copied. This is a comma separated list of coordinates like
     * <tt>de.friedenhagen.multimodule:*:1.24,de.friedenhagen.multimodule:parent:1.25</tt>
     * <p>
     * <b>Note:</b> You may enter '*' to copy all artifacts with a specific groupId.
     * </p>
     * 
     * @parameter expression="${gavs}"
     * @required
     */
    private String[] gavs;

    /**
     * @return the gavs given on the command line.
     */
    String[] getGavs() {
        return gavs;
    }

    /**
     * The repository copier to use.
     * 
     * @component
     */
    private RepositoryCopier copier;

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (getGavs().length == 0) {
            throw new MojoExecutionException("Need to have gavs");
        }
        getLog().info("gavs=" + Arrays.toString(getGavs()));
        final ArtifactRepository repository = getSourceRepository();
        try {
            repositoryDownloader.download(repository, gavs);
        } catch (WagonException e) {
            throw new MojoExecutionException("Error", e);
        } catch (IOException e) {
            throw new MojoExecutionException("Error", e);
        }
        
    }
    private ArtifactRepository getSourceRepository()
            throws MojoExecutionException, MojoFailureException {
        ArtifactRepository repo = null;

        if (sourceRepository != null) {
            getLog().info("Using source repository " + sourceRepository);

            Matcher matcher = ALT_REPO_SYNTAX_PATTERN.matcher(sourceRepository);

            if (!matcher.matches()) {
                throw new MojoFailureException(sourceRepository, "Invalid syntax for repository.",
                        "Invalid syntax for alternative repository. Use \"id::layout::url\".");
            } else {
                String id = matcher.group(1).trim();
                String layout = matcher.group(2).trim();
                String url = matcher.group(3).trim();

                ArtifactRepositoryLayout repoLayout = getLayout(layout);

                repo = repositoryFactory.createDeploymentArtifactRepository(id, url, repoLayout, true);
            }
        }

        if (repo == null) {
            String msg = "Deployment failed: invalid or missing '-DsourceRepository=id::layout::url' parameter";

            throw new MojoExecutionException(msg);
        }

        return repo;
    }

    ArtifactRepositoryLayout getLayout(String id)
            throws MojoExecutionException {
        ArtifactRepositoryLayout layout = (ArtifactRepositoryLayout) repositoryLayouts.get(id);

        if (layout == null) {
            throw new MojoExecutionException("Invalid repository layout: " + id);
        }

        return layout;
    }

}
