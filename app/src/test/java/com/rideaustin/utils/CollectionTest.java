package com.rideaustin.utils;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Sergey Petrov on 05/06/2017.
 */

public class CollectionTest {

    private int size = 1000;

    @Before
    public void setup() throws InterruptedException {
        Thread.sleep(500); // just to calm VM down :)
    }

    // TODO: test fails on Jenkins but works locally, need to check why
    //@Test(expected = ConcurrentModificationException.class)
    //public void arrayListShouldThrowConcurrentModifications() throws InterruptedException {
    //    doConcurrentModification(getArrayList(size));
    //}

    // TODO: test fails on Jenkins but works locally, need to check why
//    @Test
//    public void arrayListShouldNotThrowConcurrentModificationsIteratingCopy() throws InterruptedException {
//        doModificationIteratingCopy(getArrayList(size));
//    }

    // TODO: test fails on Jenkins but works locally, need to check why
//    @Test(expected = ConcurrentModificationException.class)
//    public void hashSetShouldThrowConcurrentModifications() throws InterruptedException {
//        doConcurrentModification(getHashSet(size));
//    }

    // TODO: test fails on Jenkins but works locally, need to check why
//    @Test(expected = ConcurrentModificationException.class)
//    public void hashSetShouldThrowConcurrentModificationsIteratingCopy() throws InterruptedException {
//        doModificationIteratingCopy(getHashSet(size));
//    }

    // TODO: test fails on Jenkins but works locally, need to check why
//    @Test(expected = ConcurrentModificationException.class)
//    public void synchronizedHashSetShouldThrowConcurrentModifications() throws InterruptedException {
//        doConcurrentModification(getSynchronizedSet(size));
//    }

    // TODO: test fails on Jenkins but works locally, need to check why
//    @Test(expected = ConcurrentModificationException.class)
//    public void synchronizedHashSetShouldThrowConcurrentModificationsIteratingCopy() throws InterruptedException {
//        doModificationIteratingCopy(getSynchronizedSet(size));
//    }

    @Test
    public void concurrentHashSetShouldNotThrowConcurrentModifications() throws InterruptedException {
        doConcurrentModification(getConcurrentSet(size));
    }

    @Test
    public void concurrentHashSetShouldNotThrowConcurrentModificationsIteratingCopy() throws InterruptedException {
        doModificationIteratingCopy(getConcurrentSet(size));
    }

    private void doConcurrentModification(Collection<String> collection) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        new Thread(() -> {
            try {
                Iterator<String> it = collection.iterator();
                while (it.hasNext()) {
                    String s = it.next();
                    it.remove();
                }
            } catch (Exception e) {
                throw e;
            } finally {
                latch.countDown();
            }
        }).start();
        do {
            for (String str : collection) {
                // do nothing, just iterate
            }
        } while (latch.getCount() == 1);

        latch.await();
    }

    private void doModificationIteratingCopy(Collection<String> collection) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        new Thread(() -> {
            try {
                Iterator<String> it = collection.iterator();
                while (it.hasNext()) {
                    String s = it.next();
                    it.remove();
                }
            } catch (Exception e) {
                throw e;
            } finally {
                latch.countDown();
            }
        }).start();
        do {
            for (String str : new ArrayList<>(collection)) {
                // do nothing, just iterate
            }
        } while (latch.getCount() == 1);

        latch.await();
    }

    private List<String> getArrayList(int size) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(String.valueOf(i));
        }
        return list;
    }

    private Set<String> getHashSet(int size) {
        Set<String> set = new HashSet<>();
        for (int i = 0; i < size; i++) {
            set.add(String.valueOf(i));
        }
        return set;
    }

    private Set<String> getSynchronizedSet(int size) {
        Set<String> set = Collections.synchronizedSet(new HashSet<>());
        for (int i = 0; i < size; i++) {
            set.add(String.valueOf(i));
        }
        return set;
    }

    private Set<String> getConcurrentSet(int size) {
        Set<String> set = Collections.newSetFromMap(new ConcurrentHashMap<>());
        for (int i = 0; i < size; i++) {
            set.add(String.valueOf(i));
        }
        return set;
    }
}
