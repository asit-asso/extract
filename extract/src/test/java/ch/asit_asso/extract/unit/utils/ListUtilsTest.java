/*
 * Copyright (C) 2025 arx iT
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ch.asit_asso.extract.unit.utils;

import ch.asit_asso.extract.utils.ListUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ListUtils class.
 *
 * Tests:
 * - castList method with various types
 * - Handling of empty collections
 * - Handling of mixed type collections
 * - ClassCastException scenarios
 *
 * @author Bruno Alves
 */
@DisplayName("ListUtils Tests")
class ListUtilsTest {

    // ==================== 1. BASIC CAST LIST TESTS ====================

    @Nested
    @DisplayName("1. Basic castList Tests")
    class BasicCastListTests {

        @Test
        @DisplayName("1.1 - Successfully casts collection of Strings")
        void successfullyCastsStringCollection() {
            // Given: A collection of Strings
            Collection<Object> rawCollection = Arrays.asList("one", "two", "three");

            // When: Casting to String list
            List<String> result = ListUtils.castList(String.class, rawCollection);

            // Then: Should contain all elements
            assertEquals(3, result.size());
            assertEquals("one", result.get(0));
            assertEquals("two", result.get(1));
            assertEquals("three", result.get(2));
        }

        @Test
        @DisplayName("1.2 - Successfully casts collection of Integers")
        void successfullyCastsIntegerCollection() {
            // Given: A collection of Integers
            Collection<Object> rawCollection = Arrays.asList(1, 2, 3, 4, 5);

            // When: Casting to Integer list
            List<Integer> result = ListUtils.castList(Integer.class, rawCollection);

            // Then: Should contain all elements
            assertEquals(5, result.size());
            assertTrue(result.contains(1));
            assertTrue(result.contains(5));
        }

        @Test
        @DisplayName("1.3 - Successfully casts collection of custom objects")
        void successfullyCastsCustomObjects() {
            // Given: A collection of custom objects
            TestObject obj1 = new TestObject("test1");
            TestObject obj2 = new TestObject("test2");
            Collection<Object> rawCollection = Arrays.asList(obj1, obj2);

            // When: Casting to TestObject list
            List<TestObject> result = ListUtils.castList(TestObject.class, rawCollection);

            // Then: Should contain all elements
            assertEquals(2, result.size());
            assertSame(obj1, result.get(0));
            assertSame(obj2, result.get(1));
        }

        @Test
        @DisplayName("1.4 - Returns empty list for empty collection")
        void returnsEmptyListForEmptyCollection() {
            // Given: An empty collection
            Collection<Object> rawCollection = Collections.emptyList();

            // When: Casting to String list
            List<String> result = ListUtils.castList(String.class, rawCollection);

            // Then: Should return empty list
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    // ==================== 2. MIXED TYPE COLLECTION TESTS ====================

    @Nested
    @DisplayName("2. Mixed Type Collection Tests")
    class MixedTypeCollectionTests {

        @Test
        @DisplayName("2.1 - Skips incompatible elements and includes compatible ones")
        void skipsIncompatibleElements() {
            // Given: A collection with mixed types
            Collection<Object> rawCollection = Arrays.asList("one", 2, "three", 4.0, "five");

            // When: Casting to String list
            List<String> result = ListUtils.castList(String.class, rawCollection);

            // Then: Should only contain Strings
            assertEquals(3, result.size());
            assertTrue(result.contains("one"));
            assertTrue(result.contains("three"));
            assertTrue(result.contains("five"));
            // Integers and Doubles should be skipped
        }

        @Test
        @DisplayName("2.2 - Returns empty list when no elements match target type")
        void returnsEmptyListWhenNoMatchingElements() {
            // Given: A collection with no matching types
            Collection<Object> rawCollection = Arrays.asList(1, 2, 3, 4, 5);

            // When: Casting to String list
            List<String> result = ListUtils.castList(String.class, rawCollection);

            // Then: Should return empty list
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("2.3 - Handles null elements in collection")
        void handlesNullElements() {
            // Given: A collection with null elements
            Collection<Object> rawCollection = Arrays.asList("one", null, "three");

            // When: Casting to String list - null can be cast to any reference type
            List<String> result = ListUtils.castList(String.class, rawCollection);

            // Then: Should contain the strings and null
            assertEquals(3, result.size());
            assertEquals("one", result.get(0));
            assertNull(result.get(1));
            assertEquals("three", result.get(2));
        }

        @Test
        @DisplayName("2.4 - Casts subclass instances to superclass type")
        void castsSubclassToSuperclass() {
            // Given: A collection with subclass instances
            ArrayList<String> list1 = new ArrayList<>();
            LinkedList<String> list2 = new LinkedList<>();
            Collection<Object> rawCollection = Arrays.asList(list1, list2);

            // When: Casting to List (supertype)
            List<List> result = ListUtils.castList(List.class, rawCollection);

            // Then: Should contain both elements
            assertEquals(2, result.size());
            assertSame(list1, result.get(0));
            assertSame(list2, result.get(1));
        }
    }

    // ==================== 3. DIFFERENT COLLECTION TYPES TESTS ====================

    @Nested
    @DisplayName("3. Different Collection Types Tests")
    class DifferentCollectionTypesTests {

        @Test
        @DisplayName("3.1 - Casts from Set to List")
        void castsFromSetToList() {
            // Given: A Set collection
            Set<Object> rawCollection = new LinkedHashSet<>(Arrays.asList("a", "b", "c"));

            // When: Casting to String list
            List<String> result = ListUtils.castList(String.class, rawCollection);

            // Then: Should contain all elements
            assertEquals(3, result.size());
            assertTrue(result.contains("a"));
            assertTrue(result.contains("b"));
            assertTrue(result.contains("c"));
        }

        @Test
        @DisplayName("3.2 - Casts from Queue to List")
        void castsFromQueueToList() {
            // Given: A Queue collection
            Queue<Object> rawCollection = new LinkedList<>(Arrays.asList(1, 2, 3));

            // When: Casting to Integer list
            List<Integer> result = ListUtils.castList(Integer.class, rawCollection);

            // Then: Should contain all elements
            assertEquals(3, result.size());
        }

        @Test
        @DisplayName("3.3 - Casts from ArrayList to List")
        void castsFromArrayListToList() {
            // Given: An ArrayList
            ArrayList<Object> rawCollection = new ArrayList<>(Arrays.asList("x", "y", "z"));

            // When: Casting to String list
            List<String> result = ListUtils.castList(String.class, rawCollection);

            // Then: Should contain all elements
            assertEquals(3, result.size());
        }
    }

    // ==================== 4. PRIMITIVE WRAPPER TESTS ====================

    @Nested
    @DisplayName("4. Primitive Wrapper Tests")
    class PrimitiveWrapperTests {

        @Test
        @DisplayName("4.1 - Casts collection of Long values")
        void castsLongCollection() {
            // Given: A collection of Long values
            Collection<Object> rawCollection = Arrays.asList(1L, 2L, 3L);

            // When: Casting to Long list
            List<Long> result = ListUtils.castList(Long.class, rawCollection);

            // Then: Should contain all elements
            assertEquals(3, result.size());
        }

        @Test
        @DisplayName("4.2 - Casts collection of Double values")
        void castsDoubleCollection() {
            // Given: A collection of Double values
            Collection<Object> rawCollection = Arrays.asList(1.1, 2.2, 3.3);

            // When: Casting to Double list
            List<Double> result = ListUtils.castList(Double.class, rawCollection);

            // Then: Should contain all elements
            assertEquals(3, result.size());
        }

        @Test
        @DisplayName("4.3 - Casts collection of Boolean values")
        void castsBooleanCollection() {
            // Given: A collection of Boolean values
            Collection<Object> rawCollection = Arrays.asList(true, false, true);

            // When: Casting to Boolean list
            List<Boolean> result = ListUtils.castList(Boolean.class, rawCollection);

            // Then: Should contain all elements
            assertEquals(3, result.size());
            assertTrue(result.get(0));
            assertFalse(result.get(1));
        }

        @Test
        @DisplayName("4.4 - Integer cannot be cast to Long")
        void integerCannotBeCastToLong() {
            // Given: A collection of Integer values
            Collection<Object> rawCollection = Arrays.asList(1, 2, 3);

            // When: Trying to cast to Long list
            List<Long> result = ListUtils.castList(Long.class, rawCollection);

            // Then: Should return empty list (Integers are not Longs)
            assertTrue(result.isEmpty());
        }
    }

    // ==================== 5. EDGE CASES ====================

    @Nested
    @DisplayName("5. Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("5.1 - Returns ArrayList instance")
        void returnsArrayListInstance() {
            // Given: Any collection
            Collection<Object> rawCollection = Arrays.asList("test");

            // When: Casting
            List<String> result = ListUtils.castList(String.class, rawCollection);

            // Then: Should be an ArrayList
            assertTrue(result instanceof ArrayList);
        }

        @Test
        @DisplayName("5.2 - Result list is mutable")
        void resultListIsMutable() {
            // Given: A collection
            Collection<Object> rawCollection = Arrays.asList("one", "two");

            // When: Casting and modifying
            List<String> result = ListUtils.castList(String.class, rawCollection);
            result.add("three");

            // Then: Should be mutable
            assertEquals(3, result.size());
            assertTrue(result.contains("three"));
        }

        @Test
        @DisplayName("5.3 - Handles large collection")
        void handlesLargeCollection() {
            // Given: A large collection
            List<Object> rawCollection = new ArrayList<>();
            for (int i = 0; i < 10000; i++) {
                rawCollection.add("item" + i);
            }

            // When: Casting
            List<String> result = ListUtils.castList(String.class, rawCollection);

            // Then: Should contain all elements
            assertEquals(10000, result.size());
        }

        @Test
        @DisplayName("5.4 - Preserves order from ordered collection")
        void preservesOrderFromOrderedCollection() {
            // Given: An ordered collection
            List<Object> rawCollection = Arrays.asList("first", "second", "third", "fourth");

            // When: Casting
            List<String> result = ListUtils.castList(String.class, rawCollection);

            // Then: Should preserve order
            assertEquals("first", result.get(0));
            assertEquals("second", result.get(1));
            assertEquals("third", result.get(2));
            assertEquals("fourth", result.get(3));
        }

        @Test
        @DisplayName("5.5 - Cast to Number includes Integer and Double")
        void castToNumberIncludesSubtypes() {
            // Given: A collection with various Number subtypes
            Collection<Object> rawCollection = Arrays.asList(1, 2.0, 3L, 4.0f);

            // When: Casting to Number list
            List<Number> result = ListUtils.castList(Number.class, rawCollection);

            // Then: Should contain all numeric types
            assertEquals(4, result.size());
        }
    }

    // ==================== 6. INTERFACE CASTING TESTS ====================

    @Nested
    @DisplayName("6. Interface Casting Tests")
    class InterfaceCastingTests {

        @Test
        @DisplayName("6.1 - Casts to CharSequence interface")
        void castsToCharSequenceInterface() {
            // Given: A collection of Strings (which implement CharSequence)
            Collection<Object> rawCollection = Arrays.asList("hello", "world");

            // When: Casting to CharSequence list
            List<CharSequence> result = ListUtils.castList(CharSequence.class, rawCollection);

            // Then: Should contain all elements
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("6.2 - Casts to Comparable interface")
        void castsToComparableInterface() {
            // Given: A collection of Integers (which implement Comparable)
            Collection<Object> rawCollection = Arrays.asList(1, 2, 3);

            // When: Casting to Comparable list
            List<Comparable> result = ListUtils.castList(Comparable.class, rawCollection);

            // Then: Should contain all elements
            assertEquals(3, result.size());
        }
    }

    // ==================== HELPER CLASSES ====================

    /**
     * Simple test object for testing custom object casting.
     */
    private static class TestObject {
        private final String name;

        TestObject(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
