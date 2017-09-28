package com.github.sherlock.resumedemo.web.rest.errors

import java.io.Serializable

/**
 * Created by TangBin on 2017/9/28.
 */

class FieldErrorVM(val objectName: String, val field: String, val message: String) : Serializable {
    companion object {

        private val serialVersionUID = -5848933L
    }
}
