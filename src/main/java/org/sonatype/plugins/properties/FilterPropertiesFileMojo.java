package org.sonatype.plugins.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.filtering.MavenProjectValueSource;
import org.codehaus.plexus.interpolation.InterpolationException;
import org.codehaus.plexus.interpolation.StringSearchInterpolator;
import org.codehaus.plexus.interpolation.ValueSource;

/**
 * @goal filter-file
 */
public class FilterPropertiesFileMojo
    extends AbstractMojo
{

    /**
     * @parameter expression="${project}"
     */
    private MavenProject mavenProject;

    /**
     * @parameter expression="${session}"
     */
    private MavenSession mavenSession;

    /**
     * @parameter
     * @read-only
     * @required
     */
    private File in;

    /**
     * @parameter
     * @read-only
     * @required
     */
    private File outDir;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {

        final Properties baseProps = new Properties();
        if ( mavenProject.getProperties() != null )
        {
            baseProps.putAll( mavenProject.getProperties() );
        }
        if ( mavenSession.getExecutionProperties() != null )
        {
            baseProps.putAll( mavenSession.getExecutionProperties() );
        }

        StringSearchInterpolator interpolator = new StringSearchInterpolator();
        interpolator.addValueSource( new MavenProjectValueSource( mavenProject, false /* escapedBackslashesInFilePath */) );
        interpolator.addValueSource( new ValueSource()
        {
            public Object getValue( String expression )
            {
                return baseProps.getProperty( expression );
            }

            public void clearFeedback()
            {
            }

            public List getFeedback()
            {
                return Collections.EMPTY_LIST;
            }
        } );

        Properties p = new Properties();

        try
        {
            InputStream is = new FileInputStream( in );
            try
            {
                p.load( is );
            }
            finally
            {
                is.close();
            }

            for ( Map.Entry entry : p.entrySet() )
            {
                String value = (String) entry.getValue();
                entry.setValue( interpolator.interpolate( value ) );
            }

            OutputStream os = new FileOutputStream( new File( outDir, in.getName() ) );
            try
            {
                p.store( os, null );
            }
            finally
            {
                os.close();
            }
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Could not filter properties file", e );
        }
        catch ( InterpolationException e )
        {
            throw new MojoExecutionException( "Could not filter properties file", e );
        }

    }
}
