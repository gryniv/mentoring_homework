package org.training.calculator.operations.hash;

import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.training.calculator.exception.TaskCompletedException;

import static org.apache.commons.lang3.StringUtils.EMPTY;

public class MD5HashOperations implements HashOperations {
    private static final Logger LOG = LogManager.getLogger(MD5HashOperations.class);

    private static final int[][] RANGES;
    private static final String[] CHARACTERS = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n",
            "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};
    private static final int THRESHOLD;
    private static final int AVAILABLE_PROCESSORS;
    private static int rangeCounter = 0;
    private static final AtomicBoolean IS_COMPLETED = new AtomicBoolean();
    private static final ExecutorService SERVICE;
    private static final String PATTERN = "###.##";
    private static final DecimalFormat FORMAT = new DecimalFormat(PATTERN);
    private long startTime;
    private String target;

    static {
        final var availableProcessors = Runtime.getRuntime().availableProcessors();
        if (availableProcessors * 2 <= CHARACTERS.length) {
            AVAILABLE_PROCESSORS = availableProcessors * 2;
        } else {
            AVAILABLE_PROCESSORS = availableProcessors;
        }
        RANGES = new int[AVAILABLE_PROCESSORS][2];
        THRESHOLD = CHARACTERS.length / AVAILABLE_PROCESSORS;
        SERVICE = Executors.newFixedThreadPool(AVAILABLE_PROCESSORS);
    }

    @Override
    @SuppressWarnings("UnstableApiUsage deprecation")
    public String encode(final String toHash) {
        Hasher hasher = Hashing.md5().newHasher();
        hasher.putString(toHash, StandardCharsets.UTF_8);
        return hasher.hash().toString();
    }

    @Override
    public String decode(final String inputHash) {
        startTime = System.currentTimeMillis();
        target = inputHash;
        fillRanges(0, CHARACTERS.length - 1);

        List<Callable<String>> futureList = new ArrayList<>();
        for (final int[] range : RANGES) {
            final int start = range[0];
            final int end = range[1];
            final Callable<String> future = () -> {
                for (int i = 0; i < CHARACTERS.length; i++) {
                    for (int k = start; k < end; k++) {
                        try {
                            permutation(CHARACTERS[k], i);
                        } catch (final InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return e.getMessage();
                        }
                    }
                }
                return EMPTY;
            };
            futureList.add(future);
        }

        try {
            final String decodedValue = SERVICE.invokeAny(futureList);
            SERVICE.shutdownNow();
            return decodedValue;
        } catch (final InterruptedException | ExecutionException e) {
            LOG.debug(e.getMessage());
            Thread.currentThread().interrupt();
            return EMPTY;
        }
    }

    private void permutation(final String chars, final int position) throws InterruptedException {
        if (position == 0) {
            if (IS_COMPLETED.get()) {
                throw new TaskCompletedException();
            }
            final String hash = encode(chars);
            if (target.equals(hash)) {
                final String total = FORMAT.format((double) (System.currentTimeMillis() - startTime) / 1_000);
                LOG.info("Hash found for {} sec", total);
                IS_COMPLETED.set(true);
                throw new InterruptedException(chars);
            }
        } else {
            for (final String character : CHARACTERS) {
                permutation(chars + character, position - 1);
            }
        }
    }

    private static void fillRanges(int start, int end) {
        if (end - start <= THRESHOLD + 1) {
            LOG.info("[start={}, end={}]", start, end);
            RANGES[rangeCounter][0] = start;
            RANGES[rangeCounter][1] = end;
            rangeCounter++;
        } else {
            final int middle = start + ((end - start) / 2);
            fillRanges(start, middle);
            fillRanges(middle, end);
        }
    }
}