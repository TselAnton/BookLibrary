package com.tsel.home.project.booklibrary.ui;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Отображает стоимость по исходным правилам библиотеки: отсутствие цены означает
 * «Бесценно», нулевая цена — «Подарок». Числовое значение в хранилище не изменяется.
 */
public final class BookPriceFormatter {

    /** Подпись отсутствующей стоимости; одновременно используется фильтром пустого значения. */
    public static final String PRICELESS = "Бесценно";

    private BookPriceFormatter() {}

    /**
     * Формирует подпись для карточки, сохраняя различие между отсутствием цены и нулём.
     * @param price сохранённая стоимость; {@code null} означает «Бесценно»
     * @return специальная подпись или число с разделителями ru-RU и знаком рубля
     */
    public static String format(Double price) {
        if (price == null) {
            return PRICELESS;
        }
        if (price == 0.0) {
            return "Подарок";
        }
        return formatAmount(price, 2);
    }

    /**
     * Формирует итоговую стоимость в целых рублях, включая нулевую сумму.
     * @param price суммарная стоимость найденных книг
     * @return число с разделителями ru-RU без дробной части и со знаком рубля
     */
    public static String formatTotal(double price) {
        return formatAmount(price, 0);
    }

    private static String formatAmount(double price, int maximumFractionDigits) {
        NumberFormat format = NumberFormat.getNumberInstance(Locale.forLanguageTag("ru-RU"));
        format.setMaximumFractionDigits(maximumFractionDigits);
        return format.format(price) + " ₽";
    }
}
