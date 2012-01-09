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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.wagon.WagonException;
import org.apache.maven.wagon.repository.Repository;

/**
 * Copies artifacts from one repository to another repository.
 * 
 * @author Jason van Zyl
 * @requiresProject false
 * @goal copy
 */
public class CopyRepositoryMojo extends AbstractRepositoryMojo {
    /**
     * The repository copier to use.
     * 
     * @component
     */
    private RepositoryCopier copier;

    public void execute() throws MojoExecutionException {
        try {
            if (getGavs().length == 0) {
                throw new MojoExecutionException("Need to have gavs");
            }
            getLog().info("gavs=" + Arrays.toString(getGavs()));
            Repository sourceRepository = new Repository(getSourceRepositoryId(), getSource());
            Repository targetRepository = new Repository(getTargetRepositoryId(), getTarget());
            copier.copy(sourceRepository, targetRepository, getGavs());
        } catch (IOException e) {
            throw new MojoExecutionException("Error copying repository from " + getSource() + " to " + getTarget(), e);
        } catch (WagonException e) {
            throw new MojoExecutionException("Error copying repository from " + getSource() + " to " + getTarget(), e);
        }
    }
}
