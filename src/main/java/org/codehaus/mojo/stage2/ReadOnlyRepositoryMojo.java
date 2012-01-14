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
public abstract class ReadOnlyRepositoryMojo extends AbstractRepositoryMojo {

 
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

    ArtifactRepository getSourceRepository()
            throws MojoExecutionException, MojoFailureException {

        return getRepository(sourceRepository, "stage.sourceRepository");
    }
    
}
