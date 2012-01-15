/**
 * Copyright 2012 Mirko Friedenhagen 
 */

package org.codehaus.mojo.stage2;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.codehaus.plexus.util.StringUtils;

class Gav {

    final String groupIdPath;

    final String groupId;

    final String artifactId;

    final String version;

    final Pattern patternFiles;

    final Pattern patternMeta;

    Gav(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        final String escapedVersion = Pattern.quote(version);
        groupIdPath = toPath(StringUtils.split(groupId, "."));
        if (artifactId.equals("*")) {
            patternFiles = compile(groupIdPath, ".*", escapedVersion);
            patternMeta = compile(groupIdPath, ".*", Constants.MAVEN_METADATA);
        } else {
            patternFiles = compile(groupIdPath, this.artifactId, escapedVersion);
            patternMeta = compile(groupIdPath, this.artifactId, Constants.MAVEN_METADATA);
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
        return patternFiles.matcher(file).find() || patternMeta.matcher(file).find();
    }

    @Override
    public String toString() {
        return String.format("gav=%s/%s/%s", groupIdPath, artifactId, version);
    }

    public String getEncodedPath() {
        try {
            final String name = artifactId.equals("*") ? "ALL_ARTIFACTS" : artifactId;
            return URLEncoder.encode(groupId + ":" + name + ":" + version, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }        
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