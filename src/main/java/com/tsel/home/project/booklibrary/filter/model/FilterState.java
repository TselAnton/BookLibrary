package com.tsel.home.project.booklibrary.filter.model;

import com.tsel.home.project.booklibrary.filter.condition.FilterCondition;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Разделяет применённые условия и редактируемый черновик.
 * Используется только UI-потоком; наружу возвращаются неизменяемые копии карт.
 */
public class FilterState {

    private final Map<String, FilterCondition> applied = new LinkedHashMap<>();
    private final Map<String, FilterCondition> draft = new LinkedHashMap<>();

    /**
     * Возвращает неизменяемый снимок условий, уже влияющих на таблицу.
     * @return применённые условия по идентификаторам полей
     */
    public Map<String, FilterCondition> applied() {
        return Map.copyOf(applied);
    }

    /**
     * Возвращает неизменяемый снимок правок, ещё не влияющих на таблицу.
     * @return черновые условия по идентификаторам полей
     */
    public Map<String, FilterCondition> draft() {
        return Map.copyOf(draft);
    }

    /**
     * Заменяет черновик копией применённых условий при открытии или отмене панели.
     */
    public void begin() {
        draft.clear();
        draft.putAll(applied);
    }

    /**
     * Меняет условие одного поля только в черновике; null снимает ограничение.
     * @param field идентификатор фильтра
     * @param condition новое условие или null
     */
    public void setDraft(String field, FilterCondition condition) {
        if (condition == null) {
            draft.remove(field);
        } else {
            draft.put(field, condition);
        }
    }

    /**
     * Заменяет применённые условия черновиком; после этого таблица может выполнить отбор.
     */
    public void apply() {
        applied.clear();
        applied.putAll(draft);
    }

    /**
     * Снимает ограничение поля и в применённых условиях, и в черновике.
     * @param field идентификатор закрываемого тега
     */
    public void remove(String field) {
        applied.remove(field);
        draft.remove(field);
    }

    /**
     * Очищает оба набора условий для полного сброса фильтров.
     */
    public void clear() {
        applied.clear();
        draft.clear();
    }
}
