package com.innovattic.medicinfo.logic.triage.model.external

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer

class ConditionDeserializer : StdDeserializer<Condition>(Condition::class.java) {

    override fun deserialize(parser: JsonParser, ctxt: DeserializationContext?): Condition {
        val node: JsonNode = parser.codec.readTree(parser)
        val conditionType = node.get("name").textValue() ?: error("name is not present")
        val operator = node.get("operator").textValue() ?: error("operator is not present")
        val value = node.get("value")
        return when {
            value.isArray -> {
                ContainsValueCondition(ConditionType.valueOf(conditionType), operator, value.asSequence().map { it.textValue() }.toList())
            }
            value.isNull -> {
                error("answer cannot be null")
            }
            else -> {
                SingleValueCondition(ConditionType.valueOf(conditionType), operator, value.textValue())
            }
        }
    }
}
