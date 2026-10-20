package com.tsel.home.project.booklibrary.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.tsel.home.project.booklibrary.dto.BookDTO;
import com.tsel.home.project.booklibrary.filter.FilterCatalog;
import com.tsel.home.project.booklibrary.filter.FilterField;
import com.tsel.home.project.booklibrary.filter.condition.NumericCondition;
import com.tsel.home.project.booklibrary.filter.condition.NumericOperation;
import com.tsel.home.project.booklibrary.filter.processor.impl.NumericFilterProcessor;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.Test;

class BookPriceFormatterTest {

    @Test
    void testPriceLabelsDistinguishPricelessGiftAndPaidBooks() {
        assertEquals("Бесценно", BookPriceFormatter.format(null));
        assertEquals("Подарок", BookPriceFormatter.format(0.0));
        assertEquals("1\u00a0250,5 ₽", BookPriceFormatter.format(1250.5));
    }

    @Test
    void testTotalPriceUsesWholeRublesAndRussianSeparatorsForAnyDefaultLocale() {
        Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(Locale.US);
            assertEquals("1\u00a0234\u00a0567 ₽", BookPriceFormatter.formatTotal(1234567));
            assertEquals("1\u00a0250 ₽", BookPriceFormatter.formatTotal(1250));
            assertEquals("0 ₽", BookPriceFormatter.formatTotal(0));
            assertEquals("1\u00a0251 ₽", BookPriceFormatter.formatTotal(1250.89));
        } finally {
            Locale.setDefault(previous);
        }
    }

    @Test
    void testPriceFilterAndTagsUseTheSameMeaningAsTheBookDetails() {
        FilterCatalog catalog = new FilterCatalog(BookDTO.class);
        FilterField field = catalog.fields().stream().filter(value -> value.id().equals("price")).findFirst().orElseThrow();
        NumericFilterProcessor processor = (NumericFilterProcessor) field.processor();
        BookDTO priceless = BookDTO.builder().price(null).build();
        BookDTO gift = BookDTO.builder().price(0.0).build();
        BookDTO paid = BookDTO.builder().price(1250.5).build();
        List<BookDTO> books = List.of(priceless, gift, paid);
        NumericCondition empty = processor.condition(NumericOperation.EMPTY, "", "");
        NumericCondition present = processor.condition(NumericOperation.NOT_EMPTY, "", "");
        assertEquals(List.of(priceless), catalog.filter(books, Map.of("price", empty)));
        assertEquals(List.of(gift, paid), catalog.filter(books, Map.of("price", present)));
        assertEquals(BookPriceFormatter.format(null), empty.describe(field));
        assertEquals("Стоимость указана", present.describe(field));
        assertFalse(empty.matches(BigDecimal.ZERO));
    }
}
