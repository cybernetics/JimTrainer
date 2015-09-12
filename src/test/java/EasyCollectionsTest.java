// Copyright 2015 PlanBase Inc. & Glen Peterson
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

import org.junit.Test;
import org.organicdesign.fp.collections.ImList;
import org.organicdesign.fp.collections.ImMap;
import org.organicdesign.fp.collections.RangeOfInt;
import org.organicdesign.fp.collections.UnmodMap.UnEntry;
import org.organicdesign.fp.tuple.Tuple2;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.organicdesign.fp.StaticImports.*;

public class EasyCollectionsTest {

    // Nothing to do here.  Just keeps IDEs from forgetting the imports.
    @Test public void forceImports() {
        assertArrayEquals(new Integer[] { 1,2,3, }, new Integer[] { 1,2,3, });
        assertEquals(1, 1);
        vec(1);
    }

    // It's easy to create a vector and add more items to the end.
    @Test public void vector1() {
//        assertEquals(vec(1, 2, 3, 4, 5),
//                     vec(1, 2).concat(vec(???))
//                             .toImList());
    }

    // Create a vector adding items conditionally
    @SuppressWarnings("ConstantConditions")
    @Test public void vector2() {
//        boolean includeFirst = true;
//        boolean includeSecond = ???;
//        boolean includeThird = ???;
//        boolean includeFourth = ???;
//        boolean includeFifth = true;
//        assertEquals(vec(1, 3, 5),
//                     vec(includeFirst ? 1 : null,
//                         includeSecond ? 2 : null,
//                         includeThird ? 3 : null,
//                         includeFourth ? 4 : null,
//                         includeFifth ? 5 : null)
//                             .filter(i -> i != null)
//                             .toImList());
    }

    public enum ColorVal {
        RED('R'),
        ORANGE('O'),
        YELLOW('Y'),
        GREEN('G'),
        BLUE('B'),
        INDIGO('I'),
        VIOLET('V'),
        PINK('P');
        private final Character ch;
        ColorVal(Character c) { ch = c; }
        public Character ch() { return ch; }

        // UncleJim's way:
        public static final ImMap<Character,ColorVal> charToColorMap =
                vec(values())
                .toImMap(v -> tup(v.ch(), v));

        // Same thing in "traditional" Java:
        public static final Map<Character,ColorVal> charToColorMap2;
        static {
            Map<Character,ColorVal> tempMap = new HashMap<>();
            for (ColorVal v : values()) {
                tempMap.put(v.ch(), v);
            }
            charToColorMap2 = Collections.unmodifiableMap(tempMap);
        }

        // Same thing with Java 8's streams.
        public static final Map<Character,ColorVal> charToColorMap3 = Collections.unmodifiableMap(
                Arrays.stream(values()).reduce(
                        new HashMap<>(),
                        (HashMap<Character,ColorVal> accum, ColorVal v) -> {
                            accum.put(v.ch(), v);
                            return accum;
                        },
                        (HashMap<Character,ColorVal> accum1, HashMap<Character,ColorVal> accum2) -> {
                            accum1.putAll(accum2);
                            return accum1;
                        }));

        // If you were using a mutable map, you'd want to protect it with a method.
        // It's still a good idea to do that on public classes to defend against having to change
        // your public interface over time.
        public static ColorVal fromChar(Character c) { return charToColorMap.get(c); }
    }

    @Test public void enumTest() {
        assertEquals(ColorVal.RED, ColorVal.fromChar('R'));
        assertEquals(ColorVal.ORANGE, ColorVal.fromChar('O'));
        assertEquals(ColorVal.YELLOW, ColorVal.fromChar('Y'));
        assertEquals(ColorVal.GREEN, ColorVal.fromChar('G'));
        assertEquals(ColorVal.BLUE, ColorVal.fromChar('B'));
        assertEquals(ColorVal.INDIGO, ColorVal.fromChar('I'));
        assertEquals(ColorVal.VIOLET, ColorVal.fromChar('V'));
        assertEquals(ColorVal.PINK, ColorVal.fromChar('P'));
        assertNull(ColorVal.fromChar('z'));

//        println("ColorVal.charToColorMap: " + ColorVal.charToColorMap);

        ImMap<Character,ColorVal> less = ColorVal.charToColorMap.without('O').without('Y');
        assertEquals(ColorVal.charToColorMap.size() - 2, less.size());

//        println("less: " + less);
    }

    public static void println(Object s) { System.out.println(String.valueOf(s)); }

//    public static Comparator<Color> COLOR_COMP =
//            (a, b) -> (a.getRed() + a.getGreen() + a.getBlue()) - (b.getRed() + b.getGreen() + b.getBlue());

//    public static <T> T ex(T t) throws IOException { return t; }

    // UncleJim's way
    @Test public void colorSquare() {
        ImList<Color> imgData = RangeOfInt.of(0, 256)
                .flatMap(i -> RangeOfInt.of(0, 256).map(j -> new Color(i, (i + j) / 2, 255)))
                .toImList();

        println("imgData: " + imgData);

        ImMap<Color,Integer> counts = imgData
                .foldLeft(map(),
                          (accum, color) -> accum.assoc(color, accum.getOrElse(color, 0) + 1));

        println("counts: " + counts);

        UnEntry<Color,Integer> mostPopularColor =
                counts.foldLeft((UnEntry<Color,Integer>) tup((Color) null, 0),
                                (max, entry) -> (entry.getValue() > max.getValue()) ? entry : max);

        println("mostPopularColor: " + mostPopularColor);
        println("number of unique colors: " + counts.size());
    }

    // Same thing in "Traditional" Java:
    @Test public void colorSquare2() {
        List<Color> imgData = new ArrayList<>();
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                imgData.add(new Color(i, (i + j) / 2, 255));
            }
        }
        println("imgData: " + imgData.toString().substring(0, 50));

        Map<Color,Integer> counts = new HashMap<>();
        for (Color c : imgData) {
            counts.put(c, counts.getOrDefault(c, 0) + 1);
        }

        println("counts: " + counts.toString().substring(0, 50));

        Map.Entry<Color,Integer> max = Tuple2.of((Color) null, 0);
        for (Map.Entry<Color,Integer> entry : counts.entrySet()) {
            if (entry.getValue() > max.getValue()) {
                max = entry;
            }
        }

        println("mostPopularColor: " + max);
        println("number of unique colors: " + counts.size());
    }

    // Same thing with Java 8's Streams
    @Test public void colorSquare3() {
        List<Color> imgData = new ArrayList<>();
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                imgData.add(new Color(i, (i + j) / 2, 255));
            }
        }
        println("imgData: " + imgData.toString().substring(0, 50));

        Map<Color,Integer> counts = imgData.stream()
                .reduce(new HashMap<>(),
                        (HashMap<Color,Integer> accum, Color c) -> {
                            accum.put(c, accum.getOrDefault(c, 0) + 1);
                            return accum;
                        },
                        (HashMap<Color,Integer> accum1, HashMap<Color,Integer> accum2) -> {
                            for (Map.Entry<Color,Integer> e : accum2.entrySet()) {
                                Color key = e.getKey();
                                accum1.put(key, accum1.getOrDefault(key, 0) + e.getValue());
                            }
                            return accum1;
                        });

        println("counts: " + counts.toString().substring(0, 50));

        Optional<Map.Entry<Color,Integer>> mostPopularColor = counts.entrySet().stream().max((e1, e2) -> e1.getValue() - e2.getValue());

        println("mostPopularColor: " + mostPopularColor.get());
        println("number of unique colors: " + counts.size());
    }


}
