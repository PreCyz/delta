package pg.delta;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

class ModelServiceTest {

    @Test
    void name() {
        System.out.println(LocalDateTime.from(LocalDate.now()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
    }
}