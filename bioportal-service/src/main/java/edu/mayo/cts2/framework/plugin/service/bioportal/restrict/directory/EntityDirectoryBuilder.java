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
package edu.mayo.cts2.framework.plugin.service.bioportal.restrict.directory;

import java.util.List;

import edu.mayo.cts2.framework.filter.directory.AbstractCallbackDirectoryBuilder;
import edu.mayo.cts2.framework.model.core.FilterComponent;
import edu.mayo.cts2.framework.model.core.MatchAlgorithmReference;
import edu.mayo.cts2.framework.model.entity.EntityDirectoryEntry;
import edu.mayo.cts2.framework.model.service.core.Query;

/**
 * The Class EntityDirectoryBuilder.
 *
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
public class EntityDirectoryBuilder extends AbstractCallbackDirectoryBuilder<EntityDirectoryEntry> {

	/**
	 * Instantiates a new entity directory builder.
	 *
	 * @param callback the callback
	 */
	public EntityDirectoryBuilder(
			Callback<EntityDirectoryEntry> callback) {
		super(callback);
	}

	/**
	 * Instantiates a new entity directory builder.
	 *
	 * @param callback the callback
	 * @param matchAlgorithmReferences the match algorithm references
	 */
	public EntityDirectoryBuilder(
			Callback<EntityDirectoryEntry> callback,
			List<MatchAlgorithmReference> matchAlgorithmReferences) {
		super(callback, matchAlgorithmReferences);
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.sdk.plugin.filter.directory.AbstractDirectoryBuilder#restrict(org.cts2.core.FilterComponent)
	 */
	@Override
	public EntityDirectoryBuilder restrict(
			FilterComponent filterComponent) {
		return (EntityDirectoryBuilder) super.restrict(filterComponent);
	}

	/* (non-Javadoc)
	 * @see edu.mayo.cts2.sdk.plugin.filter.directory.AbstractDirectoryBuilder#restrict(org.cts2.service.core.Query)
	 */
	@Override
	public EntityDirectoryBuilder restrict(Query query) {
		return (EntityDirectoryBuilder) super.restrict(query);
	}
}
