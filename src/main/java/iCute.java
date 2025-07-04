import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.rest.util.Color;
//import jdk.nashorn.internal.runtime.regexp.joni.exception.InternalException;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class iCute {

    // TODO: Find a better way to handle spam / limit how often iCute can be used.
    // TODO: Started to explore picocli command line parser framework. Continue!
    @CommandLine.Command
    public static void main(String[] args) {

        // Attempt to read token from file. In case of errors, exit the program and print debug info.
        String token;
        try {
            StringBuilder stringBuilder = new StringBuilder();
            BufferedReader bufferedReader = new BufferedReader(new FileReader("token"));
            String currentLine;
            while ((currentLine = bufferedReader.readLine()) != null) {
                // Read a full line of the file while there is one. Result gets appended to StringBuilder.
                stringBuilder.append(currentLine);
            }
            token = stringBuilder.toString();
            bufferedReader.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Token file not found in root of application!");
        } catch (IOException e) {
            throw new RuntimeException("Token file could not be read.");
        }

        final String googleIcon = "https://external-content.duckduckgo.com/iu/?u=http%3A%2F%2Fwww.androidpoli"+
                "ce.com%2Fwp-content%2Fuploads%2F2015%2F09%2Fnexus2cee_new_google_icon.png&f=1&nofb=1";
        final DiscordClient client = DiscordClient.create(token);
        final GatewayDiscordClient gateway = client.login().block();
        final ArrayList<String> adminUsers = new ArrayList<>(Arrays.asList("Tsu#5168", "og.tsu"));

        // Check if gateway was created successfully. Prevent potential null pointer exception later on.
        if (gateway == null) {
            throw new InternalError("Couldn't log in to create a gateway!");
        }

        // Start measurer.
        TsuICMP tsuICMP = TsuICMP.tsuICMP;
        tsuICMP.runMeasurer();

        https://discord.com/oauth2/authorize?client_id=1364960464777510992&scope=bot+applications.commands&permissions=563467497368640

        // Subscribe to MessageCreateEvent and execute the following code when it's fired.
        gateway.on(MessageCreateEvent.class).subscribe(event -> {
            final Message message = event.getMessage();
            final MessageChannel channel = message.getChannel().block();
            final Optional<User> author = message.getAuthor();
            //final String fullUsername = author.map(user -> user.getUsername() + "#" + user.getDiscriminator())
            //        .orElse("FULL_USERNAME_GET_ERROR");
            final String fullUsername = author.map(User::getUsername)
                    .orElse("FULL_USERNAME_GET_ERROR");
            final String username = author.map(User::getUsername).orElse("USERNAME_GET_ERROR");
            final String messageText = message.getContent();

            if (channel == null) {
                throw new InternalError("CHANNEL_IS_NULL");
            }

            if ("$ctp".equals(message.getContent())) {

                long startTimer = System.currentTimeMillis();
                message.addReaction(ReactionEmoji.unicode("\u2705")).block();
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

                tsuICMP.printResultArray();
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
                        .addField("Latency spikes", String.valueOf(tsuICMP.getSpikes()), true)
                        .addField("iCute verdict", tsuICMP.getInsight(), false)
                        //.setThumbnail(tsuICMP.getIŗmage())
                        .setFooter("Testing done against a variety of DNS servers.", googleIcon)
                        .setTimestamp(Instant.now())
                ).block();
            }

            // Toggle measuring.
            // Turn ON.
            if (messageText.startsWith("$measuring:")) {
                if (adminUsers.contains(fullUsername)) {
                    message.addReaction(ReactionEmoji.unicode("\u2705")).block();
                    if (messageText.equals("$measuring:on")) {
                        // turn on
                        if (tsuICMP.isMeasurerRunning()) {
                            channel.createMessage("Measuring is already running! Ignoring command!").block();
                        } else {
                            tsuICMP.setMeasurerRunning(true);
                            tsuICMP.runMeasurer();
                            channel.createMessage("Measuring enabled!").block();
                        }
                    } else if (messageText.equals("$measuring:off")) {
                        // turn off
                        if (tsuICMP.isMeasurerRunning()) {
                            tsuICMP.setMeasurerRunning(false);
                            channel.createMessage("Measurer disabled!").block();
                        } else {
                            channel.createMessage("Measurer is not running! Ignoring command!").block();
                        }
                    } else {
                        channel.createEmbed(spec -> spec
                                .setColor(Color.RED)
                                .setTitle("Incorrect usage of command! Consult the text box below for proper usage.")
                                .setDescription("$measuring:on - Turn on the measurer.\n$measuring:off - Turn off the measurer.")
                        ).block();
                    }
                } else {
                    // auth bad
                    System.out.println("fullUsername = " + fullUsername);
                    channel.createMessage("You are not authorized to issue this command to me "+username+"!").block();
                }
            }

            // Purge a specified amount of messages.
            if (messageText.startsWith("$purge:")) {
                // User is allowed to take this action.
                if (adminUsers.contains(fullUsername)) {
                    message.addReaction(ReactionEmoji.unicode("\u2705")).block();
                    String commandParameter = messageText.substring(7);
                    StringTools stringTools = new StringTools();
                    int parameterAsInt;
                    // Check against Strings that are dangerously large.
                    if (commandParameter.length() <= 9) {
                        if (commandParameter.length() != 0) {
                            if (!stringTools.containsCharsBesides(commandParameter, "0123456789")) {
                                parameterAsInt = Integer.parseInt(commandParameter);


                                // TODO: There has to be a better way.
                                Snowflake now = Snowflake.of(Instant.now());
                                List<Message> messages = channel.getMessagesBefore(now).buffer(parameterAsInt).blockFirst();
                                if (messages != null) {
                                    for (Message msg : messages) {
                                        msg.delete().block();
                                    }
                                } else {
                                    System.out.println("Attempted message deletion was run on null value!");
                                }
                                channel.createMessage("Purged "+parameterAsInt+" messages!").block();


                            } else {
                                channel.createMessage("Illegal parameter used! Please use numbers only!").block();
                            }
                        } else {
                            channel.createMessage("Illegal parameter used! Please supply a parameter!").block();
                        }
                    } else {
                        channel.createMessage("Illegal parameter used! Please use no more than 9 digits!").block();
                    }
                    // User is allowed to take this action.
                } else {
                    channel.createMessage("You are not authorized to issue this command to me "+username+"!").block();
                }
            }

            if (messageText.equals("$notifyme")) {
                message.addReaction(ReactionEmoji.unicode("\u2705")).block();
                tsuICMP.runReminder(message);
            }

        });

        gateway.onDisconnect().retry().block();
    }
}
