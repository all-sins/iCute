import discord4j.rest.util.Color;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

public class TsuICMP {

    private String image;
    private Color color;
    private int pointer = 0;
    private float averageMs;
    private long highestMs;
    private long lowestMs;
    private int packetsSent;
    private int packetsDropped;
    private String insight;
    private boolean measurerRunning = true;
    private float jitterPercent;
    private float jitter;

    // Values with important init values.
    private int minInterval = 1000;
    private int maxInterval = 5000;

    // Constants.
    private static final String[] domainList = new String[]{
            // OpenDNS
            "208.67.222.222", "208.67.220.220",
            // Cloudflare
            "1.1.1.1", "1.0.0.1",
            // GoogleDNS
            "8.8.8.8", "8.8.4.4",
            // ComodoDNS
            "8.26.56.26", "8.20.247.20",
            // Quad9
            "9.9.9.9", "149.112.112.112"
    };

    private final List<Spike> spikes = new ArrayList<>();
    public static final TsuICMP tsuICMP = new TsuICMP();
    private static final int timeout = 1000; // Maximum timeout for ping.
    private static final int[] msList = new int[100]; // Amount of results to keep in memory.
    public static final int msListSize = msList.length; // Measuring array size for use in Spike class.
    private static final Random randomGen = new Random(); // Random number generator.

    public boolean isMeasurerRunning() {
        return measurerRunning;
    }

    public void setMeasurerRunning(boolean measurerRunning) {
        this.measurerRunning = measurerRunning;
    }

    public float getAverageMs() {
        return averageMs;
    }

    public long getHighestMs() {
        return highestMs;
    }

    public long getLowestMs() {
        return lowestMs;
    }

    public int getPacketsSent() {
        return packetsSent;
    }

    public int getPacketsDropped() {
        return packetsDropped;
    }

    public int[] getMsList() {
        return msList;
    }

    public String getInsight() {
        return insight;
    }

    public Color getColor() {
        return color;
    }

    public String getImage() {
        return image;
    }

    public float getJitterPercent() {
        return jitterPercent;
    }

    public float getJitter() {
        return jitter;
    }

    public List<Spike> getSpikes() {
        return spikes;
    }

    public int getRandomInt(int max, int min) {
        // Both min and max are inclusive for this method.
        OptionalInt optionalInt = randomGen.ints(max, min).findAny();
        if (optionalInt.isPresent()) {
            return optionalInt.getAsInt();
        }
        // Only the min is inclusive here, so adding one to max makes it inclusive.
        return randomGen.nextInt((max + 1) - min) + min;
    }

    public int getArrayInitAmount() {
        int arrayInitAmount = 0;
        for (long l : msList) {
            if (l != 0) {
                arrayInitAmount++;
            }
        }
        return arrayInitAmount;
    }

    public void measure() {
        String host = domainList[getRandomInt(0, domainList.length - 1)];
        try {
            InetAddress inetAddress = InetAddress.getByName(host);
            try {

                // Reset the pointer if it's at the end of the array.
                if (pointer > msList.length - 1) {
                    pointer = 0;
                }

                // Measure response time and store it or the timeout maximum.
                long start = System.currentTimeMillis();
                if (inetAddress.isReachable(timeout)) {
                    msList[pointer] = (int) (System.currentTimeMillis() - start);
                    packetsSent++;
                } else {
                    msList[pointer] = timeout;
                    packetsDropped++;
                }

                // Move pointer to next position.
                pointer++;

                // Sleep for msInterval amount of time.
                Thread.sleep(getRandomInt(minInterval, maxInterval));

            } catch (InterruptedException e) {
                System.out.println("Could not sleep!");
            } catch (IOException e) {
                System.out.println("Could not reach: " + host);
            }
        } catch (UnknownHostException e) {
            System.out.println("Can not resolve: " + host);
        }
    }

    public void runMeasurer() {
        // Run a thread to constantly populate results.
        new Thread( () -> {
            while (measurerRunning) {
                tsuICMP.measure();
                // tsuICMP.printResultArray();
            }
        }).start();
    }

    // DEBUG FUNCTION
    private void printResultArray() {
        for (int i = 0; i < msList.length; i++) {
            StringBuilder formatting = new StringBuilder();

            // Calculate how many digits the number has.
            int tmpNumber = (int) msList[i];
            int amountOfDigits = 0;
            while (tmpNumber > 0) {
                tmpNumber /= 10;
                amountOfDigits++;
            }

            // Prepare formatting.
            int spacesBetweenNumbers = 5;
            if (i == pointer) {
                spacesBetweenNumbers += 4 - amountOfDigits;
                formatting.append("<");
                // Compiling with Java 1.8, so no String.repeat.
                for (int i1 = 0; i1 < spacesBetweenNumbers - 1; i1++) {
                    formatting.append(" ");
                }
            } else {
                spacesBetweenNumbers += 4 - amountOfDigits;
                // Compiling with Java 1.8, so no String.repeat.
                for (int i1 = 0; i1 < spacesBetweenNumbers; i1++) {
                    formatting.append(" ");
                }
            }

            // Print out with specific amount of columns.
            int columnLimit = 10;
            if (i % columnLimit == 0) {
                System.out.print("\n"+ msList[i] + formatting.toString());
            } else {
                System.out.print(msList[i] + formatting.toString());
            }
        }
    }

    public void analyse() {

        // Find lowest ping.
        float sum = 0;
        int min = msList[0];
        int max = msList[0];

        // Calculate sum of all elements.
        for (int i : msList) {
            sum += i;
        }
        averageMs = BigDecimal.valueOf(sum / getArrayInitAmount()).setScale(2, RoundingMode.UP).floatValue();

        for (int item : msList) {

            // Extra check for 0 while array inits.
            if (item != 0) {
                // Calculate minimum.
                if (item < min) {
                    min = item;
                }

                // If a item in the latency list deviates by half then assume that it's a spike.
                if (item > averageMs + 30) {
                    spikes.add(new Spike(item));
                }
            }

            // Lover TTL on all Spike's in spikes list or remove spike if it's TTL runs out.
            List<Spike> spikesToRemove = new ArrayList<>();
            for (Spike spike : spikes) {
                if (spike.ttl - 1 <= 0) {
                    spikesToRemove.add(spike);
                } else {
                    spike.ttl--;
                }
            }
            spikes.removeAll(spikesToRemove);

            // Calculate maximum.
            if (item > max) {
                max = item;
            }

        }
        highestMs = max;
        lowestMs = min;

        // Calculate difference between average, minimum and maximum as a number and as a percentage.
        // https://www.calculatorsoup.com/calculators/algebra/percent-difference-calculator.php
        float tempJitter = max - min;
        float tempJitterPercent = ((max - min) / ((max + min) / 2f)) * 100f;
        jitter = BigDecimal.valueOf(tempJitter).setScale(2, RoundingMode.UP).floatValue();
        jitterPercent = BigDecimal.valueOf(tempJitterPercent).setScale(2, RoundingMode.UP).floatValue();

        // Determine insight.
        // Switch statement doesn't work with logical operators, so unfortunately my hand was forced to make this
        // huge if else chain.
        if (averageMs < 30 && spikes.size() < 0.05 * msList.length) {
            insight = "The connection is currently absolutely amazing! Good for even fighting games!";
            color = Color.of(0, 255, 255);
            image = "https://i.pinimg.com/originals/9c/da/52/9cda52a9128defc55ed86e8bd7c55f54.gif";
        } else if (averageMs < 50 && spikes.size() < 0.10 * msList.length) {
            insight = "The connection is alright! Passable for shooters!";
            color = Color.of(0, 255, 0);
            image = "https://media.giphy.com/media/EktbegF3J8QIo/giphy.gif";
        } else if (averageMs < 70 && spikes.size() < 0.15 * msList.length) {
            insight = "The connection is acceptable! Sketchy though!";
            color = Color.of(255, 255, 0);
            image = "https://media.giphy.com/media/dZXFMaFBlReiA/giphy.gif";
        } else if (averageMs < 90 && spikes.size() < 0.20 * msList.length) {
            insight = "The connection is borderline laggy! You tempt fate by playing!";
            color = Color.of(255, 100, 0);
            image = "https://media.giphy.com/media/RwnFuvcQTktQA/giphy.gif";
        } else if (averageMs < 120) {
            insight = "The connection is laggy! No competitive games!";
            color = Color.of(255, 75, 8);
            image = "https://media.giphy.com/media/snEeOh54kCFxe/giphy.gif";
        } else if (averageMs < 150) {
            insight = "The connection is really bad! Playing online right now is not wise!";
            color = Color.of(255, 0, 0);
            image = "https://media.giphy.com/media/gpuwFOUBEM1aM/giphy.gif";
        } else if (averageMs < 200) {
            insight = "The connection is currently akin to a bad mood generator. Avoid online!";
            color = Color.of(180, 0, 0);
            image = "https://media.giphy.com/media/kdQqSfBiIkAVGCAIOD/giphy.gif";
        } else if (averageMs < 250) {
            insight = "Please don't waste your time with anything competitive.";
            color = Color.of(110, 0, 0);
            image = "https://media.giphy.com/media/ibv61nlDmaToQ1gqEn/giphy.gif";
        } else if (averageMs < 300) {
            insight = "By the time you get this message, it probably already got worse.";
            color = Color.of(60, 0, 0);
            image = "https://media.giphy.com/media/3LyZBPN2iv76muaPlu/giphy.gif";
        } else if (averageMs < 350) {
            insight = "Today is not a good day for even YouTube! Try using telnet for chess maybe.";
            color = Color.of(30, 0, 0);
            image = "https://media.giphy.com/media/XUFPGrX5Zis6Y/giphy.gif";
        } else {
            insight = "Mars Curiosity rover called, it wants its RTT back!";
            color = Color.of(0, 255, 0);
            image = "https://media.giphy.com/media/3og0IFrHkIglEOg8Ba/giphy.gif";
        }
    }

    public static String msListAsString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        for (int i = 0; i < msList.length; i++) {
            if (i == msList.length - 1) {
                stringBuilder.append(msList[i]);
            } else {
                stringBuilder.append(msList[i]).append(", ");
            }
        }
        stringBuilder.append("]");
        return stringBuilder.toString();
    }
}
