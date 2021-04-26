package eu.arrowhead.core.serviceregistry;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.database.entity.ServiceInterface;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.dto.internal.ServiceInterfacesListResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceInterfaceResponseDTO;
import eu.arrowhead.common.verifier.ServiceInterfaceNameVerifier;
import eu.arrowhead.core.serviceregistry.database.service.ServiceRegistryDBService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ServiceRegistryMain.class)
@ContextConfiguration (classes = { ServiceRegistryDBServiceTestContext.class })
public class ServiceRegistryControllerServiceInterfaceTest {

	//=================================================================================================
	// members

	@Autowired
	private WebApplicationContext wac;

	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean(name = "mockServiceRegistryDBService") 
	private ServiceRegistryDBService serviceRegistryDBService;

	@MockBean
	private ServiceInterfaceNameVerifier interfaceValidator;

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
	}

	//=================================================================================================
	// Tests of getServiceInterfaces

	//-------------------------------------------------------------------------------------------------
	@Test
	public void getServiceInterfacesTestWithoutParameter() throws Exception  {
		final int numOfEntries = 5;
		final Page<ServiceInterface> serviceInterfaceEntries = createServiceInterfacePageForDBMocking(numOfEntries);
		final ServiceInterfacesListResponseDTO serviceInterfaceEntriesDTO = DTOConverter.convertServiceInterfacesListToServiceInterfaceListResponseDTO(serviceInterfaceEntries);

		when(serviceRegistryDBService.getServiceInterfaceEntriesResponse(anyInt(), anyInt(), any(), any())).thenReturn(serviceInterfaceEntriesDTO);

		final MvcResult response = this.mockMvc.perform(get("/serviceregistry/mgmt/interfaces")
												.accept(MediaType.APPLICATION_JSON))
												.andExpect(status().isOk())
												.andReturn();
		final ServiceInterfacesListResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), ServiceInterfacesListResponseDTO.class);

		assertEquals(numOfEntries, responseBody.getCount());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void getServiceInterfacesTestWithPageAndSizeParameter() throws Exception {
		final int numOfEntries = 8;
		final Page<ServiceInterface> serviceInterfaceEntries = createServiceInterfacePageForDBMocking(numOfEntries);
		final ServiceInterfacesListResponseDTO serviceInterfaceEntriesDTO = DTOConverter.convertServiceInterfacesListToServiceInterfaceListResponseDTO(serviceInterfaceEntries);

		when(serviceRegistryDBService.getServiceInterfaceEntriesResponse(anyInt(), anyInt(), any(), any())).thenReturn(serviceInterfaceEntriesDTO);

		final MvcResult response = this.mockMvc.perform(get("/serviceregistry/mgmt/interfaces")
											.param("page", "0")
											.param("item_per_page", String.valueOf(numOfEntries))
											.accept(MediaType.APPLICATION_JSON))
											.andExpect(status().isOk())
											.andReturn();
		final ServiceInterfacesListResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), ServiceInterfacesListResponseDTO.class);

		assertEquals(numOfEntries, responseBody.getCount());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void getServiceInterfacesTestWithNullPageButDefinedSizeParameter() throws Exception {
		this.mockMvc.perform(get("/serviceregistry/mgmt/interfaces")
					.param("item_per_page", "1")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void getServiceInterfacesTestWithDefinedPageButNullSizeParameter() throws Exception {
		this.mockMvc.perform(get("/serviceregistry/mgmt/interfaces")
					.param("page", "0")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void getServiceInterfacesTestWithInvalidSortDirectionFlagParameter() throws Exception {
		this.mockMvc.perform(get("/serviceregistry/mgmt/interfaces")
					.param("direction", "invalid")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}

	//=================================================================================================
	// Tests of getServiceInterfacesById

	//-------------------------------------------------------------------------------------------------
	@Test
	public void getServiceInterfaceByIdTestWithExistingId() throws Exception {
		final int requestedId = 1;
		final ServiceInterfaceResponseDTO serviceInterfaceResponseDTO = new ServiceInterfaceResponseDTO(requestedId, "", "", "");		

		when(serviceRegistryDBService.getServiceInterfaceByIdResponse(anyLong())).thenReturn(serviceInterfaceResponseDTO);

		final MvcResult response = this.mockMvc.perform(get("/serviceregistry/mgmt/interfaces/" + requestedId)
												.accept(MediaType.APPLICATION_JSON))
												.andExpect(status().isOk())
												.andReturn();
		final ServiceInterfaceResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), ServiceInterfaceResponseDTO.class);

		assertEquals(requestedId, responseBody.getId());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void getServiceInterfaceByIdTestWithInvalidId() throws Exception {
		this.mockMvc.perform(get("/serviceregistry/mgmt/interfaces/-1")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}

	//=================================================================================================
	// Tests of addServiceInterface

	//-------------------------------------------------------------------------------------------------
	@Test
	public void addServiceInterfaceTestWithValidInterface() throws Exception {
		final String interfaceName = "testInterface";
		final ServiceInterfaceResponseDTO serviceInterfaceResponseDTO = new ServiceInterfaceResponseDTO(0, interfaceName,"","");

		when(interfaceValidator.isValid(anyString())).thenReturn(Boolean.TRUE);
		when(serviceRegistryDBService.createServiceInterfaceResponse(anyString())).thenReturn(serviceInterfaceResponseDTO);

		final MvcResult response = this.mockMvc.perform(post("/serviceregistry/mgmt/interfaces")
												.content("{\"interfaceName\": \"" + interfaceName + "\"}")
												.contentType(MediaType.APPLICATION_JSON)
												.accept(MediaType.APPLICATION_JSON))
												.andExpect(status().isCreated())
												.andReturn();
		final ServiceInterfaceResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), ServiceInterfaceResponseDTO.class);

		assertEquals(interfaceName, responseBody.getInterfaceName());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void addServiceInterfaceTestWithNullInterface() throws Exception {
		this.mockMvc.perform(post("/serviceregistry/mgmt/interfaces")
					.content("{}")
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void addServiceInterfaceTestWithBlankInterface() throws Exception {
		this.mockMvc.perform(post("/serviceregistry/mgmt/interfaces")
					.content("{\"serviceInterface\": \"      \"}")
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}

	//=================================================================================================
	// Tests of putUpdateServiceInterface

	//-------------------------------------------------------------------------------------------------
	@Test
	public void putUpdateServiceInterfaceTestWithValidInterface() throws Exception {
		final String interfaceName = "testInterface";
		final int id = 5;
		final ServiceInterfaceResponseDTO serviceInterfaceResponseDTO = new ServiceInterfaceResponseDTO(id, interfaceName,"","");

		when(interfaceValidator.isValid(anyString())).thenReturn(Boolean.TRUE);
		when(serviceRegistryDBService.updateServiceInterfaceByIdResponse(anyLong(), anyString())).thenReturn(serviceInterfaceResponseDTO);
		
		final MvcResult response = this.mockMvc.perform(put("/serviceregistry/mgmt/interfaces/" + id)
												.content("{\"interfaceName\": \"" + interfaceName + "\"}")
												.contentType(MediaType.APPLICATION_JSON)
												.accept(MediaType.APPLICATION_JSON))
												.andExpect(status().isOk())
												.andReturn();
		final ServiceInterfaceResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), ServiceInterfaceResponseDTO.class);

		assertEquals(interfaceName, responseBody.getInterfaceName());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void putUpdateServiceInterfaceTestWithNullInterface() throws Exception {
		this.mockMvc.perform(put("/serviceregistry/mgmt/interfaces/5")
					.content("{}")
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void putUpdateServiceInterfaceTestWithBlankInterface() throws Exception {
		this.mockMvc.perform(put("/serviceregistry/mgmt/interfaces/5")
					.content("{\"serviceInterface\": \"     \"}")
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}

	//=================================================================================================
	// Tests of patchUpdateServiceInterface

	//-------------------------------------------------------------------------------------------------
	@Test
	public void patchUpdateServiceInterfaceTestWithValidInterface() throws Exception {
		final String interfaceName = "testInterface";
		final int id = 5;
		final ServiceInterfaceResponseDTO serviceInterfaceResponseDTO = new ServiceInterfaceResponseDTO(id, interfaceName,"","");

		when(interfaceValidator.isValid(anyString())).thenReturn(Boolean.TRUE);
		when(serviceRegistryDBService.updateServiceInterfaceByIdResponse(anyLong(), anyString())).thenReturn(serviceInterfaceResponseDTO);

		final MvcResult response = this.mockMvc.perform(patch("/serviceregistry/mgmt/interfaces/" + id)
												.content("{\"interfaceName\": \"" + interfaceName + "\"}")
												.contentType(MediaType.APPLICATION_JSON)
												.accept(MediaType.APPLICATION_JSON))
												.andExpect(status().isOk())
												.andReturn();
		final ServiceInterfaceResponseDTO responseBody = objectMapper.readValue(response.getResponse().getContentAsString(), ServiceInterfaceResponseDTO.class);

		assertEquals(interfaceName, responseBody.getInterfaceName());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void patchUpdateServiceInterfaceTestWithNullInterface() throws Exception {
		this.mockMvc.perform(patch("/serviceregistry/mgmt/interfaces/5")
					.content("{}")
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void patchUpdateServiceInterfaceTestWithBlankInterface() throws Exception {
		this.mockMvc.perform(patch("/serviceregistry/mgmt/interfaces/5")
					.content("{\"serviceInterface\": \"      \"}")
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}

	//=================================================================================================
	// Tests of removeServiceInterface

	//-------------------------------------------------------------------------------------------------
	@Test
	public void removeServiceInterfaceTestWithValidId( ) throws Exception {
		this.mockMvc.perform(delete("/serviceregistry/mgmt/interfaces/4")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void removeServiceInterfaceTestWithInvalidId( ) throws Exception {
		this.mockMvc.perform(delete("/serviceregistry/mgmt/interfaces/0")
					.accept(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
	}

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private Page<ServiceInterface> createServiceInterfacePageForDBMocking(final int amountOfEntry) {
		final List<ServiceInterface> serviceInterfaceList = new ArrayList<>(amountOfEntry);
		for (int i = 0; i < amountOfEntry; ++i) {
			final ServiceInterface serviceInterface = new ServiceInterface("mockedService" + i);
			serviceInterface.setId(i);
			final ZonedDateTime timeStamp = ZonedDateTime.now();
			serviceInterface.setCreatedAt(timeStamp);
			serviceInterface.setUpdatedAt(timeStamp);
			serviceInterfaceList.add(serviceInterface);
		}

		final Page<ServiceInterface> entries = new PageImpl<ServiceInterface>(serviceInterfaceList);

		return entries;
	}

}