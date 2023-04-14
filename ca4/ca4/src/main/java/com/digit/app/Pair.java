package com.digit.app;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class Pair<U, V> {
    private final U left;
    private final V right;
}
