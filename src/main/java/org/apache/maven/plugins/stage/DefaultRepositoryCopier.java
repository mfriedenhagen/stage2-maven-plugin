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
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.maven.artifact.manager.WagonConfigurationException;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Writer;
import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.UnsupportedProtocolException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.apache.maven.wagon.repository.Repository;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * @author Jason van Zyl
 * @plexus.component
 */
public class DefaultRepositoryCopier
    implements LogEnabled, RepositoryCopier
{
    static class Gav {

        final String groupId;
        final String artifactId;
        final String version;
        final Pattern patternFiles;
        final Pattern patternMeta;

        Gav(String groupId, String artifactId, String version) {
            this.groupId = toPath(StringUtils.split(groupId, "."));
            this.artifactId = artifactId;
            this.version = version;
            final String escapedVersion = escape(version);
            if (artifactId.equals("*")) {
                patternFiles = compile(this.groupId, ".*", escapedVersion);
                patternMeta = compile(this.groupId, ".*", MAVEN_METADATA);
            } else {
                patternFiles = compile(this.groupId, this.artifactId, escapedVersion);
                patternMeta = compile(this.groupId, this.artifactId, MAVEN_METADATA);
            }
        }

        /**
         * Compiles a pattern from the splitted string combined with '/'.
         *
         * @param split
         * @return
         */
        private Pattern compile(final String...split) {
            return Pattern.compile(toPath(split));
        }

        /**
         * @param split
         * @return
         */
        private String toPath(final String... split) {
            return StringUtils.join(split, "/");
        }

        /**
         * Escape regex patterns in the string.
         *
         * @param version
         * @return
         */
        private String escape(String version) {
            return Pattern.quote(version);
        }

        public boolean matches(String file) {
            return patternFiles.matcher(file).find() || patternMeta.matcher(file).find();
        }

        public static Gav valueOf(String version) {
            String[] gavComponents = StringUtils.split(version, ":");
            if ( gavComponents.length!=3 ) {
                throw new IllegalArgumentException("version must have groupId:artifactId:version, where artifactId may be *");
            }
            return new Gav(gavComponents[0], gavComponents[1], gavComponents[2]);
        }
    }

    private MetadataXpp3Reader reader = new MetadataXpp3Reader();

    private MetadataXpp3Writer writer = new MetadataXpp3Writer();

    /** @plexus.requirement */
    private WagonManager wagonManager;

    private Logger logger;

    public void copy( Repository sourceRepository, Repository targetRepository, String[] gavStrings )
        throws WagonException, IOException
    {
        for (String gavString : gavStrings) {
            final Gav gav = Gav.valueOf(gavString);
            copy(sourceRepository, targetRepository, gav);
        }
    }

    /**
     * @param sourceRepository
     * @param targetRepository
     * @param gav
     * @throws IOException
     * @throws UnsupportedProtocolException
     * @throws WagonConfigurationException
     * @throws ConnectionException
     * @throws AuthenticationException
     * @throws TransferFailedException
     * @throws ResourceDoesNotExistException
     * @throws AuthorizationException
     */
    void copy(Repository sourceRepository, Repository targetRepository, final Gav gav) throws IOException,
            UnsupportedProtocolException, WagonConfigurationException, ConnectionException, AuthenticationException,
            TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        String prefix = "staging-plugin";

        // Work directory

        File basedir = new File( System.getProperty( "java.io.tmpdir" ), prefix + "-" + gav.version );

        logger.info( "Writing all output to " + basedir );

        FileUtils.deleteDirectory( basedir );

        basedir.mkdirs();

        Wagon sourceWagon = wagonManager.getWagon( sourceRepository );

        AuthenticationInfo sourceAuth = wagonManager.getAuthenticationInfo( sourceRepository.getId() );

        sourceWagon.connect( sourceRepository, sourceAuth );

        logger.info( "Scanning source repository for all files." );

        List<String> rawFiles = new ArrayList<String>();

        scan( sourceWagon, "", rawFiles );

        logger.debug("all files found in staging repository" + rawFiles);

        logger.info( "Scanned source repository for all files, found " +  rawFiles.size() + " files.");

        List<String> files = new ArrayList<String>();
        for (String file : rawFiles) {
            if (gav.matches(file)) {
                files.add(file);
            }
        }

        // Need to sort the files, otherwise the sha1 or md5 might be uploaded before the concrete files,
        // which will result in an error.
        Collections.sort(files);
        logger.info("Found " +  files.size() +  " matching files: " + files );

        logger.info( "Downloading files from the source repository to " + basedir );

        for ( String s : files )
        {
            File f = new File( basedir, s );

            FileUtils.mkdir( f.getParentFile().getAbsolutePath() );

            logger.debug( "Downloading file from the source repository: " + s );

            sourceWagon.get( s, f );
        }

        logger.info( "Downloaded "  + files.size() + "  files from the source repository to " + basedir );

        // ----------------------------------------------------------------------------
        // Now all the files are present locally and now we are going to grab the
        // metadata files from the targetRepositoryUrl and pull those down locally
        // so that we can merge the metadata.
        // ----------------------------------------------------------------------------

        logger.info( "Downloading metadata from the target repository." );

        Wagon targetWagon = wagonManager.getWagon( targetRepository );

        AuthenticationInfo targetAuth = wagonManager.getAuthenticationInfo( targetRepository.getId() );

        targetWagon.connect( targetRepository, targetAuth );

        final String targetRepositoryUrl = targetRepository.getUrl();
        final URI targetRepositoryUri = URI.create(targetRepositoryUrl.endsWith("/") ? targetRepositoryUrl : targetRepositoryUrl + "/");

        for ( String s : files )
        {

            if ( s.startsWith( "/" ) )
            {
                s = s.substring( 1 );
            }

            if ( s.endsWith( MAVEN_METADATA ) )
            {
                File emf = new File( basedir, s + IN_PROCESS_MARKER );

                try
                {
                    targetWagon.get( s, emf );
                }
                catch ( ResourceDoesNotExistException e )
                {
                    // We don't have an equivalent on the targetRepositoryUrl side because we have something
                    // new on the sourceRepositoryUrl side so just skip the metadata merging.

                    continue;
                }

                try
                {
                    mergeMetadata( emf );
                }
                catch ( XmlPullParserException e )
                {
                    throw new IOException( "Metadata file is corrupt " + s + " Reason: " + e.getMessage() );
                }
            }

            logger.info("Deploy " + targetRepositoryUri.resolve(s));
            targetWagon.put(new File(basedir, s), s);
        }
    }

    private void mergeMetadata( File existingMetadata )
        throws IOException, XmlPullParserException
    {
        // Existing Metadata in target stage

        Reader existingMetadataReader = new FileReader( existingMetadata );

        Metadata existing = reader.read( existingMetadataReader );

        // Staged Metadata

        File stagedMetadataFile = new File( existingMetadata.getParentFile(), MAVEN_METADATA );

        Reader stagedMetadataReader = new FileReader( stagedMetadataFile );

        Metadata staged = reader.read( stagedMetadataReader );

        // Merge

        existing.merge( staged );

        Writer writer = new FileWriter( existingMetadata );

        this.writer.write( writer, existing );

        IOUtil.close( writer );

        IOUtil.close( stagedMetadataReader );

        IOUtil.close( existingMetadataReader );

        // Mark all metadata as in-process and regenerate the checksums as they will be different
        // after the merger

        try
        {
            File oldMd5 = new File( existingMetadata.getParentFile(), MAVEN_METADATA + ".md5" );

            oldMd5.delete();

            File newMd5 = new File( existingMetadata.getParentFile(), MAVEN_METADATA + ".md5" );

            FileUtils.fileWrite( newMd5.getAbsolutePath(), checksum( existingMetadata, MD5 ) );

            File oldSha1 = new File( existingMetadata.getParentFile(), MAVEN_METADATA + ".sha1" );

            oldSha1.delete();

            File newSha1 = new File( existingMetadata.getParentFile(), MAVEN_METADATA + ".sha1" );

            FileUtils.fileWrite( newSha1.getAbsolutePath(), checksum( existingMetadata, SHA1 ) );

        }
        catch ( NoSuchAlgorithmException e )
        {
            throw new RuntimeException( e );
        }

        // We have the new merged copy so we're good

        stagedMetadataFile.delete();

        existingMetadata.renameTo(stagedMetadataFile);
    }

    private String checksum( File file,
                             String type )
        throws IOException, NoSuchAlgorithmException
    {
        MessageDigest md5 = MessageDigest.getInstance( type );

        InputStream is = new FileInputStream( file );

        byte[] buf = new byte[8192];

        int i;

        while ( ( i = is.read( buf ) ) > 0 )
        {
            md5.update( buf, 0, i );
        }

        IOUtil.close( is );

        return encode( md5.digest() );
    }

    protected String encode( byte[] binaryData )
    {
        if ( binaryData.length != 16 && binaryData.length != 20 )
        {
            int bitLength = binaryData.length * 8;
            throw new IllegalArgumentException( "Unrecognised length for binary data: " + bitLength + " bits" );
        }

        String retValue = "";

        for ( int i = 0; i < binaryData.length; i++ )
        {
            String t = Integer.toHexString( binaryData[i] & 0xff );

            if ( t.length() == 1 )
            {
                retValue += ( "0" + t );
            }
            else
            {
                retValue += t;
            }
        }

        return retValue.trim();
    }

    private void scan( Wagon wagon,
                       String basePath,
                       List<String> collected )
    {
        try
        {
            if ( basePath.indexOf( ".svn" ) >= 0 || basePath.startsWith(".index") || basePath.startsWith("/.index") ) {
            } else {
                @SuppressWarnings("unchecked")
                List<String> files = wagon.getFileList( basePath );

                if ( files.isEmpty() )
                {
                    collected.add( basePath );
                }
                else
                {
                    for ( String file : files )
                    {
                        logger.debug( "Found file in the source repository: " + file );
                        scan( wagon, basePath + file, collected );
                    }
                }
            }
        }
        catch ( TransferFailedException e )
        {
            throw new RuntimeException( e );
        }
        catch ( ResourceDoesNotExistException e )
        {
            // is thrown when calling getFileList on a file
            collected.add( basePath );
        }
        catch ( AuthorizationException e )
        {
            throw new RuntimeException( e );
        }

    }

    public void enableLogging( Logger logger )
    {
        this.logger = logger;
    }
}
