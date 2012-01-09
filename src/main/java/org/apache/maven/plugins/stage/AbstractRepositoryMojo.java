/**
 * Copyright 2012 Mirko Friedenhagen 
 */

package org.apache.maven.plugins.stage;

import org.apache.maven.plugin.AbstractMojo;

/**
 * @author mirko
 *
 */
public abstract class AbstractRepositoryMojo extends AbstractMojo {

    /**
     * The URL to the source repository.
     * 
     * @parameter expression="${source}"
     */
    private String source;
    /**
     * The URL to the target repository.
     * 
     * @parameter expression="${target}"
     */
    private String target;
    /**
     * The id of the source repository, required if you need the configuration from the user settings.
     * 
     * @parameter expression="${sourceRepositoryId}" default-value="source"
     */
    private String sourceRepositoryId;
    /**
     * The id of the target repository, required if you need the configuration from the user settings.
     * 
     * @parameter expression="${targetRepositoryId}" default-value="target"
     */
    private String targetRepositoryId;
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
     * @return the source repository url.
     */
    String getSource() {
        return source;
    }

    /**
     * @return the target repository url.
     */
    String getTarget() {
        return target;
    }

    /**
     * @return the sourceRepositoryId
     */
    String getSourceRepositoryId() {
        return sourceRepositoryId;
    }

    /**
     * @return the targetRepositoryId
     */
    String getTargetRepositoryId() {
        return targetRepositoryId;
    }

    /**
     * @return the gavs given on the command line.
     */
    String[] getGavs() {
        return gavs;
    }

}
