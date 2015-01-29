package urlshortener2014.mediumcandy;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static urlshortener2014.common.web.fixture.ShortURLFixture.someUrl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

import urlshortener2014.common.domain.ShortURL;
import urlshortener2014.common.repository.ClickRepository;
import urlshortener2014.common.repository.ShortURLRepository;
import urlshortener2014.mediumcandy.web.MediumCandyController;

//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(classes = {WebAppContext.class})
//@WebAppConfiguration
public class MediumcandyTests {
	
//	@Value("${local.server.port}")
//	private int port = 0;
	
	private String application = "/mediumcandy";
	private String linkcustomized = "/linkcustomized";
	private String url = "http://www.google.es/maps";
	private String brand = "TeamMediumCandy";
	
	private MockMvc mockMvc;

	@Mock
	private ShortURLRepository shortURLRepository;

	@Mock
	private ClickRepository clickRespository;

	@InjectMocks
	private MediumCandyController mediumCandy;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		this.mockMvc = MockMvcBuilders.standaloneSetup(mediumCandy).build();
	}
	
	@Test
	public void testThatShortenerURIIsTheRedirectedUrl() throws Exception {
		//configureTransparentSave();

		ResultActions ma = mockMvc.perform(post("http://localhost:8080/mediumcandy/linkreachable")
				.param("url", "http://www.google.es/maps"))
				.andDo(print())
				.andExpect(jsonPath("$.target", is("http://www.google.es/maps")));
		MvcResult mr = ma.andReturn();
		String redirect = mr.getResponse().getRedirectedUrl();
		
		ma.andExpect(jsonPath("$.uri", is(redirect)));		
	}
	
	@Test
	public void testThatHashOfCustomizedUrlIsTheBrand() throws Exception {
		ResultActions ma = mockMvc.perform(post("http://localhost:8080/mediumcandy/linkcustomized")
				.param("url", "http://www.google.es/maps").param("brand", brand))
				.andDo(print())
				.andExpect(jsonPath("$.hash", is(brand)));
		MvcResult mr = ma.andReturn();
		String redirect = mr.getResponse().getRedirectedUrl();
		
		ma.andExpect(jsonPath("$.uri", is(redirect)));		
	}
	
	@Test
	public void testThatExistsStatsForSavedURL() throws Exception {
		ResultActions ma = mockMvc.perform(get("http://localhost:8080/mediumcandy/linkstats")
				.param("url", url))
				.andDo(print());
		MvcResult mr = ma.andReturn();
		int status = mr.getResponse().getStatus();
		
		assertEquals(200, status);	
	}
	
	@Test
	public void testThatNotExistsStatsForUnsavedURL() throws Exception {
		ResultActions ma = mockMvc.perform(get("http://localhost:8080/mediumcandy/linkstats")
				.param("url", url+"/ErrorWanted"))
				.andDo(print());
		MvcResult mr = ma.andReturn();
		int status = mr.getResponse().getStatus();
		
		assertEquals(400, status);	
	}
}
