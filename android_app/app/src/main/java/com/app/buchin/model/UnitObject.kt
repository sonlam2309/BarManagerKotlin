package com.app.buchin.model

import java.io.Serializable

class UnitObject : Serializable {

    private var id: Long = 0
    private var name: String? = null

    constructor() {}
    constructor(id: Long, name: String?) {
        this.id = id
        this.name = name
    }

    fun getId(): Long {
        return id
    }

    fun setId(id: Long) {
        this.id = id
    }

    fun getName(): String? {
        return name
    }

    fun setName(name: String?) {
        this.name = name
    }
}