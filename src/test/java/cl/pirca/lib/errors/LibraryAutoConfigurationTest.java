package cl.pirca.lib.errors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class LibraryAutoConfigurationTest {

    @Test
    void libraryInfoCarriesTheLibraryName() {
        LibraryInfo info = new LibraryAutoConfiguration().libraryInfo();

        assertEquals("lib-errors", info.name());
    }
}
