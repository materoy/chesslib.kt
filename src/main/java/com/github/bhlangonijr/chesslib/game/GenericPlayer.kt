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
package com.github.bhlangonijr.chesslib.game

/**
 * A generic player of a chess game.
 */
class GenericPlayer : Player {
    private var id: String? = null
    private var elo = 0
    private var name: String? = null
    private var type: PlayerType? = null
    private var description: String? = null

    /**
     * Constructs a new chess player.
     */
    constructor()

    /**
     * Constructs a new chess player using their basic information.
     *
     * @param id   the ID of the player
     * @param name the name of the player
     */
    constructor(id: String?, name: String?) {
        this.id = id
        this.name = name
    }

    override fun getId(): String {
        return id!!
    }

    override fun setId(id: String) {
        this.id = id
    }

    override fun getElo(): Int {
        return elo
    }

    override fun setElo(elo: Int) {
        this.elo = elo
    }

    override fun getName(): String {
        return name!!
    }

    override fun setName(name: String) {
        this.name = name
    }

    override fun getType(): PlayerType {
        return type!!
    }

    override fun setType(type: PlayerType) {
        this.type = type
    }

    override fun getDescription(): String {
        return description!!
    }

    override fun setDescription(description: String) {
        this.description = description
    }

    override fun getLongDescription(): String {
        var desc = getName()
        if (getElo() > 0) {
            desc += " (" + getElo() + ")"
        }
        return desc
    }

    /**
     * Returns a string representation of this player.
     *
     * @return a string representation of this player
     */
    override fun toString(): String {
        return getId()
    }
}