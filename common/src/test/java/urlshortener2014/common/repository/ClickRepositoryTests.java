package urlshortener2014.common.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.HSQL;
import static urlshortener2014.common.repository.fixture.ClickFixture.click;
import static urlshortener2014.common.repository.fixture.ShortURLFixture.badUrl;
import static urlshortener2014.common.repository.fixture.ShortURLFixture.url1;
import static urlshortener2014.common.repository.fixture.ShortURLFixture.url1modified;
import static urlshortener2014.common.repository.fixture.ShortURLFixture.url2;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;

import urlshortener2014.common.domain.Click;
import urlshortener2014.common.domain.ShortURL;

public class ClickRepositoryTests {

	private EmbeddedDatabase db;
	private ClickRepository repository;
	private JdbcTemplate jdbc;

	@Before
	public void setup() {
		db = new EmbeddedDatabaseBuilder().setType(HSQL)
				.addScript("schema-hsqldb.sql").build();
		jdbc = new JdbcTemplate(db);
		ShortURLRepository shortUrlRepository = new ShortURLRepositoryImpl(jdbc);
		shortUrlRepository.save(url1());
		shortUrlRepository.save(url2());
		repository = new ClickRepositoryImpl(jdbc);
	}

	@Test
	public void thatSavePersistsTheClickURL() {
		Click click = repository.save(click(url1()));
		assertSame(jdbc.queryForObject("select count(*) from CLICK",
				Integer.class), 1);
		assertNotNull(click);
		assertNotNull(click.getId());
	}

	@Test
	public void thatErrorsInSaveReturnsNull() {
		assertNull(repository.save(click(badUrl())));
		assertSame(jdbc.queryForObject("select count(*) from CLICK",
				Integer.class), 0);
	}

	@Test
	public void thatFindByKeyReturnsAURL() {
		repository.save(click(url1()));
		repository.save(click(url2()));
		repository.save(click(url1()));
		repository.save(click(url2()));
		repository.save(click(url1()));
		assertEquals(repository.findByHash(url1().getHash()).size(), 3);
		assertEquals(repository.findByHash(url2().getHash()).size(), 2);
	}

	@Test
	public void thatFindByKeyReturnsEmpty() {
		repository.save(click(url1()));
		repository.save(click(url2()));
		repository.save(click(url1()));
		repository.save(click(url2()));
		repository.save(click(url1()));
		assertEquals(repository.findByHash(badUrl().getHash()).size(), 0);
	}

	@Test
	public void thatDeleteDelete() {
		Long id1 = repository.save(click(url1())).getId();
		Long id2 = repository.save(click(url2())).getId();
		repository.delete(id1);
		assertEquals(repository.count().intValue(), 1);
		repository.delete(id2);
		assertEquals(repository.count().intValue(), 0);
	}

	@After
	public void shutdown() {
		db.shutdown();
	}

}
