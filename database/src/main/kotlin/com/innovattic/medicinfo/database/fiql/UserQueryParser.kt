package com.innovattic.medicinfo.database.fiql

import com.innovattic.common.database.fiql.SimpleQueryParser
import com.innovattic.medicinfo.dbschema.tables.UserView.USER_VIEW
import cz.jirutka.rsql.parser.ast.ComparisonNode
import org.jooq.Condition
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

object UserQueryParser : SimpleQueryParser() {
    override fun visit(node: ComparisonNode): Condition {
        return when (node.selector) {
            "id" -> handle(USER_VIEW.PUBLIC_ID, node)
            "labelId" -> handle(USER_VIEW.LABEL_PUBLIC_ID, node)
            "created" -> handle(USER_VIEW.CREATED, node)
            "displayName" -> handle(USER_VIEW.NAME, node)
            "role" -> handle(USER_VIEW.ROLE, node)
            "email" -> handle(USER_VIEW.EMAIL, node)
            "age" -> handle(USER_VIEW.AGE, node)
            "isInsured" -> handle(USER_VIEW.IS_INSURED, node)
            "gender" -> handle(USER_VIEW.GENDER, node)
            else -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported query field ${node.selector}")
        }
    }
}
