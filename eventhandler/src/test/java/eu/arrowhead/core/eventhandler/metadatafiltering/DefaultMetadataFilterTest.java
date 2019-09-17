package eu.arrowhead.core.eventhandler.metadatafiltering;

import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class DefaultMetadataFilterTest {
	
	//=================================================================================================
	// members
	
	@InjectMocks
	private final MetadataFilteringAlgorithm algorithm = new DefaultMetadataFilter();
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDoFilteringEqualMaps() {
		
		final Map<String, String> map = Map.of("a", "a1", "b", "b1", "c", "c1");
		final MetadataFilteringParameters params = new MetadataFilteringParameters( map, map );
		
		final boolean result = algorithm.doFiltering(params);
		
		assertTrue( result );
		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDoFilteringMapsDifferentCaseInValuesOk() {
		
		final Map<String, String> map0 = Map.of("a", "a1", "b", "b1", "c", "c1");
		final Map<String, String> map1 = Map.of("a", "A1", "b", "B1", "c", "C1");
		final MetadataFilteringParameters params = new MetadataFilteringParameters( map0, map1 );
		
		final boolean result = algorithm.doFiltering(params);
		
		assertTrue( result );
		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDoFilteringMapsDifferentCaseInKeysNotOk() {
		
		final Map<String, String> map0 = Map.of("a", "a1", "b", "b1", "c", "c1");
		final Map<String, String> map1 = Map.of("A", "a1", "b", "b1", "c", "c1");
		final MetadataFilteringParameters params = new MetadataFilteringParameters( map0, map1 );
		
		final boolean result = algorithm.doFiltering(params);
		
		assertTrue( !result );
		
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDoFilteringMapsDifferentSizeNotOk() {
		
		final Map<String, String> map0 = Map.of("a", "a1", "b", "b1", "c", "c1");
		final Map<String, String> map1 = Map.of("a", "a1", "b", "b1" );
		final MetadataFilteringParameters params0 = new MetadataFilteringParameters( map0, map1 );
		final MetadataFilteringParameters params1 = new MetadataFilteringParameters( map1, map0 );
		
		final boolean result0 = algorithm.doFiltering(params0);
		final boolean result1 = algorithm.doFiltering(params1);
		
		assertTrue( !result0 );
		assertTrue( !result1 );

	}
	
}
