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

package de.uzk.hki.da.convert;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.Test;
import org.w3c.dom.Document;

import de.uzk.hki.da.model.ConversionInstruction;
import de.uzk.hki.da.model.DAFile;
import de.uzk.hki.da.model.Object;
import de.uzk.hki.da.model.contract.AudioRestriction;
import de.uzk.hki.da.model.contract.ImageRestriction;
import de.uzk.hki.da.model.contract.PublicationRight;
import de.uzk.hki.da.model.contract.PublicationRight.Audience;
import de.uzk.hki.da.service.XPathUtils;
import de.uzk.hki.da.utils.TESTHelper;



/**
 * The Class PublishAudioConversionStrategyTests.
 *
 * @author Daniel M. de Oliveira
 */
public class PublishAudioConversionStrategyTests {

	/** The base path. */
	private String basePath = "src/test/resources/convert/PublishAudioConversionStrategyTests/";
	
	
	
	/**
	 * Test.
	 *
	 * @throws FileNotFoundException the file not found exception
	 */
	@Test
	public void test() throws FileNotFoundException{
		
		CLIConnector cli = mock ( CLIConnector.class );
		String cmdPUBLIC[] = new String[]{
				"sox",
				new File(basePath+"TEST/123/data/a/audiofile.wav").getAbsolutePath(),
				basePath+"TEST/123/data/dip/public/target/audiofile.mp3",
				"trim","0","10"
		};
		when(cli.execute(cmdPUBLIC)).thenReturn(true);
		
		String cmdINSTITUTION[] = new String[]{
				"sox",
				new File(basePath+"TEST/123/data/a/audiofile.wav").getAbsolutePath(),
				basePath+"TEST/123/data/dip/institution/target/audiofile.mp3"
		};
		when(cli.execute(cmdINSTITUTION)).thenReturn(true);
		
		PublishAudioConversionStrategy s = new PublishAudioConversionStrategy();
		s.setCLIConnector( cli );
		
		Document dom = XPathUtils.parseDom("src/test/resources/convert/PublishAudioConversionStrategyTests/premis.xml");
		if (dom==null){
			throw new RuntimeException("Error while parsing premis.xml");
		} s.setDom(dom);

		
		Object o = TESTHelper.setUpObject("123",basePath);
		PublicationRight right = new PublicationRight();
		right.setAudience(Audience.PUBLIC);
		right.setAudioRestriction(new AudioRestriction());
		right.getAudioRestriction().setDuration(10);
		o.getRights().getPublicationRights().add(right);
		s.setObject(o);
		
		ConversionInstruction ci = new ConversionInstruction();
		ci.setSource_file(new DAFile(o.getLatestPackage(),"a","audiofile.wav"));
		ci.setTarget_folder("target/");
		s.convertFile(ci);
	}
}
