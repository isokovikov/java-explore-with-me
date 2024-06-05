package ru.practicum.main_service.event.repository;

import org.springframework.data.domain.Pageable;
import ru.practicum.main_service.event.enums.EventState;
import ru.practicum.main_service.event.model.Event;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class EventCustomRepositoryImpl implements EventCustomRepository {
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Получает события для администратора с возможностью фильтрации и пагинации.
     * @param users список идентификаторов пользователей
     * @param states список состояний событий
     * @param categories список категорий событий
     * @param rangeStart начало временного диапазона
     * @param rangeEnd конец временного диапазона
     * @param from начальный индекс для пагинации
     * @param size количество записей для возврата
     * @return множество событий, подходящих под указанные критерии
     */
    @Override
    public Set<Event> getEventsByAdmin(List<Long> users, List<EventState> states, List<Long> categories,
                                       LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size, Pageable pageable) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> query = builder.createQuery(Event.class);
        Root<Event> root = query.from(Event.class);
        Predicate criteria = builder.conjunction();

        // Фильтрация по пользователям
        if (users != null && !users.isEmpty()) {
            criteria = builder.and(criteria, root.get("initiator").in(users));
        }

        // Фильтрация по состояниям
        if (states != null && !states.isEmpty()) {
            criteria = builder.and(criteria, root.get("state").in(states));
        }

        // Фильтрация по категориям
        if (categories != null && !categories.isEmpty()) {
            criteria = builder.and(criteria, root.get("category").in(categories));
        }

        // Фильтрация по дате начала
        if (rangeStart != null) {
            criteria = builder.and(criteria, builder.greaterThanOrEqualTo(root.get("eventDate"), rangeStart));
        }

        // Фильтрация по дате окончания
        if (rangeEnd != null) {
            criteria = builder.and(criteria, builder.lessThanOrEqualTo(root.get("eventDate"), rangeEnd));
        }

        query.select(root).where(criteria);

        // Применение пагинации
        return entityManager.createQuery(query)
                .setFirstResult(from)
                .setMaxResults(size)
                .getResultStream()
                .collect(Collectors.toSet());
    }

    /**
     * Получает события для публичного доступа с возможностью фильтрации и пагинации.
     * @param text текст для поиска в аннотации и описании событий
     * @param categories список категорий событий
     * @param paid флаг платности события
     * @param rangeStart начало временного диапазона
     * @param rangeEnd конец временного диапазона
     * @param from начальный индекс для пагинации
     * @param size количество записей для возврата
     * @return множество событий, подходящих под указанные критерии
     */
    public Set<Event> getEventsByPublic(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                        LocalDateTime rangeEnd, Integer from, Integer size, Pageable pageable) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> query = builder.createQuery(Event.class);
        Root<Event> root = query.from(Event.class);
        Predicate criteria = builder.conjunction();

        // Фильтрация по тексту
        if (text != null && !text.isBlank()) {
            Predicate annotation = builder.like(builder.lower(root.get("annotation")), "%" + text.toLowerCase() + "%");
            Predicate description = builder.like(builder.lower(root.get("description")), "%" + text.toLowerCase() + "%");
            criteria = builder.and(criteria, builder.or(annotation, description));
        }

        // Фильтрация по категориям
        if (categories != null && !categories.isEmpty()) {
            criteria = builder.and(criteria, root.get("category").in(categories));
        }

        // Фильтрация по платности
        if (paid != null) {
            criteria = builder.and(criteria, root.get("paid").in(paid));
        }

        // Фильтрация по временным диапазонам
        if (rangeStart == null && rangeEnd == null) {
            criteria = builder.and(criteria, builder.greaterThanOrEqualTo(root.get("eventDate"), LocalDateTime.now()));
        } else {
            if (rangeStart != null) {
                criteria = builder.and(criteria, builder.greaterThanOrEqualTo(root.get("eventDate"), rangeStart));
            }
            if (rangeEnd != null) {
                criteria = builder.and(criteria, builder.lessThanOrEqualTo(root.get("eventDate"), rangeEnd));
            }
        }

        // Фильтрация по состоянию
        criteria = builder.and(criteria, root.get("state").in(EventState.PUBLISHED));

        query.select(root).where(criteria);

        return entityManager.createQuery(query).setFirstResult(from).setMaxResults(size).getResultStream().collect(Collectors.toSet());
    }
}