package edu.mayo.cts2.framework.plugin.service.bioportal.profile.association

import static org.junit.Assert.*

import javax.annotation.Resource

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import edu.mayo.cts2.framework.model.command.Page
import edu.mayo.cts2.framework.model.core.ScopedEntityName
import edu.mayo.cts2.framework.model.util.ModelUtils
import edu.mayo.cts2.framework.service.profile.entitydescription.name.EntityDescriptionReadId

@RunWith(SpringJUnit4ClassRunner)
@ContextConfiguration(locations="/bioportal-test-context.xml")
public class BioportalRestAssociationQueryServiceTestIT {
	
	@Resource
	private BioportalRestAssociationQueryService service

	
	@Test
	public void testGetChildren(){
		
		def sen = new ScopedEntityName(name:"G40-G47.9", namespace:"ICD10")
		def name = new EntityDescriptionReadId(
			sen,
			ModelUtils.nameOrUriFromName("ICD10_1998_RRF"))
		
		def ed = service.getChildrenAssociationsOfEntity(name, null, null, new Page())
		
		assertTrue ed.entries.size > 0

	}
	
	
	
}
