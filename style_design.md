# Стиль оформления кода BookLibrary

## 1. Назначение и основание

Этот документ — стандарт оформления всего будущего кода BookLibrary, включая тесты и ресурсы интерфейса. При добавлении и изменении кода необходимо руководствоваться им.

Стандарт получен из текущего содержимого проекта на 18 октября 2026 года: прочитаны все 97 Java-файлов — 83 в `src/main/java` и 14 в `src/test/java`. В это число входит `module-info.java`. Проверены все 106 объявлений именованных типов, включая вложенные: 91 класс, 7 интерфейсов, 4 типа аннотаций, 3 record и 1 enum. Также рассмотрены `pom.xml`, семь FXML, четыре CSS и `log4j2.xml`.

В основу положено фактическое оформление исходников. Внешние руководства по Java не используются. В игнорируемом Git файле `.idea/codeStyles/codeStyleConfig.xml` указан `GoogleStyle`, но это не полное описание оформления проекта: например, основной отступ исходников равен четырём пробелам. Настройки IDE не заменяют этот документ.

Документ различает:

- **Основное правило** — повторяющийся вариант, который применяется к новому коду.
- **Существующий вариант** — реально встречающееся альтернативное оформление. Его наличие не означает, что нужно перемешивать варианты в одном новом фрагменте.
- **Отсутствующая конструкция** — для неё нельзя утверждать, что в проекте уже сложился отдельный стиль.

При локальной правке сохранять группировку и порядок окружающего кода. Для нового или существенно переписываемого блока использовать основные правила ниже. Не переформатировать весь файл ради небольшой правки. Бизнес-логику, зависимости и API не менять только ради оформления.

Примеры ниже взяты из проекта или сокращены из существующих фрагментов. Сокращение состава полей и действий не задаёт новое поведение приложения. Реестр в конце перечисляет каждый проверенный Java-файл и особенности его оформления.

## 2. Файлы, кодировка и отступы

| Элемент | Основное правило | Основание |
| --- | --- | --- |
| Кодировка | UTF-8 без BOM | `pom.xml`, исходники и XML-декларации; BOM отсутствует во всех 97 Java-файлах |
| Перевод строки | CRLF для новых исходников и ресурсов | Все проверенные Java, FXML, CSS и `log4j2.xml` используют CRLF |
| Конец файла | Завершать переводом строки | Так оформлен 91 из 97 Java-файлов; исключения перечислены в разделе 19 |
| Отступ Java | 4 пробела на уровень | Подавляющее большинство классов и тестов |
| Продолжение Java | Ещё 4 пробела относительно начала выражения или объявления | Builder, Stream, Optional, многострочные параметры |
| Табуляция | Использовать пробелы | В Java-файлах символов табуляции нет |
| Выравнивание | Отступ по уровням, без колонок из пробелов для `=` и типов | Объявления полей и локальных переменных |
| Длина строки | Единого жёсткого ограничения нет | В Java есть 59 строк длиннее 120 символов; максимальная длина в текущем снимке — 154 |

Не вводить ограничение 80, 100 или 120 символов как якобы существующее правило. Короткий вызов оставлять в одной строке, длинный или структурно сложный переносить по правилам раздела 7. Число 154 описывает текущий код и не является новым пределом.

Исключения по отступам: `Timer` использует 3 пробела на уровень; отдельные цепочки в `AutoCompleteComboBoxListener` и одном тесте `SearchServiceTest` имеют продолжение на 8 пробелов; `MainViewController.onSort` содержит продолжения на 2 пробела. Для нового самостоятельного кода основной вариант — 4 пробела.

## 3. Имена и расположение исходников

- Пакеты записывать строчными буквами: `com.tsel.home.project.booklibrary.search.filter.impl`.
- Java-файл называть по основному типу: `BookRepository.java`, `SearchField.java`.
- Классы, интерфейсы, enum, record и аннотации именовать в `UpperCamelCase`.
- Сохранять употребляемые в проекте суффиксы: `Controller`, `Repository`, `Provider`, `Converter`, `SearchFilter`, `Utils`, `Matcher`, `DTO`. Аббревиатура `DTO` пишется заглавными: `BookDTO`, `BookDTOBuilder`.
- Поля, параметры, локальные переменные и методы именовать в `lowerCamelCase`: `bookForEdit`, `searchQuery`, `resolveStoragePath`.
- Константы и статические final-ссылки оформлять как `UPPER_SNAKE_CASE`: `DEFAULT_REPOSITORY_PATH`, `BOOK_NAME_COMPARATOR`, `CYCLE_REPOSITORY`. Это относится и к изменяемым объектам за final-ссылкой, например `SEARCH_FILTER_MAP`. Сериализационное поле сохраняет специальное имя `serialVersionUID`.
- Для boolean встречаются имена как с префиксом, так и без него: `isTransactionOpen`, `moveCaretToPos`, `read`, `ended`. Продолжать модель именования конкретной сущности или контроллера.
- Для простых параметров типов использовать существующие обозначения `K`, `E`, `D`, `T`, `S`, `R`. Значимые локальные переменные обычно имеют полные имена: `searchFieldDefinitionEntry`, `resolvedStoragePath`.
- В тестах сохранять семейство имён: `SearchServiceTest`, `CreateBookTests`, `TestEntity`, `AbstractJavaFxTest`. Встречающееся различие `JavaFX`/`JavaFx` само по себе не является задачей переименования.
- Ресурсы именовать строчными словами через дефис: `audio-book-sites-view.fxml`, `main-view-buttons.css`.

Опечатки в существующих идентификаторах, например `cashedImages`, `PROPERTY_SPLITERATOR`, `checkConstrains`, не задают правило для новых имён. Существующие имена не исправлять автоматически в рамках форматирования.

## 4. Структура Java-файла и импорты

Порядок частей файла:

1. `package`.
2. Пустая строка.
3. Статические импорты, если нужны.
4. Пустая строка между статическими и обычными импортами.
5. Обычные импорты.
6. Пустая строка.
7. Javadoc типа, если есть, затем аннотации и объявление типа.

Основной вариант — единый блок обычных импортов, упорядоченный по полному имени. Отдельные пустые строки для `java`, JavaFX и Lombok обычно не добавляются. Порядок, например: `com...`, `java...`, `javafx...`, `javax...`, `lombok...`, `org...`. Внутри блока статических импортов также используется порядок по полному имени.

Пример: `FileRepositoryProvider`.

```java
package com.tsel.home.project.booklibrary.helper;

import static com.tsel.home.project.booklibrary.utils.FileUtils.isNotExists;
import static com.tsel.home.project.booklibrary.utils.StringUtils.isBlank;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tsel.home.project.booklibrary.dao.annotation.FileStorageName;
```

Статические импорты применяются к методам (`getBean`, `ofNullable`, `format`, `observableArrayList`), константам (`TRUE`, `WARNING`, `UTF_8`) и assertions. Полная квалификация через тип также встречается: `SimpleApplicationContext.getBean`, `Boolean.TRUE`, `Comparator.comparing`.

Для нового файла исходный вариант — явные импорты. Wildcard-импорты являются существующим вариантом: `java.util.*` в `AbstractFileRepository`, `dao.data.*` и `dao.repository.impl.*` в `BookConverter`, `filter.impl.*` в `SearchFilterFactory`, `lombok.*` в `Genre`. Числового порога перехода на `*` в исходниках не установлено. У `Genre`, `GenreDTO` и `SearchFilterFactory` есть дополнительное разделение обычных импортов пустой строкой. Не переносить такие различия на все новые файлы.

## 5. Объявления типов и состав класса

### 5.1. Класс, наследование, модификаторы

Открывающая `{` находится на строке объявления после одного пробела. Закрывающая `}` — на отдельной строке с отступом объявления. После открытия непустого класса перед первым членом обычно есть пустая строка.

```java
public class AudioBookSiteConverter implements Converter<AudioBookSite, AudioBookSiteDTO> {

    @Override
    public AudioBookSiteDTO convert(AudioBookSite entity) {
        return AudioBookSiteDTO.builder()
            .id(entity.getId())
            .name(entity.getName())
            .build();
    }
}
```

`extends` и `implements` находятся на строке объявления, включая длинные объявления `AbstractFileRepository` и `TestFileRepository`. Несколько интерфейсов разделяются запятой и пробелом: `implements Serializable, ComboBoxDTO`.

Наблюдаемый порядок модификаторов: `public abstract class`, `public final class`, `public static class`, `protected static class`, `private static class`; у членов — `private static final`, `protected final`, `public static`, `protected abstract`, `protected final`. Не добавлять `final` всем классам или параметрам: такой всеобщей практики нет.

### 5.2. Группировка членов

Типичный порядок: константы и статические поля; зависимости и состояние; конструкторы; методы публичного или защищённого контракта; приватные вспомогательные методы. Это схема группировки, а не жёсткая сортировка всех членов по видимости.

- `static { ... }` размещается после относящихся к нему объявлений и до использующего их кода. В `PropertyProvider` он расположен после полей экземпляра, в `MainViewController` — перед ними.
- Конструкторы репозиториев идут парой: сначала принимающий `Path`, затем без аргументов; далее собственные методы и переопределения.
- Абстрактные методы в `AbstractBookController` сгруппированы перед реализацией. В других классах абстрактный hook может стоять рядом с использующими его методами.
- Методы доступа могут группироваться вместе с обслуживаемым полем, как JavaFX property API в `CustomCheckBoxTableCell`.
- Вложенные типы обычно находятся в конце внешнего типа. `App.JavaFXRunner` занимает основную часть `App`.

### 5.3. Интерфейсы, default-методы и аннотации

Методы интерфейса записываются без избыточных `public abstract`, завершаются `;` и отделяются пустыми строками. `default`-метод оформляется как обычный метод с телом.

```java
public interface SearchFilter {

    Class<?> getFilterValueClass();

    boolean filter(Object value, String searchQuery, SearchFieldDefinition definition);

    default String getTooltipInfo() {
        return "";
    }
}
```

Тип аннотации объявляется как `public @interface`. Аннотации `@Target` и `@Retention` располагаются перед объявлением, каждая на своей строке. Члены и их значения по умолчанию:

```java
public @interface Property {

    String value();

    boolean nullable() default true;
}
```

В `SearchField` массив по умолчанию оформлен как `String[] aliases() default {};`. Маркерный `BaseOperand` имеет многострочное пустое тело с пустой строкой внутри.

### 5.4. Enum

Образец — единственный enum `OperatorType`.

```java
@Getter
public enum OperatorType {
    AND("AND", "И"),
    OR("OR", "ИЛИ");

    private final List<String> names;

    OperatorType(String... names) {
        this.names = Arrays.asList(names);
    }
}
```

Каждая константа на отдельной строке; между константами запятые, после последней при наличии членов — `;`. Перед первой константой пустой строки нет. Между списком констант, полями и конструктором — пустые строки. Конструктор enum без модификатора доступа.

### 5.5. Record, вложенные и анонимные типы

Компоненты record записываются в строке объявления через запятую и пробел. В проекте есть оба варианта пустого тела:

```java
public record SearchFieldDefinition(List<String> aliases) {

}
```

```java
public record ImageHolder(Image image, boolean isDefault) {}
```

Первый — самостоятельный тип, второй — вложенный в `ImageProvider`; вложенный `SortingArchivePath` также использует `{}`. Для новых аналогичных типов продолжать соответствующий вариант. Компактных или явно объявленных конструкторов record в проекте нет.

Вложенные классы получают дополнительный уровень отступа. Примеры: `App.JavaFXRunner`, `BookDTO.BookDTOBuilder`, `MyGson.GsonExclusionStrategy`. Пустые тестовые вложенные репозитории записываются в одну строку с `{}`.

Нестатический вложенный класс оформляется с теми же отступами: `public final class ImageHolder` внутри `ImageProvider`. Модификатор `static` отсутствует, когда экземпляр использует состояние внешнего объекта; `ImageHolder` получает общую обложку из `defaultImg`.

Анонимный класс с содержательным телом оформляется многострочно (`MainViewController.loadBookViewWindow`):

```java
Stage stage = new Stage() {
    @Override
    public void hide() {
        updateTableColumns(bookTableView);
        super.hide();
    }
};
```

Между `{` анонимного класса и первым `@Override` пустой строки нет. Между его методами пустая строка есть. В `CustomCheckBoxTableCell.converter` встречается однострочная реализация `invalidated()` внутри анонимного класса; для нового содержательного тела использовать многострочный образец выше.

## 6. Поля, конструкторы и методы

### 6.1. Поля и локальные переменные

Одно объявление на строку, пробелы вокруг `=`, без выравнивания имён или инициализаторов по колонкам.

```java
private final String entityDisplayName;
private final Map<Field, Property> notNullFields;
private final IdentifierGenerator<K> keyGenerator;

private boolean isTransactionOpen;
private Map<K, E> repositoryMap;
private Map<K, E> repositoryMapSnapshot;
```

Связанные неаннотированные поля могут идти подряд: `id` и `name` в небольших DTO, зависимости в `AbstractViewController`. Аннотированные предметные поля и FXML-поля обычно разделяются одной пустой строкой. Это не означает, что нужно вставлять пустую строку между любыми двумя полями.

`volatile` располагается после модификатора доступа и перед типом. Пример флага отмены фоновой загрузки в `ImageProvider.ImageHolder`: `private volatile boolean cancelled;`.

Если initializer переносится после `=`, первая строка выражения получает ещё 4 пробела:

```java
Callback<TableColumn<AudioBookSiteDTO, String>, TableCell<AudioBookSiteDTO, String>> audioBookSiteNameCellFactory =
    (TableColumn<AudioBookSiteDTO, String> param) -> new AudioBookSiteNameEditableColumn();
```

Обычная практика — явный тип локальной переменной. `var` встречается в трёх объявлениях одного теста `SearchServiceTest.testSearchByBookSearchQueryWithIo`; это существующий локальный вариант, а не основание переводить все объявления на `var`.

### 6.2. Конструкторы и `this`/`super`

Конструкторы оформляются как методы без возвращаемого типа. Присваивания полям при совпадении имён записываются через `this`. `super(...)` находится отдельным выражением в начале конструктора наследника.

```java
public AuthorRepository(Path rootPath) {
    super(Author.class, new UUIDIdentifierGenerator(), rootPath);
}
```

Пустые приватные конструкторы утилит компактны: `private StringUtils() {}`. Их положение различается: обычно перед методами, у `TableComparators` — в конце. Сохранять выбранную в файле группировку.

`this` используется и без совпадения имён (`this.repositoryMap`, `this.getClass()`), и опускается (`listType = ...` в `FileRepositoryProvider`). Всеобщего требования добавлять или удалять `this` нет. Внутреннее обращение `AutoCompleteComboBoxListener.this` записывается без пробелов вокруг точек.

### 6.3. Методы

Возвращаемый тип и имя метода разделяются пробелом. Между именем и `(` пробела нет. Между параметрами — запятая и пробел. Внутри круглых скобок дополнительных пробелов нет. Даже метод с единственным `return` обычно имеет многострочное тело.

```java
public String getProperty(String propertyName) {
    return this.properties.getOrDefault(propertyName, DEFAULT_PROPERTIES.get(propertyName));
}
```

`throws` пишется после списка параметров на той же строке, включая строку закрытия многострочной сигнатуры. Объявление без тела завершается `;`.

Аннотации методов — каждая на своей строке непосредственно перед объявлением. Обычная пара — `@Override`, затем `@SuppressWarnings("unchecked")`. Параметрическая `@Nullable` пишется рядом с типом параметра; `@Nullable` метода располагается над объявлением. В `TestFileRepository` она стоит перед `@Override`, поэтому универсального порядка всех аннотаций нет.

Пустой hook может быть компактным (`handleActionEvent(ActionEvent event) {}`) или содержать пояснение:

```java
protected void doAfterSave(Book savedBook) {
    // FOR OVERWRITE
}
```

Для аналогичного hook сохранять существующий вариант его семейства. Не заменять комментарий фиктивным действием.

## 7. Переносы строк и пустые строки

### 7.1. Сигнатуры и списки аргументов

Короткая сигнатура остаётся в одну строку. Развёрнутая сигнатура: открывающая `(` в строке имени; каждый параметр на отдельной строке с отступом +4; закрывающая `)` на уровне объявления, затем ` {`.

```java
public void loadModalView(
    String modalViewTitle,
    String modalViewResourceFile,
    int moveByX,
    int moveByY,
    Object... initParameters
) {
    try {
        FXMLLoader loader = new FXMLLoader(this.getClass().getResource(RESOURCE_PATH + modalViewResourceFile));
    } catch (Exception e) {
        log.error("Exception while load model view", e);
    }
}
```

У многострочного вызова используются такие же уровни:

```java
riseAlert(
    WARNING,
    "Ошибка",
    "Указанный файл невозможно открыть как картинку",
    "Файл имеет не поддерживаемое расширение или повреждён"
);
```

Реально используется и смешанный вариант: начало списка остаётся на первой строке, остальные аргументы переносятся (`log.info`, `riseAlert`, `setTextWithSizeControl`, `loadModalView`). Не требуется разворачивать все короткие вызовы.

Для многострочного `List.of(...)` каждый элемент занимает строку, после последнего аргумента запятой нет, закрытие — `);`. Вложенный конструктор вокруг такого списка закрывается `));` (`CreateBookTests`).

### 7.2. Цепочки

Первый вызов может оставаться на строке присваивания или `return`; последующие вызовы начинаются с точки на новом уровне +4. Все звенья одной цепочки имеют один отступ.

```java
return ofNullable(AUTHOR_REPOSITORY.getById(book.getAuthorId()))
    .map(Author::getName)
    .orElse(null);
```

```java
return BookDTO.builder()
    .id(book.getId())
    .name(book.getName())
    .build();
```

```java
return getRepositoryMap().values()
    .stream()
    .filter(author -> author.getName().equalsIgnoreCase(name))
    .findFirst();
```

`stream()` может находиться как на первой строке (`Arrays.stream(...)`, `repositoryClasses.stream()`), так и в продолжении. Короткие цепочки также остаются однострочными. Вложенная цепочка внутри аргумента получает собственный дополнительный уровень:

```java
bookTableView.setItems(observableArrayList(
    getDtoBooks().stream()
        .sorted(Comparator.comparing(BookDTO::getName))
        .toList()
));
```

Закрывающие скобки могут группироваться как `));`. В проекте встречается и вынесенное закрытие внешнего вызова `);`; ориентироваться на реальную вложенность, без требования разносить каждую скобку на отдельную строку.

### 7.3. Логические выражения и тернарный оператор

В многострочном логическом выражении оператор начинается на строке продолжения:

```java
return Objects.equals(existedBook.getName(), newBook.getName())
    && Objects.equals(existedBook.getAuthorId(), newBook.getAuthorId())
    && Objects.equals(existedBook.getPublisherId(), newBook.getPublisherId());
```

```java
if (isBookFieldsInvalid(bookTitle, bookPagesCount, bookPrice, bookPublishYear)
    || isAuthorAndPublisherFieldsInvalid(author, publisher)
    || isCycleFieldsInvalid(cycle, cycleBookNumber, cycleTotalBookCount, cycleEndedFlag)) {
    return;
}
```

Закрывающая `)` условия и `{` находятся в конце последней строки условия. В `AutoCompleteComboBoxListener.handle` используется дополнительный отступ продолжения +8; основной вариант для нового блока — +4.

Короткий тернарный оператор остаётся однострочным. Основной многострочный вариант имеет `?` и `:` в начале отдельных строк:

```java
return rootPath != null
    ? Path.of(rootPath.toAbsolutePath().toString(), path.toString())
    : path;
```

В `CustomCheckBoxTableCell.getSelectedProperty` встречается другой вариант с `?` и `:` в конце предыдущих строк. Сохранить его как локальную особенность; новый самостоятельный фрагмент писать по основному образцу.

### 7.4. Пустые строки

- Между методами и конструкторами обычно одна пустая строка.
- Внутри метода пустая строка отделяет этапы: подготовку данных, проверку, действие, обработку результата.
- Между соседними связанными вызовами пустая строка не обязательна: настройка `Stage`, серия setter-методов, проверки assert.
- После `{` метода перед первым оператором пустая строка обычно отсутствует. Перед обычной закрывающей `}` пустая строка также обычно отсутствует.
- В проекте есть дополнительные пустые строки перед `} catch` и `} else`, особенно между крупными этапами. Они допустимы по контексту; требование вставлять их перед каждой веткой отсутствует.
- Двойные пустые строки есть между отдельными группами FXML-полей и методами контроллеров. Для нового обычного разделителя достаточно одной.

## 8. Пробелы, операторы, литералы и массивы

| Конструкция | Оформление из проекта |
| --- | --- |
| Управляющее слово | `if (condition)`, `for (...)`, `while (...)`, `switch (...)`, `catch (Exception e)` |
| Вызов | `getBean(BookRepository.class)`, без пробела перед `(` |
| Присваивание и бинарные операторы | `value = other`, `a + b`, `value != null`, `a && b`, `length >= 0` |
| Унарные операторы | `!isEmpty(collection)`, `-25`, `i++`, без внутренних пробелов |
| Тернарный оператор | `condition ? first : second` |
| Лямбда | `book -> book.getName()`, пробелы вокруг `->` |
| Ссылка на метод | `Book::getName`, `File::new`, без пробелов вокруг `::` |
| Приведение типа | `(Stage) node.getScene().getWindow()`, `(T) value` |
| Тип массива | `String[] args`, `byte[] buffer`, скобки рядом с типом |
| Создание массива | `new byte[BUFFER_SIZE]`, `new TestEntity[] {}` |
| Varargs | `String... names`, `Object... initParameters`, без пробела перед `...` |
| Типовой литерал | `Book.class`, `Boolean.class` |
| Строки | Двойные кавычки, кириллица напрямую; `"\n"`, `"\""`, `"\\"` для экранирования |
| Числа | `0`, `0.0`, `1412423095L`, `13f`, `5678d`; в проекте встречаются `L`, `f` и `d` |

У `(BooleanProperty)booleanProperty` в `CustomCheckBoxTableCell` пропущен пробел после приведения; основной вариант — пробел, как у остальных приведений. Оба варианта пустого массива `new TestEntity[] {}` и `new TestEntity[]{}` встречаются в `FileRepositoryProviderTest`; для нового подобного выражения использовать первый.

В массивах значений аннотации нет внутренних пробелов: `{"author", "автор"}`. У пустого значения — `{}`. Элементы отделяются запятой и пробелом.

Конкатенация строк оформляется с пробелами вокруг `+`. Для форматирования встречаются статический `format(...)` и `"...".formatted(...)`; сохранить принятую в соответствующем коде форму. Используются `%s`, `%d`, `%n`, а также явные `\n`; не заменять их механически друг на друга. Регулярное выражение в `SearchService` — обычный строковый литерал в `Pattern.compile(...)`. Обязательных text block или другого нового синтаксиса нет.

## 9. Управление потоком

### 9.1. Условия и ранний выход

Основной вариант — фигурные скобки даже для одного действия. `else` и `else if` продолжают строку закрывающей скобки:

```java
if (keyEvent.getCode() == KeyCode.ESCAPE) {
    cancelChanges();
}
```

```java
if (empty) {
    setText(item);
    setGraphic(null);
} else {
    setText(getString());
    setGraphic(null);
}
```

Ранние `return`, `return null`, `return false` и `continue` используются как отдельные операторы внутри блоков проверок (`SearchService`, `AbstractBookController`, `StorageBackupArchiver`). Дополнительная пустая строка после каждого раннего выхода не обязательна: несколько последовательных проверок могут идти подряд.

Существующий компактный вариант — `if (price == null) return "Бесценно";` в `BookInfoViewController.resolvePrice` и однострочные `if` в `MainViewController.cleanUpRepositoryData`. Для нового самостоятельного блока выбирать основной многострочный вариант со скобками.

### 9.2. Циклы

```java
for (Entry<Field, Property> notNullFieldEntry : this.notNullFields.entrySet()) {
    Field field = notNullFieldEntry.getKey();
    Property fieldAnnotation = notNullFieldEntry.getValue();
}
```

```java
for (int i = 0; i < comboBox.getItems().size(); i++) {
    if (Objects.equals(comboBox.getItems().get(i).getId(), itemId)) {
        comboBox.getSelectionModel().select(i);
        return;
    }
}
```

В enhanced for — пробелы вокруг `:`. В обычном `for` — пробел после каждого `;`. Тело цикла на следующем уровне.

`while` используется и с обычным условием, и с присваиванием внутри условия (`StorageBackupArchiverTest`):

```java
while ((len = zipInputStream.read(buffer)) > 0) {
    fileOutputStream.write(buffer, 0, len);
}
```

В основном `StorageBackupArchiver` есть `while((length = ...))` без пробела после слова; основной вариант с пробелом подтверждён тестами и остальными управляющими конструкциями.

### 9.3. Switch и pattern matching

Встречаются switch с правилами `->` и switch expression. Ветки имеют отступ +4 относительно `switch`. Короткое действие остаётся в строке `case`, несколько действий заключаются в блок.

```java
switch (keyEvent.getCode()) {
    case DELETE -> deleteAudioBookSite();
    case ENTER -> addAudioBookSite();
    case ESCAPE -> closeStage(addAudioBookSiteTextField);
    default -> {
        // DO NOTHING
    }
}
```

В `AutoCompleteComboBoxListener` несколько меток разделены запятой: `case BACK_SPACE, DELETE -> { ... }`.

```java
return switch (compositeOperand.getOperator()) {
    case AND -> operandStream.allMatch(operand -> filterByLogicalOperators(operand, bookDTO));
    case OR -> operandStream.anyMatch(operand -> filterByLogicalOperators(operand, bookDTO));
};
```

У switch expression после закрытия стоит `;`. В исчерпывающем switch по enum в `SearchService` ветки `default` нет. Не добавлять её только ради стиля.

Pattern matching оформляется в условии без лишнего приведения:

```java
if (searchOperands instanceof SimpleOperand simpleOperand) {
    return filterBySearchFilters(simpleOperand.getPredicate(), bookDTO);
}
```

Также встречается обычный `instanceof` с последующим явным приведением в `CustomCheckBoxTableCell`. Обязательного переписывания старого варианта документ не вводит.

## 10. Исключения и ресурсы

`try`, `catch` и try-with-resources используют тот же стиль скобок. `catch` пишется в строке закрытия предыдущего блока; несколько `catch` следуют друг за другом.

```java
try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileRepositoryPath.toFile(), UTF_8, false))) {
    writer.write(GSON.toJson(entities));

} catch (IOException e) {
    log.error("Exception while trying to write storage file '{}' line by line", fileRepositoryPath, e);
    throw new IllegalStateException(format("Проблема при записи данных в файл хранилища '%s'", fileRepositoryPath), e);
}
```

Переменные исключений называются `e` или `ex`, без единого требования выбрать одно из них. Используются выбрасывание собственного исключения, оборачивание с исходной причиной, повторный `throw ex` и возврат запасного значения после перехвата. Это разные существующие действия; стиль их записи не определяет, какое поведение необходимо новому методу.

При перехвате нескольких типов в одном `catch` разделять их оператором `|` с пробелами: `catch (IOException | IllegalArgumentException e)` в `ImageProvider.loadCover`.

Длинный конструктор исключения переносится как обычный вызов (`FileUtils.createFile`):

```java
throw new IllegalStateException(
    format("Проблема при создании файла '%s'. Невозможно создать родительскую директорию '%s'", filePath, filePath.getParent()),
    ex
);
```

Тела `ConstraintException` и `NotNullConstraintException` содержат конструктор с `super(...)`, оформленный многострочным телом, даже если это единственное действие. `@SneakyThrows` используется в тестовых файловых утилитах и одном тесте архивирования, каждая аннотация на отдельной строке.

`finally` продолжает строку закрывающей скобки `try` или `catch`. Пример из `BookWindowPlacementTest.checkModalWindow`: окно закрывается и при исключении из проверки.

```java
try {
    assertSame(owner, modal.getOwner());
    assertPlacement(modal, main, bounds);
} finally {
    modal.hide();
}
```

## 11. Дженерики, коллекции и функциональный код

### 11.1. Параметры типов

Внутри `<...>` нет дополнительных пробелов. Несколько параметров отделяются запятой и пробелом. Вложенное закрытие записывается слитно: `Map<Field, Property>`, `List<BaseEntity<T>>`.

Наблюдаемые формы:

```java
public interface BaseEntity<K extends Serializable> extends Serializable {

    K getId();

    void setId(K id);
}
```

- Ограниченный параметр: `<T extends Number>`, `<K extends Serializable, E extends BaseEntity<K>>`.
- Generic-метод: `public static <T> T getBean(Class<T> beanClass)`.
- Generic-метод с несколькими связанными ограничениями: `<T extends BaseEntity<UUID>, R extends FileRepository<UUID, T>>` в `cleanUpRepository`.
- Wildcard: `Class<?>`, `Collection<?>`, `TableColumn<BookDTO, ?>`.
- Верхняя граница wildcard: `ObservableValue<? extends Boolean>`.
- Нижняя граница wildcard: `Map<Class<?>, ? super Object>`.
- Diamond: `new HashMap<>()`, `new FileRepositoryProvider<>(...)`, `new CustomCheckBoxTableCell<>() { ... }`.
- Generic varargs: `TableColumn<AudioBookSiteDTO, ?>... tableColumns` с `@SafeVarargs`.

Явные приведения generic-значений и подавление предупреждений размещаются как в `AbstractSearchFilter` и FXML-контроллерах: `@SuppressWarnings("unchecked")` непосредственно перед методом. `@SuppressWarnings("rawtypes")` стоит над полем `TABLE_COLUMNS_SORTING`.

### 11.2. Коллекции и static initializer

Короткий `List.of(...)` остаётся в строке; длинный список разворачивается по одному элементу. Для коллекций используются интерфейсы в объявлениях и diamond у конструктора: `Map<K, E> entitiesMap = new LinkedHashMap<>();`.

Статический блок оформляется обычным блоком, отделённым пустыми строками:

```java
private static final Map<Class<?>, SearchFilter> SEARCH_FILTER_MAP;

static {
    SEARCH_FILTER_MAP = SEARCH_FILTER_LIST.stream()
        .collect(toMap(SearchFilter::getFilterValueClass, Function.identity()));
}
```

### 11.3. Лямбды, ссылки на методы, Stream и Optional

- Один параметр без явно указанного типа — без скобок: `book -> book.getId()`.
- Несколько параметров — в скобках: `(columnName, fieldName) -> { ... }`.
- Явно типизированные параметры — в скобках: `(TableColumn<AudioBookSiteDTO, String> param) -> ...`.
- Без параметров — `() -> ...`.
- Короткое выражение записывается после `->` в той же строке.
- Многооператорное тело — `{` после стрелки, операторы на следующем уровне, закрытие `});` или `})` по контексту.
- Пустая лямбда — `() -> {}` (`UnitJavaFXTest`).
- Длинная лямбда-выражение может продолжаться после `->` на следующей строке, как callback в `CreateBookTests.validateTabTest`.

```java
return cycleRepository.getByName(cycleComboBox.getName())
    .map(existedCycle -> {
        existedCycle.setEnded(isCycleEnded);
        existedCycle.setBooksInCycle(booksInCycle);
        return existedCycle;
    })
    .orElse(new Cycle(null, cycleComboBox.getName(), isCycleEnded, booksInCycle));
```

Используются ссылки `Type::method`, `instance::method`, `Type::new`, `this::method` и ссылки на вложенные record: `SortingArchivePath::archivePath`. Они не требуют специальных пробелов.

Операции Stream (`filter`, `map`, `peek`, `sorted`, `skip`, `forEach`, `reduce`, `collect`, `toList`, `anyMatch`, `allMatch`, `noneMatch`) и Optional (`map`, `filter`, `orElse`, `orElseThrow`, `ifPresent`) следуют общему стилю цепочек. В проекте есть и `collect(...)`, и `.toList()`; стандарт оформления не требует механической замены одного API другим.

## 12. Аннотации, Lombok, сущности и DTO

Аннотации типа и поля располагаются каждая на отдельной строке, без пустой строки между аннотацией и тем объявлением, к которому она относится. Аннотации идут после Javadoc.

Типичный порядок у сущности (`Author`, `AudioBookSite`, `Cycle`, `Publisher`, `UserSettings`, также `Genre`):

```java
@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EntityDisplayName("Автор")
public class Author implements BaseEntity<UUID> {

    @Serial
    private static final long serialVersionUID = -7697277154251963838L;

    @Property(value = "идентификатор")
    private UUID id;

    @Property(value = "имя", nullable = false)
    private String name;
}
```

`Book` дополнительно содержит `@EqualsAndHashCode(exclude = {"coverImgAbsolutePath"})` перед `@EntityDisplayName`. Не добавлять все эти аннотации любому классу: они описывают существующие семейства моделей.

Особенности оформления:

- Одиночный атрибут `value` может записываться сокращённо (`@EntityDisplayName("Книга")`, `@FileStorageName("bookStorage.json")`) или явно (`@Property(value = "название")`). Сохранять форму конкретной аннотации в этом семействе кода.
- Несколько атрибутов разделяются запятой и пробелом, вокруг `=` — пробелы.
- `@Serial` находится над полем `serialVersionUID`; само поле обычно предшествует данным. В `UserSettings` перед ним находится `UNIQUE_KEY`.
- У небольших справочных DTO типична последовательность `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`.
- `AudioBookSiteDTO` и `BookDTO` используют отдельные `@Getter` и `@Setter`. Для `BookDTO` затем идут `@Builder`, конструкторные аннотации, `@ToString(exclude = "cover")`, `@EqualsAndHashCode(exclude = "cover")`.
- У `TestEntity` собственный наблюдаемый порядок: `@Getter`, `@Setter`, `@ToString`, `@EqualsAndHashCode`, конструкторные аннотации.
- Дополнительный конструктор DTO и собственный `toString()` располагаются после полей. Однострочный результат метода возвращается в многострочном теле.
- Кастомизация Lombok builder — вложенный `public static class BookDTOBuilder` в конце `BookDTO`; setter builder возвращает `this` отдельным оператором.
- Поисковые поля DTO помечены `@SearchField`, массивы aliases пишутся компактно: `@SearchField(aliases = {"author", "автор"}, description = "Поиск по автору")`.

Встречаются маркерные аннотации `@FunctionalInterface`, `@Getter`, `@Setter`, `@Slf4j`, `@SafeVarargs`, `@Serial`, `@SneakyThrows`, `@Test`, `@Disabled` и аннотации с параметрами. Они следуют тем же правилам размещения.

## 13. JavaFX-контроллеры и свойства

FXML-поля обычно `private`, каждое с отдельным `@FXML`. Для генерации accessor-методов применяется `@Getter` на классе (`NewBookController`, `EditBookController`, `BookInfoViewController`) или отдельном поле (`MainViewController.mainAnchorPane`).

```java
// Основные элементы

@FXML
private AnchorPane mainAnchorPane;

@FXML
private Button exitButton;

// Элементы ввода

@FXML
private TextField nameTextFieldInput;
```

Группы элементов обозначаются обычными комментариями: основные элементы, ввод, ComboBox, цикл, обложка. Имена включают назначение и вид элемента: `publicationYearFieldInput`, `cycleEndedCheckBox`, `audioBookSiteTable`.

Обработчик имеет отдельный `@FXML`, затем обычную сигнатуру. Встречаются `public` и `private` обработчики, поэтому обязательной общей видимости нет. Имена `stageKeyPressed`, `chooseBookCover`, `saveConnections`, `initialize` записываются в lowerCamelCase.

Установка свойств контрола — последовательные однострочные вызовы. Обработчики и фабрики ячеек используют лямбды, ссылки на методы и анонимные классы по общим правилам. Например: `this.checkBox.setOnAction(this::handleActionEvent);`.

В `CustomCheckBoxTableCell` JavaFX property API оформлен группами: поле, `converterProperty()`, `getConverter()`; отдельно поле и методы `selectedStateCallback`. Имена property-методов не получают префикса `get` перед `Property`.

Приведения результатов `FXMLLoader.getNamespace().get(...)`, составные generic-типы `Callback<TableColumn<...>, TableCell<...>>` и подавление `unchecked` оформляются как обычные приведения, параметры типов и аннотации. Отдельного альтернативного стиля для UI-кода нет.

## 14. Комментарии, Javadoc и сообщения

Для обычного пояснения используется `// `, как отдельная строка или после оператора. Встречаются русские и английские комментарии. Комментарии групп полей контроллеров обычно на русском, технические заглушки — на английском.

Встроенные комментарии иногда выравниваются пробелами в группе (`CreateBookTests`), иногда отделяются несколькими пробелами (`MainViewController`). Единого числа пробелов перед `//` не установлено; сохранять читаемость и местную группировку.

Образец Javadoc — `FileRepositoryProvider`, `AbstractViewController`, `AbstractFileRepository`:

```java
/**
 * Зарезолвить путь вместе с родительским
 * @param rootPath Родительский путь
 * @param path Основной путь к файлу
 * @return Объеденённый путь
 */
public static Path resolvePath(@Nullable Path rootPath, Path path) {
    return rootPath != null
        ? Path.of(rootPath.toAbsolutePath().toString(), path.toString())
        : path;
}
```

Звёздочки выровнены; текст начинается после `* `. `@param`, `@return`, `@throws` занимают отдельные строки без табличного выравнивания описаний. Обычно между кратким описанием и тегами пустой строки нет; в `loadModalView` она есть. Javadoc только с `@return` или `@param` также используется. Ссылки записываются как `{@link Path}` и `{@link AbstractViewController#loadModalView(String, String, int, int, Object...)}`. Для type parameter встречается `@param <T>`.

Не требуется Javadoc на каждом простом getter или каждом классе: такой практики в проекте нет. В исходниках есть комментарии с URL происхождения решения (`AutoCompleteComboBoxListener`, `AbstractJavaFxTest`); сохранять их при локальных изменениях. Формат не требует обращения к этим внешним материалам.

TODO встречается как `// TODO: refactoring` и `//todo: ...`; основной повторяемый читаемый вариант для нового TODO — `// TODO: ...`.

Логи имеют два существующих оформления:

- `@Slf4j` на классе и вызовы `log.info(...)`, `log.warn(...)`, `log.error(...)`.
- `private static final Logger LOGGER = LogManager.getLogger(Type.class);` и вызовы `LOGGER.debug(...)`, особенно в поисковых классах.

Продолжать форму соответствующего класса. Сообщения логов в основном английские. Пользовательские сообщения, описания полей и ошибки валидации часто русские; заголовки окон встречаются и английские. Не переводить строки автоматически ради единообразия оформления.

Основная форма логирования параметров — placeholders `{}`; исключение передаётся последним аргументом. В некоторых местах есть `log.error(format(...), e)` — это существующий вариант. Длинный список аргументов переносится по разделу 7.

## 15. Тестовый код

Тесты используют те же отступы, скобки, переносы и импорты, что основной код. Классы конкретных тестов обычно без модификатора `public`; вспомогательные типы часто `public`. Тестовые методы обычно package-private, а lifecycle-методы в ряде классов `public`.

Существуют три схемы имён тестовых методов:

- `testSearchByReadCheckBox`, `testCreateFile`.
- `readStorageFile_shouldReadStorageFile`.
- `createPropertyProviderShouldInitPropertyFile`.

Для нового теста продолжать схему его класса. Не переименовывать все существующие тесты ради одной схемы.

Аннотации тестов располагаются по одной на строке:

```java
@ParameterizedTest(name = "Поиск по '{0}' запросу")
@ValueSource(strings = {"Book", "book", "BOOK"})
void testSearchByBookSearchQueryInDifferentRegistry(String searchQuery) {
    assertThat(
        searchService.search(searchQuery, bookDtoList),
        contains(book1, book2, book3, book4, book5)
    );
}
```

- Используются `@BeforeAll`, `@BeforeEach`, `@AfterEach`, `@Test`, `@ParameterizedTest`, `@ValueSource`, `@TempDir`, TestFX `@Start` и `@ExtendWith`.
- `@TempDir` встречается и у поля (`protected static Path temporaryDirectory`), и перед параметром: `void testCreateFile(@TempDir Path tempDir)`.
- `CreateBookTests` помечен `@Disabled` и `@ExtendWith(ApplicationExtension.class)`; это состояние существующего теста, а не требование отключать новые UI-тесты.
- Подготовка данных, действие и проверки отделяются пустыми строками и иногда поясняются комментариями. Обязательных меток Arrange/Act/Assert нет.
- Короткий assert оставлять однострочным. В многострочном Hamcrest assert фактическое значение и matcher идут отдельными аргументами, закрытие — `);` на уровне вызова.
- AssertJ-цепочки оформлять как остальные цепочки: `assertThat(testEntities)` и продолжения `.hasSize(...)`, `.contains(...)` с +4.
- Встречаются JUnit Jupiter assertions, `junit.framework.Assert`, Hamcrest и AssertJ. Не объявлять один из них единственной существующей библиотекой и не менять зависимости ради документа о стиле.
- Тестовые вложенные репозитории находятся после helper-методов и записываются компактно: `protected static class TestRepository1 extends TestFileRepository<String> {}`; перед классом отдельный `@FileStorageName(...)`.
- `UnitJavaFXTest` запускает платформу через `Platform.startup(() -> {});`; пустое тело лямбды без пробелов внутри.

## 16. Модуль и XML

### 16.1. `module-info.java`

Модуль объявлен как `open module com.tsel.home.project.booklibrary {`. После открытия — пустая строка, каждая директива `requires` на отдельной строке с отступом 4 пробела и `;` в конце. Группы зависимостей разделены пустыми строками; текущий порядок не алфавитный.

```java
open module com.tsel.home.project.booklibrary {

    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.base;

    requires org.controlsfx.controls;
}
```

`exports`, `opens`, `uses`, `provides`, `requires transitive` в текущем файле отсутствуют; отдельный сложившийся стиль их группировки не установлен.

### 16.2. `pom.xml` и `log4j2.xml`

- XML-декларация `<?xml version="1.0" encoding="UTF-8"?>` — первая строка.
- Вложенность XML в `pom.xml` и `log4j2.xml` — 4 пробела.
- Значения атрибутов в двойных кавычках; вокруг `=` пробелов нет.
- Простые текстовые элементы пишутся в одной строке: `<artifactId>BookLibrary</artifactId>`.
- Составной элемент открывается и закрывается на отдельных строках; дочерние элементы на следующем уровне.
- Блоки properties, dependencies и plugins в POM разделены пустыми строками. Комментарии — `<!-- ... -->`.
- У корневого `project` POM атрибуты продолжаются на следующих строках с выравниванием под первым атрибутом. Внутренние длинные атрибуты также могут оставаться однострочными.
- Self-closing элементы `log4j2.xml` встречаются и как `<AppenderRef ref="ConsoleAppender" />`, и как `<AppenderRef ref="FileAppender"/>`. Для нового соседнего элемента сохранять форму его группы; общепроектного единственного варианта пока нет.
- Maven-подстановки `${...}` и логирующие шаблоны `%...` остаются внутри значений; их содержимое не переформатируется как Java.

`dependency-reduced-pom.xml` и содержимое `target` — производные сборки; они не используются как дополнительный стандарт оформления рукописного кода.

## 17. FXML

В FXML нет единого отступа на весь проект. `add-view.fxml` и `edit-view.fxml` в основном используют 4 пробела; `main-view.fxml` и `info-view.fxml` содержат блоки с шагом 3; в остальных файлах сочетаются блоки с шагом 4 и 3. При добавлении элемента продолжать отступ непосредственно окружающего контейнера. Для новой разметки по образцу формы добавления или редактирования использовать 4 пробела. Не применять безусловно Java-отступ ко всему существующему FXML-файлу.

Общие наблюдаемые формы:

- XML-декларация, пустая строка, блок инструкций `<?import ...?>`, пустая строка, корневой `AnchorPane`.
- Каждый импорт на отдельной строке. Есть и явные импорты, и `javafx.scene.control.*`; продолжать стиль файла.
- `xmlns`, `xmlns:fx` и `fx:controller` записаны атрибутами корня. Полное имя контроллера — в строковом значении `fx:controller`.
- Каждый элемент интерфейса или элемент свойства начинается с новой строки. Большие списки атрибутов обычно целиком остаются в строке открывающего тега; ограничения длины 120 символов для FXML нет.
- Атрибуты: `name="value"`, двойные кавычки, без пробелов вокруг `=`; self-closing форма обычно с пробелом перед `/>`.
- `fx:id` — lowerCamelCase с названием контрола: `mainAnchorPane`, `searchQueryField`; обработчик — `onAction="#addBook"`, `onKeyPressed="#stageKeyPressed"`.
- Ссылки на ресурсы — `@../css/table.css`; подключение CSS встречается и строковым атрибутом `stylesheets`, и вложенным `<stylesheets>` с `<URL value="..." />`.
- Дочерние элементы размещаются через `<children>`, `<columns>`, `<columnConstraints>`, `<rowConstraints>`, `<font>`, `<padding>`, `<opaqueInsets>`, а также элементы attached properties `<HBox.margin>`, `<GridPane.margin>`. В `add-view.fxml` есть и непосредственное вложение `GridPane` в `AnchorPane` без явного `<children>`.
- Вложенные значения `Font`, `Insets`, `ColumnConstraints`, `RowConstraints`, `URL` оформляются обычными отдельными XML-элементами. Закрывающий тег выравнивается по соответствующему открывающему.
- Attached attributes сохраняют имя типа: `AnchorPane.topAnchor`, `GridPane.columnIndex`, `GridPane.rowIndex`.
- Размеры и координаты обычно записаны с десятичной частью (`675.0`); boolean-значения — `true`/`false`; enum-значения — `BASELINE_CENTER`, `SOMETIMES`, `DESCENDING`.
- `styleClass` использует имена CSS через дефис; inline `style` содержит CSS-декларации в строке через `; `.

Проверены элементы `AnchorPane`, `GridPane`, `HBox`, `TextFlow`, `Button`, `CheckBox`, `ComboBox`, `Label`, `TextField`, `TableView`, `TableColumn`, `ImageView` и перечисленные выше элементы свойств. Они следуют единому синтаксису XML, без отдельного правила скобок для каждого JavaFX-контрола.

Пример существующего оформления вложенных ресурсов:

```xml
<stylesheets>
    <URL value="@../css/table.css" />
    <URL value="@../css/check-box.css" />
</stylesheets>
```

## 18. JavaFX CSS

- Селектор и открывающая `{` в одной строке, перед `{` один пробел.
- Каждая декларация — отдельная строка; основной отступ 4 пробела.
- Имя свойства, `:`, пробел, значение, `;`: `-fx-background-color: #8B936B;`.
- Закрывающая `}` — отдельная строка на уровне селектора.
- Между правилами обычно пустая строка. В `check-box.css` два коротких правила идут подряд без неё.
- Имена классов строчными словами через дефис: `.search-help-button`, `.disabled-check-box`.
- Псевдоклассы примыкают без пробела: `.search-button:hover`, `.check-box:disabled`, `.table-row-cell:odd`, `.table-row-cell:focused`.
- Используются JavaFX-свойства `-fx-*` и собственные looked-up значения `-icon-paint`, `-size`; ссылки на них пишутся как значения без кавычек.
- Пути `url("../img/sign-icon.png")` используют двойные кавычки. Цвета в CSS-файлах записаны шестизначным HEX с заглавными буквами; FXML также содержит строчные HEX, их регистр не менять автоматически.
- Длинные строковые значения `-fx-shape` остаются одной строкой. Перед ними в кнопочных стилях есть пустая строка, отделяющая геометрию от остальных настроек.

```css
.search-button:hover {
    -icon-paint: #8B936B;
}
```

В `main-view-buttons.css` тела `:hover` имеют отступ 3 пробела; основной вариант нового самостоятельного правила — 4. В `table.css` существует пустое многострочное правило `.table-row-cell` с пустой строкой внутри. Это допустимая форма существующей заготовки, а не требование добавлять пустые правила.

## 19. Разночтения и границы стандарта

| Наблюдаемое различие | Где встречается | Решение для нового кода |
| --- | --- | --- |
| 3 пробела в Java | `Timer` | Самостоятельный новый код: 4; не переформатировать весь старый файл попутно |
| Продолжения на 2 или 8 пробелов | `MainViewController.onSort`, `AutoCompleteComboBoxListener`, один тест `SearchServiceTest` | Основной шаг продолжения: 4 |
| Явные и wildcard-импорты | `Genre`, `AbstractFileRepository`, `BookConverter`, `SearchFilterFactory` | Новый файл начинать с явных; не приписывать проекту запрет `*` или числовой порог |
| Пустая строка внутри блока обычных импортов | `Genre`, `GenreDTO`, `SearchFilterFactory` | Основной вариант: единый блок |
| `if` без фигурных скобок | `resolvePrice`, `cleanUpRepositoryData` | Новый блок: фигурные скобки |
| `while(` и `while (` | Архиватор и его тест | Новый блок: `while (` |
| Пробел после приведения отсутствует | `CustomCheckBoxTableCell` | Новый фрагмент: `(Type) value` |
| `?`/`:` в конце предыдущей строки | `CustomCheckBoxTableCell.getSelectedProperty` | Основной вариант: операторы в начале строк продолжения |
| `new TestEntity[] {}` и `new TestEntity[]{}` | `FileRepositoryProviderTest` | Первый вариант для нового подобного выражения |
| Compact `{}` и многострочное пустое тело | Утилитные конструкторы, record, hooks, тестовые репозитории, `BaseOperand` | Выбирать по семейству типа и назначению, как описано выше |
| `log` и `LOGGER` | Слои UI/DAO и поиск | Продолжать форму класса |
| Явные типы и `var` | Большинство кода и один поисковый тест | Основной вариант: явный тип |
| Несколько схем имён и assertions в тестах | Тестовые пакеты | Продолжать конкретный класс; миграция тестов не является форматированием |
| Нет завершающего перевода строки | `AutoCompleteComboBoxListener`, `CustomCheckBoxTableCell`, `FileRepositoryProviderTest`, `StorageBackupArchiverTest`, `SearchServiceTest`, `FileUtilsTest` | Новые файлы завершать переводом строки |
| Смешанные отступы FXML и CSS | Разметка представлений, hover-правила кнопок | Учитывать локальную разметку; новый самостоятельный CSS: 4 |

В текущих исходниках не обнаружены: локальные именованные классы; instance initializer; классический `switch` с `case ...:`; `do/while`; multi-catch; `yield`; `synchronized`-блоки; Java `assert`; sealed-типы; text blocks; объявления членов с `volatile`, `transient` или `native`. Для этих форм нельзя описывать отдельное правило как уже существующее в BookLibrary. Если они понадобятся, применять установленные общие отступы, пробелы и скобки, а специфическое правило дополнять по реально добавленному коду.

Версия языка в `pom.xml` — Java 17. Форматирование не служит основанием вводить конструкции другой версии языка.

## 20. Реестр анализа всех Java-файлов

Пути в таблицах отсчитываются от указанного каталога. Каждая строка соответствует одному проверенному файлу. Для одинаковых по структуре классов отдельно зафиксирован тот же шаблон; вложенные именованные типы указаны в строке их внешнего файла.

### 20.1. Приложение и контроллеры — 12 файлов

Каталог: `src/main/java/com/tsel/home/project/booklibrary`.

| Файл | Проверенные особенности оформления |
| --- | --- |
| `App.java` | Static-импорты; вложенный `public static class JavaFXRunner`; ссылки на конструкторы; группировка init-вызовов; try/catch |
| `controller/AbstractViewController.java` | Группы protected final-зависимостей; Javadoc; абстрактный метод; многострочная сигнатура с varargs; hooks с комментариями |
| `controller/impl/MainViewController.java` | Static initializer; FXML и Lombok над полем; многострочные условия и вызовы; сложные компараторы; анонимный Stage; inline-if и нестандартные отступы в onSort |
| `controller/impl/book/AbstractBookController.java` | Группа абстрактных accessor-методов с Javadoc; generic-метод; builder; Optional; typed pattern matching; try-with-resources; несколько catch; тернарные выражения |
| `controller/impl/book/NewBookController.java` | Аннотации класса; группы FXML-полей с комментариями; initialize; обработчики событий; многострочные вызовы |
| `controller/impl/book/EditBookController.java` | Та же группировка FXML; initController; override-hook; классический for; чередование полей и методов по назначению |
| `controller/impl/book/BookInfoViewController.java` | Группы элементов отображения; вложенные Optional-цепочки в аргументах; inline-return в if; константы числовых размеров |
| `controller/impl/audiobook/AbstractAudioBookSiteController.java` | Protected final-зависимости; вложенный Stream в вызове; Javadoc; @SafeVarargs и generic varargs |
| `controller/impl/audiobook/AbstractAudioBookSiteConnectionController.java` | Составные Callback-типы; типизированная лямбда с анонимным классом; несколько override; лямбда с телом |
| `controller/impl/audiobook/AudioBookSiteController.java` | @FXML; switch со стрелками и пустой default-hook; вложенная forEach-лямбда; перенос initializer после `=` |
| `controller/impl/audiobook/AudioBookSiteConnectionEditSiteController.java` | @Slf4j/@Getter; FXML; removeIf; многострочный addAll со Stream; Entry::getValue/Entry::getKey |
| `controller/impl/audiobook/AudioBookSiteConnectionViewController.java` | Минимальный контроллер: @Getter, одно FXML-поле, обработчик с if |

### 20.2. Конвертеры — 3 файла

Каталог: `src/main/java/com/tsel/home/project/booklibrary/converter`.

| Файл | Проверенные особенности оформления |
| --- | --- |
| `Converter.java` | Generic-интерфейс, одна сигнатура без public abstract |
| `AudioBookSiteConverter.java` | implements с двумя параметрами типов; короткая многострочная builder-цепочка |
| `BookConverter.java` | Wildcard-импорты моделей и репозиториев; static final-зависимости; длинный builder; Optional/Stream; конструктор CheckBox и setter-вызовы |

### 20.3. DAO — 25 файлов

Каталог: `src/main/java/com/tsel/home/project/booklibrary/dao`.

| Файл | Проверенные особенности оформления |
| --- | --- |
| `annotation/EntityDisplayName.java` | @Target; @Retention с явным value; @interface и метод value |
| `annotation/FileStorageName.java` | Сокращённый @Retention; однопараметрическая аннотация |
| `annotation/Property.java` | Явные атрибуты метааннотаций; boolean-член с default |
| `data/BaseEntity.java` | Ограниченный параметр типа; extends Serializable; разделённые сигнатуры getter/setter |
| `data/AudioBookSite.java` | Стек Lombok; @EntityDisplayName; @Serial; разделённые @Property-поля |
| `data/Author.java` | Тот же шаблон сущности; обязательность поля атрибутом nullable |
| `data/Book.java` | Дополнительный @EqualsAndHashCode с массивом exclude; длинная группа @Property-полей; generic-список с initializer |
| `data/Cycle.java` | Аннотированные поля с начальными значениями false и 1 |
| `data/Genre.java` | Wildcard Lombok и отдельная группа java-импортов; тот же стек аннотаций сущности |
| `data/Publisher.java` | Стандартное оформление сущности, serialVersionUID и Property |
| `data/UserSettings.java` | Константа перед serialVersionUID; явные getter/setter; поясняющий комментарий в пустом setter |
| `exception/ConstraintException.java` | Наследник RuntimeException; конструктор и конкатенация в super |
| `exception/NotNullConstraintException.java` | Тот же шаблон исключения с двумя параметрами и длинной строкой сообщения |
| `identifier/IdentifierGenerator.java` | @FunctionalInterface; generic-ограничение; единственная сигнатура |
| `identifier/StringIdentifierGenerator.java` | final-класс; @RequiredArgsConstructor; private final-поле; override |
| `identifier/UUIDIdentifierGenerator.java` | final-класс без полей; override с одним return |
| `repository/FileRepository.java` | Два связанных generic-параметра; @Nullable над методом; список сигнатур |
| `repository/AbstractFileRepository.java` | Длинное объявление типа; wildcard и вложенный Entry-импорт; final/state группы; транзакционные методы; Javadoc-hooks; reflection; Stream; orElseThrow |
| `repository/impl/AudioBookSiteRepository.java` | @FileStorageName; пара конструкторов; protected override с throws |
| `repository/impl/AuthorRepository.java` | Пара конструкторов; Optional-возврат через Stream; constraint override |
| `repository/impl/BookRepository.java` | Static getBean; Predicate-лямбды; длинный boolean-return; protected override после private helper; inline-комментарий |
| `repository/impl/CycleRepository.java` | Семейный шаблон репозитория: конструкторы, getByName-цепочка, compareEntities |
| `repository/impl/GenreRepository.java` | Тот же шаблон с типами Genre и Optional<Genre> |
| `repository/impl/PublisherRepository.java` | Тот же шаблон с типами Publisher и Optional<Publisher> |
| `repository/impl/UserSettingsRepository.java` | Константа; пара конструкторов; Optional-цепочка с File::new; терминация orElse(null) |

### 20.4. DTO — 7 файлов

Каталог: `src/main/java/com/tsel/home/project/booklibrary/dto`.

| Файл | Проверенные особенности оформления |
| --- | --- |
| `ComboBoxDTO.java` | Две сигнатуры интерфейса, пустая строка между ними |
| `AudioBookSiteDTO.java` | Раздельные Getter/Setter; стек Lombok; поля разделены пустой строкой; override toString |
| `AuthorDTO.java` | @Data/@Builder; @Serial; смежные id/name; дополнительный конструктор; toString |
| `BookDTO.java` | Аннотации exclude; SearchField-массивы; разнородные UI-поля; вложенный BookDTOBuilder с кастомным fluent-методом |
| `CycleDTO.java` | Тот же шаблон справочного DTO с Serializable и ComboBoxDTO |
| `GenreDTO.java` | Lombok-импорты перед отдельной java-группой; шаблон справочного DTO |
| `PublisherDTO.java` | Шаблон справочного DTO: serialVersionUID, конструктор по имени, toString |

### 20.5. Helper — 6 файлов

Каталог: `src/main/java/com/tsel/home/project/booklibrary/helper`.

| Файл | Проверенные особенности оформления |
| --- | --- |
| `DateProvider.java` | Минимальный public-класс с многострочным методом из одного return |
| `FileRepositoryProvider.java` | Javadoc типа и методов; generic-параметры; @Nullable; TypeToken; try-with-resources; static helper-методы после instance-методов |
| `ImageProvider.java` | final-класс; группы final-полей; constructor try/catch; вложенные проверки; try-with-resources; вложенный record ImageHolder с `{}` |
| `PropertyProvider.java` | Константы и final-поля; static initializer после полей экземпляра; Entry-циклы; длинное boolean-условие |
| `SimpleApplicationContext.java` | final-утилита; wildcard с нижней границей; private-конструктор `{}`; generic static-методы |
| `StorageBackupArchiver.java` | Группы констант; комментарии этапов; Stream; многострочная private-сигнатура с throws; byte-массив и while-присваивание; вложенный private record SortingArchivePath |

### 20.6. Поиск — 16 файлов

Каталог: `src/main/java/com/tsel/home/project/booklibrary/search`.

| Файл | Проверенные особенности оформления |
| --- | --- |
| `SearchField.java` | @interface; default пустого массива; обязательный description |
| `SearchFieldDefinition.java` | Самостоятельный record с многострочным пустым телом |
| `SearchFilterFactory.java` | Wildcard-импорт; многострочный List.of; static initializer; private-конструктор `{}` |
| `SearchService.java` | LOGGER; reflection; @Getter над полем; Javadoc на английском; instanceof с переменной; switch expression; многострочные вложенные format/append |
| `filter/SearchFilter.java` | Интерфейс с сигнатурами и default-методом |
| `filter/AbstractSearchFilter.java` | Абстрактный generic-тип; @Override/@SuppressWarnings; cast; abstract-сигнатуры |
| `filter/AbstractNumericSearchFilter.java` | T extends Number; protected static final; LOGGER; Stream; абстрактные hooks среди методов; тернарные операторы |
| `filter/impl/BooleanSearchFilter.java` | LOGGER; константа; overridden protected/public-методы; логическое выражение с цепочкой |
| `filter/impl/CheckBoxSearchFilter.java` | Тот же шаблон фильтра; короткий тернарный return |
| `filter/impl/DoubleSearchFilter.java` | Generic-наследник; override public и protected-методов; try/catch; многострочные тернарные сравнения |
| `filter/impl/IntegerSearchFilter.java` | Аналогичное оформление числового фильтра с Integer |
| `filter/impl/StringSearchFilter.java` | Static-импорт; early return; последовательная подготовка строк; LOGGER |
| `operand/BaseOperand.java` | Пустой маркерный интерфейс с многострочным телом |
| `operand/CompositeOperand.java` | @Getter; final-список с initializer и final operator; явный конструктор |
| `operand/SimpleOperand.java` | @Getter; private final-поле; явный однопараметрический конструктор |
| `operand/OperatorType.java` | Enum с аргументами; Lombok Getter; varargs-конструктор; Stream-метод |

### 20.7. Утилиты — 13 файлов

Каталог: `src/main/java/com/tsel/home/project/booklibrary/utils`.

| Файл | Проверенные особенности оформления |
| --- | --- |
| `CollectionUtils.java` | final-утилита; private-конструктор `{}`; wildcard-параметры; короткие return |
| `FileUtils.java` | Static format; Javadoc; varargs; вложенная цепочка в аргументе; @Nullable; try с несколькими catch; многострочный throw |
| `MyGson.java` | final-утилита; цепочка GsonBuilder; вложенный private static class GsonExclusionStrategy с override-методами |
| `NumericConvertUtils.java` | final-утилита; короткие методы с try/catch и многострочным тернарным return |
| `StringUtils.java` | final-утилита; compact private-конструктор; boolean-return из одной строки |
| `Timer.java` | Особый отступ 3 пробела; private-конструктор; перегруженные static-фабрики; @Nullable; числовые L-литералы |
| `table/AudioBookSiteNameEditableColumn.java` | Generic TableCell-наследник; override-методы; вложенные if; лямбда с явно типизированными параметрами и верхней wildcard-границей |
| `table/AutoCompleteComboBoxListener.java` | Javadoc с URL и type parameter; final-параметры; qualified this; switch со списком меток; продолжения +8 |
| `table/ButtonAnswer.java` | final-класс; константа и final-поле; конструктор; многострочный boolean-return |
| `table/CustomCheckBoxTableCell.java` | Абстрактный generic-класс; JavaFX property-группы; compact hook и анонимный метод; вложенные дженерики; casts; альтернативный перенос тернарного оператора |
| `table/CustomFileChooser.java` | final-класс; static ofNullable; последовательные setter-вызовы; instance method reference |
| `table/TableComparators.java` | Константы Comparator; лямбда и method reference; короткие тернарные return; private-конструктор в конце |
| `table/TableScroll.java` | @Getter; final-класс; константы; многострочное тернарное присваивание |

### 20.8. Дескриптор модуля — 1 файл

| Файл | Проверенные особенности оформления |
| --- | --- |
| `src/main/java/module-info.java` | open module; requires с отступом 4; разделённые пустыми строками группы без алфавитной сортировки |

Итого основной код: 12 + 3 + 25 + 7 + 6 + 16 + 13 + 1 = 83 файла. Вместе с 14 тестовыми файлами — 97.

### 20.9. Тесты — 14 файлов

Каталог: `src/test/java/com/tsel/home/project/booklibrary`.

| Файл | Проверенные особенности оформления |
| --- | --- |
| `CheckBoxMatcher.java` | final-утилита; private-конструктор `{}`; многострочные вызовы matcher с method reference и лямбдой |
| `ComboBoxMatcher.java` | Generic Matcher; @SuppressWarnings; varargs; Stream; многострочный return-вызов |
| `TestDateProvider.java` | Наследник Provider; Lombok Setter; override с `this` |
| `TestEntity.java` | Собственный порядок Lombok-аннотаций; три смежных поля; дополнительный конструктор |
| `TestFileRepository.java` | Абстрактный generic-тип; @Nullable перед @Override; реализации-заглушки с комментариями |
| `TestFileUtils.java` | final-утилита; private-конструктор; @SneakyThrows над static-методами |
| `UnitJavaFXTest.java` | Абстрактная база; @BeforeAll static-метод; пустая лямбда `() -> {}` |
| `javafx/AbstractJavaFxTest.java` | URL-комментарий; @TempDir static-поле; группы protected static-зависимостей; методы управления FxRobot |
| `javafx/CreateBookTests.java` | Package-private тест; @Disabled/@ExtendWith; @Start; @AfterEach; вложенный List.of; callback-продолжение после стрелки; JUnit/AssertJ/TestFX |
| `provider/FileRepositoryProviderTest.java` | Имена с underscore; @TempDir-параметры; AssertJ-цепочки; два оформления пустого массива; вложенный compact TestRepository |
| `provider/PropertyProviderTest.java` | Имена should в camelCase; статические assertions из разных пакетов; @TempDir; комментарии этапов |
| `provider/StorageBackupArchiverTest.java` | @BeforeEach; @SneakyThrows; Hamcrest; try-with-resources с вложенными while; четыре вложенных compact-класса TestRepository1–4 |
| `search/SearchServiceTest.java` | ParameterizedTest/ValueSource; группы констант и fixtures; builder; Hamcrest; три var в одном тесте с продолжением +8 |
| `utils/FileUtilsTest.java` | Короткий package-private тест; Jupiter assertions; @TempDir-параметр; строковое экранирование Windows-пути |

## 21. Применение при последующих изменениях

Перед написанием кода прочитать этот файл и ближайший существующий класс того же семейства. Для нового файла использовать основные правила; для локальной правки также учитывать документированные варианты и группировку окружающего кода. Этот порядок закреплён ссылкой из корневого `AGENTS.md`.

При проверке изменения сверять отступы 4/+4, положение скобок, импорты static/обычные, переносы цепочек и аргументов, аннотации, оформление полей и тестов. Для FXML отдельно учитывать локальный шаг отступа. Если появляется ранее отсутствовавшая конструкция, дополнить стандарт её реальным примером, не представляя выбранное решение как прежнюю практику.

Документ задаёт стиль для дальнейшей работы. Автоматического форматтера или проверяющего стиль Maven-плагина в текущем `pom.xml` нет; создание этого файла само по себе не включает автоматическое переформатирование или проверку сборкой.
