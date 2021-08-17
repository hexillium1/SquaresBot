import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.EventListener
import net.dv8tion.jda.api.interactions.components.ActionRow

import javax.annotation.Nonnull

class GameListener implements EventListener{

    //maps from gameID -> game
    var gameMap = new HashMap<Long, Game>()

    //maps from userID -> gameID
    var userMap = new HashMap<Long, Long>()

    void onButtonClick(ButtonClickEvent event){
        var compID = event.componentId
        var args = compID.split(":")
        if (args[0] == "disabled"){
            println "uh oh... something has gone wrong"
            return
        }
        var gameid = args[1] as long
        var target = args[2] as int

        var game = gameMap.get(gameid)

        if (game == null){
            event.editComponents(cloneToDisabled(event.getMessage().actionRows)).queue()
            event.getHook().sendMessage("This game seems to not exist any more.").setEphemeral(true).queue()
            return
        }

        if (game.userID != event.member.idLong){
            event.reply("This is not your game!").setEphemeral(true).queue()
            return
        }

        game.swap(target)


        if (!game.solved)
            event.editComponents(game.populateButtons()).queue()
        else {
            event.editComponents(game.populateCompletedButtons()).setContent("Congratulations!  This puzzle has been solved in ${game.moves} moves.").queue()
            userMap.remove(event.member.idLong, game.gameID)
            gameMap.remove(game.gameID, game)
        }
    }

    static List<ActionRow> cloneToDisabled(Collection<ActionRow> rows){
        var toRet = []
        for (ActionRow r in rows){
            var row = []
            r.buttons.forEach({row << it.asDisabled()})
            toRet << ActionRow.of(row)
        }
        toRet
    }

    void onGuildMessage(GuildMessageReceivedEvent event){
        if (event.message.contentRaw.startsWith("~startgame")){
            if (userMap.containsKey(event.author.idLong)){
                event.channel.sendMessage("🅱oi nice try").queue()
                return
            }
            var args = event.message.contentRaw.split("\\s+")
            int w = 5, h = 5, r = 25
            if (args.length == 4){
                w = args[1] as int //width
                h = args[2] as int //height
                r = args[3] as int //random shuffles
            }
            if (w > 5 || h > 5 || w < 2 || h < 2 || r < 2 || r > 5000){
                event.channel.sendMessage("Invalid arguments.").queue()
                return
            }
            Game game = new Game(w, h, 1, event.author.idLong)
            game.randomise(r)
            gameMap.put(game.gameID, game)
            userMap.put(event.author.idLong, game.gameID)
            event.channel.sendMessage("Press a button adjacent to the empty space to swap that tile into the space.  The correct solution" +
                    " places 1 at the top-left and the blank space at the bottom right.  The number order should ascend left-to right, overflowing onto the row underneath.\n\n" +
                    "Only you can interact with this grid.")
                    .setActionRows(game.populateButtons())
                    .queue()
        } else if (event.message.contentRaw.startsWith("~cancelgame")){
            if (!userMap.containsKey(event.author.idLong)){
                event.channel.sendMessage("You do not have an active game.").queue()
                return
            }
            gameMap.remove(userMap.remove(event.author.idLong))
            event.channel.sendMessage("Terminated game.").queue()
        }
    }




    @Override
    void onEvent(@Nonnull GenericEvent event) {
        switch (event.getClass()){
            case ButtonClickEvent:
                onButtonClick(event as ButtonClickEvent)
                break
            case GuildMessageReceivedEvent:
                onGuildMessage(event as GuildMessageReceivedEvent)
                break
        }
    }

}
