package com.github.sherlock.resumedemo.web.rest.errors

import org.zalando.problem.AbstractThrowableProblem
import org.zalando.problem.Exceptional
import org.zalando.problem.Status.BAD_REQUEST
import java.io.Serializable
import java.util.*

/**
 * Created by TangBin on 2017/9/28.
 *
 * Custom, parameterized exception, which can be translated on the client side.
 * For example:
 *
 * <pre>
 * throw new CustomParameterizedException(&quot;myCustomError&quot;, &quot;hello&quot;, &quot;world&quot;);
</pre> *
 *
 * Can be translated with:
 *
 * <pre>
 * "error.myCustomError" :  "The server says {{param0}} to {{param1}}"
</pre> *
 */

class CustomParameterizedException(message: String, paramMap: Map<String, Any>) : AbstractThrowableProblem(ErrorConstants.PARAMETERIZED_TYPE, "Parameterized Exception", BAD_REQUEST, null, null, null, toProblemParameters(message, paramMap)), Serializable {

    constructor(message: String, vararg params: String) : this(message, toParamMap(*params)) {}

    companion object {

        private const val serialVersionUID = 1L

        private val PARAM = "param"

        @JvmStatic
        fun toParamMap(vararg params: String): Map<String, Any> {
            val paramMap = HashMap<String, Any>()
            if (params != null && params.size > 0) {
                for (i in params.indices) {
                    paramMap.put(PARAM + i, params[i])
                }
            }
            return paramMap
        }

        @JvmStatic
        fun toProblemParameters(message: String, paramMap: Map<String, Any>): Map<String, Any> {
            val parameters = HashMap<String, Any>()
            parameters.put("message", message)
            parameters.put("params", paramMap)
            return parameters
        }
    }

    override fun getCause(): Exceptional {
        return super.cause as Exceptional
    }
}
