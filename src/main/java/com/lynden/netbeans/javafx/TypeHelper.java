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
}
