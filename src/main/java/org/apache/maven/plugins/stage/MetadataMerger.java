/**
 * Copyright 2012 Mirko Friedenhagen 
 */

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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Writer;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * @author Mirko Friedenhagen
 */
class MetadataMerger {

    private final File existingMetadataFile;

    private final MetadataXpp3Reader reader = new MetadataXpp3Reader();

    private final MetadataXpp3Writer writer = new MetadataXpp3Writer();

    /**
     * @param existingMetadataFile
     */
    MetadataMerger(final File existingMetadataFile) {
        this.existingMetadataFile = existingMetadataFile;
    }

    void mergeMetadata() throws IOException {
        // Existing Metadata in target stage
        final File stagedMetadataFile = new File(existingMetadataFile.getParentFile(), Constants.MAVEN_METADATA);
        final Metadata existingMetadata = readFromFile(existingMetadataFile);
        final Metadata stagedMetadata = readFromFile(stagedMetadataFile);
        existingMetadata.merge(stagedMetadata);
        stagedMetadataFile.delete();
        // Write back the merged data to the staged file.
        final Writer stagedMetadataFileWriter = new FileWriter(stagedMetadataFile);
        try {
            writer.write(stagedMetadataFileWriter, existingMetadata);
        } finally {
            stagedMetadataFileWriter.close();
        }
        // Regenerate the checksums as they will be different after the merger
        try {
            final File md5 = new File(stagedMetadataFile.getParentFile(), Constants.MAVEN_METADATA + ".md5");
            FileUtils.fileWrite(md5.getAbsolutePath(), checksum(stagedMetadataFile, Constants.MD5));
            final File sha1 = new File(stagedMetadataFile.getParentFile(), Constants.MAVEN_METADATA + ".sha1");
            FileUtils.fileWrite(sha1.getAbsolutePath(), checksum(stagedMetadataFile, Constants.SHA1));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

    }

    private Metadata readFromFile(File metadataFile) throws IOException {
        final Reader fileReader = new FileReader(metadataFile);
        try {
            return reader.read(fileReader);
        } catch (XmlPullParserException e) {
            throw new IOException("Metadata file is corrupt " + metadataFile + " Reason: " + e.getMessage());
        } finally {
            fileReader.close();
        }
    }

    private String checksum(File file, String type) throws IOException, NoSuchAlgorithmException {
        final MessageDigest digest = MessageDigest.getInstance(type);

        final InputStream is = new FileInputStream(file);

        final byte[] buf = new byte[8192];

        int i;
        try {
            while ((i = is.read(buf)) > 0) {
                digest.update(buf, 0, i);
            }
        } finally {
            is.close();
        }

        return encode(digest.digest());
    }

    protected String encode(byte[] binaryData) {
        if (binaryData.length != 16 && binaryData.length != 20) {
            int bitLength = binaryData.length * 8;
            throw new IllegalArgumentException("Unrecognised length for binary data: " + bitLength + " bits");
        }

        String retValue = "";

        for (int i = 0; i < binaryData.length; i++) {
            String t = Integer.toHexString(binaryData[i] & 0xff);

            if (t.length() == 1) {
                retValue += ("0" + t);
            } else {
                retValue += t;
            }
        }

        return retValue.trim();
    }

}