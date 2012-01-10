/**
 * Copyright 2012 Mirko Friedenhagen 
 */

package org.apache.maven.plugins.stage;

import java.util.regex.Pattern;

import org.codehaus.plexus.util.StringUtils;

class Gav {

    private final String groupId;

    final String artifactId;

    final String version;

    final Pattern patternFiles;

    final Pattern patternMeta;

    Gav(String groupId, String artifactId, String version) {
        this.groupId = toPath(StringUtils.split(groupId, "."));
        this.artifactId = artifactId;
        this.version = version;
        final String escapedVersion = Pattern.quote(version);
        if (artifactId.equals("*")) {
            patternFiles = compile(this.groupId, ".*", escapedVersion);
            patternMeta = compile(this.groupId, ".*", Constants.MAVEN_METADATA);
        } else {
            patternFiles = compile(this.groupId, this.artifactId, escapedVersion);
            patternMeta = compile(this.groupId, this.artifactId, Constants.MAVEN_METADATA);
        }
    }

    /**
     * Compiles a pattern from the splitted string combined with '/'.
     * 
     * @param split
     * @return
     */
    private Pattern compile(final String... split) {
        return Pattern.compile(toPath(split));
    }

    /**
     * @param split
     * @return
     */
    private String toPath(final String... split) {
        return StringUtils.join(split, "/");
    }

    public boolean matches(String file) {
        return patternFiles.matcher(file).find();// || patternMeta.matcher(file).find();
    }

    public static Gav valueOf(String version) {
        String[] gavComponents = StringUtils.split(version, ":");
        if (gavComponents.length != 3) {
            throw new IllegalArgumentException(
                    "version must have groupId:artifactId:version, where artifactId may be *");
        }
        return new Gav(gavComponents[0], gavComponents[1], gavComponents[2]);
    }
}