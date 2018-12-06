package com.github.longkerdandy.viki.home.hap.http.handler;

import static com.github.longkerdandy.viki.home.hap.http.handler.AttributesHandler.lookupNameValuePairs;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.junit.Test;

public class AttributesHandlerTest {

  @Test
  public void lookupNameValuePairsTest() throws URISyntaxException {
    URI uri = new URI("http://127.0.0.1:8080/characteristics?id=1.4,1.8&meta=1&type=1");
    List<NameValuePair> params = URLEncodedUtils.parse(uri, Charset.forName("UTF-8"));
    Optional<String> id = lookupNameValuePairs(params, "id");
    Optional<String> meta = lookupNameValuePairs(params, "meta");
    Optional<String> perms = lookupNameValuePairs(params, "perms");
    Optional<String> type = lookupNameValuePairs(params, "type");
    Optional<String> ev = lookupNameValuePairs(params, "ev");

    assert id.isPresent() && id.get().equals("1.4,1.8");
    assert meta.isPresent() && meta.get().equals("1");
    assert perms.isEmpty();
    assert type.isPresent() && type.get().equals("1");
    assert ev.isEmpty();
  }
}
