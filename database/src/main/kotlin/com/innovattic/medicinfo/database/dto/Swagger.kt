package com.innovattic.medicinfo.database.dto

object Swagger {
    const val API_KEY = "Only available on create"

    const val SELF_TEST_VALUE = "The allowed values depend on the chosen question type:\n" +
            "* For `boolean`, there must be exactly two values: one for `true` and one for `false`\n" +
            "* For `slider`, the values must form a sequence of ints with step 1 " +
                "(e.g if there's a value 2 and a value 4 there must also be a value 3)\n" +
            "* For `single_choice`, the values must be Strings, unique within this question"
}
