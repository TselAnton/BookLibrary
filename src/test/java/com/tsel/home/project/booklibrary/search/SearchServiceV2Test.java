package com.tsel.home.project.booklibrary.search;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;

import com.tsel.home.project.booklibrary.BaseJavaFXTest;
import com.tsel.home.project.booklibrary.dto.BookDTO;
import java.util.List;
import javafx.scene.control.CheckBox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SearchServiceV2Test extends BaseJavaFXTest {

    private final SearchServiceV2 searchService = SearchServiceV2.INSTANCE;

    private static final String CYCLE_EN_ALIAS = "cycle";
    private static final String CYCLE_RU_ALIAS = "цикл";

    private static final String READ_EN_ALIAS = "read";
    private static final String READ_RU_ALIAS = "прочитано";

    private static final String SIGN_EN_ALIAS = "sign";
    private static final String SIGN_RU_ALIAS = "автограф";

    private static final String HARDCOVER_EN_ALIAS = "hard";
    private static final String HARDCOVER_RU_ALIAS = "твердая";

    private static final String PAGES_EN_ALIAS = "pages";
    private static final String PAGES_RU_ALIAS = "страницы";

    private static final String PRICE_EN_ALIAS = "price";
    private static final String PRICE_RU_ALIAS = "цена";

    private BookDTO book_1;
    private BookDTO book_2;
    private BookDTO book_3;
    private BookDTO book_4;
    private BookDTO book_5;

    private List<BookDTO> bookDtoList;

    @BeforeEach
    public void init() {
        book_1 = BookDTO.builder()
            .name("Book 1 name")
            .author("Author 1")
            .publisher("Publisher 1")
            .cycleName("Cycle 1")
            .cycleNumber("Cycle Number 1")
            .cycleEnded(checkBox(true))
            .read(checkBox(true))
            .autograph(checkBox(true))
            .hardCover(checkBox(true))
            .price(111.111)
            .pages(100)
            .build();

        book_2 = BookDTO.builder()
            .name("Book 2 name")
            .author("Author 2")
            .publisher("Publisher 1")
            .cycleName("Cycle 2")
            .cycleNumber("Cycle Number 2")
            .cycleEnded(checkBox(false))
            .read(checkBox(false))
            .autograph(checkBox(false))
            .hardCover(checkBox(false))
            .price(222.222)
            .pages(200)
            .build();

        book_3 = BookDTO.builder()
            .name("Book 3 name")
            .author("Author 3")
            .publisher("Publisher 2")
            .cycleName("Cycle 1")
            .cycleNumber("Cycle Number 3")
            .cycleEnded(checkBox(true))
            .read(checkBox(true))
            .autograph(checkBox(true))
            .hardCover(checkBox(true))
            .price(0.0)
            .pages(300)
            .build();

        book_4 = BookDTO.builder()
            .name("Book 4 name")
            .author("Author 1")
            .publisher("Publisher 3")
            .cycleName("Cycle 3")
            .cycleNumber("Cycle Number 1")
            .cycleEnded(checkBox(false))
            .read(checkBox(false))
            .autograph(checkBox(false))
            .hardCover(checkBox(true))
            .price(null)
            .pages(400)
            .build();

        book_5 = BookDTO.builder()
            .name("Book 5 name")
            .author("Author 4")
            .publisher("Publisher 3")
            .cycleName("Cycle 4")
            .cycleNumber("Cycle Number 5")
            .cycleEnded(checkBox(true))
            .read(checkBox(true))
            .autograph(checkBox(true))
            .hardCover(checkBox(true))
            .price(555.555)
            .pages(500)
            .build();

        bookDtoList = List.of(book_1, book_2, book_3, book_4, book_5);
    }

    @ParameterizedTest(name = "Поиск по '{0}' запросу")
    @ValueSource(strings = {"Book", "book", "BOOK"})
    void testSearchByBookSearchQueryInDifferentRegistry(String searchQuery) {
        assertThat(
            searchService.search(searchQuery, bookDtoList),
            contains(book_1, book_2, book_3, book_4, book_5)
        );

    }

    @Test
    void testSearchByNameQuery() {
        assertThat(
            searchService.search("name", bookDtoList),
            contains(book_1, book_2, book_3, book_4, book_5)
        );
    }

    @Test
    void testSearchByBook1Query() {
        assertThat(
            searchService.search("Book 1", bookDtoList),
            contains(book_1)
        );
    }

    @Test
    void testSearchByAuthorQuery() {
        assertThat(
            searchService.search("Author", bookDtoList),
            contains(book_1, book_2, book_3, book_4, book_5)
        );
    }

    @Test
    void testSearchByNumber1Query() {
        assertThat(
            searchService.search("1", bookDtoList),
            contains(book_1, book_2, book_3, book_4)
        );
    }

    @Test
    void testSearchByCycleCheckBox() {
        assertThat(
            searchService.search(CYCLE_EN_ALIAS, bookDtoList),
            contains(book_1, book_2, book_3, book_4, book_5)
        );
        assertThat(
            searchService.search(CYCLE_RU_ALIAS, bookDtoList),
            contains(book_1, book_3, book_5)
        );
        assertThat(
            searchService.search("!" + CYCLE_EN_ALIAS, bookDtoList),
            contains(book_2, book_4)
        );
        assertThat(
            searchService.search("!" + CYCLE_RU_ALIAS, bookDtoList),
            contains(book_2, book_4)
        );
    }

    @Test
    void testSearchByReadCheckBox() {
        assertThat(
            searchService.search(READ_EN_ALIAS, bookDtoList),
            contains(book_1, book_3, book_5)
        );
        assertThat(
            searchService.search(READ_RU_ALIAS, bookDtoList),
            contains(book_1, book_3, book_5)
        );
        assertThat(
            searchService.search("!" + READ_EN_ALIAS, bookDtoList),
            contains(book_2, book_4)
        );
        assertThat(
            searchService.search("!" + READ_RU_ALIAS, bookDtoList),
            contains(book_2, book_4)
        );
    }

    @Test
    void testSearchBySignCheckBox() {
        assertThat(
            searchService.search(SIGN_EN_ALIAS, bookDtoList),
            contains(book_1, book_3, book_5)
        );
        assertThat(
            searchService.search(SIGN_RU_ALIAS, bookDtoList),
            contains(book_1, book_3, book_5)
        );
        assertThat(
            searchService.search("!" + SIGN_EN_ALIAS, bookDtoList),
            contains(book_2, book_4)
        );
        assertThat(
            searchService.search("!" + SIGN_RU_ALIAS, bookDtoList),
            contains(book_2, book_4)
        );
    }

    @Test
    void testSearchByHardcoverCheckBox() {
        assertThat(
            searchService.search(HARDCOVER_EN_ALIAS, bookDtoList),
            contains(book_1, book_3, book_4, book_5)
        );
        assertThat(
            searchService.search(HARDCOVER_RU_ALIAS, bookDtoList),
            contains(book_1, book_3, book_4, book_5)
        );
        assertThat(
            searchService.search("!" + HARDCOVER_EN_ALIAS, bookDtoList),
            contains(book_2)
        );
        assertThat(
            searchService.search("!" + HARDCOVER_RU_ALIAS, bookDtoList),
            contains(book_2)
        );
    }

    @Test
    void testSearchByEqualsPages() {
        assertThat(
            searchService.search("%s = 300".formatted(PAGES_EN_ALIAS), bookDtoList),
            contains(book_3)
        );
        assertThat(
            searchService.search("%s = 300".formatted(PAGES_RU_ALIAS), bookDtoList),
            contains(book_3)
        );
        assertThat(
            searchService.search("%s=300".formatted(PAGES_EN_ALIAS), bookDtoList),
            contains(book_3)
        );
        assertThat(
            searchService.search("%s=300".formatted(PAGES_RU_ALIAS), bookDtoList),
            contains(book_3)
        );
    }

    @Test
    void testSearchByMorePages() {
        assertThat(
            searchService.search("%s > 300".formatted(PAGES_EN_ALIAS), bookDtoList),
            contains(book_4, book_5)
        );
        assertThat(
            searchService.search("%s > 300".formatted(PAGES_RU_ALIAS), bookDtoList),
            contains(book_4, book_5)
        );
        assertThat(
            searchService.search("%s>300".formatted(PAGES_EN_ALIAS), bookDtoList),
            contains(book_4, book_5)
        );
        assertThat(
            searchService.search("%s>300".formatted(PAGES_RU_ALIAS), bookDtoList),
            contains(book_4, book_5)
        );
    }

    @Test
    void testSearchByMoreOrEqualsPages() {
        assertThat(
            searchService.search("%s >= 300".formatted(PAGES_EN_ALIAS), bookDtoList),
            contains(book_3, book_4, book_5)
        );
        assertThat(
            searchService.search("%s >= 300".formatted(PAGES_RU_ALIAS), bookDtoList),
            contains(book_3, book_4, book_5)
        );
        assertThat(
            searchService.search("%s>=300".formatted(PAGES_EN_ALIAS), bookDtoList),
            contains(book_3, book_4, book_5)
        );
        assertThat(
            searchService.search("%s>=300".formatted(PAGES_RU_ALIAS), bookDtoList),
            contains(book_3, book_4, book_5)
        );
    }

    @Test
    void testSearchByLessOrEqualsPages() {
        assertThat(
            searchService.search("%s <= 300".formatted(PAGES_EN_ALIAS), bookDtoList),
            contains(book_1, book_2, book_3)
        );
        assertThat(
            searchService.search("%s <= 300".formatted(PAGES_RU_ALIAS), bookDtoList),
            contains(book_1, book_2, book_3)
        );
        assertThat(
            searchService.search("%s<=300".formatted(PAGES_EN_ALIAS), bookDtoList),
            contains(book_1, book_2, book_3)
        );
        assertThat(
            searchService.search("%s<=300".formatted(PAGES_RU_ALIAS), bookDtoList),
            contains(book_1, book_2, book_3)
        );
    }

    @Test
    void testSearchByLessPages() {
        assertThat(
            searchService.search("%s < 300".formatted(PAGES_EN_ALIAS), bookDtoList),
            contains(book_1, book_2)
        );
        assertThat(
            searchService.search("%s < 300".formatted(PAGES_RU_ALIAS), bookDtoList),
            contains(book_1, book_2)
        );
        assertThat(
            searchService.search("%s<300".formatted(PAGES_EN_ALIAS), bookDtoList),
            contains(book_1, book_2)
        );
        assertThat(
            searchService.search("%s<300".formatted(PAGES_RU_ALIAS), bookDtoList),
            contains(book_1, book_2)
        );
    }

    @Test
    void testSearchByNotEqualsPages() {
        assertThat(
            searchService.search("%s != 300".formatted(PAGES_EN_ALIAS), bookDtoList),
            contains(book_1, book_2, book_4, book_5)
        );
        assertThat(
            searchService.search("%s != 300".formatted(PAGES_RU_ALIAS), bookDtoList),
            contains(book_1, book_2, book_4, book_5)
        );
        assertThat(
            searchService.search("%s!=300".formatted(PAGES_EN_ALIAS), bookDtoList),
            contains(book_1, book_2, book_4, book_5)
        );
        assertThat(
            searchService.search("%s!=300".formatted(PAGES_RU_ALIAS), bookDtoList),
            contains(book_1, book_2, book_4, book_5)
        );
    }

    @Test
    void testSearchByCloseEqualsPages() {
        assertThat(
            searchService.search("%s ~= 3".formatted(PAGES_EN_ALIAS), bookDtoList),
            contains(book_3)
        );
        assertThat(
            searchService.search("%s ~= 3".formatted(PAGES_RU_ALIAS), bookDtoList),
            contains(book_3)
        );
        assertThat(
            searchService.search("%s~=3".formatted(PAGES_EN_ALIAS), bookDtoList),
            contains(book_3)
        );
        assertThat(
            searchService.search("%s~=3".formatted(PAGES_RU_ALIAS), bookDtoList),
            contains(book_3)
        );
    }

    @Test
    void testSearchByEqualToZeroPrice() {
        assertThat(
            searchService.search("%s = 0".formatted(PRICE_EN_ALIAS), bookDtoList),
            contains(book_3, book_4)
        );
        assertThat(
            searchService.search("%s = 0".formatted(PRICE_RU_ALIAS), bookDtoList),
            contains(book_3, book_4)
        );
    }

    @Test
    void testSearchByNotEqualToZeroPrice() {
        assertThat(
            searchService.search("%s != 0".formatted(PRICE_EN_ALIAS), bookDtoList),
            contains(book_1, book_2, book_5)
        );
        assertThat(
            searchService.search("%s != 0".formatted(PRICE_RU_ALIAS), bookDtoList),
            contains(book_1, book_2, book_5)
        );
    }

    @Test
    void testSearchByMorePrice() {
        assertThat(
            searchService.search("%s > 0".formatted(PRICE_EN_ALIAS), bookDtoList),
            contains(book_1, book_2, book_5)
        );
        assertThat(
            searchService.search("%s > 0".formatted(PRICE_RU_ALIAS), bookDtoList),
            contains(book_1, book_2, book_5)
        );
    }

    @Test
    void testSearchByMoreOrEqualPrice() {
        assertThat(
            searchService.search("%s >= 0".formatted(PRICE_EN_ALIAS), bookDtoList),
            contains(book_1, book_2, book_3, book_4, book_5)
        );
        assertThat(
            searchService.search("%s >= 0".formatted(PRICE_RU_ALIAS), bookDtoList),
            contains(book_1, book_2, book_3, book_4, book_5)
        );
    }

    @Test
    void testSearchByLessOrEqualPrice() {
        assertThat(
            searchService.search("%s <= 0".formatted(PRICE_EN_ALIAS), bookDtoList),
            contains(book_3, book_4)
        );
        assertThat(
            searchService.search("%s <= 0".formatted(PRICE_RU_ALIAS), bookDtoList),
            contains(book_3, book_4)
        );
    }

    @Test
    void testSearchByLessPrice() {
        assertThat(
            searchService.search("%s < 0".formatted(PRICE_EN_ALIAS), bookDtoList),
            empty()
        );
        assertThat(
            searchService.search("%s < 0".formatted(PRICE_RU_ALIAS), bookDtoList),
            empty()
        );
    }

    @Test
    void testSearchByEqualPrice() {
        assertThat(
            searchService.search("%s = 555.555".formatted(PRICE_EN_ALIAS), bookDtoList),
            contains(book_5)
        );
        assertThat(
            searchService.search("%s = 555.555".formatted(PRICE_RU_ALIAS), bookDtoList),
            contains(book_5)
        );
    }

    @Test
    void testSearchByEqualByNotFullPrice() {
        assertThat(
            searchService.search("%s = 555".formatted(PRICE_EN_ALIAS), bookDtoList),
            empty()
        );
        assertThat(
            searchService.search("%s = 555".formatted(PRICE_RU_ALIAS), bookDtoList),
            empty()
        );
    }

    @Test
    void testSearchByCloseEqualPrice() {
        assertThat(
            searchService.search("%s ~= 555".formatted(PRICE_EN_ALIAS), bookDtoList),
            contains(book_5)
        );
        assertThat(
            searchService.search("%s ~= 555".formatted(PRICE_RU_ALIAS), bookDtoList),
            contains(book_5)
        );
    }

    @Test
    void testSearchByAndFilters() {
        assertThat(
            searchService.search(
                "%s > 100 AND %s <= 300".formatted(PAGES_EN_ALIAS, PAGES_EN_ALIAS),
                bookDtoList),
            contains(book_2, book_3)
        );
        assertThat(
            searchService.search(
                "%s > 100 И %s <= 300".formatted(PAGES_EN_ALIAS, PAGES_EN_ALIAS),
                bookDtoList),
            contains(book_2, book_3)
        );
    }

    @Test
    void testSearchByOrFilters() {
        assertThat(
            searchService.search("%s = 0 OR %s > 400".formatted(PRICE_EN_ALIAS, PRICE_RU_ALIAS), bookDtoList),
            contains(book_3, book_4, book_5)
        );
        assertThat(
            searchService.search("%s = 0 ИЛИ %s > 400".formatted(PRICE_EN_ALIAS, PRICE_RU_ALIAS), bookDtoList),
            contains(book_3, book_4, book_5)
        );
    }

    @Test
    void testSearchByOrFiltersWithNumberAndCheckbox() {
        assertThat(
            searchService.search("%s OR %s = 0 ".formatted(READ_EN_ALIAS, PRICE_EN_ALIAS), bookDtoList),
            contains(book_1, book_3, book_4, book_5)
        );
        assertThat(
            searchService.search("%s ИЛИ %s = 0 ".formatted(READ_EN_ALIAS, PRICE_EN_ALIAS), bookDtoList),
            contains(book_1, book_3, book_4, book_5)
        );
    }

    @Test
    public void testSearchByAndFiltersWithNumberAndCheckbox() {
        assertThat(
            searchService.search("%s AND %s = 0 ".formatted(READ_EN_ALIAS, PRICE_EN_ALIAS), bookDtoList),
            contains(book_3)
        );
        assertThat(
            searchService.search("%s И %s = 0 ".formatted(READ_EN_ALIAS, PRICE_EN_ALIAS), bookDtoList),
            contains(book_3)
        );
    }

    @Test
    public void testSearchByManyLogicalOperands() {
        assertThat(
            searchService.search("%s > 200 AND %s OR %s = 0 ".formatted(PAGES_EN_ALIAS, SIGN_EN_ALIAS, PRICE_EN_ALIAS), bookDtoList),
            contains(book_3, book_4, book_5)
        );
        assertThat(
            searchService.search("%s > 200 И %s ИЛИ %s = 0 ".formatted(PAGES_RU_ALIAS, SIGN_RU_ALIAS, PRICE_RU_ALIAS), bookDtoList),
            contains(book_3, book_4, book_5)
        );
    }

    private CheckBox checkBox(boolean value) {
        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(value);
        return checkBox;
    }
}