package urlshortener2014.common.repository.fixture;

import urlshortener2014.common.domain.Click;
import urlshortener2014.common.domain.ShortURL;

public class ClickFixture {

	public static Click click(ShortURL su) {
		return new Click(null, su.getHash(), null, null, null, null, null, null);
	}
}
