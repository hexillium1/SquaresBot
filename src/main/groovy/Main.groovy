import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.ChunkingFilter

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class Main {
    static void main(String[] args) {
        String token = new String(Files.readAllBytes(Paths.get("token.txt")))
        JDABuilder builder = JDABuilder.createLight(token)
        builder.addEventListeners(new GameListener())
        builder.enableIntents(GatewayIntent.GUILD_MESSAGES)
        builder.setChunkingFilter(ChunkingFilter.NONE)

        JDA jda = builder.build()
        jda.setRequiredScopes("applications.commands")

        println jda.getInviteUrl(Permission.MESSAGE_READ, Permission.MESSAGE_WRITE)

    }
}