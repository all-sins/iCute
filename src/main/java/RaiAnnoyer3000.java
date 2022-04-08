import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;

import java.util.OptionalInt;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class RaiAnnoyer3000 {

    // Joke feature.
    private boolean running = false;
    private int annoyFreqMin = 5000;
    private int annoyFreqMax = 60000;

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public int getAnnoyFreqMin() {
        return annoyFreqMin;
    }

    public void setAnnoyFreqMin(int annoyFreqMin) {
        this.annoyFreqMin = annoyFreqMin;
    }

    public int getAnnoyFreqMax() {
        return annoyFreqMax;
    }

    public void setAnnoyFreqMax(int annoyFreqMax) {
        this.annoyFreqMax = annoyFreqMax;
    }

    // TODO: Split off into an external class to be imported.
    // Copied over from TsuICMP class for fast implementation.
    private static final Random randomGen = new Random(); // Random number generator.
    public static int getRandomInt(int max, int min) {
        // Both min and max are inclusive for this method.
        OptionalInt optionalInt = randomGen.ints(max, min).findAny();
        if (optionalInt.isPresent()) {
            return optionalInt.getAsInt();
        }
        // Only the min is inclusive here, so adding one to max makes it inclusive.
        return randomGen.nextInt((max + 1) - min) + min;
    }



    public void run(Message message) {
        new Thread(() -> {
            System.out.println("Annoyer thread running.");
            MessageChannel channel = message.getChannel().block();
            boolean firstRun = true;
            while (isRunning()) {
                int timeToWait = getRandomInt(annoyFreqMin, annoyFreqMax);
                System.out.println("Timeout: "+timeToWait+"ms");
                if (channel != null) {
                    System.out.println("Channel is not null.");
                    if (firstRun) {
                        System.out.println("Sending first annoy message!");
                        channel.createMessage("Hi <@272197210626588682>!").block();
                        firstRun = false;
                    } else {
                        System.out.println("Sending annoy message!");
                        channel.createMessage("This time I waited " + timeToWait + "ms before mentioning you <@272197210626588682>!").block();
                    }
                } else {
                    System.out.println("Channel is null! Cannot create annoy message.");
                }
                try {
                    System.out.println("Sleeping for "+timeToWait+"ms!");
                    Thread.sleep(timeToWait);
                } catch (InterruptedException e) {
                    System.out.println("Could not sleep thread!");
                }
            }
        }).start();
    }


}
