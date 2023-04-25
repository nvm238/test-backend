package com.innovattic.medicinfo.database.fiql

import com.innovattic.common.database.fiql.SimpleQueryParser
import com.innovattic.medicinfo.dbschema.Tables.MESSAGE_VIEW
import cz.jirutka.rsql.parser.ast.ComparisonNode
import org.jooq.Condition
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

object MessageQueryParser : SimpleQueryParser() {
    override fun visit(node: ComparisonNode): Condition {
        return when (node.selector) {
            "created" -> handle(MESSAGE_VIEW.CREATED, node)
            else -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported query field ${node.selector}")
        }
    }
}
