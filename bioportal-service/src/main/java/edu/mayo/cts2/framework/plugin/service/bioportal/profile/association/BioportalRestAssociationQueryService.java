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
package edu.mayo.cts2.framework.plugin.service.bioportal.profile.association;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import edu.mayo.cts2.framework.filter.match.AttributeResolver;
import edu.mayo.cts2.framework.filter.match.ContainsMatcher;
import edu.mayo.cts2.framework.filter.match.ExactMatcher;
import edu.mayo.cts2.framework.filter.match.ResolvableMatchAlgorithmReference;
import edu.mayo.cts2.framework.filter.match.ResolvableModelAttributeReference;
import edu.mayo.cts2.framework.model.directory.DirectoryResult;
import edu.mayo.cts2.framework.service.command.Page;
import edu.mayo.cts2.framework.service.command.restriction.AssociationQueryServiceRestrictions;
import edu.mayo.cts2.framework.service.meta.StandardMatchAlgorithmReference;
import edu.mayo.cts2.framework.service.meta.StandardModelAttributeReference;
import edu.mayo.cts2.framework.service.profile.association.AssociationQueryService;
import edu.mayo.cts2.framework.service.profile.entitydescription.id.EntityDescriptionId;
import edu.mayo.cts2.framework.model.association.Association;
import edu.mayo.cts2.framework.model.association.AssociationDirectoryEntry;
import edu.mayo.cts2.framework.model.core.FilterComponent;
import edu.mayo.cts2.framework.model.core.MatchAlgorithmReference;
import edu.mayo.cts2.framework.model.core.ModelAttributeReference;
import edu.mayo.cts2.framework.model.core.PredicateReference;
import edu.mayo.cts2.framework.model.entity.EntityDirectoryEntry;
import edu.mayo.cts2.framework.model.service.core.Query;
import edu.mayo.cts2.framework.plugin.service.bioportal.identity.IdentityConverter;
import edu.mayo.cts2.framework.plugin.service.bioportal.profile.AbstractBioportalRestQueryService;
import edu.mayo.cts2.framework.plugin.service.bioportal.rest.BioportalRestService;
import edu.mayo.cts2.framework.plugin.service.bioportal.restrict.directory.ParentOrChildOfEntityDirectoryBuilder;
import edu.mayo.cts2.framework.plugin.service.bioportal.transform.AssociationTransform;

/**
 * The Class BioportalRestAssociationQueryService.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
@Component
public class BioportalRestAssociationQueryService 
	extends AbstractBioportalRestQueryService<edu.mayo.cts2.framework.model.service.association.AssociationQueryService>
	implements AssociationQueryService {

	@Resource
	private BioportalRestService bioportalRestService;
	
	@Resource
	private IdentityConverter identityConverter;
	
	@Resource
	private AssociationTransform associationTransform;
	
	private static final String CHILDREN_PREDICATE = "SubClass";
	private static final String PARENT_PREDICATE = "SuperClass";

	/**
	 * Do get associations of entity.
	 *
	 * @param codeSystemName the code system name
	 * @param codeSystemVersionName the code system version name
	 * @param entity the entity
	 * @param predicateName the predicate name
	 * @param filterComponent the filter component
	 * @param page the page
	 * @return the directory result
	 */
	protected DirectoryResult<EntityDirectoryEntry> doGetAssociationsOfEntity(
			final String codeSystemName, 
			final String codeSystemVersionName, 
			final String entity,
			final String predicateName,
			FilterComponent filterComponent,
			Page page) {

		String ontologyVersionId = 
			this.identityConverter.codeSystemVersionNameToOntologyVersionId(
					codeSystemVersionName);

		final String xml = this.bioportalRestService.
			getEntityByOntologyVersionIdAndEntityId(ontologyVersionId, entity);
		
		ParentOrChildOfEntityDirectoryBuilder builder;
		try {
			builder = new ParentOrChildOfEntityDirectoryBuilder(
					this.associationTransform.transformEntitiesForRelationship(
							xml, 
							codeSystemName,
							codeSystemVersionName, 
							predicateName),
						this.getKnownMatchAlgorithmReferences(),
						this.getKnownModelAttributeReferences()
					);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		return builder.restrict(filterComponent).
			addStart(page.getStart()).
			addMaxToReturn(page.getMaxtoreturn()).
			resolve();
	}

	protected List<ResolvableModelAttributeReference<EntityDirectoryEntry>> getKnownModelAttributeReferences() {
		List<ResolvableModelAttributeReference<EntityDirectoryEntry>> returnList =
			new ArrayList<ResolvableModelAttributeReference<EntityDirectoryEntry>>();
		
		ResolvableModelAttributeReference<EntityDirectoryEntry> refName = 
			ResolvableModelAttributeReference.toModelAttributeReference(
					StandardModelAttributeReference.RESOURCE_NAME.getModelAttributeReference(), 
					new AttributeResolver<EntityDirectoryEntry>(){

						public Iterable<String> resolveAttribute(
								EntityDirectoryEntry modelObject) {
							return Arrays.asList(modelObject.getResourceName());
						}
					});
		
		ResolvableModelAttributeReference<EntityDirectoryEntry> refAbout = 
			ResolvableModelAttributeReference.toModelAttributeReference(
					StandardModelAttributeReference.ABOUT.getModelAttributeReference(), 
					new AttributeResolver<EntityDirectoryEntry>(){

						public Iterable<String> resolveAttribute(
								EntityDirectoryEntry modelObject) {
							return Arrays.asList(modelObject.getAbout());
						}
					});
	
		
		returnList.add(refName);
		returnList.add(refAbout);
		
		return returnList;
	}

	protected List<ResolvableMatchAlgorithmReference> getKnownMatchAlgorithmReferences() {
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
	public DirectoryResult<AssociationDirectoryEntry> getResourceSummaries(
			Query query, 
			FilterComponent filterComponent,
			AssociationQueryServiceRestrictions restrictions, 
			Page page) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.sdk.service.profile.QueryService#getResourceList(edu.mayo.cts2.framework.model.service.core.Query, edu.mayo.cts2.framework.model.core.FilterComponent, java.lang.Object, edu.mayo.cts2.sdk.service.command.Page)
	 */
	@Override
	public DirectoryResult<Association> getResourceList(
			Query query,
			FilterComponent filterComponent,
			AssociationQueryServiceRestrictions restrictions, 
			Page page) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.sdk.service.profile.QueryService#count(edu.mayo.cts2.framework.model.service.core.Query, edu.mayo.cts2.framework.model.core.FilterComponent, java.lang.Object)
	 */
	@Override
	public int count(
			Query query, 
			FilterComponent filterComponent,
			AssociationQueryServiceRestrictions restrictions) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.sdk.service.profile.association.AssociationQueryService#getChildrenAssociationsOfEntity(edu.mayo.cts2.framework.model.service.core.Query, edu.mayo.cts2.framework.model.core.FilterComponent, edu.mayo.cts2.sdk.service.command.Page, edu.mayo.cts2.sdk.service.profile.entitydescription.id.EntityDescriptionId)
	 */
	@Override
	public DirectoryResult<EntityDirectoryEntry> getChildrenAssociationsOfEntity(
			Query query, 
			FilterComponent filterComponent,
			Page page,
			EntityDescriptionId id) {

		String codeSystemName = this.identityConverter.
				codeSystemVersionNameCodeSystemName(id.getCodeSystemVersion());
		
		return this.doGetAssociationsOfEntity(
				codeSystemName,
				id.getCodeSystemVersion(), 
				id.getName().getName(),
				CHILDREN_PREDICATE,
				filterComponent,
				page);
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.sdk.service.profile.association.AssociationQueryService#getParentAssociationsOfEntity(edu.mayo.cts2.framework.model.service.core.Query, edu.mayo.cts2.framework.model.core.FilterComponent, edu.mayo.cts2.sdk.service.command.Page, edu.mayo.cts2.sdk.service.profile.entitydescription.id.EntityDescriptionId)
	 */
	@Override
	public DirectoryResult<EntityDirectoryEntry> getParentAssociationsOfEntity(
			Query query, 
			FilterComponent filterComponent, 
			Page page,
			EntityDescriptionId id) {
		String codeSystemName = this.identityConverter.
				codeSystemVersionNameCodeSystemName(id.getCodeSystemVersion());

		return this.doGetAssociationsOfEntity(
				codeSystemName,
				id.getCodeSystemVersion(), 
				id.getName().getName(),
				PARENT_PREDICATE,
				filterComponent,
				page);
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.sdk.service.profile.association.AssociationQueryService#getSourceOfAssociationsOfEntity(edu.mayo.cts2.framework.model.service.core.Query, edu.mayo.cts2.framework.model.core.FilterComponent, edu.mayo.cts2.sdk.service.command.Page, edu.mayo.cts2.sdk.service.profile.entitydescription.id.EntityDescriptionId)
	 */
	@Override
	public DirectoryResult<AssociationDirectoryEntry> getSourceOfAssociationsOfEntity(
			Query query,
			FilterComponent filterComponent, 
			Page page,
			EntityDescriptionId id) {
		
		String ontologyVersionId = 
			this.identityConverter.codeSystemVersionNameToOntologyVersionId(
					id.getCodeSystemVersion());
		
		String codeSystemName = this.identityConverter.
				codeSystemVersionNameCodeSystemName(id.getCodeSystemVersion());

		final String xml = this.bioportalRestService.
			getEntityByOntologyVersionIdAndEntityId(ontologyVersionId, id.getName().getName());
	
		return this.associationTransform.transformSubjectOfAssociationsForEntity(
				xml, 
				codeSystemName, 
				id.getCodeSystemVersion());
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
	 * @see edu.mayo.cts2.sdk.service.profile.AbstractQueryService#registerPredicateReferences()
	 */
	@Override
	protected List<? extends PredicateReference> getAvailablePredicateReferences() {
		return null;
	}
}
