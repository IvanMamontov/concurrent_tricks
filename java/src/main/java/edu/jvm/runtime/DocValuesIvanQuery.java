/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.jvm.runtime;

import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.SortedNumericDocValuesField;
import org.apache.lucene.index.DocValues;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.search.*;

import java.io.IOException;
import java.util.Objects;

/**
 * Like {@link DocValuesTermsQuery}, but this query only
 * runs on a long {@link NumericDocValuesField} or a
 * {@link SortedNumericDocValuesField}, matching
 * all documents whose value in the specified field is
 * contained in the provided set of long values.
 * <p>
 * <p>
 * <b>NOTE</b>: be very careful using this query: it is
 * typically much slower than using {@code TermsQuery},
 * but in certain specialized cases may be faster.
 *
 * @lucene.experimental
 */
public class DocValuesIvanQuery extends Query {

    private final String field;
    private final long number;

    public DocValuesIvanQuery(String field, long number) {
        this.field = Objects.requireNonNull(field);
        this.number = number;
    }

    @Override
    public int hashCode() {
        return 31 * classHash() + Objects.hash(field, number);
    }

    public String getField() {
        return field;
    }

    @Override
    public String toString(String defaultField) {
        return field +
                ": " +
                number;
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }

    @Override
    public Weight createWeight(IndexSearcher searcher, boolean needsScores, float boost) throws IOException {
        return new ConstantScoreWeight(this, boost) {

            @Override
            public Scorer scorer(LeafReaderContext context) throws IOException {
                final NumericDocValues values = DocValues.getNumeric(context.reader(), field);
                final DocIdSetIterator iterator = new DocIdSetIterator() {

                    @Override
                    public int docID() {
                        return values.docID();
                    }

                    @Override
                    public int nextDoc() throws IOException {
                        return values.nextDoc();
                    }

                    @Override
                    public int advance(int target) throws IOException {
                        return values.advance(target);
                    }

                    @Override
                    public long cost() {
                        return 5;
                    }
                };
                return new Scorer(this) {
                    @Override
                    public int docID() {
                        return iterator.docID();
                    }

                    @Override
                    public float score() throws IOException {
                        return 1;
                    }

                    @Override
                    public int freq() throws IOException {
                        return 1;
                    }

                    @Override
                    public DocIdSetIterator iterator() {
                        return iterator;
                    }
                };
            }
        };

    }
}
