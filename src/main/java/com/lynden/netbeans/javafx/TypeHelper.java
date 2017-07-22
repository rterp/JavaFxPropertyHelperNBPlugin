/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Lynden, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.lynden.netbeans.javafx;

import java.util.ArrayList;
import java.util.List;

/**
 * Manipulates strings containing types.
 *
 */
class TypeHelper {

    /**
     * Returns only the class name part of a type name without type parameters.
     * {@code "java.util.Map<java.lang.String, java.lang.String>" -> "java.util.Map"}
     *
     * @param fullName
     * @return class name
     */
    static String getClassName(String fullName) {
        if (!fullName.contains("<")) {
            return fullName;
        } else {
            return fullName.substring(0, fullName.indexOf('<'));
        }
    }

    /**
     * Returns only the type parameters part of a type name. Type params are in
     * the same form as in the original type. If there were no type parameters,
     * null is returned.
     * {@code "java.util.Map<java.lang.String, java.lang.String>" -> "java.lang.String, java.lang.String"}
     *
     * @param fullName
     * @return type parameters
     */
    static String getTypeParameters(String fullName) {
        if (!fullName.contains("<")) {
            return null;
        } else {
            return fullName.substring(fullName.indexOf('<') + 1, fullName.lastIndexOf('>'));
        }
    }

    /** Removes packages from class names (it manages generics too). i.e.
     * {@code java.lang.String -> String} and
     * {@code java.util.List<java.lang.String> -> List<String>}.
     */
    static String removePackagesFromGenericsType(String fullName) {
        final List<String> list = new ArrayList<>();
        int idx = 0, counter = 0;
        for (char c : fullName.toCharArray()) {
            switch (c) {
                case ',':
                case '<':
                case '>':
                    list.add(fullName.substring(idx, counter));
                    list.add(String.valueOf(c));
                    idx = counter + 1;
                    break;
            }
            counter++;
        }
        if (list.isEmpty()) {
            return removePackageFromType(fullName);
        }
        StringBuilder buf = new StringBuilder();
        for (String s : list) {
            if ("<>,".contains(s)) {
                buf.append(s);
            } else {
                buf.append(removePackageFromType(s));
            }
        }
        return buf.toString();
    }

    /** Removes the package from a single class name (don't manage generics). */
    static String removePackageFromType(String fullname) {
        int lastIndexOfPoint = fullname.lastIndexOf('.');
        if (lastIndexOfPoint == -1) {
            return fullname;
        }
        return fullname.substring(lastIndexOfPoint + 1, fullname.length());
    }

}
