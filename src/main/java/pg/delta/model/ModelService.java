package pg.delta.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ModelService {

    private final Logger log;
    private final ModelRepository modelRepository;

    public ModelService(ModelRepository modelRepository) {
        this.modelRepository = modelRepository;
        log = LoggerFactory.getLogger(this.getClass());
    }

    @Cacheable(
            value = "deltaCache",
            key = "#localDate"
    )
    public List<Model> getChangedModel(LocalDate localDate) {
        log.info("Change date {}", localDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        return modelRepository.getChangedData(localDate);
    }
}
