/*
  DA-NRW Software Suite | ContentBroker
  Copyright (C) 2013 Historisch-Kulturwissenschaftliche Informationsverarbeitung
  Universität zu Köln

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package de.uzk.hki.da.cb;

import static de.uzk.hki.da.utils.C.EDM_FOR_ES_INDEX_METADATA_STREAM_ID;
import static de.uzk.hki.da.utils.C.EDM_XSLT_METADATA_STREAM_ID;
import static de.uzk.hki.da.utils.C.ENCODING_UTF_8;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import de.uzk.hki.da.action.AbstractAction;
import de.uzk.hki.da.core.PreconditionsNotMetException;
import de.uzk.hki.da.repository.MetadataIndex;
import de.uzk.hki.da.repository.RepositoryException;
import de.uzk.hki.da.util.ConfigurationException;
import de.uzk.hki.da.model.Object;
import de.uzk.hki.da.model.WorkArea;

/**
 * This action fetches EDM/RDF-Metadata from the public PIP,  
 * transforms it to hierarchical object structure
 * and indexes it in the repository's index. 
 * 
 * The index entries generated by this action can be
 * configured through <a href="http://json-ld.org/spec/latest/json-ld-framing/">
 * JSON-LD Frames</a>.
 * 
 * The context URI for every document is generated
 * by concatenating the context uri prefix with the
 * name of the frame file.
 * 
 * @author Sebastian Cuy 
 * @author Daniel M. de Oliveira
 */
public class IndexMetadataAction extends AbstractAction {
	
	private MetadataIndex metadataIndex;
	private Set<String> testContractors;
	private String indexName;
	
	public IndexMetadataAction() {
		setKILLATEXIT(true);
		}
	
	@Override
	public void checkConfiguration() {
		if (getMetadataIndex() == null) 
			throw new ConfigurationException("metadataIndex");
	}
	

	@Override
	public void checkPreconditions() {
		if (indexName == null) 
			throw new PreconditionsNotMetException("Index name not set. Make sure the action is configured properly");
		if (getTestContractors()==null)
			throw new PreconditionsNotMetException("testContractors not set");
		if (! wa.pipMetadataFile(WorkArea.PUBLIC, EDM_FOR_ES_INDEX_METADATA_STREAM_ID).exists())
			throw new PreconditionsNotMetException("Missing file: "+wa.pipMetadataFile(WorkArea.PUBLIC, EDM_FOR_ES_INDEX_METADATA_STREAM_ID));
		if (! wa.pipMetadataFile(WorkArea.PUBLIC, EDM_XSLT_METADATA_STREAM_ID).exists())
			throw new PreconditionsNotMetException("Missing file: "+wa.pipMetadataFile(WorkArea.PUBLIC, EDM_XSLT_METADATA_STREAM_ID));
	}
	
	@Override
	public boolean implementation() throws RepositoryException, IOException {

		String edmContent;
		InputStream metadataStream  = null;
		try {
			metadataStream = new FileInputStream(wa.pipMetadataFile(WorkArea.PUBLIC, EDM_FOR_ES_INDEX_METADATA_STREAM_ID));
			edmContent = IOUtils.toString(metadataStream, ENCODING_UTF_8);
			getMetadataIndex().prepareAndIndexMetadata(adjustIndexName(indexName), o.getIdentifier(), edmContent);
		} catch (Exception e) {
			throw new RepositoryException("Unable to prepare and index metadata!", e);
		}
		finally {
			metadataStream.close();
		}
		o.setObject_state(Object.ObjectStatus.ArchivedAndValidAndNotInWorkflow);
		return true;
	}

	

	
	@Override
	public void rollback() throws Exception {
		// not implemented. Retry is possible. The last point where the action fails
		// is when indexMetadata gets called. So we know when the action fails the 
		// metadata has not been indexed properly.
	}




	/**
	 * use test index for test packages
	 * @param originalIndexName
	 * @return
	 */
	private String adjustIndexName(String originalIndexName){
		
		String contractorShortName = o.getContractor().getShort_name();
		String adjustedIndexName = indexName;
		if(testContractors != null && testContractors.contains(contractorShortName)) {
			adjustedIndexName += "_test";
		}
		return adjustedIndexName;
	}

	
	/**
	 * Get the name of the index
	 * the data will be indexed in.
//	 * @return the index name
	 */
	public String getIndexName() {
		return indexName;
	}

	/**
	 * Set the name of the index
	 * the data will be indexed in.
	 * @param the index name
	 */
	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}

	/**
	 * Get the set of contractors that are considered test users.
	 * Objects ingested by these users will be indexed in the
	 * test index (index_name + "test").
	 * @return the set of test users
	 */
	public Set<String> getTestContractors() {
		return testContractors;
	}

	/**
	 * Set the set of contractors that are considered test users.
	 * Objects ingested by these users will be indexed in the
	 * test index (index_name + "test").
	 * @param the set of test users
	 */
	public void setTestContractors(Set<String> testContractors) {
		this.testContractors = testContractors;
	}
	
	public MetadataIndex getMetadataIndex() {
		return metadataIndex;
	}
	
	public void setMetadataIndex(MetadataIndex mi) {
		this.metadataIndex = mi;
	}

}
