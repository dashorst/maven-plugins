package org.apache.maven.plugin.resources;

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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.filtering.MavenFileFilter;
import org.apache.maven.shared.filtering.MavenFilteringException;
import org.apache.maven.shared.filtering.MavenResourcesExecution;
import org.apache.maven.shared.filtering.MavenResourcesFiltering;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.StringUtils;

/**
 * Copy resources for the main source code to the main output directory.
 *
 * @author <a href="michal.maczka@dimatics.com">Michal Maczka</a>
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @author Andreas Hoheneder
 * @author William Ferguson
 * @version $Id$
 * @goal resources
 * @phase process-resources
 */
public class ResourcesMojo
    extends AbstractMojo
{

    /**
     * The character encoding scheme to be applied when filtering resources.
     *
     * @parameter expression="${encoding}" default-value="${project.build.sourceEncoding}"
     */
    protected String encoding;

    /**
     * The output directory into which to copy the resources.
     *
     * @parameter expression="${project.build.outputDirectory}"
     * @required
     */
    private File outputDirectory;

    /**
     * The list of resources we want to transfer.
     *
     * @parameter expression="${project.resources}"
     * @required
     * @readonly
     */
    private List resources;

    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * The list of additional filter properties files to be used along with System and project
     * properties, which would be used for the filtering.
     * <br/>
     * See also: {@link ResourcesMojo#extraFilters}.
     *
     * @parameter expression="${project.build.filters}"
     */
    protected List filters;
    
    /**
     * The list of extra filter properties files to be used along with System properties,
     * project properties, and filter properties files specified in the POM build/filters section,
     * which should be used for the filtering during the current mojo execution.
     * <br/>
     * Normally, these will be configured from a plugin's execution section, to provide a different
     * set of filters for a particular execution. For instance, starting in Maven 2.2.0, you have the
     * option of configuring executions with the id's <code>default-resources</code> and 
     * <code>default-testResources</code> to supply different configurations for the two 
     * different types of resources. By supplying <code>extraFilters</code> configurations, you
     * can separate which filters are used for which type of resource.
     *
     * @parameter
     * @since 2.4
     */
    protected List extraFilters;
    
    /**
     * 
     * @component role="org.apache.maven.shared.filtering.MavenResourcesFiltering" role-hint="default"
     * @required
     */    
    protected MavenResourcesFiltering mavenResourcesFiltering;    
    
    /**
     * @parameter expression="${session}"
     * @readonly
     * @required
     */
    protected MavenSession session;   
    
    /**
     * Expression preceded with the String won't be interpolated 
     * \${foo} will be replaced with ${foo}
     * @parameter expression="${maven.resources.escapeString}"
     * @since 2.3
     */    
    protected String escapeString;
    
    /**
     * Overwrite existing files even if the destination files are newer.
     * @parameter expression="${maven.resources.overwrite}" default-value="false"
     * @since 2.3
     */
    private boolean overwrite;
    
    /**
     * Copy any empty directories included in the Ressources.
     * @parameter expression="${maven.resources.includeEmptyDirs}" default-value="false"
     * @since 2.3
     */    
    protected boolean includeEmptyDirs;
    
    /**
     * Additionnal file extensions to not apply filtering (already defined are : jpg, jpeg, gif, bmp, png)
     * @parameter 
     * @since 2.3
     */
    protected List nonFilteredFileExtensions;
    
    /**
     * Whether to escape backslashes and colons in windows-style paths.
     * @parameter expression="${maven.resources.escapeWindowsPaths} default-value="true"
     * @since 2.4
     */
    protected boolean escapeWindowsPaths;
    
    /**
     * @component role-hint="default"
     */
    private MavenFileFilter mavenFileFilter;

    public void execute()
        throws MojoExecutionException
    {
        try
        {
            
            if ( StringUtils.isEmpty( encoding ) && isFilteringEnabled( getResources() ) )
            {
                getLog().warn(
                               "File encoding has not been set, using platform encoding " + ReaderFactory.FILE_ENCODING
                                   + ", i.e. build is platform dependent!" );
            }
            
            List filters = getFilters();

            MavenResourcesExecution mavenResourcesExecution = new MavenResourcesExecution( getResources(), 
                                                                                           getOutputDirectory(),
                                                                                           project, encoding, filters,
                                                                                           Collections.EMPTY_LIST,
                                                                                           session );
            
            List filterWrappers = mavenFileFilter.getDefaultFilterWrappers( project, filters, escapeWindowsPaths,
                                                                            session, mavenResourcesExecution, null );
            
            mavenResourcesExecution.setFilterWrappers( filterWrappers );
            mavenResourcesExecution.setUseDefaultFilterWrappers( false );
            
            mavenResourcesExecution.setEscapeString( escapeString );
            mavenResourcesExecution.setOverwrite( overwrite );
            mavenResourcesExecution.setIncludeEmptyDirs( includeEmptyDirs );
            if ( nonFilteredFileExtensions != null )
            {
                mavenResourcesExecution.setNonFilteredFileExtensions( nonFilteredFileExtensions );
            }
            mavenResourcesFiltering.filterResources( mavenResourcesExecution );
        }
        catch ( MavenFilteringException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }
    }
    
    protected List getFilters()
    {
        if ( extraFilters == null || extraFilters.isEmpty() )
        {
            return filters;
        }
        else
        {
            List result = new ArrayList( extraFilters );
            
            if ( filters != null && !filters.isEmpty() )
            {
                result.addAll( filters );
            }
            
            return result;
        }
    }

    /**
     * Determines whether filtering has been enabled for any resource.
     * 
     * @param resources The set of resources to check for filtering, may be <code>null</code>.
     * @return <code>true</code> if at least one resource uses filtering, <code>false</code> otherwise.
     */
    private boolean isFilteringEnabled( Collection resources )
    {
        if ( resources != null )
        {
            for ( Iterator i = resources.iterator(); i.hasNext(); )
            {
                Resource resource = (Resource) i.next();
                if ( resource.isFiltering() )
                {
                    return true;
                }
            }
        }
        return false;
    }

    public List getResources()
    {
        return resources;
    }

    public void setResources( List resources )
    {
        this.resources = resources;
    }

    public File getOutputDirectory()
    {
        return outputDirectory;
    }

    public void setOutputDirectory( File outputDirectory )
    {
        this.outputDirectory = outputDirectory;
    }

    public boolean isOverwrite()
    {
        return overwrite;
    }

    public void setOverwrite( boolean overwrite )
    {
        this.overwrite = overwrite;
    }

    public boolean isIncludeEmptyDirs()
    {
        return includeEmptyDirs;
    }

    public void setIncludeEmptyDirs( boolean includeEmptyDirs )
    {
        this.includeEmptyDirs = includeEmptyDirs;
    }
    
    
}
