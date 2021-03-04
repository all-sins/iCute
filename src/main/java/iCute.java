import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.discordjson.json.UserModifyRequest;
import discord4j.discordjson.json.gateway.StatusUpdate;
import discord4j.rest.util.Color;

import java.io.*;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

public class iCute {

    private static String token = "";

    // TODO: Find a better way to handle spam / limit how often iCute can be used.
    public static void main(String[] args) {

        // Attempt to read token from file. In case of errors, exit the program and print debug info.
        boolean errorOccurred = false;
        try {
            StringBuilder stringBuilder = new StringBuilder();
            BufferedReader bufferedReader = new BufferedReader(new FileReader("token"));
            String currentLine;
            while ((currentLine = bufferedReader.readLine()) != null) {
                // Read character gets returned as a int, so to get the character representation, I have to cast it
                // to a char. Result gets appended to StringBuilder.
                stringBuilder.append(currentLine);
            }
            token = stringBuilder.toString();
            bufferedReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("Could not find token file in root of application! Exiting!");
            errorOccurred = true;
        } catch (IOException e) {
            e.printStackTrace();
            errorOccurred = true;
        }

        // Exit the program if an error occurred while reading the token. This is a graceful way of preventing the
        // disaster which would happen if token was empty and was passed to DiscordClient builder.
        if (errorOccurred) {
            return;
        }

        final String googleIcon = "https://external-content.duckduckgo.com/iu/?u=http%3A%2F%2Fwww.androidpoli"+
                "ce.com%2Fwp-content%2Fuploads%2F2015%2F09%2Fnexus2cee_new_google_icon.png&f=1&nofb=1";
        final DiscordClient client = DiscordClient.create(token);
        final GatewayDiscordClient gateway = client.login().block();
        final ArrayList<String> adminUsers = new ArrayList<>(Arrays.asList("Tsu#5168"));

        // Check if gateway was created succesfully. Prevent potential null pointer exception later on.
        if (gateway == null) {
            throw new InternalError("Couldn't log in to create a gateway!");
        }

        // Start measurer.
        TsuICMP tsuICMP = new TsuICMP();
        tsuICMP.runMeasurer("8.8.8.8", 1000L);

        // Subscribe to MessageCreateEvent and execute the following code when it's fired.
        gateway.on(MessageCreateEvent.class).subscribe(event -> {
            final Message message = event.getMessage();
            final MessageChannel channel = message.getChannel().block();
            final Optional<User> author = message.getAuthor();
            final String fullUsername = author.map(user -> user.getUsername() + user.getDiscriminator()).orElse("FULL_USERNAME_GET_ERROR");
            final String username = author.map(User::getUsername).orElse("USERNAME_GET_ERROR");
            final String messageText = message.getContent();

            if (channel == null) {
                throw new InternalError("CHANNEL_IS_NULL");
            }

            if ("$ctp".equals(message.getContent())) {

                long startTimer = System.currentTimeMillis();
                message.addReaction(ReactionEmoji.unicode("\u2705"));
                System.out.println("Processing \"$ctp\" request from "+username);
                tsuICMP.analyse();
                String poolUtilization = (tsuICMP.getArrayInitAmount() * 100) / tsuICMP.getMsList().length+ "%";
                float timeTaken = (System.currentTimeMillis() - startTimer) / 1000f;
                String titleMessage;
                if (timeTaken > 5000L) {
                    titleMessage = "Request took a worrying amount of time! "+username+", please contact @Tsu!";
                } else {
                    titleMessage = "Request took "+timeTaken+" seconds.";
                }

                channel.createEmbed(spec -> spec
                        .setColor(tsuICMP.getColor())
                        .setImage(tsuICMP.getImage())
                        .setTitle(titleMessage)
                        .addField("Maximum", tsuICMP.getHighestMs() + " ms", true)
                        .addField("Minimum", tsuICMP.getLowestMs() + " ms", true)
                        .addField("Average", tsuICMP.getAverageMs() + " ms", true)
                        .addField("Packets sent", String.valueOf(tsuICMP.getPacketsSent()), true)
                        .addField("Packets dropped", String.valueOf(tsuICMP.getPacketsDropped()), true)
                        .addField("Jitter", tsuICMP.getJitter() + " ms", true)
                        .addField("Jitter %", tsuICMP.getJitterPercent() + " %", true)
                        .addField("Pool size", String.valueOf(tsuICMP.getMsList().length), true)
                        .addField("Pool utilization", poolUtilization, true)
                        .addField("iCute verdict", tsuICMP.getInsight(), true)
                        //.setThumbnail(tsuICMP.getIŗmage())
                        .setFooter("Testing done against Google DNS servers.", googleIcon)
                        .setTimestamp(Instant.now())
                ).block();
            }

            // Toggle measuring.
            // Turn ON.
            if (messageText.startsWith("$measuring:")) {
                if (adminUsers.contains(fullUsername)) {
                    if (messageText.equals("$measuring:on")) {
                        // turn on
                        if (tsuICMP.isMeasurerRunning()) {
                            channel.createMessage("Measuring is already running "+username+"! Ignoring command!").block();
                        } else {
                            tsuICMP.runMeasurer("8.8.8.8", 1000L);
                            channel.createMessage("Measuring enabled "+username+"!").block();
                        }
                    } else if (messageText.equals("$measuring:off")) {
                        // turn off
                        if (tsuICMP.isMeasurerRunning()) {
                            tsuICMP.setMeasurerRunning(false);
                            channel.createMessage("Measurer disabled "+username+"!").block();
                        } else {
                            channel.createMessage("Measurer is not running "+username+"!").block();
                        }
                    } else {
                        channel.createEmbed(spec -> spec
                                .setColor(Color.RED)
                                .setTitle("Incorrect usage of command! Consult the text box below for proper usage.")
                                .setDescription("$measuring:on - Turn on the measurer.\n$measuring:off - Turn off the measurer.")
                                .setTimestamp(Instant.now())
                        ).block();
                    }
                } else {
                    // auth bad
                    channel.createMessage("You are not authorized to issue this command to me "+username+"!").block();
                }
            }

        });

        gateway.onDisconnect().block();
    }
}
