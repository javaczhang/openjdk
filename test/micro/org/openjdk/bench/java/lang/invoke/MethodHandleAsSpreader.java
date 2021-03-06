/*
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package org.openjdk.bench.java.lang.invoke;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.concurrent.TimeUnit;

/**
 * Microbenchmark assesses MethodHandle.asSpreader() performance
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class MethodHandleAsSpreader {

    /*
    * Implementation notes:
    *   - simple array-parameter method is being called
    *   - baselineRaw calls method directly with dynamically instantiating the array
    *   - baselineCached calls method directly with pre-cached array
    *   - additional testCreate() test harnesses the collector acquisition performance
    *   - testCollector() can be faster than both baselines: it can wrapping array at all
    */

    public int i;
    private static MethodHandle mh;
    private static MethodHandle spreaderMH;
    private static int[] cachedArgs;

    @Setup
    public void setup() throws IllegalAccessException, NoSuchMethodException {
        mh = MethodHandles.lookup().findVirtual(MethodHandleAsSpreader.class, "doWork", MethodType.methodType(void.class, int.class, int.class, int.class, int.class, int.class));
        spreaderMH = mh.asSpreader(int[].class, 5);
        cachedArgs = new int[]{1, 2, 3, 4, 5};
    }

    @Benchmark
    public void baselineMH() throws Throwable {
        mh.invokeExact(this, 1, 2, 3, 4, 5);
    }

    @Benchmark
    public void baselineRaw() throws Throwable {
        doWork(1, 2, 3, 4, 5);
    }

    @Benchmark
    public MethodHandle testCreate() {
        return mh.asSpreader(int[].class, 5);
    }

    @Benchmark
    public void testSpreader() throws Throwable {
        spreaderMH.invokeExact(this, new int[] { 1, 2, 3, 4, 5 });
    }

    @Benchmark
    public void testSpreaderCached() throws Throwable {
        spreaderMH.invokeExact(this, cachedArgs);
    }

    public void doWork(int arg1, int arg2, int arg3, int arg4, int arg5) {
        i += (arg1 + arg2 + arg3 + arg4 + arg5);
    }

}
