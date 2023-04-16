package pg.delta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping(path = "/model", produces = MediaType.APPLICATION_JSON_VALUE)
public class ModelController {

    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";
    private final Logger log ;
    private final ModelService modelService;

    public ModelController(ModelService modelService) {
        this.modelService = modelService;
        log = LoggerFactory.getLogger(ModelController.class);
    }

    @GetMapping(path = "/changelog/{localDateTime}")
    public ResponseEntity<List<Model>> getChangelog(
            @PathVariable
            @DateTimeFormat(pattern = DATE_TIME_PATTERN)
            LocalDateTime localDateTime
    ) {
        log.info("GET changelog with the timestamp {}", localDateTime.format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)));
        return ResponseEntity.ok(modelService.getChangedModel(localDateTime.toLocalDate()));
    }
}
