package com.big.dreamer.doccentral.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class HtmlCacheControlFilterTests {

    private final HtmlCacheControlFilter filter = new HtmlCacheControlFilter();

    @Test
    void preventsCachingApplicationHtml() throws Exception {
        for (String path : new String[]{"/compra-venta", "/mutuo", "/historial", "/configuracion"}) {
            MockHttpServletResponse response = filter(path);
            assertThat(response.getHeader("Cache-Control"))
                    .isEqualTo("no-store, no-cache, must-revalidate, max-age=0");
            assertThat(response.getHeader("Pragma")).isEqualTo("no-cache");
            assertThat(response.getDateHeader("Expires")).isZero();
        }
    }

    @Test
    void leavesVersionedAssetsCacheable() throws Exception {
        MockHttpServletResponse response = filter("/assets/CarSale-version.js");

        assertThat(response.getHeader("Cache-Control")).isNull();
    }

    private MockHttpServletResponse filter(String requestUri) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", requestUri);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }
}
