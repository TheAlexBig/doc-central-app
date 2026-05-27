package com.big.dreamer.doccentral;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "app.storage.data-directory=target/test-data",
        "app.storage.documents-directory=target/test-documents"
})
class DocCentralApplicationTests {

    @Test
    void contextLoads() {
    }
}
