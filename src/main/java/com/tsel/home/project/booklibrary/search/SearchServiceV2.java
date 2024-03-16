package com.tsel.home.project.booklibrary.search;

import static com.tsel.home.project.booklibrary.search.operand.OperatorType.AND;
import static com.tsel.home.project.booklibrary.search.operand.OperatorType.OR;
import static com.tsel.home.project.booklibrary.utils.StringUtils.isBlank;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;

import com.tsel.home.project.booklibrary.dto.BookDTO;
import com.tsel.home.project.booklibrary.search.filter.SearchFilter;
import com.tsel.home.project.booklibrary.search.operand.BaseOperand;
import com.tsel.home.project.booklibrary.search.operand.CompositeOperand;
import com.tsel.home.project.booklibrary.search.operand.OperatorType;
import com.tsel.home.project.booklibrary.search.operand.SimpleOperand;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SearchServiceV2 {

    public static final SearchServiceV2 INSTANCE = new SearchServiceV2();

    private static final Logger LOGGER = LogManager.getLogger(SearchServiceV2.class);
    private static final Comparator<BookDTO> BOOK_NAME_COMPARATOR = Comparator.comparing(BookDTO::getName);
    private static final Pattern OPERATOR_PATTERN = Pattern.compile("(.*?) (AND|И|OR|ИЛИ) (.*?)");

    private final Map<Field, SearchFieldDefinition> fieldDefinitionMap = new ConcurrentHashMap<>();
    private String generatedTooltip;

    private SearchServiceV2() {
        initialize();
    }

    public String getGeneratedTooltip() {
        return generatedTooltip;
    }

    /**
     * Search method
     * @param searchQuery String query from search bar
     * @param bookDTOList List of book DTO
     * @return Filtered list by search query
     */
    public List<BookDTO> search(String searchQuery, List<BookDTO> bookDTOList) {
        if (isBlank(searchQuery)) {
            LOGGER.debug("Search query is empty");
            return bookDTOList;
        }

        LOGGER.debug("Start search by query '{}'", searchQuery);
        BaseOperand searchOperands = prepareSearchQuery(searchQuery);
        return ofNullable(bookDTOList)
            .orElse(Collections.emptyList())
            .stream()
            .filter(bookDTO -> filterByLogicalOperators(searchOperands, bookDTO))
            .sorted(BOOK_NAME_COMPARATOR)
            .toList();
    }

    private BaseOperand prepareSearchQuery(String searchQuery) {
        String filteredSearchQuery = searchQuery.trim();
        Matcher operatorMatcher = OPERATOR_PATTERN.matcher(filteredSearchQuery);
        if (operatorMatcher.matches()) {
            String leftPart = operatorMatcher.group(1);
            String rightPart = operatorMatcher.group(3);
            String operator = operatorMatcher.group(2);

            CompositeOperand compositeOperand = new CompositeOperand(OperatorType.getOperator(operator));
            compositeOperand.getOperands().add(new SimpleOperand(leftPart));
            compositeOperand.getOperands().add(prepareSearchQuery(rightPart));
            return compositeOperand;
        }
        return new SimpleOperand(filteredSearchQuery.toLowerCase(Locale.ROOT));
    }

    private boolean filterByLogicalOperators(BaseOperand searchOperands, BookDTO bookDTO) {
        if (searchOperands instanceof SimpleOperand simpleOperand) {
            return filterBySearchFilters(simpleOperand.getPredicate(), bookDTO);
        }
        if (searchOperands instanceof CompositeOperand compositeOperand) {
            Stream<BaseOperand> operandStream = compositeOperand.getOperands().stream();
            return switch (compositeOperand.getOperator()) {
                case AND -> operandStream.allMatch(operand -> filterByLogicalOperators(operand, bookDTO));
                case OR -> operandStream.anyMatch(operand -> filterByLogicalOperators(operand, bookDTO));
            };
        }
        return false;
    }

    private boolean filterBySearchFilters(String searchQuery, BookDTO bookDTO) {
        for (Entry<Field, SearchFieldDefinition> searchFieldDefinitionEntry : fieldDefinitionMap.entrySet()) {
            try {
                Field searchField = searchFieldDefinitionEntry.getKey();
                SearchFieldDefinition searchFieldDefinition = searchFieldDefinitionEntry.getValue();

                SearchFilter searchFilter = SearchFilterFactory.getSearchFilter(searchField.getType());
                if (searchFilter != null) {
                    Object fieldValue = searchField.get(bookDTO);
                    if (searchFilter.filter(fieldValue, searchQuery, searchFieldDefinition)) {
                        return true;
                    }
                }

            } catch (Exception e) {
                LOGGER.error("Exception while search by query '{}' for DTO: {}", searchQuery, bookDTO, e);
            }
        }

        return false;
    }

    /**
     * Init search fields definitions
     */
    private void initialize() {
        List<Field> searchFields = Arrays.stream(BookDTO.class.getDeclaredFields())
            .filter(dtoField -> dtoField.isAnnotationPresent(SearchField.class))
            .peek(searchField -> searchField.setAccessible(true))
            .toList();

        initializeFieldsDefinitionMap(searchFields);
        initializeFieldsSearchTooltip(searchFields);
    }

    private void initializeFieldsDefinitionMap(List<Field> searchFields) {
        for (Field searchField : searchFields) {
            SearchField searchFieldAnnotation = searchField.getAnnotation(SearchField.class);
            SearchFieldDefinition searchFieldDefinition = new SearchFieldDefinition(
                Arrays.asList(searchFieldAnnotation.aliases())
            );

            fieldDefinitionMap.put(searchField, searchFieldDefinition);
        }
    }

    private void initializeFieldsSearchTooltip(List<Field> searchFields) {
        StringBuilder stringFieldsTooltipBuilder = new StringBuilder();
        StringBuilder tooltipBuilder = new StringBuilder(
            format("Поиск через условие: %s и %s\n",
                arrayToString(AND.getNames()),
                arrayToString(OR.getNames())
            ));

        for (Field searchField : searchFields) {
            SearchField searchFieldAnnotation = searchField.getAnnotation(SearchField.class);
            if (searchFieldAnnotation.aliases().length == 0) {
                stringFieldsTooltipBuilder
                    .append("- ")
                    .append(searchFieldAnnotation.description())
                    .append("\n");

            } else {
                SearchFilter searchFilter = SearchFilterFactory.getSearchFilter(searchField.getType());
                tooltipBuilder.append(format("%s %s, %s\n",
                    searchFieldAnnotation.description(),
                    arrayToString(searchFieldAnnotation.aliases()),
                    searchFilter.getTooltipInfo()
                ));
            }
        }

        tooltipBuilder
            .append("\n")
            .append("Помимо этого происходит:\n")
            .append(stringFieldsTooltipBuilder);

        this.generatedTooltip = tooltipBuilder.toString();
    }

    private String arrayToString(String[] aliases) {
        return arrayToString(Arrays.asList(aliases));
    }

    private String arrayToString(List<String> names) {
        return "[" + String.join(" или ", names) + "]";
    }
}
