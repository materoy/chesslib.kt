/*
 * Copyright 2017 Ben-Hur Carlos Vieira Langoni Junior
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.bhlangonijr.chesslib.move

import com.github.bhlangonijr.chesslib.*
import com.github.bhlangonijr.chesslib.Bitboard.bbToSquareList
import com.github.bhlangonijr.chesslib.Bitboard.bitScanForward
import com.github.bhlangonijr.chesslib.Bitboard.bitScanReverse
import com.github.bhlangonijr.chesslib.Bitboard.getBbtable
import com.github.bhlangonijr.chesslib.Bitboard.getFilebb
import com.github.bhlangonijr.chesslib.Bitboard.getRankbb
import com.github.bhlangonijr.chesslib.Bitboard.hasOnly1Bit
import com.github.bhlangonijr.chesslib.Piece.Companion.fromFenSymbol
import com.github.bhlangonijr.chesslib.Piece.Companion.make
import com.github.bhlangonijr.chesslib.PieceType.Companion.fromSanSymbol
import com.github.bhlangonijr.chesslib.Square.Companion.squareAt
import com.github.bhlangonijr.chesslib.move.MoveConversionException
import com.github.bhlangonijr.chesslib.util.StringUtil
import org.apache.commons.lang3.StringUtils
import java.util.*
import java.util.function.Function

/**
 * A convenient data structure to store an ordered sequence of moves and access to their human-readable representation
 * in one of the standard chess formats. This implementation can be used to hold the list of moves played in a chess
 * game.
 *
 *
 * The move list keeps a reference to a base initial position (by default, the standard starting chess position) used
 * to validate and disambiguate between moves.
 *
 *
 * This data structure is a [List], thus the standard API of the Java collection is available for this class as
 * well.
 */
class MoveList
/**
 * Constructs an empty move list, using the standard starting position as a base.
 */ @JvmOverloads constructor(
        /**
         * Returns the Forsyth–Edwards Notation (FEN) string that defines the initial position of the list of moves.
         *
         * @return the FEN representation of the initial position
         */
        val startFen: String = Constants.startStandardFENPosition) : LinkedList<Move>(), MutableList<Move> {
    private var dirty = true
    private var sanArray: Array<String> = arrayOf()
    private var fanArray: Array<String> = arrayOf()
    /**
     * Returns the parent index of the list of moves.
     *
     * @return the parent index
     */
    /**
     * Sets the parent index of the list of moves.
     *
     * @param parent the parent index to set
     */
    var parent = 0
    /**
     * Returns the index of the moves list.
     *
     * @return the index of the list
     */
    /**
     * Sets the index of the moves list.
     *
     * @param index the index of the list
     */
    var index = 0
    /**
     * Constructs an empty move list, using the provided initial position as a base, retrieved in Forsyth-Edwards
     * Notation (FEN).
     *
     * @param startFen the FEN representation of the base position
     */
    /**
     * Constructs a new move list starting from an existing one. The new instance will use the initial position of the
     * existing list as a base.
     *
     * @param halfMoves the existing move list
     */
    constructor(halfMoves: MoveList) : this(halfMoves.startFen) {
        super.addAll(halfMoves)
    }

    override fun add(index: Int, move: Move) {
        dirty = true
        super.add(index, move)
    }

    override fun add(move: Move): Boolean {
        dirty = true
        return super.add(move)
    }

    override fun addAll(moves: Collection<Move>): Boolean {
        dirty = true
        return super.addAll(moves)
    }

    override fun addAll(index: Int, moves: Collection<Move>): Boolean {
        dirty = true
        return super.addAll(index, moves)
    }

    override fun removeFirst(): Move {
        dirty = true
        return super.removeFirst()
    }

    override fun removeLast(): Move {
        dirty = true
        return super.removeLast()
    }

    override fun remove(move: Move): Boolean {
        dirty = true
        return super.remove(move)
    }

    override fun removeAt(index: Int): Move {
        dirty = true
        return super.removeAt(index)
    }

    override fun clear() {
        dirty = true
        sanArray = arrayOf()
        fanArray = arrayOf()
        super.clear()
    }

    /**
     * Converts the list of moves into a Short Algebraic Notation (SAN) representation that does not include move
     * numbers, for example `"e4 e5 Nf3 Bc5"`.
     *
     * @return the SAN representation of the list of moves without move numbers
     * @throws MoveConversionException in case a conversion error occurs during the process
     * @see MoveList.toSanWithMoveNumbers
     */
    @Throws(MoveConversionException::class)
    fun toSan(): String {
        return toStringWithoutMoveNumbers(toSanArray())
    }

    /**
     * Converts the list of moves into a Short Algebraic Notation (SAN) representation that does include move numbers,
     * for example `"1. e4 e5 2. Nf3 Bc5"`.
     *
     * @return the SAN representation of the list of moves with move numbers
     * @throws MoveConversionException in case a conversion error occurs during the process
     * @see MoveList.toSan
     */
    @Throws(MoveConversionException::class)
    fun toSanWithMoveNumbers(): String {
        return toStringWithMoveNumbers(toSanArray())
    }

    /**
     * Converts the list of moves into a Figurine Algebraic Notation (FAN) representation that does not include move
     * numbers, for example `"♙e4 ♟e5 ♘f3 ♝c5"`.
     *
     * @return the FAN representation of the list of moves without move numbers
     * @throws MoveConversionException in case a conversion error occurs during the process
     * @see MoveList.toFanWithMoveNumbers
     */
    @Throws(MoveConversionException::class)
    fun toFan(): String {
        return toStringWithoutMoveNumbers(toFanArray())
    }

    /**
     * Converts the list of moves into a Figurine Algebraic Notation (FAN) representation that does include move
     * numbers, for example `"1. ♙e4 ♟e5 2. ♘f3 ♝c5"`.
     *
     * @return the FAN representation of the list of moves with move numbers
     * @throws MoveConversionException in case a conversion error occurs during the process
     * @see MoveList.toFan
     */
    @Throws(MoveConversionException::class)
    fun toFanWithMoveNumbers(): String {
        return toStringWithMoveNumbers(toFanArray())
    }

    @Throws(MoveConversionException::class)
    private fun toStringWithoutMoveNumbers(moveArray: Array<String>): String {
        val sb = StringBuilder()
        for (move in moveArray) {
            sb.append(move)
            sb.append(StringUtils.SPACE)
        }
        return sb.toString()
    }

    @Throws(MoveConversionException::class)
    private fun toStringWithMoveNumbers(moveArray: Array<String>): String {
        val sb = StringBuilder()
        for (halfMove in moveArray.indices) {
            if (halfMove % 2 == 0) {
                sb.append(halfMove / 2 + 1).append(". ")
            }
            sb.append(moveArray[halfMove]).append(StringUtils.SPACE)
        }
        return sb.toString()
    }

    /**
     * Returns an array of strings representing the moves in Short Algebraic Notation (SAN).
     *
     * @return the SAN representations of the list of moves
     * @throws MoveConversionException in case a conversion error occurs during the process
     */
    @Throws(MoveConversionException::class)
    fun toSanArray(): Array<String> {
        if (!dirty && sanArray != null) {
            return sanArray
        }
        updateSanArray()
        updateFanArray()
        return sanArray
    }

    /**
     * Returns an array of strings representing the moves in Figurine Algebraic Notation (FAN).
     *
     * @return the FAN representations of the list of moves
     * @throws MoveConversionException in case a conversion error occurs during the process
     */
    @Throws(MoveConversionException::class)
    fun toFanArray(): Array<String> {
        if (!dirty) {
            return fanArray
        }
        updateSanArray()
        updateFanArray()
        return fanArray
    }

    @Throws(MoveConversionException::class)
    private fun updateSanArray() {
        sanArray = arrayOf()
        val b = board
        if (b.fen != startFen) {
            b.loadFromFen(startFen)
        }
        var i = 0
        for (move in this) {
            sanArray[i++] = encodeToSan(b, move)
        }
        dirty = false
    }

    @Throws(MoveConversionException::class)
    private fun updateFanArray() {
        fanArray = arrayOf()
        val b = board
        if (b.fen != startFen) {
            b.loadFromFen(startFen)
        }
        var i = 0
        for (move in this) {
            fanArray[i++] = encodeToFan(b, move)
        }
        dirty = false
    }

    /**
     * Reloads the list with a sequence of moves separated by spaces and provided in input in their algebraic form (e.g.
     * `"e2e4"` or `"g8f6"`). The base initial position will be left untouched.
     *
     * @param text the string representing the algebraic list of moves
     * @throws MoveConversionException if it is not possible to parse and convert the moves
     */
    @Synchronized
    @Throws(MoveConversionException::class)
    fun loadFromText(text: String) {
        var text = text
        val b = board
        if (b.fen != startFen) {
            b.loadFromFen(startFen)
        }
        try {
            var side = b.sideToMove
            text = StringUtil.normalize(text)
            val m = text.split(StringUtils.SPACE.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            var i = 0
            for (strMove in m) {
                val move = Move(strMove, side!!)
                add(i++, move)
                side = side.flip()
            }
        } catch (e: Exception) {
            throw MoveConversionException("Couldn't parse text to MoveList: " + e.message)
        }
    }
    /**
     * Adds a move defined by its Short Algebraic Notation (SAN) to the list. It is possible to control whether to
     * replay the moves already present in the list, as well as if to perform a full validation or not. When a full
     * validation is requested, additional checks are performed to assess the validity of the move, such as if the side
     * to move is consistent with the position, if castle moves or promotions are allowed, etc.
     *
     * @param san            the SAN representation of the move to be added
     * @param replay         if `true`, existing moves will be played again
     * @param fullValidation if `true`, a full validation of the position will be performed
     * @throws MoveConversionException if it is not possible to parse or validate the move
     */
    /**
     * Adds a move defined by its Short Algebraic Notation (SAN) to the list.
     *
     *
     * Same as invoking `addSanMove(san, false, true)`.
     *
     * @param san the SAN representation of the move to be added
     * @throws MoveConversionException if it is not possible to parse or validate the move
     * @see MoveList.addSanMove
     */
    @JvmOverloads
    @Throws(MoveConversionException::class)
    fun addSanMove(san: String, replay: Boolean = false, fullValidation: Boolean = true) {
        val b = board
        if (replay) {
            if (b.fen != startFen) {
                b.loadFromFen(startFen)
            }
            for (move in this) {
                if (!b.doMove(move, false)) {
                    throw MoveConversionException("Couldn't parse SAN to MoveList: Illegal move: " +
                            move + " [" + move.toString() + "] on " + b.fen)
                }
            }
        }
        val move = decodeSan(b, san, b.sideToMove)
        if (move === nullMove) {
            return
        }
        move!!.san = san
        if (!b.doMove(move, fullValidation)) {
            throw MoveConversionException("Couldn't parse SAN to MoveList: Illegal move: " +
                    move + " [" + san + "] on " + b.fen)
        }
        add(this.size, move)
    }

    /**
     * Reloads the list with a sequence of moves provided in input in their Short Algebraic Notation (SAN) (e.g.
     * `"1. e4 e5 2. Nf3 Bc5"`). The base initial position will be left untouched.
     *
     * @param text the SAN representation of the list of moves
     * @throws MoveConversionException if it is not possible to parse and convert the moves
     */
    @Throws(MoveConversionException::class)
    fun loadFromSan(text: String) {
        var text = text
        val b = board
        if (b.fen != startFen) {
            b.loadFromFen(startFen)
        }
        try {
            text = StringUtil.normalize(text)
            val m = text.split(StringUtils.SPACE.toRegex()).dropLastWhile { it.isEmpty() }.toMutableList()
            val strMoveIterator = m.listIterator()
            while (strMoveIterator.hasNext()) {
                val strMove = strMoveIterator.next()
                if (strMove.startsWith("$")) {
                    continue
                }
                if (strMove.contains("...")) {
                    continue
                }
                if (strMove.contains(".")) {
                    strMoveIterator.set(StringUtil.afterSequence(strMove, "."))
                }
                if (StringUtils.isBlank(strMove)) {
                    continue
                }
                addSanMove(strMove)
            }
        } catch (e1: MoveConversionException) {
            throw e1
        } catch (e2: Exception) {
            throw MoveConversionException("Couldn't parse SAN to MoveList: " + e2.message)
        }
    }

    /**
     * Converts a move defined by its Short Algebraic Notation (SAN) to an instance of [Move], using the given
     * board and side as context.
     *
     * @param board the board in which the move is played
     * @param san   the SAN representation of the move
     * @param side  the side executing the move
     * @return the converted move
     * @throws MoveConversionException if it is not possible to convert the move
     */
    // decode SAN to move
    @Throws(MoveConversionException::class)
    protected fun decodeSan(board: Board, san: String, side: Side?): Move? {
        var san = san
        if (san.equals("Z0", ignoreCase = true)) {
            return nullMove
        }
        san = normalizeSan(san)
        var strPromotion = StringUtil.afterSequence(san, "=", 1)
        san = StringUtil.beforeSequence(san, "=")
        val lastChar = san[san.length - 1]
        //FIX missing equal sign for pawn promotions
        if (Character.isLetter(lastChar) && lastChar.uppercaseChar() != 'O') {
            san = san.substring(0, san.length - 1)
            strPromotion = lastChar.toString()
        }
        if (san == "O-O" || san == "O-O-O") { // is castle
            return if (san == "O-O") {
                board.context.getoo(side!!)
            } else {
                board.context.getooo(side!!)
            }
        }
        if (san.length == 3 &&
                Character.isUpperCase(san[2])) {
            strPromotion = san.substring(2, 3)
            san = san.substring(0, 2)
        }
        var from = Square.NONE
        val to: Square
        to = try {
            Square.valueOf(StringUtil.lastSequence(san.uppercase(Locale.getDefault()), 2))
        } catch (e: Exception) {
            throw MoveConversionException("Couldn't parse destination square[" + san + "]: " +
                    san.uppercase(Locale.getDefault()))
        }
        val promotion = if (StringUtils.isEmpty(strPromotion)) Piece.NONE else fromFenSymbol(if (side == Side.WHITE) strPromotion.uppercase(Locale.getDefault()) else strPromotion.lowercase(Locale.getDefault()))
        if (san.length == 2) { //is pawn move
            val mask = getBbtable(to) - 1L
            val xfrom = (if (side == Side.WHITE) mask else mask.inv()) and getFilebb(to) and
                    board.getBitboard(make(side!!, PieceType.PAWN))
            val f = if (side == Side.BLACK) bitScanForward(xfrom) else bitScanReverse(xfrom)
            if (f >= 0 && f <= 63) {
                from = squareAt(f)
            }
        } else {
            val strFrom = if (san.contains("x")) StringUtil.beforeSequence(san, "x") else san.substring(0, san.length - 2)
            if (strFrom == null || strFrom.length == 0 || strFrom.length > 3) {
                throw MoveConversionException("Couldn't parse 'from' square $san: Too many/few characters.")
            }
            var fromPiece = PieceType.PAWN
            if (Character.isUpperCase(strFrom[0])) {
                fromPiece = fromSanSymbol(strFrom[0].toString())
            }
            if (strFrom.length == 3) {
                from = Square.valueOf(strFrom.substring(1, 3).uppercase(Locale.getDefault()))
            } else {
                var location = StringUtils.EMPTY
                if (strFrom.length == 2) {
                    if (Character.isUpperCase(strFrom[0])) {
                        location = strFrom.substring(1, 2)
                    } else {
                        location = strFrom.substring(0, 2)
                        from = Square.valueOf(location.uppercase(Locale.getDefault()))
                    }
                } else {
                    if (Character.isLowerCase(strFrom[0])) {
                        location = strFrom
                    }
                }
                if (location.length < 2) {
                    //resolving ambiguous from
                    var xfrom = board.squareAttackedByPieceType(to,
                            board.sideToMove, fromPiece)
                    if (location.length > 0) {
                        xfrom = if (Character.isDigit(location[0])) {
                            val irank = location.toInt()
                            if (!(irank >= 1 && irank <= 8)) {
                                throw MoveConversionException("Couldn't parse rank: $location")
                            }
                            val rank = Rank.allRanks[irank - 1]
                            xfrom and getRankbb(rank)
                        } else {
                            try {
                                val file = File.valueOf("FILE_" + location.uppercase(Locale.getDefault()))
                                xfrom and getFilebb(file)
                            } catch (e: Exception) {
                                throw MoveConversionException("Couldn't parse file: $location")
                            }
                        }
                    }
                    if (xfrom != 0L) {
                        if (!hasOnly1Bit(xfrom)) {
                            xfrom = findLegalSquares(board, to, promotion, xfrom)
                        }
                        val f = bitScanForward(xfrom)
                        if (f >= 0 && f <= 63) {
                            from = squareAt(f)
                        }
                    }
                }
            }
        }
        if (from == Square.NONE) {
            throw MoveConversionException("Couldn't parse 'from' square " + san + " to setup: " + board.fen)
        }
        return Move(from, to, promotion)
    }

    /**
     * Returns the Forsyth-Edwards Notation (FEN) representation of the position after the moves of this list, until
     * index `atMoveIndex` (excluded), are executed from the base initial position.
     *
     *
     * Same as invoking `getFen(atMoveIndex, true)`.
     *
     * @param atMoveIndex the index until which to execute the moves
     * @return the FEN string notation of the position after the wanted number of moves are played from the initial
     * position
     * @see MoveList.getFen
     */
    fun getFen(atMoveIndex: Int): String? {
        return getFen(atMoveIndex, true)
    }

    /**
     * Returns the Forsyth-Edwards Notation (FEN) representation of the position after the moves of this list, until
     * index `atMoveIndex` (excluded), are executed from the base initial position. Full and half moves counters
     * are included in the output if the relative flag is enabled.
     *
     * @param atMoveIndex     the index until which to execute the moves
     * @param includeCounters if `true`, move counters are included in the resulting string
     * @return the FEN string notation of the position after the wanted number of moves are played from the initial
     * position
     */
    fun getFen(atMoveIndex: Int, includeCounters: Boolean): String? {
        val b = board
        if (b.fen != startFen) {
            b.loadFromFen(startFen)
        }
        var i = 0
        for (move in this) {
            i++
            require(b.doMove(move, false)) {
                "Couldn't parse SAN to MoveList: Illegal move: " +
                        move + " [" + move.toString() + "] on " + b.getFen(includeCounters)
            }
            if (i >= atMoveIndex) {
                return b.getFen(includeCounters)
            }
        }
        return null
    }

    val fen: String?
        /**
         * Returns the Forsyth-Edwards Notation (FEN) representation of the position after all the moves of this list are
         * executed starting from the base initial position.
         *
         *
         * Same as invoking `getFen(this.size(), true)`.
         *
         * @return the FEN string notation of the position after the moves are played from the initial position
         * @see MoveList.getFen
         */
        get() = getFen(this.size)

    /**
     * Returns a string representation of the list.
     *
     * @return a string representation of the list of moves
     */
    override fun toString(): String {
        val b = StringBuilder()
        for (move in this) {
            b.append(move.toString())
            b.append(StringUtils.SPACE)
        }
        return b.toString().trim { it <= ' ' }
    }

    /**
     * Returns a hash code value for this move list.
     *
     * @return a hash value for this move list
     */
    override fun hashCode(): Int {
        val prime = 31
        var result = super.hashCode()
        result = prime * result + toString().hashCode()
        return result
    }

    /**
     * Checks if this list is equivalent to another, that is, if the lists contains the same moves in the same order.
     *
     * @param obj the other object reference to compare to this list of moves
     * @return `true` if this list and the object reference are equivalent
     */
    override fun equals(obj: Any?): Boolean {
        if (obj is MoveList) {
            val l = obj
            if (l.size != this.size) {
                return false
            }
            for (i in l.indices) {
                if (!l[i]!!.equals(this[i])) {
                    return false
                }
            }
            return true
        }
        return false
    }

    private fun normalizeSan(san: String): String { //TODO regex?
        return san.replace("+", StringUtils.EMPTY)
                .replace("#", StringUtils.EMPTY)
                .replace("!", StringUtils.EMPTY)
                .replace("?", StringUtils.EMPTY)
                .replace("ep", StringUtils.EMPTY)
                .replace("\n", StringUtils.SPACE)
    }

    companion object {
        private const val serialVersionUID = -6204280556340150806L
        private val boardHolder = ThreadLocal.withInitial { Board() }
        private val nullMove = Move(Square.NONE, Square.NONE)
        private val board: Board
            /**
             * Returns a reference to the board representing the last position after the moves are played.
             *
             * @return the board representing the position after the moves are played
             */
            private get() = boardHolder.get()

        /**
         * Encodes the move to its Short Algebraic Notation (SAN), using the context of the given board.
         *
         * @param board the board used as context for encoding the move
         * @param move  the move to encode
         * @return the SAN notation of the move
         * @throws MoveConversionException if the move conversion fails
         */
        // encode the move to SAN move and update thread local board
        @Throws(MoveConversionException::class)
        private fun encodeToSan(board: Board, move: Move): String {
            return encode(board, move, Piece::sanSymbol)
        }

        /**
         * Encodes the move to its Figurine Algebraic Notation (FAN), using the context of the given board.
         *
         * @param board the board used as context for encoding the move
         * @param move  the move to encode
         * @return the FAN notation of the move
         * @throws MoveConversionException if the move conversion fails
         */
        // encode the move to FAN move and update thread local board
        @Throws(MoveConversionException::class)
        private fun encodeToFan(board: Board, move: Move): String {
            return encode(board, move, Piece::fanSymbol)
        }

        /**
         * Encodes the move using a conversion function and the given board as context.
         *
         * @param board    the board used as context for encoding the move
         * @param move     the move to encode
         * @param notation the conversion function to transform the move to its string representation
         * @return the notation of the move
         * @throws MoveConversionException if the move conversion fails
         */
        // encode the move to SAN/FAN move and update thread local board
        @Throws(MoveConversionException::class)
        private fun encode(board: Board, move: Move, notation: Function<Piece, String>): String {
            val san = StringBuilder()
            val piece = board.getPiece(move.from)
            if (piece.pieceType == PieceType.KING) {
                val delta = move.to.file.ordinal -
                        move.from.file.ordinal
                if (Math.abs(delta) >= 2) { // is castle
                    if (!board.doMove(move, true)) {
                        throw MoveConversionException("Invalid move [" +
                                move + "] for current setup: " + board.fen)
                    }
                    san.append(if (delta > 0) "O-O" else "O-O-O")
                    addCheckFlag(board, san)
                    return san.toString()
                }
            }
            val pawnMove = piece.pieceType == PieceType.PAWN && move.from.file == move.to.file
            var ambResolved = false
            san.append(notation.apply(piece))
            if (!pawnMove) {
                //resolving ambiguous move
                var amb = board.squareAttackedByPieceType(move.to,
                        board.sideToMove, piece.pieceType)
                amb = amb and move.from.bitboard.inv()
                if (amb != 0L) {
                    val fromList = bbToSquareList(amb)
                    for (from in fromList) {
                        if (!board.isMoveLegal(Move(from, move.to), false)) {
                            amb = amb xor move.from.bitboard
                        }
                    }
                }
                if (amb != 0L) {
                    if (getFilebb(move.from) and amb == 0L) {
                        san.append(move.from.file.notation.lowercase(Locale.getDefault()))
                    } else if (getRankbb(move.from) and amb == 0L) {
                        san.append(move.from.rank.notation.lowercase(Locale.getDefault()))
                    } else {
                        san.append(move.from.toString().lowercase(Locale.getDefault()))
                    }
                    ambResolved = true
                }
            }
            if (!board.doMove(move, true)) {
                throw MoveConversionException("Invalid move [" +
                        move + "] for current setup: " + board.fen)
            }
            val captured = board.backup.last.capturedPiece
            val isCapture = captured != Piece.NONE
            if (isCapture) {
                if (!ambResolved && piece.pieceType == PieceType.PAWN) {
                    san.append(move.from.file.notation.lowercase(Locale.getDefault()))
                }
                san.append("x")
            }
            san.append(move.to.toString().lowercase(Locale.getDefault()))
            if (move.promotion != Piece.NONE) {
                san.append("=")
                san.append(notation.apply(move.promotion))
            }
            addCheckFlag(board, san)
            return san.toString()
        }

        private fun addCheckFlag(board: Board, san: StringBuilder) {
            if (board.isKingAttacked) {
                if (board.isMated) {
                    san.append("#")
                } else {
                    san.append("+")
                }
            }
        }

        private fun findLegalSquares(board: Board, to: Square, promotion: Piece, pieces: Long): Long {
            var result = 0L
            if (pieces != 0L) {
                for (sqSource in bbToSquareList(pieces)) {
                    val move = Move(sqSource, to, promotion)
                    if (board.isMoveLegal(move, true)) {
                        result = result or sqSource.bitboard
                        break
                    }
                }
            }
            return result
        }

        /**
         * Creates a new instance using an existing list of moves. The new instance will use the initial position of the
         * existing list as a base.
         *
         *
         * The returned list will contain only the first `finalIndex` moves of the original list, or all the elements
         * if `finalIndex` is outside the boundaries of the source list.
         *
         * @param startMoves the existing list of moves
         * @param finalIndex the last index of the source list to use
         * @return the new list of moves
         * @throws MoveConversionException if the starting list of moves is invalid
         */
        @Throws(MoveConversionException::class)
        fun createMoveListFrom(startMoves: MoveList, finalIndex: Int): MoveList {
            var fen: String? = null
            val b = board
            if (b.fen != startMoves.startFen) {
                b.loadFromFen(startMoves.startFen)
            }
            var i = 0
            for (move in startMoves) {
                i++
                if (!b.doMove(move, false)) {
                    throw MoveConversionException("Couldn't parse SAN to MoveList: Illegal move: " +
                            move + " [" + move.toString() + "] on " + b.fen)
                }
                if (i >= finalIndex) {
                    fen = b.fen
                    break
                }
            }
            if (fen == null) {
                fen = b.fen
            }
            return MoveList(fen)
        }
    }
}