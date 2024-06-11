package com.bawnorton.autocodec.util;

import com.mojang.datafixers.util.Either;

/**
 * Simpler version of {@link Either} as all the overhead is not needed.
 */
public record Or<A, B>(A left, B right) {
    public Or {
        if (left == null && right == null) {
            throw new IllegalArgumentException("Either left or right must be non-null");
        }
    }

    public boolean isLeft() {
        return left != null;
    }

    public boolean isRight() {
        return right != null;
    }

    public static <A, B> Or<A, B> left(A left) {
        return new Or<>(left, null);
    }

    public static <A, B> Or<A, B> right(B right) {
        return new Or<>(null, right);
    }
}
