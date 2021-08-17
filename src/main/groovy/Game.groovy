import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.Button
import net.dv8tion.jda.api.interactions.components.ButtonStyle

import java.util.concurrent.ThreadLocalRandom

class Game {

    final int width
    final int height
    final long gameID
    final long userID
    private static long gameIDState = 0

    int moves = 0;

    final ArrayList<Integer> tiles

    int spacepos

    Game(int width, int height, int hardness, long uid){
        this.userID = uid
        gameID = gameIDState++
        this.width = width
        this.height = height
        int total = width * height
        tiles = new ArrayList<>()
        //define a custom definition to test if this list is solved or not
        tiles.metaClass.isSolved << {
            ->
            var lst = delegate as ArrayList<Integer>
            for (int i in 1..<lst.size() - 1) {
                if (lst.get(i) < lst.get(i-1)) {
                    return false
                }
            }
            lst.get(lst.size()-1) == -1
        }
        tiles.metaClass.toString = {
            var lst = delegate as ArrayList<Integer>
            var strbld = new StringBuilder()
            strbld << "["
            for (int i in 0..<lst.size()){
                strbld << "${i < 10 ? "0" + i : i}:${lst.get(i) >= 10 ?lst.get(i): lst.get(i) + " "}\t"
                if (i % width == width - 1){
                    strbld << "\n"
                }
            }
            strbld.append("]")

        }
        for (int i = 1; i <= total-1; i++){
            tiles.add(i)
        }
        tiles.add(-1) //this is the 'space' tile
        spacepos = width * height - 1
    }

    void randomise(int times){
        Random random = ThreadLocalRandom.current()
        for (var in 0..<times){
            var moves = findValidMoves()
            internalSwap(moves.get(random.nextInt(moves.size())))
        }
        //debug
//        println(tiles)
    }

    private void internalSwap(int position){
        tiles.set(spacepos, tiles.get(position))
        spacepos = position
        tiles.set(spacepos, -1)
    }

    void swap(int position){
        assert position in findValidMoves()
        internalSwap(position)
        moves++
    }

    List<Integer> findValidMoves(){
        int currentX = spacepos % width
        int currentY = spacepos.intdiv(width)
        var moves = []
        if (currentX > 0){
            moves.add(spacepos-1)
        }
        if (currentY > 0){
            moves.add(spacepos - width)
        }
        if (currentY < height - 1){
            moves.add(spacepos + width)
        }
        if (currentX < width - 1){
            moves.add(spacepos + 1)
        }
//        //debug
//        println "Valid moves for the following board are: $moves";
//        println tiles.toString()
//        println "----------------------------------"
        return moves
    }

    List<ActionRow> populateButtons(){
        var rows = []
        var validMoves = findValidMoves()
        int pos = 0
        ButtonStyle style
        for (j in 0 ..< height){
            var row = []
            for (i in 0 ..< width){
                var current = pos++
                style = ButtonStyle.PRIMARY;
                if (tiles.get(current) - 1 == current){
                    style = ButtonStyle.SUCCESS
                }
                if (tiles.get(current) == -1){
                    row << Button.secondary("disabled:empty", " ").asDisabled()
                } else if (current in validMoves){
                    row << Button.of(style, "swap:${this.gameID}:$current", "${tiles.get(current)}")
                } else {
                    row << Button.of(style, "disabled:${this.gameID}:$current", "${tiles.get(current)}").asDisabled()
                }
            }
            rows << ActionRow.of(row)
        }
        return rows
    }

    List<ActionRow> populateCompletedButtons(){
        var rows = []
        int pos = 0
        for (j in 0 ..< height){
            var row = []
            for (i in 0 ..< width){
                var current = pos++
                if (tiles.get(current) == -1){
                    row << Button.secondary("disabled:empty", " ").asDisabled()
                } else {
                    row << Button.success("swap:${this.gameID}:$current", "${tiles.get(current)}").asDisabled()
                }
            }
            rows << ActionRow.of(row)
        }
        return rows
    }

    boolean isSolved(){
        tiles.isSolved()
    }
}
