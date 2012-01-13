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

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 *
 * @author mirko
 */
public abstract class ReadOnlyRepositoryMojo extends AbstractMojo {
    
    private static final Pattern ALT_REPO_SYNTAX_PATTERN = Pattern.compile( "(.+)::(.+)::(.+)" );

    /**
     * Map that contains the layouts.
     *
     * @component role="org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout"
     */
    private Map repositoryLayouts;

 
   /**
     * Specifies an repository from which the project artifacts should be downloaded.
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
     * @parameter expression="${stage.gavs}"
     * @required
     */
    private String[] gavs;


    /**
     * Component used to create a repository.
     *
     * @component
     */
    ArtifactRepositoryFactory repositoryFactory;

    ArtifactRepository getSourceRepository()
            throws MojoExecutionException, MojoFailureException {
        ArtifactRepository repo = null;

        if (sourceRepository != null) {
            getLog().info("Using source repository " + sourceRepository);

            Matcher matcher = ALT_REPO_SYNTAX_PATTERN.matcher(sourceRepository);

            if (!matcher.matches()) {
                throw new MojoFailureException(sourceRepository, "Invalid syntax for repository.",
                        "Invalid syntax for sourceRepository. Use \"id::layout::url\".");
            } else {
                String id = matcher.group(1).trim();
                String layout = matcher.group(2).trim();
                String url = matcher.group(3).trim();

                ArtifactRepositoryLayout repoLayout = getLayout(layout);

                repo = repositoryFactory.createDeploymentArtifactRepository(id, url, repoLayout, true);
            }
        }

        if (repo == null) {
            String msg = "Deployment failed: invalid or missing '-Dstage.sourceRepository=id::layout::url' parameter";

            throw new MojoExecutionException(msg);
        }

        return repo;
    }


    /**
     * @return the gavs given on the command line.
     */
    String[] getGavs() {
        return gavs;
    }

    ArtifactRepositoryLayout getLayout(String id) throws MojoExecutionException {
        ArtifactRepositoryLayout layout = (ArtifactRepositoryLayout) repositoryLayouts.get(id);
        if (layout == null) {
            throw new MojoExecutionException("Invalid repository layout: " + id);
        }
        return layout;
    }
    
}
