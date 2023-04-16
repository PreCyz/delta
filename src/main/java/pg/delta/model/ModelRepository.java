package pg.delta.model;

import org.springframework.cache.annotation.CacheEvict;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ModelRepository {

    private final List<Model> mockedData = List.of(
            new Model("1", LocalDateTime.now().minusDays(30), ChangeType.CREATED, "init"),
            new Model("2", LocalDateTime.now().minusDays(29), ChangeType.CREATED, "init"),
            new Model("3", LocalDateTime.now().minusDays(28), ChangeType.CREATED, "init"),
            new Model("4", LocalDateTime.now().minusDays(27), ChangeType.CREATED, "init"),
            new Model("5", LocalDateTime.now().minusDays(26), ChangeType.CREATED, "init"),
            new Model("6", LocalDateTime.now().minusDays(25), ChangeType.CREATED, "init"),
            new Model("7", LocalDateTime.now().minusDays(24), ChangeType.CREATED, "init"),
            new Model("8", LocalDateTime.now().minusDays(23), ChangeType.CREATED, "init"),
            new Model("9", LocalDateTime.now().minusDays(22), ChangeType.CREATED, "init"),
            new Model("10", LocalDateTime.now().minusDays(21), ChangeType.CREATED, "init"),
            new Model("11", LocalDateTime.now().minusDays(20), ChangeType.CREATED, "init"),
            new Model("12", LocalDateTime.now().minusDays(19), ChangeType.CREATED, "init"),
            new Model("13", LocalDateTime.now().minusDays(18), ChangeType.CREATED, "init"),
            new Model("14", LocalDateTime.now().minusDays(17), ChangeType.CREATED, "init"),
            new Model("15", LocalDateTime.now().minusDays(16), ChangeType.CREATED, "init"),
            new Model("16", LocalDateTime.now().minusDays(15), ChangeType.CREATED, "init"),
            new Model("17", LocalDateTime.now().minusDays(14), ChangeType.CREATED, "init"),
            new Model("18", LocalDateTime.now().minusDays(13), ChangeType.CREATED, "init"),
            new Model("19", LocalDateTime.now().minusDays(12), ChangeType.CREATED, "init"),
            new Model("20", LocalDateTime.now().minusDays(11), ChangeType.CREATED, "init"),
            new Model("21", LocalDateTime.now().minusDays(10), ChangeType.CREATED, "init"),
            new Model("22", LocalDateTime.now().minusDays(9), ChangeType.CREATED, "init"),
            new Model("23", LocalDateTime.now().minusDays(8), ChangeType.CREATED, "init"),
            new Model("24", LocalDateTime.now().minusDays(7), ChangeType.CREATED, "init"),
            new Model("25", LocalDateTime.now().minusDays(6), ChangeType.CREATED, "init"),
            new Model("26", LocalDateTime.now().minusDays(5), ChangeType.CREATED, "init"),
            new Model("27", LocalDateTime.now().minusDays(4), ChangeType.CREATED, "init"),
            new Model("28", LocalDateTime.now().minusDays(3), ChangeType.CREATED, "init"),
            new Model("29", LocalDateTime.now().minusDays(2), ChangeType.CREATED, "init"),
            new Model("30", LocalDateTime.now().minusDays(1), ChangeType.CREATED, "init")
    );

    List<Model> getChangedData(LocalDate localDate) {
        return mockedData.stream()
                .filter(it -> it.changeDateTime().toLocalDate().isAfter(localDate) || it.changeDateTime().toLocalDate().isEqual(localDate))
                .toList();
    }

    @CacheEvict(cacheNames = "deltaCache")
    public Map<LocalDate, Model> generateDelta(LocalDate localDate) {
        Predicate<Model> changePredicate = it -> it.changeDateTime().toLocalDate().isAfter(localDate) ||
                it.changeDateTime().toLocalDate().isEqual(localDate);

        return mockedData.stream()
                .filter(changePredicate)
                .map(it -> {
                    if (Integer.parseInt(it.id()) % 2 == 0) {
                        return new Model(
                                it.id(),
                                it.changeDateTime().plusHours(1),
                                ChangeType.MODIFIED,
                                String.format("%s %s", it.details(), LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")))
                        );
                    }
                    return it;
                })
                .collect(Collectors.toMap(
                        it -> it.changeDateTime().toLocalDate(),
                        it -> it,
                        (v1, v2) -> v1
                ));
    }
}
