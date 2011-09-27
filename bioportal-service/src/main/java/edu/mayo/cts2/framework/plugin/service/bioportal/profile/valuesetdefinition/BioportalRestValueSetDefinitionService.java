/*
 * Copyright: (c) 2004-2011 Mayo Foundation for Medical Education and 
 * Research (MFMER). All rights reserved. MAYO, MAYO CLINIC, and the
 * triple-shield Mayo logo are trademarks and service marks of MFMER.
 *
 * Except as contained in the copyright notice above, or as used to identify 
 * MFMER as the author of this software, the trade names, trademarks, service
 * marks, or product names of the copyright holder shall not be used in
 * advertising, promotion or otherwise in connection with this software without
 * prior written authorization of the copyright holder.
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
package edu.mayo.cts2.framework.plugin.service.bioportal.profile.valuesetdefinition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import edu.mayo.cts2.framework.filter.match.AttributeResolver;
import edu.mayo.cts2.framework.filter.match.ContainsMatcher;
import edu.mayo.cts2.framework.filter.match.ExactMatcher;
import edu.mayo.cts2.framework.filter.match.ResolvableMatchAlgorithmReference;
import edu.mayo.cts2.framework.filter.match.ResolvableModelAttributeReference;
import edu.mayo.cts2.framework.model.directory.DirectoryResult;
import edu.mayo.cts2.framework.model.util.RestModelUtils;
import edu.mayo.cts2.framework.service.command.Page;
import edu.mayo.cts2.framework.service.command.restriction.ValueSetDefinitionQueryServiceRestrictions;
import edu.mayo.cts2.framework.service.meta.StandardMatchAlgorithmReference;
import edu.mayo.cts2.framework.service.meta.StandardModelAttributeReference;
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.ValueSetDefinitionQueryService;
import edu.mayo.cts2.framework.model.core.FilterComponent;
import edu.mayo.cts2.framework.model.core.MatchAlgorithmReference;
import edu.mayo.cts2.framework.model.core.ModelAttributeReference;
import edu.mayo.cts2.framework.model.core.PredicateReference;
import edu.mayo.cts2.framework.model.service.core.Query;
import edu.mayo.cts2.framework.model.valuesetdefinition.ValueSetDefinition;
import edu.mayo.cts2.framework.model.valuesetdefinition.ValueSetDefinitionDirectoryEntry;
import edu.mayo.cts2.framework.plugin.service.bioportal.identity.IdentityConverter;
import edu.mayo.cts2.framework.plugin.service.bioportal.profile.AbstractBioportalRestQueryService;
import edu.mayo.cts2.framework.plugin.service.bioportal.rest.BioportalRestService;
import edu.mayo.cts2.framework.plugin.service.bioportal.restrict.directory.ValueSetDefinitionDirectoryBuilder;
import edu.mayo.cts2.framework.plugin.service.bioportal.transform.ValueSetDefinitionTransform;

/**
 * The Class BioportalRestValueSetDefinitionService.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
@Component
@Qualifier("local")
public class BioportalRestValueSetDefinitionService 
	extends AbstractBioportalRestQueryService<edu.mayo.cts2.framework.model.service.valuesetdefinition.ValueSetDefinitionQueryService>
	implements ValueSetDefinitionQueryService {

	@Resource
	private BioportalRestService bioportalRestService;
	
	@Resource
	private ValueSetDefinitionTransform valueSetDefinitionTransform;
	
	@Resource
	private IdentityConverter identityConverter;

	/* (non-Javadoc)
	 * @see org.cts2.rest.service.ValueSetDefinitionService#getValueSetDefinitions(org.cts2.rest.service.command.Page)
	 */
	/**
	 * Gets the value set definitions.
	 *
	 * @param query the query
	 * @param filterComponent the filter component
	 * @param page the page
	 * @return the value set definitions
	 */
	private DirectoryResult<ValueSetDefinitionDirectoryEntry> getValueSetDefinitions(
			Query query,
			FilterComponent filterComponent,
			Page page) {	
		
		String xml = this.bioportalRestService.getLatestViews();
		
		ValueSetDefinitionDirectoryBuilder builder = new ValueSetDefinitionDirectoryBuilder(
					this.valueSetDefinitionTransform.transformResourceVersions(xml),
					this.getKnownMatchAlgorithmReferences(),
					this.getKnownModelAttributeReferences()
					);
	
		return builder.restrict(query).
				restrict(filterComponent).
				addStart(page.getStart()).
				addMaxToReturn(page.getMaxtoreturn()).
				resolve();	
	}
	
	/* (non-Javadoc)
	 * @see org.cts2.rest.service.ValueSetDefinitionService#getValueSetDefinitionsOfCodeSystem(org.cts2.rest.service.command.Page, java.lang.String)
	 */
	/**
	 * Gets the value set definitions of value set.
	 *
	 * @param query the query
	 * @param filterComponent the filter component
	 * @param page the page
	 * @param valueSetName the value set name
	 * @return the value set definitions of value set
	 */
	private DirectoryResult<ValueSetDefinitionDirectoryEntry> getValueSetDefinitionsOfValueSet(
			Query query,
			FilterComponent filterComponent,
			Page page,
			String valueSetName) {
		String ontologyId = this.identityConverter.valueSetNameToOntologyId(valueSetName);
		
		String xml = this.bioportalRestService.getOntologyVersionsByOntologyId(ontologyId);

		ValueSetDefinitionDirectoryBuilder builder = new ValueSetDefinitionDirectoryBuilder(
					this.valueSetDefinitionTransform.transformVersionsOfResource(xml),
					this.getKnownMatchAlgorithmReferences(),
					this.getKnownModelAttributeReferences()
					);
	
		return builder.restrict(query).
				restrict(filterComponent).
				addStart(page.getStart()).
				addMaxToReturn(page.getMaxtoreturn()).
				resolve();	
	}

	/**
	 * Gets the value set definitions of value set count.
	 *
	 * @param query the query
	 * @param filterComponent the filter component
	 * @param valueSetName the value set name
	 * @return the value set definitions of value set count
	 */
	private int getValueSetDefinitionsOfValueSetCount(
			Query query,
			FilterComponent filterComponent, 
			String valueSetName) {
		String ontologyId = this.identityConverter.valueSetNameToOntologyId(valueSetName);
		
		String xml = this.bioportalRestService.getOntologyVersionsByOntologyId(ontologyId);
		
		ValueSetDefinitionDirectoryBuilder builder = new ValueSetDefinitionDirectoryBuilder(
				this.valueSetDefinitionTransform.transformVersionsOfResource(xml),
				this.getKnownMatchAlgorithmReferences(),
				this.getKnownModelAttributeReferences()
				);
		
		return builder.restrict(query).
				restrict(filterComponent).
				count();	
	}
	
	/**
	 * Gets the value set definitions count.
	 *
	 * @param query the query
	 * @param filterComponent the filter component
	 * @return the value set definitions count
	 */
	private int getValueSetDefinitionsCount(
			Query query,
			FilterComponent filterComponent) {
		String xml = this.bioportalRestService.getLatestOntologyVersions();
		
		ValueSetDefinitionDirectoryBuilder builder = new ValueSetDefinitionDirectoryBuilder(
					this.valueSetDefinitionTransform.transformResourceVersions(xml),
					this.getKnownMatchAlgorithmReferences(),
					this.getKnownModelAttributeReferences()
					);
	
		return builder.
				restrict(query).
				restrict(filterComponent).
				count();
	}


	protected List<ResolvableMatchAlgorithmReference> getKnownMatchAlgorithmReferences(){
		List<ResolvableMatchAlgorithmReference> returnList = new ArrayList<ResolvableMatchAlgorithmReference>();
		
		MatchAlgorithmReference exactMatch = 
			StandardMatchAlgorithmReference.EXACT_MATCH.getMatchAlgorithmReference();
		
		returnList.add(
				ResolvableMatchAlgorithmReference.toResolvableMatchAlgorithmReference(exactMatch, new ExactMatcher()));
		
		MatchAlgorithmReference contains = 
			StandardMatchAlgorithmReference.CONTAINS.getMatchAlgorithmReference();
		
		returnList.add(
				ResolvableMatchAlgorithmReference.toResolvableMatchAlgorithmReference(contains, new ContainsMatcher()));
		
		return returnList;
	}
	
	protected List<ResolvableModelAttributeReference<ValueSetDefinitionDirectoryEntry>> getKnownModelAttributeReferences(){
		List<ResolvableModelAttributeReference<ValueSetDefinitionDirectoryEntry>> returnList =
			new ArrayList<ResolvableModelAttributeReference<ValueSetDefinitionDirectoryEntry>>();
		
		ResolvableModelAttributeReference<ValueSetDefinitionDirectoryEntry> refName = 
			ResolvableModelAttributeReference.toModelAttributeReference(
					StandardModelAttributeReference.RESOURCE_NAME.getModelAttributeReference(), 
					new AttributeResolver<ValueSetDefinitionDirectoryEntry>(){

						public Iterable<String> resolveAttribute(
								ValueSetDefinitionDirectoryEntry modelObject) {
							return Arrays.asList(modelObject.getResourceName());
						}
					});
		
		ResolvableModelAttributeReference<ValueSetDefinitionDirectoryEntry> refAbout = 
			ResolvableModelAttributeReference.toModelAttributeReference(
					StandardModelAttributeReference.ABOUT.getModelAttributeReference(), 
					new AttributeResolver<ValueSetDefinitionDirectoryEntry>(){

						public Iterable<String> resolveAttribute(
								ValueSetDefinitionDirectoryEntry modelObject) {
							return Arrays.asList(modelObject.getAbout());
						}
					});
		
		ResolvableModelAttributeReference<ValueSetDefinitionDirectoryEntry> refSynopsis = 
			ResolvableModelAttributeReference.toModelAttributeReference(
					StandardModelAttributeReference.RESOURCE_SYNOPSIS.getModelAttributeReference(), 
					new AttributeResolver<ValueSetDefinitionDirectoryEntry>(){

						public Iterable<String> resolveAttribute(
								ValueSetDefinitionDirectoryEntry modelObject) {
							return Arrays.asList(RestModelUtils.getResourceSynopsisValue(modelObject));
						}
					});
		
		
		returnList.add(refName);
		returnList.add(refAbout);
		returnList.add(refSynopsis);
		
		return returnList;
	}
	
	/* (non-Javadoc)
	 * @see edu.mayo.cts2.sdk.service.profile.AbstractQueryService#registerPredicateReferences()
	 */
	@Override
	protected List<? extends PredicateReference> getAvailablePredicateReferences() {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see edu.mayo.cts2.sdk.service.profile.AbstractQueryService#registerMatchAlgorithmReferences()
	 */
	@Override
	protected List<? extends MatchAlgorithmReference> getAvailableMatchAlgorithmReferences() {
		return this.getKnownMatchAlgorithmReferences();
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.sdk.service.profile.AbstractQueryService#registerModelAttributeReferences()
	 */
	@Override
	protected List<? extends ModelAttributeReference> getAvailableModelAttributeReferences() {
		return this.getKnownModelAttributeReferences();
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.sdk.service.profile.QueryService#getPropertyReference(java.lang.String)
	 */
	public PredicateReference getPropertyReference(String nameOrUri) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.sdk.service.profile.QueryService#getResourceSummaries(edu.mayo.cts2.framework.model.service.core.Query, edu.mayo.cts2.framework.model.core.FilterComponent, java.lang.Object, edu.mayo.cts2.sdk.service.command.Page)
	 */
	@Override
	public DirectoryResult<ValueSetDefinitionDirectoryEntry> getResourceSummaries(
			Query query, 
			FilterComponent filterComponent,
			ValueSetDefinitionQueryServiceRestrictions restrictions, 
			Page page) {
		String valueSetName = restrictions.getValueset();
		
		if(StringUtils.isNotBlank(valueSetName)){
			return this.getValueSetDefinitionsOfValueSet(
					query, 
					filterComponent,
					page, 
					valueSetName);
		} else {
			return this.getValueSetDefinitions(
					query, 
					filterComponent,
					page);
		}
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.sdk.service.profile.QueryService#getResourceList(edu.mayo.cts2.framework.model.service.core.Query, edu.mayo.cts2.framework.model.core.FilterComponent, java.lang.Object, edu.mayo.cts2.sdk.service.command.Page)
	 */
	@Override
	public DirectoryResult<ValueSetDefinition> getResourceList(
			Query query,
			FilterComponent filterComponent,
			ValueSetDefinitionQueryServiceRestrictions restrictions, 
			Page page) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.sdk.service.profile.QueryService#count(edu.mayo.cts2.framework.model.service.core.Query, edu.mayo.cts2.framework.model.core.FilterComponent, java.lang.Object)
	 */
	@Override
	public int count(Query query,
			FilterComponent filterComponent,
			ValueSetDefinitionQueryServiceRestrictions restrictions) {
		String valueSetName = restrictions.getValueset();
		
		if(StringUtils.isNotBlank(valueSetName)){
			return this.getValueSetDefinitionsOfValueSetCount(
					query, 
					filterComponent,
					valueSetName);
		} else {
			return this.getValueSetDefinitionsCount(
					query, 
					filterComponent);
		}
	}
}
