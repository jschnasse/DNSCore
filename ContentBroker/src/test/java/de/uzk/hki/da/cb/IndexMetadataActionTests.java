package de.uzk.hki.da.cb;

import java.io.IOException;
import java.util.HashSet;


import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;

import de.uzk.hki.da.repository.Fedora3RepositoryFacade;
import de.uzk.hki.da.repository.RepositoryException;
import de.uzk.hki.da.test.TC;
import de.uzk.hki.da.util.Path;

public class IndexMetadataActionTests extends ConcreteActionUnitTest{

	@ActionUnderTest
	IndexMetadataAction action = new IndexMetadataAction();
	
	private static final Path WORK_AREA_ROOT_PATH = Path.make(TC.TEST_ROOT_CB,"IndexMetadataAction");
	
	@Before
	public void setUp() {
		n.setWorkAreaRootPath(WORK_AREA_ROOT_PATH);
		action.setIndexName("collection-open");
		action.setTestContractors(new HashSet<String>());
		
		Fedora3RepositoryFacade fed = mock(Fedora3RepositoryFacade.class);
		action.setRepositoryFacade(fed);
	}
	
	@Test
	public void test() throws RepositoryException, IOException {
		
		action.checkSystemStatePreconditions();
		action.implementation();
	}
	
}