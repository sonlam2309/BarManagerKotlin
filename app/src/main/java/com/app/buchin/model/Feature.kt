package com.app.buchin.model

class Feature(private var id: Int, private var resource: Int, private var title: String?) {

    fun getId(): Int {
        return id
    }

    fun setId(id: Int) {
        this.id = id
    }

    fun getResource(): Int {
        return resource
    }

    fun setResource(resource: Int) {
        this.resource = resource
    }

    fun getTitle(): String? {
        return title
    }

    fun setTitle(title: String?) {
        this.title = title
    }

    companion object {
        const val FEATURE_LIST_MENU = 0
        const val FEATURE_MANAGE_UNIT = 1
        const val FEATURE_ADD_DRINK = 2
        const val FEATURE_MANAGE_DRINK = 3
        const val FEATURE_DRINK_USED = 4
        const val FEATURE_DRINK_OUT_OF_STOCK = 5
        const val FEATURE_REVELUE = 6
        const val FEATURE_COST = 7
        const val FEATURE_PROFIT = 8
        const val FEATURE_DRINK_POPULAR = 9
    }
}