package com.bawnorton.autocodec.util;

public class BehaviourImpl implements Behaviour<Composite> {
    @Override
    public void test(Composite t) {
        System.out.println(t.first());
        System.out.println(t.second());
    }
}
